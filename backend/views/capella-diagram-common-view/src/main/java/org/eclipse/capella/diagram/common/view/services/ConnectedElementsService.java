/*******************************************************************************
 * Copyright (c) 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.common.view.services;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewModifier;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.emf.api.IViewAQLInterpreterFactory;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.syson.sysml.Element;
import org.springframework.stereotype.Service;

/**
 * Displays immediate neighbors using the relations supported by the current diagram. Edge neighbors reveal their endpoint
 * dependencies without expanding the other connections of those endpoints. Direct ports are treated as part of their owner;
 * connections of nested components or functions are not included.
 *
 * @author jgout
 */
@Service
public class ConnectedElementsService {

    private final IViewDiagramDescriptionSearchService descriptionSearchService;

    private final IViewAQLInterpreterFactory interpreterFactory;

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    private final RelatedElementsService relatedElementsService;

    private final CommonQueryService queryService = new CommonQueryService();

    public ConnectedElementsService(IViewDiagramDescriptionSearchService descriptionSearchService, IViewAQLInterpreterFactory interpreterFactory,
            IObjectSearchService objectSearchService, IIdentityService identityService, RelatedElementsService relatedElementsService) {
        this.descriptionSearchService = Objects.requireNonNull(descriptionSearchService);
        this.interpreterFactory = Objects.requireNonNull(interpreterFactory);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = Objects.requireNonNull(identityService);
        this.relatedElementsService = Objects.requireNonNull(relatedElementsService);
    }

    public Element showConnectedElements(Element self, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.descriptionSearchService.findById(editingContext, diagramContext.diagram().getDescriptionId()).ifPresent(description -> {
            var interpreter = this.interpreterFactory.createInterpreter(editingContext, (View) description.eContainer());
            var root = this.objectSearchService.getObject(editingContext, diagramContext.diagram().getTargetObjectId()).orElse(self);
            Map<String, Object> rootVariables = Map.of(VariableManager.SELF, root, IEditingContext.EDITING_CONTEXT, editingContext,
                    DiagramContext.DIAGRAM_CONTEXT, diagramContext, Node.SELECTED_NODE, selectedNode);
            Set<Element> neighbours = new LinkedHashSet<>();
            Set<String> relationIds = new LinkedHashSet<>();
            Map<Element, Set<Element>> edgeEndpoints = new LinkedHashMap<>();
            var endpointEdgeDescriptions = description.getEdgeDescriptions().stream()
                    .flatMap(edge -> Stream.concat(edge.getSourceDescriptions().stream(), edge.getTargetDescriptions().stream()))
                    .filter(EdgeDescription.class::isInstance).collect(Collectors.toSet());
            description.getEdgeDescriptions().stream().filter(EdgeDescription::isIsDomainBasedEdge).forEach(edge -> {
                for (Element relation : this.evaluate(interpreter, rootVariables, edge.getSemanticCandidatesExpression())) {
                    var variables = new java.util.HashMap<>(rootVariables);
                    variables.put(VariableManager.SELF, relation);
                    var sources = this.evaluate(interpreter, variables, edge.getSourceExpression());
                    var targets = this.evaluate(interpreter, variables, edge.getTargetExpression());
                    if (endpointEdgeDescriptions.contains(edge)) {
                        var endpoints = edgeEndpoints.computeIfAbsent(relation, key -> new LinkedHashSet<>());
                        endpoints.addAll(sources);
                        endpoints.addAll(targets);
                    }
                    if (sources.stream().anyMatch(endpoint -> this.isEndpoint(self, endpoint)) || targets.stream().anyMatch(endpoint -> this.isEndpoint(self, endpoint))) {
                        neighbours.addAll(sources);
                        neighbours.addAll(targets);
                        relationIds.add(this.identityService.getId(relation));
                    }
                }
            });
            Set<Element> nodes = new LinkedHashSet<>();
            Set<Element> visited = new LinkedHashSet<>();
            neighbours.forEach(element -> this.collectNodeDependencies(element, edgeEndpoints, nodes, relationIds, visited));
            this.relatedElementsService.showElementsWithOwners(nodes, editingContext, diagramContext, convertedNodes);
            var hiddenEdges = diagramContext.diagram().getEdges().stream().filter(edge -> edge.getState() == ViewModifier.Hidden)
                    .filter(edge -> relationIds.contains(edge.getTargetObjectId())).map(Edge::getId).collect(Collectors.toSet());
            if (!hiddenEdges.isEmpty()) {
                diagramContext.diagramEvents().add(new HideDiagramElementEvent(hiddenEdges, false));
            }
        });
        return self;
    }

    /**
     * Expands only the endpoints required to display an edge used as another edge's endpoint, without exploring their other connections.
     */
    private void collectNodeDependencies(Element element, Map<Element, Set<Element>> edgeEndpoints, Set<Element> nodes, Set<String> relationIds, Set<Element> visited) {
        if (visited.add(element)) {
            if (edgeEndpoints.containsKey(element)) {
                relationIds.add(this.identityService.getId(element));
                edgeEndpoints.get(element).forEach(endpoint -> this.collectNodeDependencies(endpoint, edgeEndpoints, nodes, relationIds, visited));
            } else {
                nodes.add(element);
            }
        }
    }

    private List<Element> evaluate(AQLInterpreter interpreter, Map<String, Object> variables, String expression) {
        return interpreter.evaluateExpression(variables, expression).asObjects().orElse(List.of()).stream()
                .filter(Element.class::isInstance).map(Element.class::cast).toList();
    }

    private boolean isEndpoint(Element selected, Element endpoint) {
        return endpoint == selected || ((this.queryService.isComponentPort(endpoint) || this.queryService.isFunctionPort(endpoint)) && endpoint.getOwner() == selected);
    }
}

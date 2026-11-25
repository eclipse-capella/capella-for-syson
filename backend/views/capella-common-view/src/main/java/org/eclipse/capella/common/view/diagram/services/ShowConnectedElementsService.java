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

package org.eclipse.capella.common.view.diagram.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.variables.CoreVariables;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.RepresentationVariables;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.view.emf.api.IViewAQLInterpreterFactory;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.stereotype.Service;

/**
 * Services to show the connected elements in the provided context.
 *
 * @author gdaniel
 */
@Service
public class ShowConnectedElementsService {

    private final IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService;

    private final IViewAQLInterpreterFactory interpreterFactory;

    private final DiagramMutationElementService diagramMutationElementService;

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    private final CommonQueryService commonQueryService;

    public ShowConnectedElementsService(IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService, IViewAQLInterpreterFactory interpreterFactory,
            DiagramMutationElementService diagramMutationElementService, IObjectSearchService objectSearchService, IIdentityService identityService) {
        this.viewRepresentationDescriptionSearchService = Objects.requireNonNull(viewRepresentationDescriptionSearchService);
        this.interpreterFactory = Objects.requireNonNull(interpreterFactory);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = Objects.requireNonNull(identityService);
        this.commonQueryService = new CommonQueryService();
    }

    public Element showAllConnectedElements(Element self, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (selectedNode != null) {
            // We can't show the connected elements of the diagram itself.
            List<Element> connectedElements = this.getConnectedElements(self, selectedNode, editingContext, diagramContext, convertedNodes);
            for (Element connectedElement : connectedElements) {
                // The code to display the elements should be factorized with the one used to display elements dropped from the explorer.
                // For the moment, we create a node in its direct graphical parent if it is visible, or we put it on the diagram.
                Node parentNode = this.getParentNode(connectedElement, editingContext, diagramContext, convertedNodes).orElse(null);
                this.diagramMutationElementService.createView(connectedElement, editingContext, diagramContext, parentNode, convertedNodes);
            }
        }
        return self;
    }

    private List<Element> getConnectedElements(Element self, Node selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<Element> result = new ArrayList<>();

        List<String> borderNodeTargetObjectIds = selectedNode.getBorderNodes().stream()
                .map(Node::getTargetObjectId)
                .toList();

        VariableManager variableManager = new VariableManager();
        variableManager.put(RepresentationVariables.SELF.name(), self);
        variableManager.put(CoreVariables.EDITING_CONTEXT.name(), editingContext);
        variableManager.put(DiagramContext.DIAGRAM_CONTEXT, diagramContext);

        Optional<DiagramDescription> optionalDiagramDescription = this.viewRepresentationDescriptionSearchService.findById(editingContext, diagramContext.diagram().getDescriptionId())
                .filter(DiagramDescription.class::isInstance)
                .map(DiagramDescription.class::cast);

        if (optionalDiagramDescription.isPresent()) {
            DiagramDescription diagramDescription = optionalDiagramDescription.get();
            AQLInterpreter interpreter = this.interpreterFactory.createInterpreter(editingContext, (View) diagramDescription.eContainer());

            for (EdgeDescription edgeDescription : diagramDescription.getEdgeDescriptions()) {
                List<Element> edgeSemanticCandidates = this.evaluateAsElements(interpreter, variableManager, edgeDescription.getSemanticCandidatesExpression());
                for (Element edgeSemanticCandidate : edgeSemanticCandidates) {
                    VariableManager edgeVariableManager = variableManager.createChild();
                    edgeVariableManager.put(RepresentationVariables.SELF.name(), edgeSemanticCandidate);
                    List<Element> edgeSources = this.evaluateAsElements(interpreter, edgeVariableManager, edgeDescription.getSourceExpression());
                    List<Element> edgeTargets = this.evaluateAsElements(interpreter, edgeVariableManager, edgeDescription.getTargetExpression());
                    if (edgeSources.size() == 1 && edgeTargets.size() == 1) {
                        // For now we only handle single source/target
                        Element edgeSource = edgeSources.getFirst();
                        Element edgeTarget = edgeTargets.getFirst();
                        if (Objects.equals(edgeSource, self) || borderNodeTargetObjectIds.contains(this.identityService.getId(edgeSource))) { // ICI on veut passer aussi si edgeSource == self.borderNode
                            // It is quite hard an inefficient to try to deduce the parent of a border node that doesn't exist yet.
                            if (this.commonQueryService.isFunctionPort(edgeTarget) || this.commonQueryService.isComponentPort(edgeTarget)) {
                                result.add(edgeTarget.getOwner());
                            } else {
                                result.add(edgeTarget);
                            }
                        }
                        if (Objects.equals(edgeTarget, self) || borderNodeTargetObjectIds.contains(this.identityService.getId(edgeTarget))) {
                            if (this.commonQueryService.isFunctionPort(edgeSource) || this.commonQueryService.isComponentPort(edgeSource)) {
                                result.add(edgeSource.getOwner());
                            } else {
                                result.add(edgeSource);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private Optional<Node> getParentNode(Element self, IEditingContext editingContext, DiagramContext diagramContext, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        return new NodeFinder(diagramContext.diagram()).getOneNodeMatching(node -> {
            boolean result = false;

            Optional<Object> optionalNodeTargetObject = this.objectSearchService.getObject(editingContext, node.getTargetObjectId());
            if (optionalNodeTargetObject.isPresent()) {
                Object nodeTargetObject = optionalNodeTargetObject.get();
                VariableManager variableManager = new VariableManager();
                variableManager.put(RepresentationVariables.SELF.name(), nodeTargetObject);
                variableManager.put(CoreVariables.EDITING_CONTEXT.name(), editingContext);
                variableManager.put(DiagramContext.DIAGRAM_CONTEXT, diagramContext);

                Optional<NodeDescription> optionalNodeDescription = convertedNodes.values().stream()
                        .filter(nodeDescription -> Objects.equals(nodeDescription.getId(), node.getDescriptionId()))
                        .findFirst();
                if (optionalNodeDescription.isPresent()) {
                    NodeDescription nodeDescription = optionalNodeDescription.get();
                    result = this.getAllChildNodeDescriptions(nodeDescription, convertedNodes).stream()
                            .anyMatch(childNodeDescription -> childNodeDescription.getSemanticElementsProvider().apply(variableManager).contains(self));
                }
            }

            return result;
        });
    }

    private List<Element> evaluateAsElements(AQLInterpreter interpreter, VariableManager variableManager, String aqlExpression) {
        return interpreter.evaluateExpression(variableManager.getVariables(), aqlExpression)
                .asObjects()
                .orElse(List.of())
                .stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .toList();
    }

    private List<NodeDescription> getAllChildNodeDescriptions(NodeDescription nodeDescription, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        List<NodeDescription> result = new ArrayList<>();
        result.addAll(nodeDescription.getChildNodeDescriptions());
        result.addAll(convertedNodes.values().stream()
                .filter(convertedNode -> nodeDescription.getReusedChildNodeDescriptionIds().contains(convertedNode.getId()))
                .toList());
        return result;
    }
}

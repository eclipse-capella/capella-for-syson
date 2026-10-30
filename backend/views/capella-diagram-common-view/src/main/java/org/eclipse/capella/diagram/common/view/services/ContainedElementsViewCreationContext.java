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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.components.NodeIdProvider;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.emf.DomainClassPredicate;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.ViewUsage;

/**
 * Selects node descriptions with caches scoped to one population operation, which leaves the semantic model unchanged.
 *
 * @author Jerome Gout
 */
public final class ContainedElementsViewCreationContext {

    private final DiagramMutationElementService elementService;

    private final IObjectSearchService objectSearchService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IEditingContext editingContext;

    private final DiagramContext diagramContext;

    private final Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes;

    private final Map<String, NodeDescription> descriptionsById = new LinkedHashMap<>();

    private final Map<NodeDescription, org.eclipse.sirius.components.view.diagram.NodeDescription> sourceDescriptions = new HashMap<>();

    private final Map<NodeDescription, DomainClassPredicate> domainPredicates = new HashMap<>();

    private final Map<String, Optional<Object>> objectsById = new HashMap<>();

    private final Map<Object, ParentContext> parents = new HashMap<>();

    private final Map<CandidatesKey, Set<?>> candidates = new HashMap<>();

    public ContainedElementsViewCreationContext(DiagramMutationElementService elementService, IObjectSearchService objectSearchService,
            IRepresentationDescriptionSearchService representationDescriptionSearchService, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.elementService = elementService;
        this.objectSearchService = objectSearchService;
        this.representationDescriptionSearchService = representationDescriptionSearchService;
        this.editingContext = editingContext;
        this.diagramContext = diagramContext;
        this.convertedNodes = convertedNodes;
        convertedNodes.forEach((source, converted) -> {
            this.descriptionsById.putIfAbsent(converted.getId(), converted);
            this.sourceDescriptions.putIfAbsent(converted, source);
            this.domainPredicates.putIfAbsent(converted, new DomainClassPredicate(Optional.ofNullable(source.getDomainType()).orElse("")));
        });
    }

    public void registerElement(String id, Element element) {
        this.objectsById.put(id, Optional.of(element));
    }

    public ViewCreationRequest createView(Element element, Object parent) {
        ViewCreationRequest request = null;
        if (this.convertedNodes.isEmpty()) {
            request = this.elementService.createView(element, this.editingContext, this.diagramContext, parent, this.convertedNodes);
        } else {
            var parentContext = this.parents.computeIfAbsent(parent, this::resolveParent);
            var description = parentContext.descriptions().stream()
                    .filter(candidate -> this.canRender(candidate, element, parentContext.owner()))
                    .findFirst();
            if (description.isPresent()) {
                request = this.elementService.createView(element, parentContext.id(), description.get().getId(), this.editingContext, this.diagramContext, NodeContainmentKind.CHILD_NODE);
            }
        }
        return request;
    }

    private ParentContext resolveParent(Object parent) {
        String parentId = this.diagramContext.diagram().getId();
        String targetId = this.diagramContext.diagram().getTargetObjectId();
        NodeDescription parentDescription = null;
        if (parent instanceof Node node) {
            parentId = node.getId();
            targetId = node.getTargetObjectId();
            parentDescription = this.descriptionsById.get(node.getDescriptionId());
        } else if (parent instanceof ViewCreationRequest request) {
            parentId = new NodeIdProvider().getNodeId(request.getParentElementId(), request.getDescriptionId(), NodeContainmentKind.CHILD_NODE, request.getTargetObjectId());
            targetId = request.getTargetObjectId();
            parentDescription = this.descriptionsById.get(request.getDescriptionId());
        }
        List<NodeDescription> descriptions = new ArrayList<>();
        if (!(parent instanceof Node) && !(parent instanceof ViewCreationRequest request && request.getDescriptionId() != null)) {
            targetId = this.diagramContext.diagram().getTargetObjectId();
            descriptions.addAll(this.representationDescriptionSearchService.findById(this.editingContext, this.diagramContext.diagram().getDescriptionId())
                    .filter(DiagramDescription.class::isInstance).map(DiagramDescription.class::cast)
                    .map(DiagramDescription::getNodeDescriptions).orElse(List.of()));
        } else if (parentDescription != null) {
            descriptions.addAll(parentDescription.getChildNodeDescriptions());
            descriptions.addAll(parentDescription.getBorderNodeDescriptions());
            var reusedChildren = parentDescription.getReusedChildNodeDescriptionIds();
            var reusedBorders = parentDescription.getReusedBorderNodeDescriptionIds();
            this.convertedNodes.values().stream().filter(node -> reusedChildren.contains(node.getId())).forEach(descriptions::add);
            this.convertedNodes.values().stream().filter(node -> reusedBorders.contains(node.getId())).forEach(descriptions::add);
        }
        return new ParentContext(parentId, this.getObject(targetId).orElse(null), descriptions);
    }

    private Optional<Object> getObject(String id) {
        return this.objectsById.computeIfAbsent(id, key -> this.objectSearchService.getObject(this.editingContext, key));
    }

    private boolean canRender(NodeDescription description, Element element, Object owner) {
        var source = this.sourceDescriptions.get(description);
        boolean matches = false;
        if (source == null || this.domainPredicates.get(description).test(element.eClass())) {
            Set<?> elements;
            if (source != null && !Optional.ofNullable(source.getSemanticCandidatesExpression()).orElse("").contains(DiagramContext.DIAGRAM_CONTEXT)) {
                elements = this.candidates.computeIfAbsent(new CandidatesKey(owner, description), key -> this.computeCandidates(description, owner));
            } else {
                // These providers can observe newly queued views, such as OAB activity placement, and must be evaluated again.
                elements = this.computeCandidates(description, owner);
            }
            if (elements.contains(element)) {
                matches = description.getShouldRenderPredicate().test(this.variables(element, null));
            }
        }
        return matches;
    }

    private Set<?> computeCandidates(NodeDescription description, Object owner) {
        List<Object> ancestors = new ArrayList<>();
        this.getObject(this.diagramContext.diagram().getTargetObjectId()).filter(ViewUsage.class::isInstance).ifPresent(ancestors::add);
        return new HashSet<>(description.getSemanticElementsProvider().apply(this.variables(owner, ancestors)));
    }

    private VariableManager variables(Object self, Object ancestors) {
        var variables = new VariableManager();
        variables.put(VariableManager.SELF, self);
        variables.put(IEditingContext.EDITING_CONTEXT, this.editingContext);
        variables.put(DiagramContext.DIAGRAM_CONTEXT, this.diagramContext);
        variables.put(NodeDescription.ANCESTORS, ancestors);
        return variables;
    }

    /**
     * Resolved graphical parent and ordered descriptions, including border and reused descriptions.
     */
    private record ParentContext(String id, Object owner, List<NodeDescription> descriptions) {
    }

    /**
     * A provider's candidates depend on both its description and its semantic parent.
     */
    private record CandidatesKey(Object owner, NodeDescription description) {
    }
}

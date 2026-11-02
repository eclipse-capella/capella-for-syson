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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.stereotype.Service;

/**
 * Displays existing Capella elements without changing their semantic containment or allocations.
 *
 * @author jgout
 */
@Service
public class RelatedElementsService {

    private final CommonQueryService queryService;

    private final DiagramMutationElementService elementService;

    private final IIdentityService identityService;

    private final IObjectSearchService objectSearchService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    public RelatedElementsService(DiagramMutationElementService elementService, IIdentityService identityService, IObjectSearchService objectSearchService,
            IRepresentationDescriptionSearchService representationDescriptionSearchService) {
        this.elementService = Objects.requireNonNull(elementService);
        this.identityService = Objects.requireNonNull(identityService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.queryService = new CommonQueryService();
    }

    /**
     * Displays contained elements in the diagram, recursively when requested. These include namespace owned members and,
     * for part usages, subfunctions and performed operational activities. Existing nodes and pending view creation requests
     * are reused without changing semantic containment or allocations.
     * <p>This service is used by tools Show contained elements (recursive or not)</p>
     *
     * @param self
     *            the element whose contained elements to display
     * @param recursive
     *            whether to also display descendants of the contained elements
     * @param editingContext
     *            the editing context
     * @param diagramContext
     *            the diagram context receiving view creation requests
     * @param selectedNode
     *            the selected node; its outermost ancestor representing the same element is used as the parent
     * @param convertedNodes
     *            the mapping from view node descriptions to converted diagram node descriptions
     * @return {@code self}
     */
    public Element showContainedElements(Element self, boolean recursive, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        Object parent = selectedNode;
        var nodeFinder = new NodeFinder(diagramContext.diagram());
        while (parent instanceof Node node && nodeFinder.getParent(node) instanceof Node parentNode
                && Objects.equals(node.getTargetObjectId(), parentNode.getTargetObjectId())) {
            parent = parentNode;
        }
        Map<String, Object> viewsByElementId = new HashMap<>();
        nodeFinder.getAllNodesMatching(node -> true).forEach(node -> viewsByElementId.putIfAbsent(node.getTargetObjectId(), node));
        diagramContext.viewCreationRequests().forEach(request -> viewsByElementId.putIfAbsent(request.getTargetObjectId(), request));
        var viewCreationContext = new ContainedElementsViewCreationContext(this.elementService, this.objectSearchService, this.representationDescriptionSearchService, editingContext, diagramContext, convertedNodes);
        viewCreationContext.registerElement(this.identityService.getId(self), self);
        this.showChildren(self, parent, recursive, new RenderingContext(viewsByElementId, viewCreationContext), new HashSet<>());
        return self;
    }

    private void showChildren(Element element, Object parent, boolean recursive, RenderingContext context, Set<Element> visited) {
        if (visited.add(element)) {
            var children = new LinkedHashSet<Element>();
            if (element instanceof Namespace namespace) {
                children.addAll(namespace.getOwnedMember());
            }
            if (element instanceof PartUsage partUsage) {
                children.addAll(this.queryService.getSubFunctions(partUsage));
                children.addAll(this.queryService.getPerformedActions(partUsage, this.queryService::isOperationalActivity));
            }
            for (Element child : children) {
                var view = this.showNode(child, parent, context);
                if (recursive) {
                    this.showChildren(child, view.orElse(parent), true, context, visited);
                }
            }
        }
    }

    private Optional<Object> showNode(Element element, Object parent, RenderingContext context) {
        String elementId = this.identityService.getId(element);
        context.viewCreation().registerElement(elementId, element);
        Object view = context.viewsByElementId().get(elementId);
        if (view == null) {
            view = context.viewCreation().createView(element, parent);
            if (view != null) {
                context.viewsByElementId().put(elementId, view);
            }
        }
        return Optional.ofNullable(view);
    }

    /**
     * Context shared by recursive view creation requests.
     */
    private record RenderingContext(Map<String, Object> viewsByElementId, ContainedElementsViewCreationContext viewCreation) {
    }
}

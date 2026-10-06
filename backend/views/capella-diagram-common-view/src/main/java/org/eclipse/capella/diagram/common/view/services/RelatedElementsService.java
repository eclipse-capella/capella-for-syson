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
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public RelatedElementsService(DiagramMutationElementService elementService, IIdentityService identityService) {
        this(new CommonQueryService(), elementService, identityService);
    }

    public RelatedElementsService(CommonQueryService queryService, DiagramMutationElementService elementService, IIdentityService identityService) {
        this.queryService = Objects.requireNonNull(queryService);
        this.elementService = Objects.requireNonNull(elementService);
        this.identityService = Objects.requireNonNull(identityService);
    }

    public Element showContainedElements(Element self, boolean recursive, IEditingContext editingContext, DiagramContext diagramContext, Object selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        Object parent = selectedNode;
        var nodeFinder = new NodeFinder(diagramContext.diagram());
        while (parent instanceof Node node && nodeFinder.getParent(node) instanceof Node parentNode
                && Objects.equals(node.getTargetObjectId(), parentNode.getTargetObjectId())) {
            parent = parentNode;
        }
        this.showChildren(self, parent, recursive, new RenderingContext(editingContext, diagramContext, convertedNodes), new HashSet<>());
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
        return this.findView(element, context.diagramContext())
                .or(() -> Optional.ofNullable(this.elementService.createView(element, context.editingContext(), context.diagramContext(), parent, context.convertedNodes())));
    }

    private Optional<Object> findView(Element element, DiagramContext diagramContext) {
        String elementId = this.identityService.getId(element);
        var node = new NodeFinder(diagramContext.diagram()).getOneNodeMatching(candidate -> elementId.equals(candidate.getTargetObjectId()));
        return node.<Object>map(candidate -> candidate)
                .or(() -> diagramContext.viewCreationRequests().stream().filter(request -> elementId.equals(request.getTargetObjectId())).findFirst());
    }

    /**
     * Context shared by recursive view creation requests.
     */
    private record RenderingContext(IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
    }
}

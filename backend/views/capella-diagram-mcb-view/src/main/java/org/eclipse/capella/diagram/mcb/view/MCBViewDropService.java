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
package org.eclipse.capella.diagram.mcb.view;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.model.transverse.services.CommonViewCreationRequestSubtreeService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.stereotype.Service;

/**
 * Atomic graphical reveal and actor reparenting for MCB nodes.
 *
 * @author tbezierslafosse
 */
@Service
public class MCBViewDropService {

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    private final MCBViewQueryService queryService = new MCBViewQueryService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final CommonViewCreationRequestSubtreeService subtreeService = new CommonViewCreationRequestSubtreeService();

    public MCBViewDropService(IObjectSearchService objectSearchService, IIdentityService identityService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = Objects.requireNonNull(identityService);
    }

    public Element dropFromExplorer(Element element, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var context = this.getContext(editingContext, diagramContext);
        if (context.isEmpty() || !this.queryService.isReachableNode(element, context.get())) {
            return element;
        }
        List<? extends Element> path = List.of(element);
        if (element instanceof PartUsage actor) {
            path = this.queryService.getActorPath(actor);
        }
        if (path.stream().noneMatch(candidate -> this.getDescriptionId(candidate, convertedNodes).isEmpty())) {
            String parentId = diagramContext.diagram().getId();
            for (Element candidate : path) {
                var existingNode = new NodeFinder(diagramContext.diagram()).getOneNodeMatching(node -> this.identityService.getId(candidate).equals(node.getTargetObjectId()));
                if (existingNode.isPresent()) {
                    parentId = existingNode.get().getId();
                } else {
                    var pending = diagramContext.viewCreationRequests().stream().filter(request -> this.identityService.getId(candidate).equals(request.getTargetObjectId())).findFirst();
                    ViewCreationRequest request;
                    if (pending.isPresent()) {
                        request = pending.get();
                    } else {
                        request = this.newRequest(candidate, this.getDescriptionId(candidate, convertedNodes).orElseThrow(), parentId);
                        diagramContext.viewCreationRequests().add(request);
                    }
                    parentId = this.subtreeService.getNodeIdFromViewCreationRequest(request);
                }
            }
        }
        return element;
    }

    public Element dropOnBackground(Element element, Node node, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.getContext(editingContext, diagramContext).ifPresent(context -> {
            if (element instanceof PartUsage actor && this.queryService.isReachableNode(actor, context)) {
                this.commonQueryService.getStructurePackage(context).ifPresent(parent -> this.moveActor(actor, node, parent, null, diagramContext, convertedNodes));
            }
        });
        return element;
    }

    public Element dropIntoActor(Element element, Node node, Element target, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.getContext(editingContext, diagramContext).ifPresent(context -> {
            if (element instanceof PartUsage actor && target instanceof PartUsage && this.queryService.isReachableNode(actor, context)
                    && this.queryService.isReachableNode(target, context)) {
                this.moveActor(actor, node, target, targetNode, diagramContext, convertedNodes);
            }
        });
        return element;
    }

    private void moveActor(PartUsage actor, Node node, Element parent, Node targetNode, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var descriptionId = this.getDescriptionId(actor, convertedNodes);
        if (node == null || descriptionId.isEmpty()) {
            return;
        }
        if (!this.identityService.getId(actor).equals(node.getTargetObjectId()) || actor.getOwner() == parent) {
            return;
        }
        if (new CommonUpdateService().setSystemActorContainer(actor, parent)) {
            String parentId = diagramContext.diagram().getId();
            if (targetNode != null) {
                parentId = targetNode.getId();
            }
            var request = this.newRequest(actor, descriptionId.get(), parentId);
            diagramContext.viewCreationRequests().add(request);
            this.subtreeService.addRecursiveViewCreationRequestsForSubtree(node, this.subtreeService.getNodeIdFromViewCreationRequest(request), diagramContext, new HashSet<>());
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(node.getId()).build());
        }
    }

    private ViewCreationRequest newRequest(Element element, String descriptionId, String parentId) {
        return ViewCreationRequest.newViewCreationRequest().parentElementId(parentId).descriptionId(descriptionId)
                .targetObjectId(this.identityService.getId(element)).containmentKind(NodeContainmentKind.CHILD_NODE).build();
    }

    private Optional<Element> getContext(IEditingContext editingContext, DiagramContext diagramContext) {
        return this.objectSearchService.getObject(editingContext, diagramContext.diagram().getTargetObjectId())
                .filter(Element.class::isInstance).map(Element.class::cast).filter(this.commonQueryService::isSystemAnalysisCapabilitiesPackage);
    }

    private Optional<String> getDescriptionId(Element element, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        String name;
        if (this.commonQueryService.isMission(element)) {
            name = "MissionNodeDescription";
        } else if (this.commonQueryService.isCapability(element)) {
            name = "CapabilityNodeDescription";
        } else if (this.commonQueryService.isSystemAnalysisActor(element)) {
            name = "SystemActorNodeDescription";
        } else {
            name = "RequirementNodeDescription";
        }
        return convertedNodes.entrySet().stream().filter(entry -> name.equals(entry.getKey().getName())).map(entry -> entry.getValue().getId()).findFirst();
    }
}

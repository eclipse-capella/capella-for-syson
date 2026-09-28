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
package org.eclipse.capella.model.services.operational.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.components.NodeIdProvider;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.util.NodeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services related to the drop tools.
 *
 * @author fbarbin
 */
public class OARepresentationDropServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(OARepresentationDropServices.class);

    private final DiagramMutationElementService diagramMutationElementService;

    private final ISysMLMoveElementService moveService;

    private final CommonQueryService commonQueryService;

    private final IIdentityService identityService;

    private final IObjectSearchService objectSearchService;

    private final CommonDeletionService commonDeletionService;

    private final CommonUpdateService commonUpdateService;

    private final OARepresentationQueryService representationQueryService;

    public OARepresentationDropServices(IIdentityService identityService, ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService,
            IObjectSearchService objectSearchService) {
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.moveService = Objects.requireNonNull(moveService);
        this.commonQueryService = new CommonQueryService();
        this.identityService = Objects.requireNonNull(identityService);
        this.representationQueryService = new OARepresentationQueryService(identityService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.commonDeletionService = new CommonDeletionService();
        this.commonUpdateService = new CommonUpdateService();
    }

    public Element dropIntoComponentFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.commonQueryService.isComponent(targetElement)) {
            if (this.commonQueryService.isComponent(droppedElement)) {
                this.droppedComponentIntoComponentCase(droppedElement, droppedNode, targetElement, targetNode, editingContext, diagramContext, convertedNodes);
            } else if (droppedElement instanceof ActionUsage activity && targetElement instanceof PartUsage targetComponent
                    && this.commonQueryService.isOperationalActivity(activity) && this.isOperationalComponent(targetComponent)) {
                this.dropActivityIntoComponent(activity, droppedNode, targetComponent, targetNode, editingContext, diagramContext, convertedNodes);
            }
        }
        return droppedElement;
    }

    public Element dropIntoActivityFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext,
            DiagramContext diagramContext, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (droppedElement != targetElement && this.commonQueryService.isOperationalActivity(droppedElement) && this.commonQueryService.isOperationalActivity(targetElement)) {
            this.commonQueryService.getAllocatingComponent((ActionUsage) targetElement)
                    .filter(this::isOperationalComponent).ifPresent(targetComponent -> {
                        var previousOwner = droppedElement.getOwner();
                        if (previousOwner == targetElement || this.moveService.moveSemanticElement(droppedElement, targetElement).isSuccess()) {
                            if (!this.dropActivityIntoComponent((ActionUsage) droppedElement, droppedNode, targetComponent, targetNode, editingContext, diagramContext, convertedNodes)
                                    && previousOwner != targetElement) {
                                this.moveService.moveSemanticElement(droppedElement, previousOwner);
                            }
                        }
                    });
        }
        return droppedElement;
    }

    public Element dropIntoDiagram(Element droppedElement, Node droppedNode, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.commonQueryService.isComponent(droppedElement)) {
            Optional<Package> optionalStructurePackage = this.commonQueryService.getStructurePackage(droppedElement);
            if (optionalStructurePackage.isPresent()) {
                this.moveService.moveSemanticElement(droppedElement, optionalStructurePackage.get());
                this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode,
                        convertedNodes);
                diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
            }
        }
        return droppedElement;
    }

    public Element dropIntoDiagramFromExplorer(Element droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (droppedElement instanceof RequirementUsage requirement && !this.commonQueryService.isOperationalAnalysisPerspective(requirement)) {
            LOGGER.atWarn().log("Ignoring drop of requirement {} outside Operational Analysis perspective", requirement.getElementId());
        } else if (droppedElement instanceof ActionUsage activity && this.commonQueryService.isOperationalActivity(activity)) {
            if (this.objectSearchService.getObject(editingContext, diagramContext.diagram().getTargetObjectId())
                    .filter(ActionUsage.class::isInstance)
                    .filter(object -> this.commonQueryService.isOperationalActivity((ActionUsage) object))
                    .isPresent()) {
                this.diagramMutationElementService.createView(activity, editingContext, diagramContext, selectedNode, convertedNodes);
                return droppedElement;
            }
            this.getOperationalComponentNode(selectedNode, editingContext, diagramContext)
                    .ifPresent(targetNode -> this.getOperationalComponent(targetNode, editingContext).ifPresent(targetComponent ->
                            this.dropActivityIntoComponent(activity, null, targetComponent, targetNode, editingContext, diagramContext, convertedNodes)));
        } else {
            var parentView = this.getDisplayedParentView(droppedElement, diagramContext).<Object> map(node -> node).orElse(selectedNode);
            ViewCreationRequest droppedView = this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, parentView, convertedNodes);
            if (droppedElement instanceof PartUsage component && droppedView != null) {
                this.moveDisplayedChildrenUnderParent(component, droppedView, editingContext, diagramContext, convertedNodes);
            }
        }
        return droppedElement;
    }

    private void droppedComponentIntoComponentCase(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.moveService.moveSemanticElement(droppedElement, targetElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
        diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
    }

    private boolean dropActivityIntoComponent(ActionUsage activity, Node droppedNode, PartUsage targetComponent, Node targetNode, IEditingContext editingContext,
            DiagramContext diagramContext, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var hierarchy = new ArrayList<>(List.of(activity));
        hierarchy.addAll(this.commonQueryService.getDescendants(activity, this.commonQueryService::isOperationalActivity).stream()
                .map(ActionUsage.class::cast).toList());
        var components = new ArrayList<>(this.commonQueryService.getComponents(activity));
        if (!components.contains(targetComponent)) {
            components.add(targetComponent);
        }
        var previousAllocations = new LinkedHashMap<ActionUsage, List<PartUsage>>();
        hierarchy.forEach(candidate -> previousAllocations.put(candidate, new ArrayList<>()));
        components.forEach(component -> this.commonQueryService.getPerformedActions(component, hierarchy::contains)
                .forEach(candidate -> previousAllocations.get(candidate).add(component)));
        var activitiesToDisplay = hierarchy.stream()
                .filter(candidate -> candidate == activity || this.representationQueryService.findView(candidate, diagramContext).isPresent()).toList();
        var previousCreations = List.copyOf(diagramContext.viewCreationRequests());
        var previousDeletions = List.copyOf(diagramContext.viewDeletionRequests());
        var targetAllocation = List.of(targetComponent);
        previousAllocations.forEach((candidate, previous) -> this.setActivityAllocations(candidate, previous, targetAllocation));
        for (var candidate : activitiesToDisplay) {
            var parent = this.representationQueryService.getActivityParent(candidate, targetComponent, diagramContext);
            var parentView = this.representationQueryService.findView(parent, diagramContext).orElse(targetNode);
            if (!this.moveActivityView(candidate, parentView, editingContext, diagramContext, convertedNodes)) {
                previousAllocations.forEach((element, previous) -> this.setActivityAllocations(element, targetAllocation, previous));
                diagramContext.viewCreationRequests().clear();
                diagramContext.viewCreationRequests().addAll(previousCreations);
                diagramContext.viewDeletionRequests().clear();
                diagramContext.viewDeletionRequests().addAll(previousDeletions);
                return false;
            }
        }
        if (droppedNode != null && !this.getViewId(this.representationQueryService.findView(activity, diagramContext).orElseThrow()).equals(droppedNode.getId())
                && diagramContext.viewDeletionRequests().stream().noneMatch(request -> request.getElementId().equals(droppedNode.getId()))) {
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
        }
        return true;
    }

    private void setActivityAllocations(ActionUsage activity, List<PartUsage> previousComponents, List<PartUsage> targetComponents) {
        if (!previousComponents.equals(targetComponents)) {
            previousComponents.stream().distinct().forEach(component -> this.commonDeletionService.deletePerformedActionUsage(component, activity));
            targetComponents.forEach(component -> this.commonUpdateService.setPerformAction(component, activity));
        }
    }

    private boolean moveActivityView(ActionUsage activity, Object parentView, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var finder = new NodeFinder(diagramContext.diagram());
        Object view = this.representationQueryService.findView(activity, diagramContext).orElse(null);
        String parentId = this.getViewId(parentView);
        String previousParentId = null;
        if (view instanceof Node node) {
            previousParentId = this.getViewId(finder.getParent(node));
        } else if (view instanceof ViewCreationRequest request) {
            previousParentId = request.getParentElementId();
        }
        if (!parentId.equals(previousParentId)) {
            view = this.diagramMutationElementService.createView(activity, editingContext, diagramContext, parentView, convertedNodes);
        }
        if (view == null) {
            return false;
        }
        String retainedNodeId = this.getViewId(view);
        String activityId = this.identityService.getId(activity);
        Object retainedView = view;
        diagramContext.viewCreationRequests().removeIf(request -> activityId.equals(request.getTargetObjectId()) && request != retainedView);
        finder.getAllNodesMatching(node -> activityId.equals(node.getTargetObjectId()) && !retainedNodeId.equals(node.getId())).forEach(node -> {
            if (diagramContext.viewDeletionRequests().stream().noneMatch(request -> request.getElementId().equals(node.getId()))) {
                diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(node.getId()).build());
            }
        });
        return true;
    }

    private String getViewId(Object view) {
        String id;
        if (view instanceof ViewCreationRequest request) {
            id = new NodeIdProvider().getNodeId(request.getParentElementId(), request.getDescriptionId(), request.getContainmentKind(), request.getTargetObjectId());
        } else if (view instanceof Node node) {
            id = node.getId();
        } else {
            id = ((Diagram) view).getId();
        }
        return id;
    }

    private Optional<Node> getOperationalComponentNode(Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext) {
        var finder = new NodeFinder(diagramContext.diagram());
        Object current = selectedNode;
        while (current instanceof Node node) {
            if (this.getOperationalComponent(node, editingContext).isPresent()) {
                return Optional.of(node);
            }
            current = finder.getParent(node);
        }
        return Optional.empty();
    }

    private Optional<PartUsage> getOperationalComponent(Node node, IEditingContext editingContext) {
        return this.objectSearchService.getObject(editingContext, node.getTargetObjectId())
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this::isOperationalComponent);
    }

    private boolean isOperationalComponent(PartUsage component) {
        return this.commonQueryService.isComponent(component) && this.commonQueryService.isOperationalAnalysisPerspective(component);
    }

    private Optional<Node> getDisplayedParentView(Element droppedElement, DiagramContext diagramContext) {
        return this.commonQueryService.getParentComponent(droppedElement)
                .flatMap(parentComponent -> new NodeFinder(diagramContext.diagram())
                        .getOneNodeMatching(node -> this.identityService.getId(parentComponent).equals(node.getTargetObjectId())));
    }

    private void moveDisplayedChildrenUnderParent(PartUsage parentComponent, ViewCreationRequest parentView, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var nodeFinder = new NodeFinder(diagramContext.diagram());
        for (PartUsage childComponent : this.commonQueryService.getSubComponents(parentComponent)) {
            nodeFinder.getOneNodeMatching(node -> this.identityService.getId(childComponent).equals(node.getTargetObjectId()))
                    .filter(node -> !(nodeFinder.getParent(node) instanceof Node))
                    .ifPresent(node -> {
                        this.diagramMutationElementService.createView(childComponent, editingContext, diagramContext, parentView, convertedNodes);
                        diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(node.getId()).build());
                    });
        }
    }
}

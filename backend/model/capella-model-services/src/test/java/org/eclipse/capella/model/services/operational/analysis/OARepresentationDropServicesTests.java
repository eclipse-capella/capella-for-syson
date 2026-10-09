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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonMoveService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.components.NodeIdProvider;
import org.eclipse.sirius.components.diagrams.layoutdata.DiagramLayoutData;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests for OAB Operational Activity drop behavior.
 *
 * @author tbezierslafosse
 */
public class OARepresentationDropServicesTests extends AbstractSemanticTests {

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final OAMutationService oaMutationService = new OAMutationService();

    private final CommonDeletionService commonDeletionService = new CommonDeletionService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    @Test
    public void createFunctionViewWhenDroppedWithoutAllocationShouldAllocateAndCreateView() {
        var entity = this.createOperationalEntity();
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);
        this.commonDeletionService.deletePerformedActionUsage(entity, activity);
        var diagramServices = new RecordingDiagramServices();
        var targetNode = this.createNode(entity.getElementId(), "entity-node");
        var diagramContext = this.createDiagramContext();

        this.createDropServices(Map.of(entity.getElementId(), entity), diagramServices)
                .dropIntoDiagramFromExplorer(activity, targetNode, null, diagramContext, Map.of());

        assertEquals(Optional.of(entity), this.commonQueryService.getAllocatingComponent(activity));
        assertEquals(List.of(activity), diagramServices.createdElements);
    }

    @Test
    public void createFunctionViewWhenDroppedIntoAnotherOperationalEntityShouldReallocateAndMoveView() {
        var allocatingEntity = this.createOperationalEntity();
        var targetEntity = this.createOperationalEntity();
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(allocatingEntity);
        var diagramServices = new RecordingDiagramServices();
        var droppedNode = this.createNode(activity.getElementId(), "activity-node");
        var targetNode = this.createNode(targetEntity.getElementId(), "target-entity-node");
        var diagramContext = this.createDiagramContext();

        this.createDropServices(Map.of(), diagramServices)
                .dropIntoComponentFromDiagram(activity, droppedNode, targetEntity, targetNode, null, diagramContext, Map.of());

        assertEquals(Optional.of(targetEntity), this.commonQueryService.getAllocatingComponent(activity));
        assertEquals(List.of(activity), diagramServices.createdElements);
        assertEquals(List.of("activity-node"), diagramContext.viewDeletionRequests().stream().map(request -> request.getElementId()).toList());
    }

    @Test
    public void createFunctionViewWhenDroppedIntoDiagramShouldPreserveAllocationAndCreateView() {
        var entity = this.createOperationalEntity();
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext();
        var allocation = this.commonQueryService.getAllocatedFunctions(entity);

        this.createDropServices(Map.of("diagram-target", activity), diagramServices)
                .dropIntoDiagramFromExplorer(activity, diagramContext.diagram(), null, diagramContext, Map.of());

        assertEquals(allocation, this.commonQueryService.getAllocatedFunctions(entity));
        assertEquals(Optional.of(entity), this.commonQueryService.getAllocatingComponent(activity));
        assertEquals(List.of(activity), diagramServices.createdElements);
    }

    @Test
    public void createFunctionViewWhenDroppedFromExplorerInOAIBShouldPreserveAllocation() {
        var entity = this.createOperationalEntity();
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext();
        var allocation = this.commonQueryService.getAllocatedFunctions(entity);

        this.createDropServices(Map.of("diagram-target", activity), diagramServices)
                .dropIntoDiagramFromExplorer(activity, diagramContext.diagram(), null, diagramContext, Map.of());

        assertEquals(allocation, this.commonQueryService.getAllocatedFunctions(entity));
        assertEquals(List.of(activity), diagramServices.createdElements);
    }

    @Test
    public void createFunctionViewWhenDroppedFromExplorerInOAIBShouldUseDisplayedParent() {
        var entity = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(entity);
        var child = this.oaMutationService.createOperationalActivityOA(parent);
        var parentNode = this.createNode("view-" + parent.getElementId(), "parent-node");
        var diagramContext = this.createDiagramContext(parentNode);
        var diagramServices = new RecordingDiagramServices("view-");
        var identity = mock(IIdentityService.class);
        when(identity.getId(any(Element.class))).thenAnswer(invocation -> "view-" + ((Element) invocation.getArgument(0)).getElementId());
        IObjectSearchService objectSearchService = (editingContext, objectId) -> Optional.<Object>of(parent).filter(element -> "diagram-target".equals(objectId));

        new OARepresentationDropServices(identity, mock(ISysMLMoveElementService.class), diagramServices.elementService, objectSearchService)
                .dropIntoDiagramFromExplorer(child, diagramContext.diagram(), null, diagramContext, Map.of());

        assertEquals(List.of(child), diagramServices.createdElements);
        assertEquals("parent-node", diagramContext.viewCreationRequests().getFirst().getParentElementId());
    }

    @Test
    public void createFunctionViewWhenDroppedFromExplorerInOABShouldUseDisplayedParent() {
        var entity = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(entity);
        var child = this.oaMutationService.createOperationalActivityOA(parent);
        var parentNode = this.createNode(parent.getElementId(), "parent-node");
        var diagramContext = this.createDiagramContext(parentNode);
        var diagramServices = new RecordingDiagramServices();

        this.createDropServices(Map.of("diagram-target", parent), diagramServices)
                .dropIntoDiagramFromExplorer(child, diagramContext.diagram(), null, diagramContext, Map.of());

        assertEquals(List.of(child), diagramServices.createdElements);
        assertEquals("parent-node", diagramContext.viewCreationRequests().getFirst().getParentElementId());
    }

    @Test
    public void createRequirementViewWhenDroppedFromExplorerInOAIBShouldOnlyCreateView() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        RequirementUsage requirement = this.commonCreationService.createRequirement(structurePackage);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext();

        this.createDropServices(Map.of(), diagramServices)
                .dropIntoDiagramFromExplorer(requirement, diagramContext.diagram(), null, diagramContext, Map.of());

        assertEquals(List.of(requirement), diagramServices.createdElements);
    }

    @Test
    public void createFunctionViewWhenDroppedOnActivityInAnotherComponentShouldReallocateAndCreateViewUnderOperationalEntity() {
        var entity = this.createOperationalEntity();
        ActionUsage selectedActivity = this.oaMutationService.createOperationalActivityOA(entity);
        var sourceEntity = this.createOperationalEntity();
        ActionUsage droppedActivity = this.oaMutationService.createOperationalActivityOA(sourceEntity);
        var diagramServices = new RecordingDiagramServices();
        var activityNode = this.createNode(selectedActivity.getElementId(), "activity-node");
        var entityNode = this.createNode(entity.getElementId(), "entity-node");
        when(entityNode.getChildNodes()).thenReturn(List.of(activityNode));
        var diagramContext = new DiagramContext(Diagram.newDiagram("diagram")
                .targetObjectId("diagram-target")
                .descriptionId("diagram-description")
                .nodes(List.of(entityNode))
                .edges(List.of())
                .style(DiagramStyle.newDiagramStyle().build())
                .layoutData(new DiagramLayoutData(Map.of(), Map.of(), Map.of(), false))
                .build());

        this.createDropServices(Map.of(entity.getElementId(), entity, selectedActivity.getElementId(), selectedActivity), diagramServices)
                .dropIntoDiagramFromExplorer(droppedActivity, activityNode, null, diagramContext, Map.of());

        assertEquals(Optional.of(entity), this.commonQueryService.getAllocatingComponent(droppedActivity));
        assertEquals(List.of(), this.commonQueryService.getAllocatedFunctions(sourceEntity));
        assertEquals(List.of(droppedActivity), diagramServices.createdElements);
        verify(diagramServices.elementService).createView(same(droppedActivity), any(), same(diagramContext), same(entityNode), any());
    }

    @Test
    public void moveSemanticElementActivityWhenDroppedOnActivityInAnotherComponentShouldUpdateParentAllocationAndView() {
        var sourceEntity = this.createOperationalEntity();
        var targetEntity = this.createOperationalEntity();
        var parentActivity = this.oaMutationService.createOperationalActivityOA(sourceEntity);
        var activity = this.oaMutationService.createOperationalActivityOA(parentActivity);
        var childActivity = this.oaMutationService.createOperationalActivityOA(activity);
        var targetActivity = this.oaMutationService.createOperationalActivityOA(targetEntity);
        var droppedNode = this.createNode(activity.getElementId(), "activity-node");
        var targetNode = this.createNode(targetActivity.getElementId(), "target-node");
        var diagramContext = this.createDiagramContext();
        var diagramServices = new RecordingDiagramServices();

        this.createDropServices(Map.of(), diagramServices)
                .dropIntoActivityFromDiagram(activity, droppedNode, targetActivity, targetNode, null, diagramContext, Map.of());

        assertEquals(targetActivity, activity.getOwner());
        assertEquals(Optional.of(targetEntity), this.commonQueryService.getAllocatingComponent(activity));
        assertEquals(Optional.of(targetEntity), this.commonQueryService.getAllocatingComponent(childActivity));
        assertEquals(List.of(parentActivity), this.commonQueryService.getAllocatedFunctions(sourceEntity));
        verify(diagramServices.elementService).createView(same(activity), any(), same(diagramContext), same(targetNode), any());
        assertEquals(List.of("activity-node"), diagramContext.viewDeletionRequests().stream().map(request -> request.getElementId()).toList());
    }

    @Test
    public void moveSemanticElementActivityWhenDroppedOnOAIBActivityShouldPreserveAllocationAndCreateView() {
        var sourceEntity = this.createOperationalEntity();
        var targetEntity = this.createOperationalEntity();
        var activity = this.oaMutationService.createOperationalActivityOA(sourceEntity);
        var targetActivity = this.oaMutationService.createOperationalActivityOA(targetEntity);
        var droppedNode = this.createNode(activity.getElementId(), "activity-node");
        var targetNode = this.createNode(targetActivity.getElementId(), "target-node");
        var diagramContext = this.createDiagramContext(droppedNode, targetNode);
        var diagramServices = new RecordingDiagramServices();

        this.createDropServices(Map.of(), diagramServices)
                .dropIntoOAIBActivity(activity, droppedNode, targetActivity, targetNode, null, diagramContext, Map.of());

        assertEquals(targetActivity, activity.getOwner());
        assertEquals(Optional.of(sourceEntity), this.commonQueryService.getAllocatingComponent(activity));
        assertEquals(List.of(activity), diagramServices.createdElements);
        assertEquals("target-node", diagramContext.viewCreationRequests().getFirst().getParentElementId());
        assertEquals(List.of("activity-node"), diagramContext.viewDeletionRequests().stream().map(request -> request.getElementId()).toList());
    }

    @Test
    public void moveSemanticElementActivityWhenDroppedOnItselfOrDescendantShouldPreserveModelAndViews() {
        var entity = this.createOperationalEntity();
        var parentActivity = this.oaMutationService.createOperationalActivityOA(entity);
        var childActivity = this.oaMutationService.createOperationalActivityOA(parentActivity);
        var originalOwner = parentActivity.getOwner();
        var diagramContext = this.createDiagramContext();
        var diagramServices = new RecordingDiagramServices();
        var dropServices = this.createDropServices(Map.of(), diagramServices);

        for (var target : List.of(parentActivity, childActivity)) {
            dropServices.dropIntoActivityFromDiagram(parentActivity, this.createNode(parentActivity.getElementId(), "parent-node"),
                    target, this.createNode(target.getElementId(), "target-node"), null, diagramContext, Map.of());
        }

        assertEquals(originalOwner, parentActivity.getOwner());
        assertEquals(parentActivity, childActivity.getOwner());
        assertEquals(Optional.of(entity), this.commonQueryService.getAllocatingComponent(parentActivity));
        assertEquals(List.of(), diagramServices.createdElements);
        assertEquals(List.of(), diagramContext.viewDeletionRequests());
    }

    @Test
    public void moveSemanticElementActivityFromDiagramWhenParentMovesShouldAllocateHiddenDescendantsWithoutChangingOwners() {
        var source = this.createOperationalEntity();
        var target = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(source);
        var child = this.oaMutationService.createOperationalActivityOA(parent);
        var grandchild = this.oaMutationService.createOperationalActivityOA(child);
        var owner = parent.getOwner();
        var services = new RecordingDiagramServices();
        var context = this.createDiagramContext();
        var drops = this.createDropServices(Map.of(target.getElementId(), target), services);
        var targetNode = this.createNode(target.getElementId(), "target-node");

        drops.dropIntoComponentFromDiagram(parent, this.createNode(parent.getElementId(), "parent-node"), target, targetNode, null, context, Map.of());
        drops.dropIntoDiagramFromExplorer(parent, targetNode, null, context, Map.of());

        assertEquals(List.of(), this.commonQueryService.getAllocatedFunctions(source));
        assertEquals(List.of(parent, child, grandchild), this.commonQueryService.getAllocatedFunctions(target));
        assertEquals(owner, parent.getOwner());
        assertEquals(parent, child.getOwner());
        assertEquals(child, grandchild.getOwner());
        assertEquals(List.of(parent), services.createdElements);
        assertEquals(1, context.viewCreationRequests().size());
    }

    @Test
    public void moveSemanticElementActivityFromExplorerWhenViewCannotBeCreatedShouldRestoreAllocationsAndRequests() {
        var source = this.createOperationalEntity();
        var target = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(source);
        var child = this.oaMutationService.createOperationalActivityOA(parent);
        this.commonDeletionService.deletePerformedActionUsage(source, child);
        var services = new RecordingDiagramServices();
        when(services.elementService.createView(any(Element.class), any(), any(DiagramContext.class), any(), any())).thenReturn(null);
        var context = this.createDiagramContext();

        this.createDropServices(Map.of(target.getElementId(), target), services)
                .dropIntoDiagramFromExplorer(parent, this.createNode(target.getElementId(), "target-node"), null, context, Map.of());

        assertEquals(List.of(parent), this.commonQueryService.getAllocatedFunctions(source));
        assertEquals(List.of(), this.commonQueryService.getAllocatedFunctions(target));
        assertEquals(Optional.empty(), this.commonQueryService.getAllocatingComponent(child));
        assertEquals(List.of(), context.viewCreationRequests());
        assertEquals(List.of(), context.viewDeletionRequests());
    }

    @Test
    public void moveSemanticElementActivityFromExplorerWhenGraphicalIdsDifferShouldPlaceChildUnderDisplayedParent() {
        var entity = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(entity);
        var child = this.oaMutationService.createOperationalActivityOA(parent);
        var parentNode = this.createNode("view-" + parent.getElementId(), "parent-node");
        var entityNode = this.createNode("view-" + entity.getElementId(), "entity-node");
        when(entityNode.getChildNodes()).thenReturn(List.of(parentNode));
        var context = this.createDiagramContext(entityNode);
        var services = new RecordingDiagramServices("view-");
        var identity = mock(IIdentityService.class);
        when(identity.getId(any(Element.class))).thenAnswer(invocation -> "view-" + ((Element) invocation.getArgument(0)).getElementId());
        IObjectSearchService objects = (editingContext, id) -> Optional.of(entity).filter(element -> id.equals(identity.getId(element))).map(Object.class::cast);

        new OARepresentationDropServices(identity, mock(ISysMLMoveElementService.class), services.elementService, objects)
                .dropIntoDiagramFromExplorer(child, entityNode, null, context, Map.of());

        assertEquals(List.of(child), services.createdElements);
        assertEquals("parent-node", context.viewCreationRequests().getFirst().getParentElementId());
        assertEquals(parent, child.getOwner());
        assertEquals(Optional.of(entity), this.commonQueryService.getAllocatingComponent(child));
    }

    @Test
    public void moveSemanticElementActivityFromDiagramWhenChildAlreadyInsideParentShouldReuseView() {
        var entity = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(entity);
        var child = this.oaMutationService.createOperationalActivityOA(parent);
        var childNode = this.createNode(child.getElementId(), "child-node");
        var parentNode = this.createNode(parent.getElementId(), "parent-node");
        var entityNode = this.createNode(entity.getElementId(), "entity-node");
        when(parentNode.getChildNodes()).thenReturn(List.of(childNode));
        when(entityNode.getChildNodes()).thenReturn(List.of(parentNode));
        var context = this.createDiagramContext(entityNode);
        var services = new RecordingDiagramServices();

        this.createDropServices(Map.of(), services).dropIntoActivityFromDiagram(child, childNode, parent, parentNode, null, context, Map.of());

        assertEquals(parent, child.getOwner());
        assertEquals(List.of(), services.createdElements);
        assertEquals(List.of(), context.viewCreationRequests());
        assertEquals(List.of(), context.viewDeletionRequests());
        assertEquals(List.of(parent, child), this.commonQueryService.getAllocatedFunctions(entity));
    }

    @Test
    public void moveSemanticElementActivityFromExplorerWhenIntermediateParentIsMissingShouldNestUnderNearestDisplayedAncestor() {
        var entity = this.createOperationalEntity();
        var parent = this.oaMutationService.createOperationalActivityOA(entity);
        var middle = this.oaMutationService.createOperationalActivityOA(parent);
        var child = this.oaMutationService.createOperationalActivityOA(middle);
        var parentNode = this.createNode(parent.getElementId(), "parent-node");
        var entityNode = this.createNode(entity.getElementId(), "entity-node");
        when(entityNode.getChildNodes()).thenReturn(List.of(parentNode));
        var context = this.createDiagramContext(entityNode);
        var services = new RecordingDiagramServices();
        var drops = this.createDropServices(Map.of(entity.getElementId(), entity), services);

        drops.dropIntoDiagramFromExplorer(child, entityNode, null, context, Map.of());
        assertEquals("parent-node", context.viewCreationRequests().getFirst().getParentElementId());
        drops.dropIntoDiagramFromExplorer(middle, entityNode, null, context, Map.of());

        var middleView = context.viewCreationRequests().stream().filter(request -> request.getTargetObjectId().equals(middle.getElementId())).findFirst().orElseThrow();
        var childViews = context.viewCreationRequests().stream().filter(request -> request.getTargetObjectId().equals(child.getElementId())).toList();
        assertEquals(1, childViews.size());
        assertEquals(new NodeIdProvider().getNodeId(middleView.getParentElementId(), middleView.getDescriptionId(), middleView.getContainmentKind(), middleView.getTargetObjectId()),
                childViews.getFirst().getParentElementId());
        assertEquals(middle, child.getOwner());
    }

    private OARepresentationDropServices createDropServices(Map<String, Object> objectsById, RecordingDiagramServices diagramServices) {
        IObjectSearchService objectSearchService = (editingContext, objectId) -> Optional.ofNullable(objectsById.get(objectId));
        ISysMLMoveElementService moveService = mock(ISysMLMoveElementService.class);
        when(moveService.moveSemanticElement(any(Element.class), any(Element.class)))
                .thenAnswer(invocation -> new CommonMoveService().moveSemanticElement(invocation.getArgument(0), invocation.getArgument(1)));
        var identity = mock(IIdentityService.class);
        when(identity.getId(any(Element.class))).thenAnswer(invocation -> ((Element) invocation.getArgument(0)).getElementId());
        return new OARepresentationDropServices(identity, moveService, diagramServices.elementService, objectSearchService);
    }

    private PartUsage createOperationalEntity() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        return this.commonCreationService.createComponent(structurePackage);
    }

    private DiagramContext createDiagramContext(Node... nodes) {
        var diagramLayoutData = new DiagramLayoutData(Map.of(), Map.of(), Map.of(), false);
        return new DiagramContext(Diagram.newDiagram("diagram")
                .targetObjectId("diagram-target")
                .descriptionId("diagram-description")
                .nodes(List.of(nodes))
                .edges(List.of())
                .style(DiagramStyle.newDiagramStyle().build())
                .layoutData(diagramLayoutData)
                .build());
    }

    private Node createNode(String targetObjectId, String nodeId) {
        Node node = mock(Node.class);
        when(node.getTargetObjectId()).thenReturn(targetObjectId);
        when(node.getId()).thenReturn(nodeId);
        return node;
    }

    private static final class RecordingDiagramServices {

        private final List<Element> createdElements = new ArrayList<>();

        private final DiagramMutationElementService elementService = mock(DiagramMutationElementService.class);

        private final String idPrefix;

        private RecordingDiagramServices() {
            this("");
        }

        private RecordingDiagramServices(String idPrefix) {
            this.idPrefix = idPrefix;
            doAnswer(invocation -> this.recordView(invocation.getArgument(0), invocation.getArgument(2), invocation.getArgument(3)))
                    .when(this.elementService).createView(any(Element.class), any(), any(DiagramContext.class), any(), any());
        }

        private ViewCreationRequest recordView(Element element, DiagramContext diagramContext, Object parent) {
            this.createdElements.add(element);
            String parentId = diagramContext.diagram().getId();
            if (parent instanceof Node node) {
                parentId = node.getId();
            } else if (parent instanceof ViewCreationRequest request) {
                parentId = new NodeIdProvider().getNodeId(request.getParentElementId(), request.getDescriptionId(), request.getContainmentKind(), request.getTargetObjectId());
            }
            ViewCreationRequest request = ViewCreationRequest.newViewCreationRequest()
                    .parentElementId(parentId)
                    .descriptionId("description")
                    .targetObjectId(this.idPrefix + element.getElementId())
                    .containmentKind(NodeContainmentKind.CHILD_NODE)
                    .build();
            diagramContext.viewCreationRequests().add(request);
            return request;
        }
    }
}

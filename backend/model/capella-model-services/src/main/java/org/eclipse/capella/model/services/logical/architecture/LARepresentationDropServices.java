/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.model.services.logical.architecture;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;

/**
 * Services related to the drop tools.
 *
 * @author fbarbin
 */
public class LARepresentationDropServices {

    private final LAQueryService laQueryService;

    private final DiagramMutationElementService diagramMutationElementService;

    private final IObjectSearchService objectSearchService;

    private final IIdentityService identityService;

    private final ISysMLMoveElementService moveService;

    private final TransverseQueryService transverseQueryService;

    private final TransverseMutationService transverseMutationService;

    private final LAViewCreationRequestSubtreeService viewCreationRequestSubtreeService;

    public LARepresentationDropServices(IObjectSearchService objectSearchService, IIdentityService identityService, ISysMLMoveElementService moveService,
            DiagramMutationElementService diagramMutationElementService) {
        this.laQueryService = new LAQueryService();
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.identityService = Objects.requireNonNull(identityService);
        this.moveService = Objects.requireNonNull(moveService);
        this.transverseQueryService = new TransverseQueryService();
        this.transverseMutationService = new TransverseMutationService();
        this.viewCreationRequestSubtreeService = new LAViewCreationRequestSubtreeService();
    }

    public Element dropIntoComponentFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.transverseQueryService.isComponent(targetElement)) {
            if (this.transverseQueryService.isFunction(droppedElement)) {
                this.droppedFunctionIntoComponentCase(droppedElement, droppedNode, targetElement, targetNode, editingContext, diagramContext, convertedNodes);
            } else if (this.transverseQueryService.isComponent(droppedElement)) {
                this.droppedComponentIntoComponentCase(droppedElement, droppedNode, targetElement, targetNode, editingContext, diagramContext, convertedNodes);
            }
        }
        return droppedElement;
    }

    public Element dropIntoDiagram(Element droppedElement, Node droppedNode, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.transverseQueryService.isComponent(droppedElement)) {
            Optional<Package> optionalStructurePackage = this.transverseQueryService.getStructurePackage(droppedElement);
            if (optionalStructurePackage.isPresent()) {
                this.moveService.moveSemanticElement(droppedElement, optionalStructurePackage.get());
                this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode,
                        convertedNodes);
                diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
            }
        }
        return droppedElement;
    }

    /**
     * Handles dropping an element into a Package on the LAB diagram.
     * Supports dropping Package, RequirementUsage, and Comment into Package.
     */
    public Element dropIntoPackageFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (targetElement instanceof Package targetPackage) {
            // Move the element into the target package
            this.moveService.moveSemanticElement(droppedElement, targetPackage);
            // Create a new view at the target node
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
            // Delete the old view
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
        }
        return droppedElement;
    }

    public Element dropIntoFunctionFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.transverseQueryService.isFunction(targetElement)) {
            if (this.transverseQueryService.isFunction(droppedElement)) {
                this.droppedFunctionIntoFunctionCase(droppedElement, droppedNode, targetElement, targetNode, editingContext, diagramContext, convertedNodes);
            }
        }
        return droppedElement;
    }

    public Element dropIntoDiagramFromExplorer(Element droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        // Handle Package drop from explorer - move to Structure package if needed
        if (droppedElement instanceof Package droppedPackage && this.transverseQueryService.isUserPackage(droppedPackage)) {
            // Move the package to the Structure package so it becomes visible on the LAB diagram
            // toComponentsPackage returns the Structure package ('Logical Architecture'::Structure)
            Optional<Package> optionalStructurePackage = this.transverseQueryService.getStructurePackage(droppedElement);
            if (optionalStructurePackage.isPresent() && !optionalStructurePackage.get().equals(droppedPackage.getOwningNamespace())) {
                this.moveService.moveSemanticElement(droppedPackage, optionalStructurePackage.get());
            }
        }

        boolean droppedInsideContainer = this.isDroppedInsideContainer(droppedElement, selectedNode);
        boolean preventedCreation = this.transverseQueryService.isComponent(droppedElement)
                && (this.isAlreadyContainedInParentNode(droppedElement, diagramContext)
                        || (this.isContainerRepresentedInDiagram(droppedElement, diagramContext) && !droppedInsideContainer));
        if (!preventedCreation) {
            var parentViewCreationRequest = this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, selectedNode, convertedNodes);
            if (droppedElement instanceof PartUsage droppedComponent) {
                this.moveExistingSubComponentViewsUnderParent(droppedComponent, parentViewCreationRequest, editingContext, diagramContext, convertedNodes);
            }
        }
        return droppedElement;

    }

    private void droppedFunctionIntoFunctionCase(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.moveService.moveSemanticElement(droppedElement, targetElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode,
                convertedNodes);
        this.getPreviousParentContainer(droppedNode, editingContext, diagramContext)
                .ifPresent(previousContainer -> this.handlePreviousFunctionContainer(previousContainer, droppedElement));
    }

    private void droppedComponentIntoComponentCase(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.moveService.moveSemanticElement(droppedElement, targetElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode,
                convertedNodes);
        diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
    }

    private void droppedFunctionIntoComponentCase(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.transverseMutationService.setPerformAction(targetElement, (ActionUsage) droppedElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
        // A Function dropped into a container needs to be moved in the functions package.
        Optional<Package> optionalFunctionsPackage = this.transverseQueryService.getFunctionsPackage(targetElement);
        if (optionalFunctionsPackage.isPresent()) {
            this.moveService.moveSemanticElement(droppedElement, optionalFunctionsPackage.get());
            this.getPreviousParentContainer(droppedNode, editingContext, diagramContext)
                    .ifPresent(previousContainer -> this.handlePreviousFunctionContainer(previousContainer, droppedElement));
        }

    }

    private Optional<Object> getPreviousParentContainer(Node droppedNode, IEditingContext editingContext, DiagramContext diagramContext) {
        var parent = new NodeFinder(diagramContext.diagram()).getParent(droppedNode);
        if (parent instanceof Node parentNode) {
            return this.objectSearchService.getObject(editingContext, parentNode.getTargetObjectId());
        }
        return Optional.empty();
    }

    private void handlePreviousFunctionContainer(Object formerContainer, Element droppedElement) {
        if (formerContainer instanceof PartUsage partUsage) {
            if (this.transverseQueryService.isComponent(partUsage)) {
                this.transverseMutationService.deletePerformedActionUsage(partUsage, (ActionUsage) droppedElement);
            }
        }
    }

    private boolean isAlreadyContainedInParentNode(Element droppedElement, DiagramContext diagramContext) {
        String droppedElementId = this.identityService.getId(droppedElement);
        Set<String> removedNodeIds = diagramContext.viewDeletionRequests().stream()
                .map(ViewDeletionRequest::getElementId)
                .collect(Collectors.toSet());
        var nodeFinder = new NodeFinder(diagramContext.diagram());
        for (Node node : nodeFinder.getAllNodesMatching(candidate -> !removedNodeIds.contains(candidate.getId()))) {
            if (Objects.equals(droppedElementId, node.getTargetObjectId()) && nodeFinder.getParent(node) instanceof Node) {
                return true;
            }
        }
        return false;
    }

    private void moveExistingSubComponentViewsUnderParent(PartUsage droppedComponent, ViewCreationRequest parentViewCreationRequest, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        // Resolve semantic sub-components of the dropped parent: only those can be moved under it.
        var subComponents = this.transverseQueryService.getSubComponents(droppedComponent);
        if (subComponents.isEmpty()) {
            return;
        }

        // Build a fast lookup by semantic id to match existing diagram nodes to semantic sub-components.
        Map<String, PartUsage> subComponentById = new HashMap<>();
        for (PartUsage subComponent : subComponents) {
            subComponentById.put(this.identityService.getId(subComponent), subComponent);
        }

        // Track pending removals/creations to avoid conflicting or duplicate view requests in the same drop action.
        Set<String> removedNodeIds = diagramContext.viewDeletionRequests().stream()
                .map(ViewDeletionRequest::getElementId)
                .collect(Collectors.toSet());
        Set<String> alreadyCreatedTargetIds = diagramContext.viewCreationRequests().stream()
                .map(ViewCreationRequest::getTargetObjectId)
                .collect(Collectors.toSet());
        Set<String> alreadyCreatedNodeIds = new HashSet<>();
        // Existing node ids on the diagram (excluding nodes already scheduled for deletion).
        alreadyCreatedNodeIds.addAll(new NodeFinder(diagramContext.diagram()).getAllNodesMatching(node -> !removedNodeIds.contains(node.getId()))
                .stream()
                .map(Node::getId)
                .toList());
        // Future node ids from already queued creation requests.
        alreadyCreatedNodeIds.addAll(diagramContext.viewCreationRequests().stream()
                .map(this.viewCreationRequestSubtreeService::getNodeIdFromViewCreationRequest)
                .collect(Collectors.toSet()));

        var nodeFinder = new NodeFinder(diagramContext.diagram());
        for (Node node : nodeFinder.getAllNodesMatching(candidate -> !removedNodeIds.contains(candidate.getId()))) {
            // Only move root-level sub-component views currently shown on diagram blank.
            if (!(nodeFinder.getParent(node) instanceof Node) && subComponentById.containsKey(node.getTargetObjectId())) {
                PartUsage subComponent = subComponentById.get(node.getTargetObjectId());
                if (!alreadyCreatedTargetIds.contains(node.getTargetObjectId())) {
                    // Recreate the sub-component under the newly created parent view.
                    ViewCreationRequest movedSubComponentRequest = this.diagramMutationElementService.createView(subComponent, editingContext, diagramContext, parentViewCreationRequest, convertedNodes);
                    if (movedSubComponentRequest != null) {
                        String movedSubComponentNodeId = this.viewCreationRequestSubtreeService.getNodeIdFromViewCreationRequest(movedSubComponentRequest);
                        alreadyCreatedNodeIds.add(movedSubComponentNodeId);
                        // Recreate existing descendants under the new node id to preserve the visible subtree.
                        this.viewCreationRequestSubtreeService.addRecursiveViewCreationRequestsForSubtree(node, movedSubComponentNodeId, diagramContext, alreadyCreatedNodeIds);
                        // Remove the old root-level view now that its replacement subtree is queued.
                        diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(node.getId()).build());
                    }
                    alreadyCreatedTargetIds.add(node.getTargetObjectId());
                } else {
                    // If the sub-component is already being created elsewhere in this action, just delete old root view.
                    diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(node.getId()).build());
                }
            }
        }
    }

    private boolean isContainerRepresentedInDiagram(Element droppedElement, DiagramContext diagramContext) {
        return this.getContainerComponent(droppedElement)
                .map(this.identityService::getId)
                .map(containerId -> {
                    Set<String> removedNodeIds = diagramContext.viewDeletionRequests().stream()
                            .map(ViewDeletionRequest::getElementId)
                            .collect(Collectors.toSet());
                    var nodeFinder = new NodeFinder(diagramContext.diagram());
                    boolean representedInNodes = nodeFinder.getAllNodesMatching(candidate -> !removedNodeIds.contains(candidate.getId())).stream()
                            .map(Node::getTargetObjectId)
                            .anyMatch(containerId::equals);
                    boolean representedInCreationRequests = diagramContext.viewCreationRequests().stream()
                            .map(ViewCreationRequest::getTargetObjectId)
                            .anyMatch(containerId::equals);
                    return representedInNodes || representedInCreationRequests;
                })
                .orElse(false);
    }

    private Optional<PartUsage> getContainerComponent(Element droppedElement) {
        Optional<PartUsage> containerComponent = Optional.empty();
        if (droppedElement instanceof PartUsage partUsage) {
            if (partUsage.getOwningUsage() instanceof PartUsage owningContainer && this.transverseQueryService.isComponent(owningContainer)) {
                containerComponent = Optional.of(owningContainer);
            } else if (partUsage.getOwner() instanceof PartUsage ownerContainer) {
                containerComponent = Optional.of(ownerContainer).filter(this.transverseQueryService::isComponent);
            }
        }
        return containerComponent;
    }

    private boolean isDroppedInsideContainer(Element droppedElement, Object selectedNode) {
        String selectedTargetId = null;
        if (selectedNode instanceof Node node) {
            selectedTargetId = node.getTargetObjectId();
        } else if (selectedNode instanceof PartUsage partUsage) {
            selectedTargetId = this.identityService.getId(partUsage);
        } else if (selectedNode instanceof ViewCreationRequest viewCreationRequest) {
            selectedTargetId = viewCreationRequest.getTargetObjectId();
        } else if (selectedNode instanceof String targetId) {
            selectedTargetId = targetId;
        }
        String finalSelectedTargetId = selectedTargetId;
        return this.getContainerComponent(droppedElement)
                .map(this.identityService::getId)
                .filter(containerId -> Objects.equals(containerId, finalSelectedTargetId))
                .isPresent();
    }

}

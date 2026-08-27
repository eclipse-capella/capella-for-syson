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
package org.eclipse.capella.model.services.system.analysis;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.components.NodeIdProvider;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.stereotype.Service;

/**
 * Services related to SAB drop tools.
 *
 * @author mbats
 */
@Service
public class SARepresentationDropServices {

    private final SAQueryService saQueryService;

    private final DiagramMutationElementService diagramMutationElementService;

    private final DiagramMutationExposeService diagramMutationExposeService;

    private final TransverseQueryService transverseQueryService;

    private final SAMutationService saMutationService;

    private final ISysMLMoveElementService moveService;

    public SARepresentationDropServices(ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService,
            DiagramMutationExposeService diagramMutationExposeService) {
        this.saQueryService = new SAQueryService();
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.diagramMutationExposeService = Objects.requireNonNull(diagramMutationExposeService);
        this.transverseQueryService = new TransverseQueryService();
        this.saMutationService = new SAMutationService();
        this.moveService = moveService;
    }

    public Element dropIntoDiagramFromExplorer(Element droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (droppedElement instanceof PartUsage partUsage && this.isDroppableComponent(partUsage)) {
            this.dropComponent(partUsage, selectedNode, editingContext, diagramContext, convertedNodes);
        } else if (droppedElement instanceof FlowUsage flowUsage) {
            this.dropFunctionalExchange(flowUsage, editingContext, diagramContext, selectedNode, convertedNodes);
        } else if (droppedElement instanceof RequirementUsage) {
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, selectedNode, convertedNodes);
        } else if (droppedElement instanceof ActionUsage actionUsage && this.transverseQueryService.isFunctionalChain(actionUsage)) {
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, selectedNode, convertedNodes);
        } else if (droppedElement instanceof ActionUsage actionUsage) {
            this.dropFunction(actionUsage, editingContext, diagramContext, convertedNodes);
        }
        return droppedElement;
    }

    public Element dropIntoComponentFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext,
            DiagramContext diagramContext, Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.moveService != null && droppedElement instanceof PartUsage droppedComponent && targetElement instanceof PartUsage targetComponent
                && this.canReparentComponent(droppedComponent, targetComponent)) {
            this.moveService.moveSemanticElement(droppedElement, targetElement);
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
        } else if (droppedElement instanceof ActionUsage droppedFunction && targetElement instanceof PartUsage targetComponent && this.canAllocateFunction(droppedFunction, targetComponent)) {
            this.saMutationService.moveFunctionToComponent(droppedFunction, targetComponent);
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
        } else if (this.moveService != null && droppedElement instanceof ActionUsage droppedFunction && targetElement instanceof ActionUsage targetFunction
                && this.canReparentFunction(droppedFunction, targetFunction)) {
            this.moveService.moveSemanticElement(droppedFunction, targetFunction);
            this.transverseQueryService.getAllocatingComponent(targetFunction)
                    .ifPresent(component -> this.saMutationService.moveFunctionToComponent(droppedFunction, component));
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
        }
        return droppedElement;
    }

    private boolean canReparentComponent(PartUsage droppedComponent, PartUsage targetComponent) {
        return this.isReparentableComponent(droppedComponent)
                && this.isValidComponentContainer(droppedComponent, targetComponent)
                && !this.isSameOrDescendant(targetComponent, droppedComponent);
    }

    private boolean isReparentableComponent(PartUsage droppedComponent) {
        return this.saQueryService.isSystemComponent(droppedComponent) || this.transverseQueryService.isComponentActor(droppedComponent);
    }

    private boolean isValidComponentContainer(PartUsage droppedComponent, PartUsage targetComponent) {
        boolean validContainer = this.saQueryService.isSystemOfInterest(targetComponent) || this.saQueryService.isSystemComponent(targetComponent);
        if (this.transverseQueryService.isComponentActor(droppedComponent)) {
            validContainer = validContainer || this.transverseQueryService.isComponentActor(targetComponent);
        }
        return validContainer;
    }

    private boolean canAllocateFunction(ActionUsage droppedFunction, PartUsage targetComponent) {
        return !this.transverseQueryService.isFunctionalChain(droppedFunction)
                && this.isFunctionAllocationTarget(targetComponent);
    }

    private boolean isFunctionAllocationTarget(PartUsage targetComponent) {
        return this.saQueryService.isSystemOfInterest(targetComponent)
                || this.saQueryService.isSystemComponent(targetComponent)
                || this.transverseQueryService.isComponentActor(targetComponent);
    }

    private boolean canReparentFunction(ActionUsage droppedFunction, ActionUsage targetFunction) {
        return !this.transverseQueryService.isFunctionalChain(droppedFunction) && !this.transverseQueryService.isFunctionalChain(targetFunction)
                && !this.isSameOrDescendant(targetFunction, droppedFunction);
    }

    private boolean isSameOrDescendant(Element candidate, Element ancestor) {
        boolean result = candidate == ancestor;
        var current = candidate;
        while (!result && current.getOwner() != null) {
            Element owner = current.getOwner();
            result = owner == ancestor;
            current = owner;
        }
        return result;
    }

    private boolean isDroppableComponent(PartUsage partUsage) {
        return this.transverseQueryService.isComponent(partUsage)
                && (this.transverseQueryService.isComponentActor(partUsage)
                        || this.saQueryService.isSystemComponent(partUsage));
    }

    private void dropComponent(PartUsage partUsage, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.saQueryService.isSystemComponent(partUsage)) {
            this.revealComponentInSemanticParent(partUsage, selectedNode, editingContext, diagramContext, convertedNodes);
        } else {
            this.revealNode(partUsage, selectedNode, editingContext, diagramContext, convertedNodes);
        }
    }

    private Optional<Object> revealComponentInSemanticParent(PartUsage component, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var parentView = Optional.empty();
        if (component.getOwner() instanceof PartUsage parentComponent) {
            parentView = this.findView(diagramContext, parentComponent.getElementId());
            if (parentView.isEmpty() && this.saQueryService.isSystemComponent(parentComponent)) {
                parentView = this.revealComponentInSemanticParent(parentComponent, selectedNode, editingContext, diagramContext, convertedNodes);
            }
            parentView = parentView.or(() -> Optional.ofNullable(selectedNode));
        }
        return parentView.flatMap(parent -> this.revealNode(component, parent, editingContext, diagramContext, convertedNodes));
    }

    private void dropFunction(ActionUsage actionUsage, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.transverseQueryService.getAllocatingComponent(actionUsage)
                .flatMap(component -> this.revealAllocatedComponent(component, diagramContext.getDiagram(), editingContext, diagramContext, convertedNodes))
                .ifPresent(componentNode -> this.diagramMutationElementService.createView(actionUsage, editingContext, diagramContext, componentNode, convertedNodes));
    }

    private void dropFunctionalExchange(FlowUsage flowUsage, IEditingContext editingContext, DiagramContext diagramContext, Object selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var sourcePort = this.transverseQueryService.getFunctionalExchangeSource(flowUsage);
        var targetPort = this.transverseQueryService.getFunctionalExchangeTarget(flowUsage);
        var sourceFunction = Optional.ofNullable(sourcePort)
                .map(Element::getOwner)
                .filter(this.transverseQueryService::isFunction)
                .map(ActionUsage.class::cast);
        var targetFunction = Optional.ofNullable(targetPort)
                .map(Element::getOwner)
                .filter(this.transverseQueryService::isFunction)
                .map(ActionUsage.class::cast);
        var sourceComponent = sourceFunction.flatMap(this.transverseQueryService::getAllocatingComponent);
        var targetComponent = targetFunction.flatMap(this.transverseQueryService::getAllocatingComponent);
        if (this.hasAllocatedEndpoints(sourceFunction, targetFunction, sourceComponent, targetComponent)
                && this.canRevealBorderNode(sourcePort, sourceFunction, editingContext, diagramContext, convertedNodes)
                && this.canRevealBorderNode(targetPort, targetFunction, editingContext, diagramContext, convertedNodes)) {
            var sourceFunctionContainer = this.revealAllocatedComponent(sourceComponent.get(), selectedNode, editingContext, diagramContext, convertedNodes);
            var targetFunctionContainer = this.revealAllocatedComponent(targetComponent.get(), selectedNode, editingContext, diagramContext, convertedNodes);
            var sourceFunctionView = sourceFunctionContainer.flatMap(parent -> this.revealNode(sourceFunction.get(), parent, editingContext, diagramContext, convertedNodes));
            var targetFunctionView = targetFunctionContainer.flatMap(parent -> this.revealNode(targetFunction.get(), parent, editingContext, diagramContext, convertedNodes));
            var sourcePortView = sourceFunctionView.flatMap(parent -> this.revealBorderNode(sourcePort, parent, editingContext, diagramContext, convertedNodes));
            var targetPortView = targetFunctionView.flatMap(parent -> this.revealBorderNode(targetPort, parent, editingContext, diagramContext, convertedNodes));
            int creationRequestCount = diagramContext.viewCreationRequests().size();
            if (sourcePortView.isPresent() && targetPortView.isPresent()) {
                Object exposedEdge = this.expose(flowUsage, editingContext, diagramContext, selectedNode, convertedNodes);
                if (exposedEdge == null) {
                    this.rollbackCreatedViews(diagramContext, creationRequestCount);
                }
            } else {
                this.rollbackCreatedViews(diagramContext, creationRequestCount);
            }
        }
    }

    private boolean hasAllocatedEndpoints(Optional<ActionUsage> sourceFunction, Optional<ActionUsage> targetFunction, Optional<PartUsage> sourceComponent,
            Optional<PartUsage> targetComponent) {
        return sourceFunction.isPresent() && targetFunction.isPresent()
                && sourceComponent.isPresent() && targetComponent.isPresent();
    }

    private void rollbackCreatedViews(DiagramContext diagramContext, int creationRequestCount) {
        while (diagramContext.viewCreationRequests().size() > creationRequestCount) {
            diagramContext.viewCreationRequests().remove(diagramContext.viewCreationRequests().size() - 1);
        }
    }

    private Optional<Object> revealAllocatedComponent(PartUsage component, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var componentView = Optional.empty();
        if (this.saQueryService.isSystemComponent(component)) {
            componentView = this.revealComponentInSemanticParent(component, selectedNode, editingContext, diagramContext, convertedNodes);
        } else {
            componentView = this.revealNode(component, selectedNode, editingContext, diagramContext, convertedNodes);
        }
        return componentView;
    }

    private boolean canRevealBorderNode(Element element, Object parent, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        return this.findView(diagramContext, element.getElementId()).isPresent()
                || this.diagramMutationElementService.getBorderNodeDescriptionIdForRendering(element, editingContext, diagramContext, parent, convertedNodes).isPresent();
    }

    private Optional<Object> revealNode(Element element, Object parent, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var existingView = this.findView(diagramContext, element.getElementId());
        if (existingView.isPresent()) {
            return existingView;
        }
        return Optional.ofNullable(this.diagramMutationElementService.createView(element, editingContext, diagramContext, parent, convertedNodes));
    }

    private Optional<Object> revealBorderNode(Element element, Object parent, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var existingView = this.findView(diagramContext, element.getElementId());
        if (existingView.isPresent()) {
            return existingView;
        }
        var descriptionId = this.diagramMutationElementService.getBorderNodeDescriptionIdForRendering(element, editingContext, diagramContext, parent, convertedNodes);
        return descriptionId.map(id -> this.diagramMutationElementService.createView(element, this.getParentElementId(parent, diagramContext), id, editingContext, diagramContext,
                NodeContainmentKind.BORDER_NODE));
    }

    private Optional<Object> findView(DiagramContext diagramContext, String targetObjectId) {
        return new NodeFinder(diagramContext.getDiagram()).getOneNodeMatching(node -> targetObjectId.equals(node.getTargetObjectId()))
                .<Object>map(node -> node)
                .or(() -> diagramContext.viewCreationRequests().stream()
                        .filter(viewCreationRequest -> targetObjectId.equals(viewCreationRequest.getTargetObjectId()))
                        .findFirst());
    }

    private String getParentElementId(Object parent, DiagramContext diagramContext) {
        String parentElementId = diagramContext.getDiagram().getId();
        if (parent instanceof Node node) {
            parentElementId = node.getId();
        } else if (parent instanceof ViewCreationRequest viewCreationRequest) {
            parentElementId = new NodeIdProvider().getNodeId(viewCreationRequest.getParentElementId(), viewCreationRequest.getDescriptionId(),
                    NodeContainmentKind.CHILD_NODE, viewCreationRequest.getTargetObjectId());
        }
        return parentElementId;
    }

    private Element expose(Element element, IEditingContext editingContext, DiagramContext diagramContext, Object selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        Node targetNode = null;
        if (selectedNode instanceof Node node) {
            targetNode = node;
        }
        return this.diagramMutationExposeService.expose(element, editingContext, diagramContext, targetNode, convertedNodes);
    }
}

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
package org.eclipse.capella.model.services.logical.architecture;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Services related to the drop tools.
 *
 * @author fbarbin
 */
public class LARepresentationDropServices {

    private final LAQueryService laQueryService;

    private final LAMutationService laMutationService;

    private final DiagramMutationElementService diagramMutationElementService;

    private final IObjectSearchService objectSearchService;

    private final ISysMLMoveElementService moveService;

    private final TransverseQueryService transverseQueryService;

    public LARepresentationDropServices(IObjectSearchService objectSearchService, ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService) {
        this.laQueryService = new LAQueryService();
        this.laMutationService = new LAMutationService();
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.moveService = Objects.requireNonNull(moveService);
        this.transverseQueryService = new TransverseQueryService();
    }

    public Element dropIntoComponentFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.transverseQueryService.isComponent(targetElement)) {
            if (this.laQueryService.isFunction(droppedElement)) {
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
            Package componentsPackage = this.laQueryService.toComponentsPackage(droppedElement);
            this.moveService.moveSemanticElement(droppedElement, componentsPackage);
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode,
                    convertedNodes);
            diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
        }
        return droppedElement;
    }

    public Element dropIntoFunctionFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.laQueryService.isFunction(targetElement)) {
            if (this.laQueryService.isFunction(droppedElement)) {
                this.droppedFunctionIntoFunctionCase(droppedElement, droppedNode, targetElement, targetNode, editingContext, diagramContext, convertedNodes);
            }
        }
        return droppedElement;
    }

    public Element dropIntoDiagramFromExplorer(Element droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, selectedNode, convertedNodes);
        return droppedElement;

    }

    private void droppedFunctionIntoFunctionCase(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.moveService.moveSemanticElement(droppedElement, targetElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode,
                convertedNodes);
        this.getPreviousParentContainer(droppedElement, droppedNode, editingContext, diagramContext)
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
        this.laMutationService.setPerformedActionUsage((PartUsage) targetElement, (ActionUsage) droppedElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
        // A Function dropped into a container needs to be moved in the functions package.
        Package functionsPackage = this.laQueryService.toFunctionsPackage(targetElement);
        this.moveService.moveSemanticElement(droppedElement, functionsPackage);
        this.getPreviousParentContainer(droppedElement, droppedNode, editingContext, diagramContext)
                .ifPresent(previousContainer -> this.handlePreviousFunctionContainer(previousContainer, droppedElement));

    }

    private Optional<Object> getPreviousParentContainer(Element droppedElement, Node droppedNode, IEditingContext editingContext, DiagramContext diagramContext) {
        var parent = new NodeFinder(diagramContext.diagram()).getParent(droppedNode);
        if (parent instanceof Node parentNode) {
            return this.objectSearchService.getObject(editingContext, parentNode.getTargetObjectId());
        }
        return Optional.empty();
    }

    private void handlePreviousFunctionContainer(Object formerContainer, Element droppedElement) {
        if (formerContainer instanceof PartUsage partUsage) {
            if (this.transverseQueryService.isComponent(partUsage)) {
                this.laMutationService.deletePerformedActionUsage(partUsage, (ActionUsage) droppedElement);
            }
        }
    }

}

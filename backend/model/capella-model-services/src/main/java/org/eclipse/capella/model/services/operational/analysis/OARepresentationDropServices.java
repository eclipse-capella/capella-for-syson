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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;

/**
 * Services related to the drop tools.
 *
 * @author fbarbin
 */
public class OARepresentationDropServices {

    private final DiagramMutationElementService diagramMutationElementService;

    private final ISysMLMoveElementService moveService;

    private final TransverseQueryService transverseQueryService;

    private final IIdentityService identityService;

    public OARepresentationDropServices(IIdentityService identityService, ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService) {
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.moveService = Objects.requireNonNull(moveService);
        this.transverseQueryService = new TransverseQueryService();
        this.identityService = Objects.requireNonNull(identityService);
    }

    public Element dropIntoComponentFromDiagram(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.transverseQueryService.isComponent(targetElement)) {
            if (this.transverseQueryService.isComponent(droppedElement)) {
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

    public Element dropIntoDiagramFromExplorer(Element droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var parentView = this.getDisplayedParentView(droppedElement, diagramContext).<Object>map(node -> node).orElse(selectedNode);
        ViewCreationRequest droppedView = this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, parentView, convertedNodes);
        if (droppedElement instanceof PartUsage component && droppedView != null) {
            this.moveDisplayedChildrenUnderParent(component, droppedView, editingContext, diagramContext, convertedNodes);
        }
        return droppedElement;
    }

    private void droppedComponentIntoComponentCase(Element droppedElement, Node droppedNode, Element targetElement, Node targetNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.moveService.moveSemanticElement(droppedElement, targetElement);
        this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, targetNode, convertedNodes);
        diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(droppedNode.getId()).build());
    }

    private Optional<Node> getDisplayedParentView(Element droppedElement, DiagramContext diagramContext) {
        return this.transverseQueryService.getParentComponent(droppedElement)
                .flatMap(parentComponent -> new NodeFinder(diagramContext.diagram())
                        .getOneNodeMatching(node -> this.identityService.getId(parentComponent).equals(node.getTargetObjectId())));
    }

    private void moveDisplayedChildrenUnderParent(PartUsage parentComponent, ViewCreationRequest parentView, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var nodeFinder = new NodeFinder(diagramContext.diagram());
        for (PartUsage childComponent : this.transverseQueryService.getSubComponents(parentComponent)) {
            nodeFinder.getOneNodeMatching(node -> this.identityService.getId(childComponent).equals(node.getTargetObjectId()))
                    .filter(node -> !(nodeFinder.getParent(node) instanceof Node))
                    .ifPresent(node -> {
                        this.diagramMutationElementService.createView(childComponent, editingContext, diagramContext, parentView, convertedNodes);
                        diagramContext.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(node.getId()).build());
                    });
        }
    }
}

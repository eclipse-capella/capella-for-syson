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
package org.eclipse.capella.diagram.mcb.view.services;

import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import java.util.HashSet;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonViewCreationRequestSubtreeService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.syson.util.NodeFinder;
import org.eclipse.syson.services.api.ISysMLMoveElementService;

/**
 * Handles explorer reveals and Actor containment changes in MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class MCBRepresentationDropService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final DiagramMutationElementService diagramMutationElementService;

    private final ISysMLMoveElementService moveService;

    private final CommonViewCreationRequestSubtreeService subtreeService = new CommonViewCreationRequestSubtreeService();

    public MCBRepresentationDropService(DiagramMutationElementService diagramMutationElementService, ISysMLMoveElementService moveService) {
        this.moveService = Objects.requireNonNull(moveService);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
    }

    public Element dropIntoDiagramFromExplorer(Element element, IEditingContext editingContext, DiagramContext context,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (!this.commonQueryService.isInPerspective(element, ArcadiaEngineeringPerspective.SystemAnalysis)) {
            return element;
        }
        if (this.commonQueryService.isComponentActor(element)) {
            // Explorer drops only reveal the Actor in its closest displayed parent (or diagram root).
            var ancestor = this.commonQueryService.getParentComponent(element);
            var parent = Optional.empty();
            while (ancestor.isPresent() && parent.isEmpty()) {
                String ancestorId = ancestor.get().getElementId();
                parent = new NodeFinder(context.diagram())
                        .getOneNodeMatching(node -> ancestorId.equals(node.getTargetObjectId()))
                        .map(Object.class::cast);
                ancestor = this.commonQueryService.getParentComponent(ancestor.get());
            }
            this.diagramMutationElementService.createView(element, editingContext, context, parent.orElse(context.diagram()), convertedNodes);
        } else {
            this.diagramMutationElementService.createView(element, editingContext, context, context.diagram(), convertedNodes);
        }
        return element;
    }

    public Element dropIntoDiagramFromDiagram(Element element, Node sourceNode, IEditingContext editingContext, DiagramContext context,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.commonQueryService.isComponentActor(element)) {
            this.commonQueryService.getStructurePackage(element)
                    .ifPresent(parent -> this.moveActor(element, sourceNode, parent, context.diagram(), editingContext, context, convertedNodes));
        }
        return element;
    }

    public Element dropIntoActorFromDiagram(Element element, Node sourceNode, Element target, Node targetNode, IEditingContext editingContext, DiagramContext context,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        if (this.canMoveActor(element, target)) {
            this.moveActor(element, sourceNode, target, targetNode, editingContext, context, convertedNodes);
        }
        return element;
    }

    private boolean canMoveActor(Element element, Element target) {
        boolean areActors = this.commonQueryService.isComponentActor(element) && this.commonQueryService.isComponentActor(target)
                && !this.commonQueryService.isComponentHumanActor(target);
        return areActors && !EcoreUtil.isAncestor(element, target);
    }

    private void moveActor(Element element, Node sourceNode, Element target, Object targetView, IEditingContext editingContext, DiagramContext context,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        this.moveService.moveSemanticElement(element, target);
        var view = this.diagramMutationElementService.createView(element, editingContext, context, targetView, convertedNodes);
        if (view != null) {
            // createView returns a request; compute the future Node id to avoid deleting it when the drop keeps the same graphical parent.
            var newViewId = this.subtreeService.getNodeIdFromViewCreationRequest(view);
            if (!sourceNode.getId().equals(newViewId)) {
                // Replay displayed descendants under the new view; hidden descendants remain hidden.
                var createdNodeIds = new HashSet<>(new NodeFinder(context.diagram()).getAllNodesMatching(node -> true).stream().map(Node::getId).toList());
                context.viewCreationRequests().stream().map(this.subtreeService::getNodeIdFromViewCreationRequest).forEach(createdNodeIds::add);
                this.subtreeService.addRecursiveViewCreationRequestsForSubtree(sourceNode, newViewId, context, createdNodeIds);
                context.viewDeletionRequests().add(ViewDeletionRequest.newViewDeletionRequest().elementId(sourceNode.getId()).build());
            }
        }
    }
}

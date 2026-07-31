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
package org.eclipse.capella.model.services.transverse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;

/**
 * Transverse mutation service.
 * This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class TransverseRepresentationQueryService {

    private final TransverseQueryService transverseQueryService;

    private final IObjectSearchService objectSearchService;

    public TransverseRepresentationQueryService(IObjectSearchService objectSearchService) {
        this.transverseQueryService = new TransverseQueryService();
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    public List<ActionUsage> getFunctionalChainInDiagram(DiagramContext diagramContext, IEditingContext editingContext) {

        // We ignore nodes currently removed.
        List<String> removedNodeIds = diagramContext.viewDeletionRequests().stream().map(ViewDeletionRequest::getElementId).toList();
        List<ActionUsage> allFunctionalChainInDiagram = new ArrayList<>();

        allFunctionalChainInDiagram.addAll(diagramContext.diagram().getNodes().stream()
                .filter(node -> !removedNodeIds.contains(node.getId()))
                .map(Node::getTargetObjectId)
                .map(targetObjectId -> this.targetObjectIdToFunctionalChain(targetObjectId, editingContext))
                .<ActionUsage> mapMulti(Optional::ifPresent)
                .toList());

        // We add the currently dragged Functional Chain

        diagramContext.viewCreationRequests().stream()
                .map(ViewCreationRequest::getTargetObjectId)
                .map(targetObjectId -> this.targetObjectIdToFunctionalChain(targetObjectId, editingContext))
                .<ActionUsage> mapMulti(Optional::ifPresent)
                .forEach(allFunctionalChainInDiagram::add);

        return allFunctionalChainInDiagram;
    }

    private Optional<ActionUsage> targetObjectIdToFunctionalChain(String targetObjectId, IEditingContext editingContext) {
        return Optional.ofNullable(targetObjectId)
                .flatMap(objectId -> this.objectSearchService.getObject(editingContext, objectId))
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.transverseQueryService::isFunctionalChain);

    }

    /**
     * Get the index of the functional chain, in the diagram, in which the Functional Exchange is implied in.
     *
     * @param functionalExchange
     *         the functional exchange
     * @param editingContext
     *         the editing context.
     * @param diagramContext
     *         the diagram context.
     * @return -1 if not implied in a functionalChain, the index starting from 0 or 99 if implied in several functional chain.
     */
    public int getImpliedInFunctionalChainIndex(FlowUsage functionalExchange, IEditingContext editingContext, DiagramContext diagramContext) {
        List<ActionUsage> functionalChainsInDiagram = this.getFunctionalChainInDiagram(diagramContext, editingContext);
        List<ActionUsage> functionalChainsImpliedIn = this.transverseQueryService.getFunctionalChainsImpliedIn(functionalExchange);
        return this.computeIndex(functionalChainsInDiagram, functionalChainsImpliedIn);
    }

    /**
     * Get the index of the functional chain, in the diagram, in which the Function is implied in..
     *
     * @param actionUsage
     *         the function
     * @param editingContext
     *         the editing context.
     * @param diagramContext
     *         the diagram context.
     * @return -1 if not implied in a functionalChain, the index starting from 0 or 99 if implied in several functional chain.
     */
    public int getImpliedInFunctionalChainIndex(ActionUsage actionUsage, IEditingContext editingContext, DiagramContext diagramContext) {
        List<ActionUsage> functionalChainsInDiagram = this.getFunctionalChainInDiagram(diagramContext, editingContext);
        List<ActionUsage> functionalChainsImpliedIn = this.transverseQueryService.getFunctionalChainsImpliedIn(actionUsage);
        return this.computeIndex(functionalChainsInDiagram, functionalChainsImpliedIn);
    }

    public int getFunctionalChainIndexInDiagram(ActionUsage functionalChain, IEditingContext editingContext, DiagramContext diagramContext) {
        List<ActionUsage> functionalChainsInDiagram = this.getFunctionalChainInDiagram(diagramContext, editingContext);
        return functionalChainsInDiagram.indexOf(functionalChain);
    }

    private int computeIndex(List<ActionUsage> functionalChainsInDiagram, List<ActionUsage> functionalChainsImpliedIn) {
        int index = -1;
        List<ActionUsage> functionalChainsImpliedInDiagram = functionalChainsImpliedIn.stream()
                .filter(functionalChainsInDiagram::contains)
                .toList();
        if (functionalChainsImpliedInDiagram.size() > 1) {
            index = 99;
        } else if (functionalChainsImpliedInDiagram.size() == 1) {
            index = functionalChainsInDiagram.indexOf(functionalChainsImpliedInDiagram.get(0));
        }
        return index;
    }

}

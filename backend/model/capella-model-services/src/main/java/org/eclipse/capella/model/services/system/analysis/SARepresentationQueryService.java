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

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * System Analysis (SA) related representation query service.
 *
 * @author frouene
 */
public class SARepresentationQueryService {

    private static final int FUNCTIONAL_CHAIN_STYLE_COUNT = 3;

    private final IObjectSearchService objectSearchService;

    private final SAQueryService saQueryService;

    public SARepresentationQueryService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.saQueryService = new SAQueryService();
    }

    public List<ActionUsage> getFunctionalChainInDiagram(
            DiagramContext diagramContext,
            IEditingContext editingContext) {
        List<String> removedNodeIds = diagramContext.viewDeletionRequests().stream()
                .map(ViewDeletionRequest::getElementId)
                .toList();
        List<ActionUsage> functionalChains = new ArrayList<>();
        diagramContext.diagram().getNodes().stream()
                .filter(node -> !removedNodeIds.contains(node.getId()))
                .map(Node::getTargetObjectId)
                .map(targetObjectId -> this.targetObjectIdToFunctionalChain(targetObjectId, editingContext))
                .<ActionUsage>mapMulti(Optional::ifPresent)
                .forEach(functionalChains::add);
        diagramContext.viewCreationRequests().stream()
                .map(ViewCreationRequest::getTargetObjectId)
                .map(targetObjectId -> this.targetObjectIdToFunctionalChain(targetObjectId, editingContext))
                .<ActionUsage>mapMulti(Optional::ifPresent)
                .forEach(functionalChains::add);
        return functionalChains;
    }

    public int getFunctionalChainIndexInDiagram(ActionUsage functionalChain,
                                                IEditingContext editingContext,
                                                DiagramContext diagramContext) {
        int index = this.getFunctionalChainInDiagram(diagramContext, editingContext).indexOf(functionalChain);
        if (index >= 0) {
            index = index % FUNCTIONAL_CHAIN_STYLE_COUNT;
        }
        return index;
    }

    public int getImpliedInFunctionalChainIndex(FlowUsage functionalExchange,
                                                IEditingContext editingContext,
                                                DiagramContext diagramContext) {
        return this.computeStyleIndex(this.getFunctionalChainInDiagram(diagramContext, editingContext), this.saQueryService.getFunctionalChainsImpliedIn(functionalExchange));
    }

    public int getImpliedInFunctionalChainIndex(ActionUsage function,
                                                IEditingContext editingContext,
                                                DiagramContext diagramContext) {
        return this.computeStyleIndex(this.getFunctionalChainInDiagram(diagramContext, editingContext), this.saQueryService.getFunctionalChainsImpliedIn(function));
    }

    private Optional<ActionUsage> targetObjectIdToFunctionalChain(String targetObjectId,
                                                                  IEditingContext editingContext) {
        return Optional.ofNullable(targetObjectId)
                .flatMap(objectId -> this.objectSearchService.getObject(editingContext, objectId))
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.saQueryService::isFunctionalChain);
    }

    private int computeStyleIndex(List<ActionUsage> functionalChainsInDiagram,
                                  List<ActionUsage> functionalChainsImpliedIn) {
        List<ActionUsage> visibleMatches = functionalChainsImpliedIn.stream()
                .filter(functionalChainsInDiagram::contains)
                .toList();
        int index = -1;
        if (visibleMatches.size() > 1) {
            index = 99;
        } else if (visibleMatches.size() == 1) {
            index = functionalChainsInDiagram.indexOf(visibleMatches.get(0)) % FUNCTIONAL_CHAIN_STYLE_COUNT;
        }
        return index;
    }

}

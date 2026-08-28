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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.Package;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LARepresentationQueryService}.
 *
 * @author fbarbin
 */
public class LARepresentationQueryServiceTests {

    private final LATestModelFixture fixture = new LATestModelFixture();

    private final LADiagramTestFixture diagramFixture = new LADiagramTestFixture();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    @Test
    @DisplayName("GIVEN a diagram context, WHEN querying functional chains in diagram, THEN only represented and non-deleted chains are returned")
    public void testGetFunctionalChainInDiagram() {
        Package root = this.fixture.createRootPackage();
        ActionUsage chainInNode = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain In Node");
        ActionUsage chainInCreationRequest = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain In Creation Request");
        ActionUsage nonChainAction = this.fixture.createArcadiaTypedFunction(root, "Function");

        String chainInNodeId = "chain-in-node-id";
        String chainInCreationRequestId = "chain-in-creation-request-id";
        String nonChainActionId = "non-chain-action-id";

        IObjectSearchService objectSearchService = this.createObjectSearchService(Map.of(
                chainInNodeId, chainInNode,
                chainInCreationRequestId, chainInCreationRequest,
                nonChainActionId, nonChainAction));

        TransverseRepresentationQueryService transverseRepresentationQueryService = new TransverseRepresentationQueryService(objectSearchService);

        Node keptNode = this.diagramFixture.createNode("kept-node", chainInNodeId);
        Node deletedNode = this.diagramFixture.createNode("deleted-node", nonChainActionId);

        Diagram diagram = this.diagramFixture.createDiagram(List.of(keptNode, deletedNode));
        List<ViewCreationRequest> creationRequests = List.of(
                this.diagramFixture.createViewCreationRequest(chainInCreationRequestId),
                this.diagramFixture.createViewCreationRequest(nonChainActionId));
        List<ViewDeletionRequest> deletionRequests = List.of(this.diagramFixture.createViewDeletionRequest("deleted-node"));

        DiagramContext diagramContext = new DiagramContext(diagram, creationRequests, deletionRequests, List.of());

        assertThat(transverseRepresentationQueryService.getFunctionalChainInDiagram(diagramContext, new IEditingContext.NoOp()))
                .containsExactly(chainInNode, chainInCreationRequest);
    }

    @Test
    @DisplayName("GIVEN functional chains in diagram, WHEN querying implied and chain indexes, THEN deterministic values are returned")
    public void testFunctionalChainIndexes() {
        Package root = this.fixture.createRootPackage();

        ActionUsage functionA = this.fixture.createArcadiaTypedFunction(root, "Function A");
        ActionUsage functionB = this.fixture.createArcadiaTypedFunction(root, "Function B");
        ActionUsage functionC = this.fixture.createArcadiaTypedFunction(root, "Function C");

        FlowUsage flowAB = this.fixture.createArcadiaTypedFunctionalExchange(root, "Flow AB", functionA, functionB);
        FlowUsage flowBC = this.fixture.createArcadiaTypedFunctionalExchange(root, "Flow BC", functionB, functionC);

        ActionUsage chainA = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain A");
        ActionUsage chainB = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain B");

        this.fixture.setInvolvedFunctionalExchanges(chainA, flowAB, flowBC);
        this.fixture.setInvolvedFunctionalExchanges(chainB, flowAB);

        String chainAId = "chain-a-id";
        String chainBId = "chain-b-id";

        IObjectSearchService objectSearchService = this.createObjectSearchService(Map.of(chainAId, chainA, chainBId, chainB));
        TransverseRepresentationQueryService transverseRepresentationQueryService = new TransverseRepresentationQueryService(objectSearchService);

        Diagram diagram = this.diagramFixture.createDiagram(List.of(this.diagramFixture.createNode("chain-a-node", chainAId), this.diagramFixture.createNode("chain-b-node", chainBId)));
        DiagramContext diagramContext = new DiagramContext(diagram, List.of(), List.of(), List.of());

        IEditingContext editingContext = new IEditingContext.NoOp();

        assertThat(transverseRepresentationQueryService.getImpliedInFunctionalChainIndex(flowAB, editingContext, diagramContext)).isEqualTo(99);
        assertThat(transverseRepresentationQueryService.getImpliedInFunctionalChainIndex(flowBC, editingContext, diagramContext)).isEqualTo(-1);
        assertThat(transverseRepresentationQueryService.getImpliedInFunctionalChainIndex(functionB, editingContext, diagramContext)).isEqualTo(99);
        assertThat(transverseRepresentationQueryService.getImpliedInFunctionalChainIndex(functionC, editingContext, diagramContext)).isEqualTo(-1);
        assertThat(transverseRepresentationQueryService.getImpliedInFunctionalChainIndex(functionA, editingContext, diagramContext)).isEqualTo(99);
        assertThat(transverseRepresentationQueryService.getFunctionalChainIndexInDiagram(chainB, editingContext, diagramContext)).isEqualTo(1);

        FlowUsage unrelatedFlow = this.fixture.createArcadiaTypedFunctionalExchange(root, "Unrelated Flow", functionC, functionA);
        assertThat(transverseRepresentationQueryService.getImpliedInFunctionalChainIndex(unrelatedFlow, editingContext, diagramContext)).isEqualTo(-1);
    }

    private IObjectSearchService createObjectSearchService(Map<String, Object> objectsById) {
        return (editingContext, objectId) -> Optional.ofNullable(objectsById.get(objectId));
    }
}

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

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link LAViewCreationRequestSubtreeService}.
 *
 * @author fbarbin
 */
public class LAViewCreationRequestSubtreeServiceTests {

    private final LADiagramTestFixture diagramFixture = new LADiagramTestFixture();

    private final LAViewCreationRequestSubtreeService viewCreationRequestSubtreeService = new LAViewCreationRequestSubtreeService();

    @Test
    @DisplayName("GIVEN a moved component node with nested descendants, WHEN recreating subtree view requests, THEN child and border descendants are recreated recursively")
    public void testAddRecursiveViewCreationRequestsForSubtree() {
        Node nestedChildNode = this.createNode("nested-child-node-id", "nested-child-target-id", "nested-child-description-id", false, List.of(), List.of());
        Node childNode = this.createNode("child-node-id", "child-target-id", "child-description-id", false, List.of(nestedChildNode), List.of());
        Node borderNode = this.createNode("border-node-id", "border-target-id", "border-description-id", true, List.of(), List.of());
        Node sourceRootNode = this.createNode("source-root-node-id", "source-root-target-id", "source-root-description-id", false, List.of(childNode), List.of(borderNode));

        Diagram diagram = this.diagramFixture.createDiagram(List.of(sourceRootNode));
        DiagramContext diagramContext = new DiagramContext(diagram, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        String newParentNodeId = "new-parent-node-id";

        this.viewCreationRequestSubtreeService.addRecursiveViewCreationRequestsForSubtree(sourceRootNode, newParentNodeId, diagramContext, new HashSet<>());

        assertThat(diagramContext.viewCreationRequests()).hasSize(3);

        ViewCreationRequest childRequest = this.findRequest(diagramContext.viewCreationRequests(), "child-target-id");
        assertThat(childRequest.getParentElementId()).isEqualTo(newParentNodeId);
        assertThat(childRequest.getDescriptionId()).isEqualTo("child-description-id");
        assertThat(childRequest.getContainmentKind()).isEqualTo(NodeContainmentKind.CHILD_NODE);

        String movedChildNodeId = this.viewCreationRequestSubtreeService.getNodeIdFromViewCreationRequest(childRequest);
        ViewCreationRequest nestedChildRequest = this.findRequest(diagramContext.viewCreationRequests(), "nested-child-target-id");
        assertThat(nestedChildRequest.getParentElementId()).isEqualTo(movedChildNodeId);
        assertThat(nestedChildRequest.getDescriptionId()).isEqualTo("nested-child-description-id");
        assertThat(nestedChildRequest.getContainmentKind()).isEqualTo(NodeContainmentKind.CHILD_NODE);

        ViewCreationRequest borderRequest = this.findRequest(diagramContext.viewCreationRequests(), "border-target-id");
        assertThat(borderRequest.getParentElementId()).isEqualTo(newParentNodeId);
        assertThat(borderRequest.getDescriptionId()).isEqualTo("border-description-id");
        assertThat(borderRequest.getContainmentKind()).isEqualTo(NodeContainmentKind.BORDER_NODE);
    }

    @Test
    @DisplayName("GIVEN already-created node identifiers, WHEN recreating subtree view requests, THEN duplicate node requests are skipped")
    public void testAddRecursiveViewCreationRequestsForSubtreeWithDuplicateNodeId() {
        Node childNode = this.createNode("child-node-id", "child-target-id", "child-description-id", false, List.of(), List.of());
        Node sourceRootNode = this.createNode("source-root-node-id", "source-root-target-id", "source-root-description-id", false, List.of(childNode), List.of());

        Diagram diagram = this.diagramFixture.createDiagram(List.of(sourceRootNode));
        DiagramContext diagramContext = new DiagramContext(diagram, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        String newParentNodeId = "new-parent-node-id";

        ViewCreationRequest existingChildRequest = ViewCreationRequest.newViewCreationRequest()
                .parentElementId(newParentNodeId)
                .descriptionId("child-description-id")
                .targetObjectId("child-target-id")
                .containmentKind(NodeContainmentKind.CHILD_NODE)
                .build();
        Set<String> alreadyCreatedNodeIds = new HashSet<>(List.of(this.viewCreationRequestSubtreeService.getNodeIdFromViewCreationRequest(existingChildRequest)));

        this.viewCreationRequestSubtreeService.addRecursiveViewCreationRequestsForSubtree(sourceRootNode, newParentNodeId, diagramContext, alreadyCreatedNodeIds);

        assertThat(diagramContext.viewCreationRequests()).isEmpty();
    }

    private Node createNode(String nodeId, String targetObjectId, String descriptionId, boolean borderNode, List<Node> childNodes, List<Node> borderNodes) {
        Node seedNode = this.diagramFixture.createNode(nodeId, targetObjectId);
        return Node.newNode(seedNode)
                .descriptionId(descriptionId)
                .borderNode(borderNode)
                .childNodes(childNodes)
                .borderNodes(borderNodes)
                .build();
    }

    private ViewCreationRequest findRequest(List<ViewCreationRequest> requests, String targetObjectId) {
        return requests.stream()
                .filter(request -> targetObjectId.equals(request.getTargetObjectId()))
                .findFirst()
                .orElseThrow();
    }
}

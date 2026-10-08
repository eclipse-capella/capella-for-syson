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
package org.eclipse.capella.model.transverse.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CommonViewCreationRequestSubtreeService}.
 *
 * @author fbarbin
 */
public class CommonViewCreationRequestSubtreeServiceTests {

    private final CommonViewCreationRequestSubtreeService commonViewCreationRequestSubtreeService = new CommonViewCreationRequestSubtreeService();

    @Test
    @DisplayName("GIVEN a moved component node with nested descendants, WHEN recreating subtree view requests, THEN child and border descendants are recreated recursively")
    public void testAddRecursiveViewCreationRequestsForSubtree() {
        Node nestedChildNode = this.createNode("nested-child-node-id", "nested-child-target-id", "nested-child-description-id", false, List.of(), List.of());
        Node childNode = this.createNode("child-node-id", "child-target-id", "child-description-id", false, List.of(nestedChildNode), List.of());
        Node borderNode = this.createNode("border-node-id", "border-target-id", "border-description-id", true, List.of(), List.of());
        Node sourceRootNode = this.createNode("source-root-node-id", "source-root-target-id", "source-root-description-id", false, List.of(childNode), List.of(borderNode));

        Diagram diagram = this.createDiagram(List.of(sourceRootNode));
        DiagramContext diagramContext = new DiagramContext(diagram, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        String newParentNodeId = "new-parent-node-id";

        this.commonViewCreationRequestSubtreeService.addRecursiveViewCreationRequestsForSubtree(sourceRootNode, newParentNodeId, diagramContext, new HashSet<>());

        assertThat(diagramContext.viewCreationRequests()).hasSize(3);

        ViewCreationRequest childRequest = this.findRequest(diagramContext.viewCreationRequests(), "child-target-id");
        assertThat(childRequest.getParentElementId()).isEqualTo(newParentNodeId);
        assertThat(childRequest.getDescriptionId()).isEqualTo("child-description-id");
        assertThat(childRequest.getContainmentKind()).isEqualTo(NodeContainmentKind.CHILD_NODE);

        String movedChildNodeId = this.commonViewCreationRequestSubtreeService.getNodeIdFromViewCreationRequest(childRequest);
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

        Diagram diagram = this.createDiagram(List.of(sourceRootNode));
        DiagramContext diagramContext = new DiagramContext(diagram, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        String newParentNodeId = "new-parent-node-id";

        ViewCreationRequest existingChildRequest = ViewCreationRequest.newViewCreationRequest()
                .parentElementId(newParentNodeId)
                .descriptionId("child-description-id")
                .targetObjectId("child-target-id")
                .containmentKind(NodeContainmentKind.CHILD_NODE)
                .build();
        Set<String> alreadyCreatedNodeIds = new HashSet<>(List.of(this.commonViewCreationRequestSubtreeService.getNodeIdFromViewCreationRequest(existingChildRequest)));

        this.commonViewCreationRequestSubtreeService.addRecursiveViewCreationRequestsForSubtree(sourceRootNode, newParentNodeId, diagramContext, alreadyCreatedNodeIds);

        assertThat(diagramContext.viewCreationRequests()).isEmpty();
    }

    private Node createNode(String nodeId, String targetObjectId, String descriptionId, boolean borderNode, List<Node> childNodes, List<Node> borderNodes) {
        Node node = mock(Node.class);
        when(node.getId()).thenReturn(nodeId);
        when(node.getTargetObjectId()).thenReturn(targetObjectId);
        when(node.getDescriptionId()).thenReturn(descriptionId);
        when(node.isBorderNode()).thenReturn(borderNode);
        when(node.getChildNodes()).thenReturn(childNodes);
        when(node.getBorderNodes()).thenReturn(borderNodes);
        return node;
    }

    private Diagram createDiagram(List<Node> nodes) {
        return Diagram.newDiagram("diagram-id")
                .descriptionId("diagram-description-id")
                .targetObjectId("diagram-target-id")
                .nodes(nodes)
                .edges(List.of())
                .style(DiagramStyle.newDiagramStyle().build())
                .build();
    }

    private ViewCreationRequest findRequest(List<ViewCreationRequest> requests, String targetObjectId) {
        return requests.stream()
                .filter(request -> targetObjectId.equals(request.getTargetObjectId()))
                .findFirst()
                .orElseThrow();
    }
}

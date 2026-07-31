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

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.components.NodeIdProvider;

import java.util.List;
import java.util.Set;

/**
 * Services used to recreate nested view creation requests when a node is moved to a new parent.
 *
 * @author fbarbin
 */
public class LAViewCreationRequestSubtreeService {

    public String getNodeIdFromViewCreationRequest(ViewCreationRequest viewCreationRequest) {
        // Keep id generation aligned with diagram runtime so duplicate detection is reliable.
        return new NodeIdProvider().getNodeId(
                viewCreationRequest.getParentElementId(),
                viewCreationRequest.getDescriptionId(),
                viewCreationRequest.getContainmentKind(),
                viewCreationRequest.getTargetObjectId());
    }

    public void addRecursiveViewCreationRequestsForSubtree(Node sourceNode, String newParentElementId, DiagramContext diagramContext, Set<String> alreadyCreatedNodeIds) {
        // Replay child and border descendants exactly as currently visible under the source node.
        this.addViewCreationRequestsForNodes(sourceNode.getChildNodes(), NodeContainmentKind.CHILD_NODE, newParentElementId, diagramContext, alreadyCreatedNodeIds);
        this.addViewCreationRequestsForNodes(sourceNode.getBorderNodes(), NodeContainmentKind.BORDER_NODE, newParentElementId, diagramContext, alreadyCreatedNodeIds);
    }

    private void addViewCreationRequestsForNodes(List<Node> sourceNodes, NodeContainmentKind containmentKind, String parentElementId, DiagramContext diagramContext,
            Set<String> alreadyCreatedNodeIds) {
        for (Node sourceNode : sourceNodes) {
            ViewCreationRequest creationRequest = ViewCreationRequest.newViewCreationRequest()
                    .parentElementId(parentElementId)
                    .descriptionId(sourceNode.getDescriptionId())
                    .targetObjectId(sourceNode.getTargetObjectId())
                    .containmentKind(containmentKind)
                    .build();
            String createdNodeId = this.getNodeIdFromViewCreationRequest(creationRequest);
            if (alreadyCreatedNodeIds.add(createdNodeId)) {
                // Queue creation once per computed node id, then recurse into its descendants.
                diagramContext.viewCreationRequests().add(creationRequest);
                this.addRecursiveViewCreationRequestsForSubtree(sourceNode, createdNodeId, diagramContext, alreadyCreatedNodeIds);
            }
        }
    }
}

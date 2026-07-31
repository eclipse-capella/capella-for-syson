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

import java.util.List;
import java.util.Set;

import org.eclipse.sirius.components.diagrams.CollapsingState;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.FreeFormLayoutStrategy;
import org.eclipse.sirius.components.diagrams.LineStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.NodeType;
import org.eclipse.sirius.components.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.ViewModifier;
import org.eclipse.sirius.components.diagrams.components.BorderNodePosition;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;

/**
 * Shared diagram fixture for {@link LARepresentationQueryServiceTests}.
 *
 * @author fbarbin
 */
class LADiagramTestFixture {

    Diagram createDiagram(List<Node> nodes) {
        return Diagram.newDiagram("diagram-id")
                .descriptionId("diagram-description-id")
                .targetObjectId("diagram-target-id")
                .nodes(nodes)
                .edges(List.of())
                .style(DiagramStyle.newDiagramStyle().build())
                .build();
    }

    Node createNode(String nodeId, String targetObjectId) {
        return Node.newNode(nodeId)
                .type(NodeType.NODE_RECTANGLE)
                .targetObjectId(targetObjectId)
                .targetObjectKind("")
                .targetObjectLabel("")
                .descriptionId("node-description-id")
                .borderNode(false)
                .initialBorderNodePosition(BorderNodePosition.EAST)
                .modifiers(Set.of())
                .state(ViewModifier.Normal)
                .collapsingState(CollapsingState.EXPANDED)
                .outsideLabels(List.of())
                .style(RectangularNodeStyle.newRectangularNodeStyle()
                        .borderColor("#000000")
                        .borderSize(1)
                        .borderStyle(LineStyle.Solid)
                        .background("#FFFFFF")
                        .childrenLayoutStrategy(new FreeFormLayoutStrategy())
                        .build())
                .borderNodes(List.of())
                .childNodes(List.of())
                .labelEditable(false)
                .deletable(true)
                .pinned(false)
                .customizedStyleProperties(Set.of())
                .decorators(List.of())
                .build();
    }

    ViewCreationRequest createViewCreationRequest(String targetObjectId) {
        return ViewCreationRequest.newViewCreationRequest()
                .parentElementId("parent-element-id")
                .descriptionId("description-id")
                .targetObjectId(targetObjectId)
                .containmentKind(NodeContainmentKind.CHILD_NODE)
                .build();
    }

    ViewDeletionRequest createViewDeletionRequest(String nodeId) {
        return ViewDeletionRequest.newViewDeletionRequest()
                .elementId(nodeId)
                .build();
    }
}

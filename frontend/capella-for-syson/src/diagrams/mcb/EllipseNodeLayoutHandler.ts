/*******************************************************************************
 * Copyright (c) 2023, 2026 Obeo.
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
import {
  DiagramNodeType, ForcedDimensions, ILayoutEngine, INodeLayoutHandler, NodeData, RawDiagram, computePreviousSize,
} from '@eclipse-sirius/sirius-components-diagrams';
import { Node } from '@xyflow/react';

/** Layout for leaf MCB ellipses; their external labels are anchored above the shape. */
export class EllipseNodeLayoutHandler implements INodeLayoutHandler<NodeData> {
  canHandle(node: Node<NodeData, DiagramNodeType>) {
    return node.type === 'ellipseNode';
  }

  handle(_engine: ILayoutEngine, previousDiagram: RawDiagram | null, node: Node<NodeData>,
    _visibleNodes: Node<NodeData, DiagramNodeType>[], _children: Node<NodeData, DiagramNodeType>[],
    _newNodes: Node<NodeData, DiagramNodeType>[], forceDimensions?: ForcedDimensions) {
    const previous = previousDiagram?.nodes.find((candidate) => candidate.id === node.id);
    const size = computePreviousSize(previous, node);
    node.width = forceDimensions?.width ?? (node.data.resizedByUser ? size.width : node.data.defaultWidth ?? 180);
    node.height = forceDimensions?.height ?? (node.data.resizedByUser ? size.height : node.data.defaultHeight ?? 70);
    node.data.minComputedWidth = 20;
    node.data.minComputedHeight = 20;
    Object.values(node.data.outsideLabels).forEach((label) => {
      if (!label.movedByUser) {
        label.position = { x: 0, y: -node.height! - label.height - 4 };
      }
    });
  }
}

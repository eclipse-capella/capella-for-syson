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
package org.eclipse.capella.diagram.common.view.nodes;

import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;

/**
 * Provide Delete from diagram tool.
 *
 * @author frouene
 */
public class NodeDeleteFromDiagramToolProvider {

    private final ViewBuilders viewBuilderHelper = new ViewBuilders();

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();


    public NodeTool getDeleteFromDiagramTool() {
        var changeContext = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of3(DiagramMutationExposeService::removeFromExposedElements)
                        .aqlSelf(Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT));
        var deleteView = this.diagramBuilderHelper.newDeleteView()
                .viewExpression("aql:selectedNode")
                .children(changeContext.build());
        return this.diagramBuilderHelper.newNodeTool()
                .name("Delete from Diagram")
                .iconURLsExpression("/images/graphicalDelete.svg")
                .body(deleteView.build())
                .build();
    }

}

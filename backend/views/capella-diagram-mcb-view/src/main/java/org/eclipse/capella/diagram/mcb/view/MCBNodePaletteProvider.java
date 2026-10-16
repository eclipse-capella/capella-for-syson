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
package org.eclipse.capella.diagram.mcb.view;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Editing and deletion interactions shared by all MCB node concepts.
 *
 * @author tbezierslafosse
 */
public class MCBNodePaletteProvider {

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    private final ViewBuilders viewBuilders = new ViewBuilders();

    public NodePalette createNodePalette() {
        return this.diagramBuilders.newNodePalette()
                .deleteTool(this.diagramBuilders.newDeleteTool().name("Delete from Model")
                        .body(this.viewBuilders.newChangeContext().expression(ServiceMethod.of0(CommonDeletionService::delete).aqlSelf()).build()).build())
                .labelEditTool(this.diagramBuilders.newLabelEditTool().name("Edit")
                        .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Element>of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                        .body(this.viewBuilders.newChangeContext().expression(ServiceMethod.of1(DiagramMutationLabelService::directEditNode).aqlSelf("newLabel")).build()).build())
                .quickAccessTools(new NodeDeleteFromDiagramToolProvider().getDeleteFromDiagramTool())
                .build();
    }
}

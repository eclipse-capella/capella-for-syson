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
package org.eclipse.capella.diagram.oaib.view.edges.describes;

import java.util.Objects;

import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.EdgeReconnectionTool;
import org.eclipse.sirius.components.view.diagram.SourceEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.TargetEdgeEndReconnectionTool;
import org.eclipse.syson.diagram.common.view.DiagramDefaultToolsFactory;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the Describes edge palette for OAIB diagrams.
 *
 * @author tbezierslafosse
 */
public class DescribesPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    public DescribesPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
    }

    public EdgePalette createEdgePalette() {
        return this.diagramBuilderHelper.newEdgePalette()
                .deleteTool(this.diagramBuilderHelper.newDeleteTool().name("Delete from Model")
                        .body(this.viewBuilderHelper.newChangeContext().expression(ServiceMethod.of0(CommonDeletionService::delete).aqlSelf()).build()).build())
                .edgeReconnectionTools(this.createEdgeReconnectionTools())
                .toolSections(new DiagramDefaultToolsFactory().createDefaultHideRevealEdgeToolSection())
                .build();
    }

    private EdgeReconnectionTool[] createEdgeReconnectionTools() {
        SourceEdgeEndReconnectionTool sourceTool = this.diagramBuilderHelper.newSourceEdgeEndReconnectionTool()
                .name("DescribesSourceReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of2(TransverseRepresentationReconnectToolServices::reconnectDescribes)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET, "true"))
                        .build())
                .build();
        TargetEdgeEndReconnectionTool targetTool = this.diagramBuilderHelper.newTargetEdgeEndReconnectionTool()
                .name("DescribesTargetReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of2(TransverseRepresentationReconnectToolServices::reconnectDescribes)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET, "false"))
                        .build())
                .build();
        return new EdgeReconnectionTool[] { sourceTool, targetTool };
    }
}

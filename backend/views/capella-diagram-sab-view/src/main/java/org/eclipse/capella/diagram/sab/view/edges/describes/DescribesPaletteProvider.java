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
package org.eclipse.capella.diagram.sab.view.edges.describes;

import java.util.Objects;

import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.capella.model.services.system.analysis.SARepresentationReconnectToolServices;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.eclipse.sirius.components.view.diagram.EdgeReconnectionTool;
import org.eclipse.sirius.components.view.diagram.SourceEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.TargetEdgeEndReconnectionTool;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.util.AQLConstants;

/**
 * SAB describes edge palette provider.
 *
 * @author mbats
 */
public class DescribesPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final DefaultToolsFactory defaultToolsFactory;

    public DescribesPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.defaultToolsFactory = new DefaultToolsFactory();
    }

    public EdgePalette createEdgePalette() {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(DeleteService::deleteFromModel).aqlSelf())
                        .build());

        return this.diagramBuilderHelper.newEdgePalette()
                .deleteTool(deleteTool.build())
                .edgeReconnectionTools(this.createEdgeReconnectionTool())
                .toolSections(this.defaultToolsFactory.createDefaultHideRevealEdgeToolSection())
                .build();
    }

    private EdgeReconnectionTool[] createEdgeReconnectionTool() {
        SourceEdgeEndReconnectionTool sourceEdgeEndReconnectionTool = this.diagramBuilderHelper.newSourceEdgeEndReconnectionTool()
                .name("describesSourceReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of2(SARepresentationReconnectToolServices::reconnectDescribes)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET, "true"))
                        .build())
                .build();

        TargetEdgeEndReconnectionTool targetEdgeEndReconnectionTool = this.diagramBuilderHelper.newTargetEdgeEndReconnectionTool()
                .name("describesTargetReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of2(SARepresentationReconnectToolServices::reconnectDescribes)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET, "false"))
                        .build())
                .build();

        return new EdgeReconnectionTool[] { sourceEdgeEndReconnectionTool, targetEdgeEndReconnectionTool };
    }
}

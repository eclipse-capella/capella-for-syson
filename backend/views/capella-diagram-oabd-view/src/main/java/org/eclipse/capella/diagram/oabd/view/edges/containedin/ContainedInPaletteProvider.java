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
package org.eclipse.capella.diagram.oabd.view.edges.containedin;

import java.util.Objects;

import org.eclipse.capella.model.transverse.services.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.core.api.IEditingContext;
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
 * Provides the palette for Contained In edges.
 *
 * @author tbezierslafosse
 */
public class ContainedInPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final DiagramDefaultToolsFactory diagramDefaultToolsFactory;

    public ContainedInPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.diagramDefaultToolsFactory = new DiagramDefaultToolsFactory();
    }

    public EdgePalette createEdgePalette() {
        return this.diagramBuilderHelper.newEdgePalette()
                .edgeReconnectionTools(this.createEdgeReconnectionTools())
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealEdgeToolSection())
                .build();
    }

    private EdgeReconnectionTool[] createEdgeReconnectionTools() {
        SourceEdgeEndReconnectionTool sourceTool = this.diagramBuilderHelper.newSourceEdgeEndReconnectionTool()
                .name("ContainedInSourceReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of3(TransverseRepresentationReconnectToolServices::reconnectContainedIn)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET, AQLConstants.DIAGRAM, IEditingContext.EDITING_CONTEXT))
                        .build())
                .build();
        TargetEdgeEndReconnectionTool targetTool = this.diagramBuilderHelper.newTargetEdgeEndReconnectionTool()
                .name("ContainedInTargetReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(TransverseRepresentationReconnectToolServices::reconnectContainedInTarget)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET))
                        .build())
                .build();
        return new EdgeReconnectionTool[] { sourceTool, targetTool };
    }
}

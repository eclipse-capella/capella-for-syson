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
package org.eclipse.capella.diagram.ocb.view.edges.generalization;

import java.util.Objects;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.EdgeReconnectionTool;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the palette tools of Operational Capability generalization edges.
 *
 * @author tbezierslafosse
 */
public class GeneralizationPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final DefaultToolsFactory defaultToolsFactory = new DefaultToolsFactory();

    public GeneralizationPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
    }

    public EdgePalette createEdgePalette() {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf())
                        .build());

        return this.diagramBuilderHelper.newEdgePalette()
                .deleteTool(deleteTool.build())
                .edgeReconnectionTools(this.createEdgeReconnectionTools())
                .toolSections(this.defaultToolsFactory.createDefaultHideRevealEdgeToolSection())
                .build();
    }

    private EdgeReconnectionTool[] createEdgeReconnectionTools() {
        var sourceTool = this.diagramBuilderHelper.newSourceEdgeEndReconnectionTool()
                .name("Reconnect Source")
                .body(this.createReconnectToolBody().build())
                .build();

        var targetTool = this.diagramBuilderHelper.newTargetEdgeEndReconnectionTool()
                .name("Reconnect Target")
                .body(this.createReconnectToolBody().build())
                .build();
        return new EdgeReconnectionTool[] { sourceTool, targetTool };
    }

    private ChangeContextBuilder createReconnectToolBody() {
        return this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of2(TransverseRepresentationReconnectToolServices::reconnectGeneralization)
                        .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET,
                                AQLConstants.SEMANTIC_RECONNECTION_SOURCE));
    }
}

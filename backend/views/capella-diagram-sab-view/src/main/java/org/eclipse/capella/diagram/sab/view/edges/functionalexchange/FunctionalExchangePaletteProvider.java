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
package org.eclipse.capella.diagram.sab.view.edges.functionalexchange;

import java.util.Objects;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.EdgeReconnectionTool;
import org.eclipse.sirius.components.view.diagram.SourceEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.TargetEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Palette provider for SAB functional exchange edges.
 *
 * @author mbats
 */
public class FunctionalExchangePaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final DefaultToolsFactory defaultToolsFactory;

    public FunctionalExchangePaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.defaultToolsFactory = new DefaultToolsFactory();
    }

    public EdgePalette createEdgePalette() {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf())
                        .build());
        var labelEditTool = this.diagramBuilderHelper.newLabelEditTool()
                .name("Edit")
                .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Element>of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.<DiagramMutationLabelService, Element, String>of1(DiagramMutationLabelService::editEdgeCenterLabel).aqlSelf("newLabel"))
                        .build());

        return this.diagramBuilderHelper.newEdgePalette()
                .deleteTool(deleteTool.build())
                .centerLabelEditTool(labelEditTool.build())
                .edgeReconnectionTools(this.createEdgeReconnectionTool())
                .toolSections(this.defaultToolsFactory.createDefaultHideRevealEdgeToolSection())
                .build();
    }

    private EdgeReconnectionTool[] createEdgeReconnectionTool() {
        SourceEdgeEndReconnectionTool sourceEdgeEndReconnectionTool = this.diagramBuilderHelper.newSourceEdgeEndReconnectionTool()
                .name("functionalExchangeSourceReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of6(TransverseRepresentationReconnectToolServices::reconnectFunctionalExchangeSource)
                                .aql(
                                        AQLConstants.EDGE_SEMANTIC_ELEMENT,
                                        AQLConstants.SEMANTIC_RECONNECTION_TARGET,
                                        AQLConstants.SEMANTIC_RECONNECTION_SOURCE,
                                        AQLConstants.RECONNECTION_TARGET_VIEW,
                                        AQLConstants.OTHER_END,
                                        IEditingContext.EDITING_CONTEXT,
                                        AQLConstants.DIAGRAM))
                        .build())
                .build();

        TargetEdgeEndReconnectionTool targetEdgeEndReconnectionTool = this.diagramBuilderHelper.newTargetEdgeEndReconnectionTool()
                .name("functionalExchangeTargetReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of6(TransverseRepresentationReconnectToolServices::reconnectFunctionalExchangeTarget)
                                .aql(
                                        AQLConstants.EDGE_SEMANTIC_ELEMENT,
                                        AQLConstants.SEMANTIC_RECONNECTION_TARGET,
                                        AQLConstants.SEMANTIC_RECONNECTION_SOURCE,
                                        AQLConstants.OTHER_END,
                                        AQLConstants.RECONNECTION_TARGET_VIEW,
                                        IEditingContext.EDITING_CONTEXT,
                                        AQLConstants.DIAGRAM))
                        .build())
                .build();

        return new EdgeReconnectionTool[] { sourceEdgeEndReconnectionTool, targetEdgeEndReconnectionTool };
    }
}

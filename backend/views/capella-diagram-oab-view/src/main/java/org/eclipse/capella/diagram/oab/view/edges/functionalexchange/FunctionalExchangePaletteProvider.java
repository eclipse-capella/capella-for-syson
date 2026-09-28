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
package org.eclipse.capella.diagram.oab.view.edges.functionalexchange;

import java.util.Objects;

import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationReconnectToolServices;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.EdgeReconnectionTool;
import org.eclipse.sirius.components.view.diagram.SourceEdgeEndReconnectionTool;
import org.eclipse.sirius.components.view.diagram.TargetEdgeEndReconnectionTool;
import org.eclipse.syson.diagram.common.view.DiagramDefaultToolsFactory;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides OAB Functional Exchange edge tools.
 *
 * @author tbezierslafosse
 */
public class FunctionalExchangePaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final DiagramDefaultToolsFactory diagramDefaultToolsFactory;

    public FunctionalExchangePaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.diagramDefaultToolsFactory = new DiagramDefaultToolsFactory();
    }

    public EdgePalette createEdgePalette() {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(OAMutationService::deleteInteractionOA).aqlSelf())
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
                .edgeReconnectionTools(this.createEdgeReconnectionTools())
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealEdgeToolSection())
                .build();
    }

    private EdgeReconnectionTool[] createEdgeReconnectionTools() {
        SourceEdgeEndReconnectionTool sourceTool = this.diagramBuilderHelper.newSourceEdgeEndReconnectionTool()
                .name("functionalExchangeSourceReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of3(OARepresentationReconnectToolServices::reconnectInteractionSource)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET,
                                        IEditingContext.EDITING_CONTEXT, AQLConstants.DIAGRAM))
                        .build())
                .build();
        TargetEdgeEndReconnectionTool targetTool = this.diagramBuilderHelper.newTargetEdgeEndReconnectionTool()
                .name("functionalExchangeTargetReconnectionTool")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of3(OARepresentationReconnectToolServices::reconnectInteractionTarget)
                                .aql(AQLConstants.EDGE_SEMANTIC_ELEMENT, AQLConstants.SEMANTIC_RECONNECTION_TARGET,
                                        IEditingContext.EDITING_CONTEXT, AQLConstants.DIAGRAM))
                        .build())
                .build();
        return new EdgeReconnectionTool[] { sourceTool, targetTool };
    }
}

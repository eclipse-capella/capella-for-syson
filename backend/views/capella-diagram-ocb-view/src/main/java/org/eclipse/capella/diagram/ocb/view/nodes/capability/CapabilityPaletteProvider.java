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
package org.eclipse.capella.diagram.ocb.view.nodes.capability;

import java.util.Objects;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the Operational Capability palette.
 *
 * @author tbezierslafosse
 */
public class CapabilityPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    public CapabilityPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper,
            NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.nodeDeleteFromDiagramToolProvider = Objects.requireNonNull(nodeDeleteFromDiagramToolProvider);
    }

    public NodePalette createNodePalette(IViewDiagramElementFinder cache) {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf())
                        .build());
        var labelEditTool = this.diagramBuilderHelper.newLabelEditTool()
                .name("Edit")
                .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Element>of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(DiagramMutationLabelService::directEditNode).aqlSelf("newLabel"))
                        .build());
        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .labelEditTool(labelEditTool.build())
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .toolSections(new DefaultToolsFactory().createDefaultHideRevealNodeToolSection())
                .build();
    }
}

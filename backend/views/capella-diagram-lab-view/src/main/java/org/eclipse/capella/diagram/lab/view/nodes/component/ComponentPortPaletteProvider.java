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
package org.eclipse.capella.diagram.lab.view.nodes.component;

import java.util.Objects;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.diagram.lab.view.edges.componentexchange.ComponentExchangeToolProvider;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide Palette for Component ports.
 *
 * @author fbarbin
 */
public class ComponentPortPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    private final DefaultToolsFactory defaultToolsFactory;

    public ComponentPortPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper, NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.nodeDeleteFromDiagramToolProvider = Objects.requireNonNull(nodeDeleteFromDiagramToolProvider);
        this.defaultToolsFactory = new DefaultToolsFactory();
    }

    public NodePalette createNodePalette(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf())
                        .build());

        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .edgeTools(
                        new ComponentExchangeToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewComponentExchangeTool(cache))
                .toolSections(this.defaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }

}

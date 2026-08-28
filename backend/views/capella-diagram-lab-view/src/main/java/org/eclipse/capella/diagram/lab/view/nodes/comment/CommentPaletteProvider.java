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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view.nodes.comment;

import java.util.Objects;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.diagram.lab.view.edges.annotating.AnnotatingToolProvider;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.syson.diagram.common.view.DiagramDefaultToolsFactory;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.sysml.Comment;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide Palette for Comment nodes.
 *
 * @author vkravchenko
 */
public class CommentPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    private final DiagramDefaultToolsFactory diagramDefaultToolsFactory;

    public CommentPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper, NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.nodeDeleteFromDiagramToolProvider = Objects.requireNonNull(nodeDeleteFromDiagramToolProvider);
        this.diagramDefaultToolsFactory = new DiagramDefaultToolsFactory();
    }

    public NodePalette createNodePalette(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf())
                        .build());

        var labelEditTool = this.diagramBuilderHelper.newLabelEditTool()
                .name("Edit")
                .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Comment> of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(DiagramMutationAQLService::directEditNode).aqlSelf("newLabel"))
                        .build());

        var linkTool = new AnnotatingToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper)
                .createLinkCommentTool(cache);

        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .labelEditTool(labelEditTool.build())
                .edgeTools(linkTool)
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }
}

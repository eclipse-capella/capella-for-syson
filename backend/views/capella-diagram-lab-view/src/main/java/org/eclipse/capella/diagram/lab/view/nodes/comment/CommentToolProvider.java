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

import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.AQLUtils;

/**
 * Provide tools to create comment node.
 *
 * @author vkravchenko
 */
public class CommentToolProvider {

    protected final ViewBuilders viewBuilderHelper;
    private final DiagramBuilders diagramBuilderHelper;

    public CommentToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewCommentNodeTool(IViewDiagramElementFinder cache) {

        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Comment")
                .iconURLsExpression("/icons/full/obj16/Comment.svg");

        cache.getNodeDescription(CommentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {

            ChangeContextBuilder changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(org.eclipse.syson.util.ServiceMethod.of0(LARepresentationMutationService::createComment).aqlSelf())
                    .children(
                            this.diagramBuilderHelper.newCreateView()
                                    .containmentKind(NodeContainmentKind.CHILD_NODE)
                                    .elementDescription(nodeDescription)
                                    .parentViewExpression(AQLUtils.aqlString(Node.SELECTED_NODE))
                                    .semanticElementExpression(AQLConstants.AQL_SELF)
                                    .variableName("newInstanceView").build());

            nodeToolBuilder.body(changeContextBuilder.build());
        });

        return nodeToolBuilder.build();
    }
}

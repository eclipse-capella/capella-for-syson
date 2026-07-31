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
package org.eclipse.capella.diagram.lab.view.edges.annotating;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.lab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.comment.CommentNodeDescriptionProvider;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides tool to create annotation link from Comment to annotated elements.
 *
 * @author vkravchenko
 */
public class AnnotatingToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public AnnotatingToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createLinkCommentTool(IViewDiagramElementFinder cache) {
        List<DiagramElementDescription> targetDiagramElementDescriptions = new ArrayList<>(cache.getNodeDescriptions());
        // Remove Comment from targets - can't link a comment to itself
        cache.getNodeDescription(CommentNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                .ifPresent(targetDiagramElementDescriptions::remove);

        // Also allow linking to edges (Functional Exchange, Component Exchange)
        cache.getEdgeDescription(FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                .ifPresent(targetDiagramElementDescriptions::add);
        cache.getEdgeDescription(ComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                .ifPresent(targetDiagramElementDescriptions::add);

        var edgeToolBuilder = this.diagramBuilderHelper.newEdgeTool()
                .name("Link to Element")
                .iconURLsExpression("/icons/full/obj16/Annotation.svg")
                .targetElementDescriptions(targetDiagramElementDescriptions.toArray(new DiagramElementDescription[0]))
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(LARepresentationMutationService::createCommentLink).aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build());

        return edgeToolBuilder.build();
    }
}

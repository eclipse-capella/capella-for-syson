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
package org.eclipse.capella.diagram.mcb.view.edges.involvement;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import java.util.Objects;

import org.eclipse.capella.diagram.mcb.view.nodes.actor.SystemActorNodeDescriptionProvider;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the MCB involvement creation tool.
 *
 * @author tbezierslafosse
 */
public class InvolvementToolProvider {

    private final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public InvolvementToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
    }

    public EdgeTool createNewInvolvementTool(IViewDiagramElementFinder cache) {
        var target = cache.getNodeDescription(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElseThrow();
        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Involvement")
                .targetElementDescriptions(target)
                .palette(this.diagramBuilderHelper.newNodePalette()
                        .nodeTools(this.diagramBuilderHelper.newNodeTool()
                                .name("New Involvement")
                                .iconURLsExpression("/icons/full/obj16/Describes.svg")
                                .body(this.viewBuilderHelper.newChangeContext()
                                        .expression(ServiceMethod.of1(CommonCreationService::createCapabilityInvolvement)
                                                .aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                                        .build())
                                .build())
                        .build())
                .build();
    }
}

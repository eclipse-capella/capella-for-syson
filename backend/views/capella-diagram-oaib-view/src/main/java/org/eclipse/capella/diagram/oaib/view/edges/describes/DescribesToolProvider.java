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
package org.eclipse.capella.diagram.oaib.view.edges.describes;

import java.util.ArrayList;

import java.util.List;
import org.eclipse.capella.diagram.oaib.view.edges.interaction.InteractionEdgeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.process.OperationalProcessNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the creation tool for OAIB Describes edges.
 *
 * @author tbezierslafosse
 */
public class DescribesToolProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    public DescribesToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public List<DiagramElementDescription> getTargetDescriptions(IViewDiagramElementFinder cache) {
        List<DiagramElementDescription> targets = new ArrayList<>();
        cache.getNodeDescription(OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getNodeDescription(OperationalProcessNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getEdgeDescription(InteractionEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME).ifPresent(targets::add);
        return targets;
    }

    public EdgeTool createNewDescribesTool(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Describes")
                .targetElementDescriptions(this.getTargetDescriptions(cache).toArray(DiagramElementDescription[]::new))
                .palette(this.diagramBuilderHelper.newNodePalette()
                        .nodeTools(this.createNewDescribesAction())
                        .build())
                .build();
    }

    private NodeTool createNewDescribesAction() {
        return this.diagramBuilderHelper.newNodeTool()
                .name("New Describes")
                .iconURLsExpression("/icons/full/obj16/Describes.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(CommonCreationService::createDescribes)
                                .aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build())
                .build();
    }
}

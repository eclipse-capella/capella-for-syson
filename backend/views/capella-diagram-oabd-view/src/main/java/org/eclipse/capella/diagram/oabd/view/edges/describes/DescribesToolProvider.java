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
package org.eclipse.capella.diagram.oabd.view.edges.describes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.oabd.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide tools to create describes relation.
 *
 * @author fbarbin
 */
public class DescribesToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public DescribesToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createNewDescribesTool(IViewDiagramElementFinder cache) {
        List<DiagramElementDescription> targetDiagramElementDescriptions = this.getTargetDescriptions(cache);
        var edgeToolBuilder = this.diagramBuilderHelper.newEdgeTool()
                .name("New Describes")
                .targetElementDescriptions(targetDiagramElementDescriptions.toArray(new DiagramElementDescription[0]))
                .iconURLsExpression("/icons/full/obj16/Describes.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(CommonCreationService::createDescribes).aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build());

        return edgeToolBuilder.build();
    }

    /**
     * Returns the descriptions representing Arcadia usages that can be described.
     *
     * @param cache the diagram element descriptions
     * @return the targets shared by the creation tool and the edge mapping
     */
    public List<DiagramElementDescription> getTargetDescriptions(IViewDiagramElementFinder cache) {
        List<DiagramElementDescription> targets = new ArrayList<>();
        cache.getNodeDescription(OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        return targets;
    }
}

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
package org.eclipse.capella.diagram.lab.view.edges.describes;

import org.eclipse.capella.diagram.lab.view.LABViewDiagramDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationMutationService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide tools to create describes relation.
 *
 * @author fbarbin
 */
public class DescribesToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    private final TransverseQueryService transverseQueryService;

    public DescribesToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
        this.transverseQueryService = new TransverseQueryService();
    }

    public EdgeTool createNewDescribesTool(IViewDiagramElementFinder cache) {
        List<DiagramElementDescription> targetDiagramElementDescriptions = new ArrayList<>(
                this.transverseQueryService.getDiagramNodeDescriptions(LABViewDiagramDescriptionProvider.DESCRIPTION_NAME, cache)
        );
        cache.getEdgeDescription(ComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                .ifPresent(targetDiagramElementDescriptions::add);
        cache.getEdgeDescription(FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                .ifPresent(targetDiagramElementDescriptions::add);

        var edgeToolBuilder = this.diagramBuilderHelper.newEdgeTool()
                .name("New Describes")
                .targetElementDescriptions(targetDiagramElementDescriptions.toArray(new DiagramElementDescription[0]))
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(TransverseRepresentationMutationService::createRequirementDescribes).aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build());

        return edgeToolBuilder.build();
    }
}

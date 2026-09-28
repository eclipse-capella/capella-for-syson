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

import org.eclipse.capella.diagram.oab.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the tool creating Functional Exchanges between OAB Operational Activities.
 *
 * @author tbezierslafosse
 */
public class FunctionalExchangeToolProvider {

    private final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public FunctionalExchangeToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createNewFunctionalExchangeTool(IViewDiagramElementFinder cache) {
        NodeDescription targetDescription = cache.getNodeDescription(OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);
        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Functional Exchange")
                .targetElementDescriptions(targetDescription)
                .iconURLsExpression("/icons/full/obj16/FunctionalExchange.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(OAMutationService::createInteractionOA)
                                .aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build())
                .build();
    }
}

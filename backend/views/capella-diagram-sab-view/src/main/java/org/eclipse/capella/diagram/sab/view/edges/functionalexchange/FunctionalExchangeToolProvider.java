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
package org.eclipse.capella.diagram.sab.view.edges.functionalexchange;

import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionPortNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Tools to create SAB functional exchanges.
 *
 * @author mbats
 */
public class FunctionalExchangeToolProvider {

    private final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public FunctionalExchangeToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createNewFunctionalExchangeTool(IViewDiagramElementFinder cache) {
        NodeDescription targetPortDescription = cache.getNodeDescription(FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);
        NodeDescription targetFunctionDescription = cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);
        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Functional Exchange")
                .preconditionExpression(ServiceMethod.of0(TransverseQueryService::canBeExchangeSource)
                        .aqlSelf())
                .targetElementDescriptions(targetPortDescription, targetFunctionDescription)
                .iconURLsExpression("/icons/full/obj16/FunctionalExchange.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(TransverseMutationService::createFunctionalExchange)
                                .aql(
                                        EdgeDescription.SEMANTIC_EDGE_SOURCE,
                                        EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build())
                .build();
    }
}

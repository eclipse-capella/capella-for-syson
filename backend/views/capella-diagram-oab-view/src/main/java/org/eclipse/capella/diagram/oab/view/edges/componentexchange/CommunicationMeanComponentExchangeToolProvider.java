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
package org.eclipse.capella.diagram.oab.view.edges.componentexchange;

import org.eclipse.capella.diagram.oab.view.nodes.component.EntityComponentNodeDescriptionProvider;
import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide tools to create communication mean component exchanges.
 *
 * @author fbarbin
 */
public class CommunicationMeanComponentExchangeToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public CommunicationMeanComponentExchangeToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createNewComponentExchangeTool(IViewDiagramElementFinder cache) {
        NodeDescription targetNodeDescription = cache.getNodeDescription(EntityComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);
        var edgeToolBuilder = this.diagramBuilderHelper.newEdgeTool()
                .name("New Communication Mean")
                .targetElementDescriptions(targetNodeDescription)
                .iconURLsExpression("/icons/full/obj16/ComponentExchange.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(OAMutationService::createCommunicationMeanComponentExchangeOA)
                                .aql(
                                        EdgeDescription.SEMANTIC_EDGE_SOURCE,
                                        EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build());

        return edgeToolBuilder.build();
    }
}

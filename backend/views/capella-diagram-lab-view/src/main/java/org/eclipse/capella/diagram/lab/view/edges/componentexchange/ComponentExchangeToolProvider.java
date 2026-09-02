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
package org.eclipse.capella.diagram.lab.view.edges.componentexchange;

import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentPortNodeDescriptionProvider;
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
 * Provide tools to create component exchanges.
 *
 * @author fbarbin
 */
public class ComponentExchangeToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public ComponentExchangeToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createNewComponentExchangeTool(IViewDiagramElementFinder cache) {
        NodeDescription targetPortDescription = cache.getNodeDescription(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);
        NodeDescription targetNodeDescription = cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);
        var edgeToolBuilder = this.diagramBuilderHelper.newEdgeTool()
                .name("New Component Exchange")
                .preconditionExpression(ServiceMethod.of0(TransverseQueryService::canBeExchangeSource)
                        .aqlSelf())
                .targetElementDescriptions(targetPortDescription, targetNodeDescription)
                .iconURLsExpression("/icons/full/obj16/ComponentExchange.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(TransverseMutationService::createComponentExchange)
                                .aql(
                                        EdgeDescription.SEMANTIC_EDGE_SOURCE,
                                        EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build());

        return edgeToolBuilder.build();
    }
}

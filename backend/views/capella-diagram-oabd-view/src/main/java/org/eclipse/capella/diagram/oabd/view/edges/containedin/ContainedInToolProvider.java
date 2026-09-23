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
package org.eclipse.capella.diagram.oabd.view.edges.containedin;

import java.util.Objects;

import org.eclipse.capella.diagram.oabd.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the creation tool for Contained In edges.
 *
 * @author tbezierslafosse
 */
public class ContainedInToolProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    public ContainedInToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
    }

    public EdgeTool createNewContainedInTool(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Contained In")
                .iconURLsExpression("/icons/full/obj16/OperationalActivity.svg")
                .targetElementDescriptions(cache.getNodeDescription(OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).stream().toArray(
                        org.eclipse.sirius.components.view.diagram.DiagramElementDescription[]::new))
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(TransverseRepresentationReconnectToolServices::reconnectContainedInTarget)
                                .aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build())
                .build();
    }
}

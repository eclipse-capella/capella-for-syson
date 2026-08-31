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
package org.eclipse.capella.diagram.ocb.view.edges.generalization;

import java.util.Objects;

import org.eclipse.capella.diagram.ocb.view.nodes.capability.CapabilityNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the tool creating an Operational Capability generalization as a SysMLv2 Subsetting.
 *
 * @author tbezierslafosse
 */
public class GeneralizationToolProvider {

    private final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public GeneralizationToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
    }

    public EdgeTool createNewGeneralizationTool(IViewDiagramElementFinder cache) {
        var targetNode = cache.getNodeDescription(CapabilityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).orElse(null);

        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Generalization")
                .iconURLsExpression("/icons/full/obj16/Subsetting.svg")
                .targetElementDescriptions(targetNode)
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(TransverseMutationService::createGeneralization)
                                .aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build())
                .build();
    }
}

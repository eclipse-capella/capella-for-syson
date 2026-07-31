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

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionPortNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Functional exchange edge description for SAB.
 *
 * @author mbats
 */
public class FunctionalExchangeEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "SABFunctionalExchangeEdgeDescription";

    public FunctionalExchangeEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getFlowUsage());
        FunctionalExchangeEdgeStyleProvider styleProvider = new FunctionalExchangeEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(EDGE_DESCRIPTION_NAME)
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getFunctionalExchanges).aqlSelf())
                .sourceExpression(ServiceMethod.of0(TransverseQueryService::getFunctionalExchangeSource).aqlSelf())
                .style(styleProvider.createEdgeStyle())
                .conditionalStyles(styleProvider.createConditionalEdgeStyles())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getFunctionalExchangeTarget).aqlSelf())
                .palette(new FunctionalExchangePaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getEdgeDescription(EDGE_DESCRIPTION_NAME).ifPresent(edgeDescription -> {
            diagramDescription.getEdgeDescriptions().add(edgeDescription);
            cache.getNodeDescription(FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
                edgeDescription.getSourceDescriptions().add(nodeDescription);
                edgeDescription.getTargetDescriptions().add(nodeDescription);
            });
        });
    }
}

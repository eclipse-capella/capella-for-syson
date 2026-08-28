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
package org.eclipse.capella.diagram.ddv.view.view.edges.functionalexchange;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.ddv.view.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.ddv.view.view.nodes.function.RootFunctionNodeDescriptionProvider;
import org.eclipse.capella.model.services.functional.context.DDVQueryService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Functional Exchange edge description.
 *
 * @author fbarbin
 */
public class FunctionalExchangeEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "DDVFunctionalExchangeEdgeDescription";

    public FunctionalExchangeEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getFlowUsage());
        FunctionalExchangeEdgeStyleProvider functionalExchangeEdgeStyleProvider = new FunctionalExchangeEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(this.getEdgeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(DDVQueryService::getRelatedFunctionalExchanges).aqlSelf())
                .sourceExpression(ServiceMethod.of0(TransverseQueryService::getFunctionalExchangeSourceFunction).aqlSelf())
                .style(functionalExchangeEdgeStyleProvider.createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getFunctionalExchangeTargetFunction).aqlSelf())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(this.getEdgeDescriptionName());
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            cache.getNodeDescription(RootFunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
                edgeDescription.getSourceDescriptions().add(nodeDescription);
                edgeDescription.getTargetDescriptions().add(nodeDescription);
            });
            cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
                edgeDescription.getSourceDescriptions().add(nodeDescription);
                edgeDescription.getTargetDescriptions().add(nodeDescription);
            });
        }
    }

    private String getEdgeDescriptionName() {
        return EDGE_DESCRIPTION_NAME;
    }
}

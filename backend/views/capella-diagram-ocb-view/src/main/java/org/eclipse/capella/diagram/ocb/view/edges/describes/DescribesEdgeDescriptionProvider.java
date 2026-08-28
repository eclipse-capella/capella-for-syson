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
package org.eclipse.capella.diagram.ocb.view.edges.describes;

import java.util.ArrayList;
import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.edges.componentexchange.CommunicationMeanComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes edge description.
 *
 * @author tbezierslafosse
 */
public class DescribesEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "DescribesEdgeDescription";

    public DescribesEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        var domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAllocationUsage());
        var describesEdgeStyleProvider = new DescribesEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(EDGE_DESCRIPTION_NAME)
                .centerLabelExpression("")
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getDescribes).aqlSelf())
                .sourceExpression(ServiceMethod.of0(TransverseQueryService::getDescribesSource).aqlSelf())
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getDescribesTarget).aqlSelf())
                .style(describesEdgeStyleProvider.createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .palette(new DescribesPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(EDGE_DESCRIPTION_NAME);
        if (optEdgeDescription.isPresent()) {
            var edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            var diagramElementDescriptions = new ArrayList<DiagramElementDescription>(cache.getNodeDescriptions());
            cache.getEdgeDescription(CommunicationMeanComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                    .ifPresent(diagramElementDescriptions::add);

            cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(edgeDescription.getSourceDescriptions()::add);
            edgeDescription.getTargetDescriptions().addAll(diagramElementDescriptions);
        }
    }
}

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
package org.eclipse.capella.diagram.sab.view.edges.describes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
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
 * Describes edge description for SAB.
 *
 * @author mbats
 */
public class DescribesEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "DescribesEdgeDescription";

    public DescribesEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAllocationUsage());
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(EDGE_DESCRIPTION_NAME)
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getDescribes).aqlSelf())
                .sourceExpression(ServiceMethod.of0(TransverseQueryService::getDescribesSource).aqlSelf())
                .style(new DescribesEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getDescribesTarget).aqlSelf())
                .palette(new DescribesPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(EDGE_DESCRIPTION_NAME);
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            List<DiagramElementDescription> diagramElementDescriptions = new ArrayList<>(cache.getNodeDescriptions());
            cache.getEdgeDescription(ComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME).ifPresent(diagramElementDescriptions::add);
            cache.getEdgeDescription(FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME).ifPresent(diagramElementDescriptions::add);

            cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(edgeDescription.getSourceDescriptions()::add);
            diagramElementDescriptions.forEach(edgeDescription.getTargetDescriptions()::add);
        }
    }
}

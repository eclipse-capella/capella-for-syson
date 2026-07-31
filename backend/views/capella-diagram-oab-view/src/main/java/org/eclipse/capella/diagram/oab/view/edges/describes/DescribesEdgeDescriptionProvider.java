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
package org.eclipse.capella.diagram.oab.view.edges.describes;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.oab.view.OABViewDiagramDescriptionProvider;
import org.eclipse.capella.diagram.oab.view.edges.componentexchange.CommunicationMeanComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes edge description.
 *
 * @author fbarbin
 */
public class DescribesEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "DescribesEdgeDescription";

    private final TransverseQueryService transverseQueryService;

    public DescribesEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAllocationUsage());
        DescribesEdgeStyleProvider describesEdgeStyleProvider = new DescribesEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(this.getEdgeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getAllocationUsage).aqlSelf())
                .sourceExpression(AQLConstants.AQL_SELF + ".source->first()")
                .style(describesEdgeStyleProvider.createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(AQLConstants.AQL_SELF + ".target->first()")
                .palette(new DescribesPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(this.getEdgeDescriptionName());
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            List<DiagramElementDescription> diagramElementDescriptions = new ArrayList<>(
                    this.transverseQueryService.getDiagramNodeDescriptions(OABViewDiagramDescriptionProvider.DESCRIPTION_NAME, cache)
            );
            cache.getEdgeDescription(CommunicationMeanComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                    .ifPresent(diagramElementDescriptions::add);

            diagramElementDescriptions.forEach(diagramElementDescription -> {
                edgeDescription.getSourceDescriptions().add(diagramElementDescription);
                edgeDescription.getTargetDescriptions().add(diagramElementDescription)
                ;
            });
        }
    }

    private String getEdgeDescriptionName() {
        return EDGE_DESCRIPTION_NAME;
    }
}

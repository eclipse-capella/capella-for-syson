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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.oab.view.edges.componentexchange.CommunicationMeanComponentExchangeEdgeDescriptionProvider;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.model.services.aql.ModelQueryAQLService;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes edge description.
 *
 * @author fbarbin
 */
public class DescribesEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "DescribesEdgeDescription";

    public DescribesEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAllocationUsage());
        DescribesEdgeStyleProvider describesEdgeStyleProvider = new DescribesEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(this.getEdgeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(ModelQueryAQLService::getAllReachableAllocateEdges).aqlSelf())
                .sourceExpression(ServiceMethod.of0(ModelQueryAQLService::getSourceAllocateEdge).aqlSelf())
                .style(describesEdgeStyleProvider.createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(ModelQueryAQLService::getTargetAllocateEdge).aqlSelf())
                .palette(new DescribesPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(this.getEdgeDescriptionName());
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            List<DiagramElementDescription> diagramElementDescriptions = new ArrayList<>(cache.getNodeDescriptions());
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

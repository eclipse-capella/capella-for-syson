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
import org.eclipse.capella.diagram.oab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
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
                .centerLabelExpression("")
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
        cache.getEdgeDescription(EDGE_DESCRIPTION_NAME).ifPresent(edgeDescription -> {
            diagramDescription.getEdgeDescriptions().add(edgeDescription);
            cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(edgeDescription.getSourceDescriptions()::add);
            edgeDescription.getTargetDescriptions().addAll(
                    new DescribesToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).getTargetDescriptions(cache));
        });
    }

    private String getEdgeDescriptionName() {
        return EDGE_DESCRIPTION_NAME;
    }
}

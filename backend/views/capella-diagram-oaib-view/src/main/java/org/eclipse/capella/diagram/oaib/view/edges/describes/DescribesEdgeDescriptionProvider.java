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
package org.eclipse.capella.diagram.oaib.view.edges.describes;

import java.util.ArrayList;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.edges.interaction.InteractionEdgeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.process.OperationalProcessNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
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
 * Provides synchronized Describes edges in OAIB diagrams.
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
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAllocationUsage()))
                .isDomainBasedEdge(true)
                .name(EDGE_DESCRIPTION_NAME)
                .centerLabelExpression("")
                .semanticCandidatesExpression(ServiceMethod.of0(CommonQueryService::getDescribes).aqlSelf())
                .sourceExpression(ServiceMethod.of0(CommonQueryService::getDescribesSource).aqlSelf())
                .style(new DescribesEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(CommonQueryService::getDescribesTarget).aqlSelf())
                .palette(new DescribesPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getEdgeDescription(EDGE_DESCRIPTION_NAME).ifPresent(edgeDescription -> {
            diagramDescription.getEdgeDescriptions().add(edgeDescription);
            cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(edgeDescription.getSourceDescriptions()::add);
            var targetDescriptions = new ArrayList<DiagramElementDescription>();
            cache.getNodeDescription(OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targetDescriptions::add);
            cache.getNodeDescription(OperationalProcessNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targetDescriptions::add);
            cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targetDescriptions::add);
            cache.getEdgeDescription(InteractionEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME).ifPresent(targetDescriptions::add);
            targetDescriptions.forEach(edgeDescription.getTargetDescriptions()::add);
        });
    }
}

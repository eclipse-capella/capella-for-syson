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
package org.eclipse.capella.diagram.ocb.view.edges.involvement;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.capability.CapabilityNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes synchronized involvements from an Operational Capability to its involved participants.
 *
 * @author tbezierslafosse
 */
public class InvolvementEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "OperationalCapabilityInvolvementEdgeDescription";

    public InvolvementEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getOccurrenceUsage());
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(EDGE_DESCRIPTION_NAME)
                .centerLabelExpression("")
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getCapabilities).aqlSelf())
                .sourceExpression(AQLConstants.AQL_SELF)
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getInvolvedComponents).aqlSelf())
                .style(new InvolvementEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .palette(new InvolvementPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getEdgeDescription(EDGE_DESCRIPTION_NAME).ifPresent(edgeDescription -> {
            diagramDescription.getEdgeDescriptions().add(edgeDescription);
            cache.getNodeDescription(CapabilityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(edgeDescription.getSourceDescriptions()::add);
            cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(edgeDescription.getTargetDescriptions()::add);
        });
    }
}

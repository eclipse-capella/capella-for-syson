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
package org.eclipse.capella.diagram.ocb.view.nodes.capability;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes Operational Capability nodes in OCB diagrams.
 *
 * @author tbezierslafosse
 */
public class CapabilityNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "OperationalCapabilityNodeDescription";

    private static final String DEFAULT_WIDTH = "72";

    private static final String DEFAULT_HEIGHT = "72";

    public CapabilityNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        var domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getOccurrenceUsage());
        return this.diagramBuilderHelper.newNodeDescription()
                .domainType(domainType)
                .insideLabel(new CapabilityLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .outsideLabels(new CapabilityLabelProvider(this.diagramBuilderHelper, this.colorProvider).createOutsideLabelDescription())
                .name(NODE_DESCRIPTION_NAME)
                .defaultHeightExpression(DEFAULT_HEIGHT)
                .defaultWidthExpression(DEFAULT_WIDTH)
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getCapabilities).aqlSelf())
                .style(new CapabilityNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createNodeStyle())
                .userResizable(UserResizableDirection.NONE)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
            nodeDescription.setPalette(new CapabilityPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider)
                    .createNodePalette(cache));
        });
    }
}

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
package org.eclipse.capella.diagram.mcb.view.nodes.actor;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.mcb.view.MCBDiagramPaletteProvider;
import org.eclipse.capella.diagram.mcb.view.MCBNodePaletteProvider;
import org.eclipse.capella.diagram.mcb.view.MCBNodeToolProvider;
import org.eclipse.capella.diagram.mcb.view.MCBViewQueryService;
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
 * Describes human and non-human actors in MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class SystemActorNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "SystemActorNodeDescription";

    public SystemActorNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription().name(NODE_DESCRIPTION_NAME).collapsible(true)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage()))
                .defaultWidthExpression("180").defaultHeightExpression("70").userResizable(UserResizableDirection.BOTH)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .semanticCandidatesExpression(ServiceMethod.of0(MCBViewQueryService::getActorCandidates).aqlSelf())
                .insideLabel(new SystemActorLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .style(this.diagramBuilderHelper.newRectangularNodeStyleDescription().background(this.colorProvider.getColor("ACTOR_BACKGROUND"))
                        .borderColor(this.colorProvider.getColor("ACTOR_BORDER")).borderRadius(0).borderSize(2).build())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var description = cache.getNodeDescription(NODE_DESCRIPTION_NAME).orElseThrow();
        description.getReusedChildNodeDescriptions().add(description);
        var palette = new MCBNodePaletteProvider().createNodePalette();
        palette.getNodeTools().add(new MCBNodeToolProvider().createActorTool(cache));
        palette.setDropNodeTool(new MCBDiagramPaletteProvider().createActorDropTool(cache));
        description.setPalette(palette);
        diagramDescription.getNodeDescriptions().add(description);
    }
}

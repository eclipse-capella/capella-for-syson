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
import org.eclipse.capella.diagram.mcb.view.MCBViewConstants;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
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
                .semanticCandidatesExpression(ServiceMethod.of0(SAQueryService::getSystemActors).aqlSelf())
                .insideLabel(new SystemActorLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .style(this.diagramBuilderHelper.newRectangularNodeStyleDescription().background(this.colorProvider.getColor(MCBViewConstants.ACTOR_BACKGROUND_COLOR))
                        .borderColor(this.colorProvider.getColor(MCBViewConstants.ACTOR_BORDER_COLOR)).borderRadius(0).borderSize(2).build())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var description = cache.getNodeDescription(NODE_DESCRIPTION_NAME).orElseThrow();
        description.setPalette(this.createNodePalette());
        diagramDescription.getNodeDescriptions().add(description);
    }
    private NodePalette createNodePalette() {
        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(this.diagramBuilderHelper.newDeleteTool().name("Delete from Model")
                        .body(this.viewBuilderHelper.newChangeContext().expression(ServiceMethod.of0(CommonDeletionService::delete).aqlSelf()).build()).build())
                .labelEditTool(this.diagramBuilderHelper.newLabelEditTool().name("Edit")
                        .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Element>of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                        .body(this.viewBuilderHelper.newChangeContext().expression(ServiceMethod.of1(DiagramMutationLabelService::directEditNode).aqlSelf("newLabel")).build()).build())
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .build();
    }

}

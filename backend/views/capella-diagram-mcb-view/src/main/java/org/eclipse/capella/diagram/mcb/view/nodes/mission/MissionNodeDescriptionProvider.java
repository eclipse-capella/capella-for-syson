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
package org.eclipse.capella.diagram.mcb.view.nodes.mission;

import org.eclipse.capella.common.view.diagram.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
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
 * Describes Mission nodes in MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class MissionNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "MissionNodeDescription";

    public MissionNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        var labelProvider = new MissionLabelProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newNodeDescription()
                .name(NODE_DESCRIPTION_NAME)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getOccurrenceUsage()))
                .defaultWidthExpression("72")
                .defaultHeightExpression("72")
                .userResizable(UserResizableDirection.NONE)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .semanticCandidatesExpression(ServiceMethod.of0(CommonQueryService::getMissions).aqlSelf())
                .insideLabel(labelProvider.createInsideLabelDescription())
                .outsideLabels(labelProvider.createOutsideLabelDescription())
                .style(new MissionNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createNodeStyle())
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

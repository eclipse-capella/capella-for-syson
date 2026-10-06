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
package org.eclipse.capella.diagram.oaib.view.nodes.process;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.OAIBViewConstants;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes explicitly revealed Operational Processes in OAIB diagrams.
 *
 * @author tbezierslafosse
 */
public class OperationalProcessNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "OAIBOperationalProcessNodeDescription";

    public OperationalProcessNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .name(NODE_DESCRIPTION_NAME)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage()))
                .semanticCandidatesExpression(ServiceMethod.of0(CommonQueryService::getFunctionalChains).aqlSelf())
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .defaultWidthExpression("30")
                .defaultHeightExpression("30")
                .userResizable(UserResizableDirection.BOTH)
                .outsideLabels(this.diagramBuilderHelper.newOutsideLabelDescription()
                        .labelExpression("aql:self.name")
                        .overflowStrategy(LabelOverflowStrategy.NONE)
                        .textAlign(LabelTextAlign.CENTER)
                        .style(this.diagramBuilderHelper.newOutsideLabelStyle()
                                .borderSize(0)
                                .fontSize(12)
                                .labelColor(this.colorProvider.getColor(OAIBViewConstants.ACTIVITY_LABEL_COLOR))
                                .showIconExpression("aql:true")
                                .labelIcon("/icons/full/obj16/FunctionalChain.svg")
                                .build())
                        .build())
                .style(this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                        .background(this.colorProvider.getColor(OAIBViewConstants.OPERATIONAL_PROCESS_BACKGROUND_COLOR))
                        .borderColor(this.colorProvider.getColor(OAIBViewConstants.ACTIVITY_BORDER_COLOR))
                        .borderSize(2)
                        .borderRadius(0)
                        .build())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
            nodeDescription.setPalette(this.diagramBuilderHelper.newNodePalette()
                    .deleteTool(this.diagramBuilderHelper.newDeleteTool().name("Delete from Model")
                            .body(this.viewBuilderHelper.newChangeContext().expression(ServiceMethod.of0(CommonDeletionService::delete).aqlSelf()).build()).build())
                    .labelEditTool(this.diagramBuilderHelper.newLabelEditTool().name("Edit")
                            .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Element>of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                            .body(this.viewBuilderHelper.newChangeContext().expression(ServiceMethod.of1(DiagramMutationLabelService::directEditNode).aqlSelf("newLabel")).build()).build())
                    .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                    .build());
        });
    }
}

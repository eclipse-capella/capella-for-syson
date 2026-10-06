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
package org.eclipse.capella.diagram.oaib.view.nodes.requirement;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.DescriptionNameGenerator;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes explicitly revealed Requirements in OAIB diagrams.
 *
 * @author tbezierslafosse
 */
public class RequirementNodeDescriptionProvider extends AbstractNodeDescriptionProvider {
    public static final String NODE_DESCRIPTION_NAME = "RequirementNodeDescription";

    private final IDescriptionNameGenerator nameGenerator = new DescriptionNameGenerator("OAIB");

    public RequirementNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .name(NODE_DESCRIPTION_NAME)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getRequirementUsage()))
                .semanticCandidatesExpression(ServiceMethod.of0(CommonQueryService::getRequirements).aqlSelf())
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .collapsible(true)
                .userResizable(UserResizableDirection.BOTH)
                .insideLabel(new RequirementLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .style(new RequirementNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createRequirementNodeStyle())
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
                    .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool()).build());
            String documentationCompartmentName = this.nameGenerator.getCompartmentName(SysmlPackage.eINSTANCE.getRequirementUsage(),
                    SysmlPackage.eINSTANCE.getElement_Documentation());
            cache.getNodeDescription(documentationCompartmentName)
                    .ifPresent(compartmentNode -> nodeDescription.getChildrenDescriptions().add(compartmentNode));
        });
    }
}

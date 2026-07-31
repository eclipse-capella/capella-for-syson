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
package org.eclipse.capella.diagram.lab.view.nodes.functionalchain;

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
 * Functional Chain node description.
 *
 * @author fbarbin
 */
public class FunctionalChainNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "FunctionalChainNodeDescription";

    private static final String COMPONENT_DEFAULT_WIDTH = "30";

    private static final String COMPONENT_DEFAULT_HEIGHT = "30";

    public FunctionalChainNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage());
        FunctionalChainNodeStyleProvider functionalChainNodeStyleProvider = new FunctionalChainNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .domainType(domainType)
                .outsideLabels(new FunctionalChainLabelProvider(this.diagramBuilderHelper, this.colorProvider).createOutsideLabelDescription())
                .name(this.getNodeDescriptionName())
                .defaultHeightExpression(COMPONENT_DEFAULT_HEIGHT)
                .defaultWidthExpression(COMPONENT_DEFAULT_WIDTH)
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getFunctionalChains).aqlSelf())
                .style(functionalChainNodeStyleProvider.createComponentNodeStyle())
                .conditionalStyles(functionalChainNodeStyleProvider.createConditionalNodeStyles())
                .userResizable(UserResizableDirection.BOTH)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .build();
    }

    private String getNodeDescriptionName() {
        return NODE_DESCRIPTION_NAME;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(nodeDescription -> {

            diagramDescription.getNodeDescriptions().add(nodeDescription);
            nodeDescription
                    .setPalette(
                            new FunctionalChainPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));

        });
    }


}

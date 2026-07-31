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
package org.eclipse.capella.diagram.sab.view.nodes.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.componentport.ComponentPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.system.SystemOfInterestLabelProvider;
import org.eclipse.capella.diagram.sab.view.nodes.system.SystemOfInterestNodeStyleProvider;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
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
 * Node description for internal System Components in SAB.
 *
 * @author mbats
 */
public class SystemComponentNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "SystemComponentNodeDescription";

    private static final String COMPONENT_DEFAULT_WIDTH = "180";

    private static final String COMPONENT_DEFAULT_HEIGHT = "70";

    public SystemComponentNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage());
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .domainType(domainType)
                .insideLabel(new SystemOfInterestLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .name(NODE_DESCRIPTION_NAME)
                .defaultHeightExpression(COMPONENT_DEFAULT_HEIGHT)
                .defaultWidthExpression(COMPONENT_DEFAULT_WIDTH)
                .semanticCandidatesExpression(ServiceMethod.of0(SAQueryService::getSystemComponents).aqlSelf())
                .style(new SystemOfInterestNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createNodeStyle())
                .userResizable(UserResizableDirection.BOTH)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
            cache.getNodeDescription(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(nodeDescription.getReusedBorderNodeDescriptions()::add);
            cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(nodeDescription.getReusedChildNodeDescriptions()::add);
            nodeDescription.getReusedChildNodeDescriptions().addAll(this.getReusedChildren(cache));
            nodeDescription.setPalette(new SystemComponentPaletteProvider(this.diagramBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));
        });
    }

    private List<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var reusedChildren = new ArrayList<NodeDescription>();
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(reusedChildren::add);
        return reusedChildren;
    }
}

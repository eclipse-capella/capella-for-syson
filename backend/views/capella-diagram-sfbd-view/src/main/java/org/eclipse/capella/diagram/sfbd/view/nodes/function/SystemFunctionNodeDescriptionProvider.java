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
package org.eclipse.capella.diagram.sfbd.view.nodes.function;

import org.eclipse.capella.common.view.diagram.nodes.AbstractNodeDescriptionProvider;
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
 * Describes a flat, explicitly revealed SFBD activity.
 *
 * @author adieumegard
 */
public class SystemFunctionNodeDescriptionProvider extends AbstractNodeDescriptionProvider {
    public static final String NODE_DESCRIPTION_NAME = "SystemFunctionNodeDescription";

    public SystemFunctionNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .name(NODE_DESCRIPTION_NAME)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage()))
                .semanticCandidatesExpression(ServiceMethod.of0(SAQueryService::getSystemFunctions).aqlSelf())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .collapsible(true)
                .userResizable(UserResizableDirection.BOTH)
                .insideLabel(new SystemFunctionLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .style(new SystemFunctionNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createOperationalActivityNodeStyle())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(node -> {
            diagramDescription.getNodeDescriptions().add(node);
            SystemFunctionPaletteProvider systemFunctionPaletteProvider = new SystemFunctionPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider);
            node.setPalette(systemFunctionPaletteProvider.createNodePalette(node, cache));
            node.getEdgeTools().addAll(this.edgeToolConverter.convertToEdgeToolsWithPalette(systemFunctionPaletteProvider.createEdgeTools(cache)));
        });
    }
}

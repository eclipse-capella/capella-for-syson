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
package org.eclipse.capella.diagram.oabd.view.nodes.activity;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
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
 * Describes a flat, explicitly revealed OABD activity.
 *
 * @author tbezierslafosse
 */
public class OperationalActivityNodeDescriptionProvider extends AbstractNodeDescriptionProvider {
    public static final String NODE_DESCRIPTION_NAME = "OperationalActivityNodeDescription";

    public OperationalActivityNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .name(NODE_DESCRIPTION_NAME)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage()))
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getOperationalActivities).aqlSelf())
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .collapsible(true)
                .userResizable(UserResizableDirection.BOTH)
                .insideLabel(new OperationalActivityLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .style(new OperationalActivityNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createOperationalActivityNodeStyle())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(node -> {
            diagramDescription.getNodeDescriptions().add(node);
            node.setPalette(new OperationalActivityPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider)
                    .createNodePalette(node, cache));
        });
    }
}

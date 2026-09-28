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

package org.eclipse.capella.diagram.oab.view.nodes.activity;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
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
 * Describes Operational Activity nodes allocated to Operational Entities and Actors in OAB diagrams.
 *
 * @author tbezierslafosse
 */
public class OperationalActivityNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "OABOperationalActivityNodeDescription";

    public OperationalActivityNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .name(NODE_DESCRIPTION_NAME)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage()))
                .semanticCandidatesExpression(ServiceMethod.of1(OARepresentationQueryService::getActivityCandidates).aqlSelf(DiagramContext.DIAGRAM_CONTEXT))
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .collapsible(true)
                .userResizable(UserResizableDirection.BOTH)
                .insideLabel(new OperationalActivityLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .style(new OperationalActivityNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createOperationalActivityNodeStyle())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
            nodeDescription.setPalette(new OperationalActivityPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider)
                    .createNodePalette(cache));
            nodeDescription.getReusedChildNodeDescriptions().addAll(this.getReusedChildren(cache));
        });
    }

    private List<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var reusedChildren = new ArrayList<NodeDescription>();
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(reusedChildren::add);
        return reusedChildren;
    }
}

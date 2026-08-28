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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view.nodes.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.services.LABDiagramService;
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
 * Function node description.
 *
 * @author frouene
 */
public class FunctionNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "FunctionNodeDescription";

    public FunctionNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage());
        FunctionNodeStyleProvider functionNodeStyleProvider = new FunctionNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .domainType(domainType)
                .insideLabel(new FunctionLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .name(this.getNodeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getSubFunctions).aqlSelf())
                .style(functionNodeStyleProvider.createFunctionNodeStyle())
                .conditionalStyles(functionNodeStyleProvider.createFunctionConditionalNodeStyles())
                .userResizable(UserResizableDirection.BOTH)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .isHiddenByDefaultExpression(ServiceMethod.of0(LABDiagramService::isFunctionHidden).aqlSelf())
                .build();
    }

    private String getNodeDescriptionName() {
        return NODE_DESCRIPTION_NAME;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(nodeDescription -> {
            // NOTE: FunctionNode is contained in ComponentNode.childrenDescriptions, so we do NOT add it to 
            // diagramDescription.nodeDescriptions (it would conflict with the containment from Component).
            nodeDescription
                    .setPalette(new FunctionPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));
            Optional<NodeDescription> optionalPortNodeDescription = cache.getNodeDescription(FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME);
            optionalPortNodeDescription.ifPresent(nodeDescription.getBorderNodesDescriptions()::add);
            nodeDescription.getReusedChildNodeDescriptions().addAll(this.getReusedChildren(cache));
        });
    }

    private List<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var reusedChildren = new ArrayList<NodeDescription>();

        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(reusedChildren::add);
        return reusedChildren;
    }
}

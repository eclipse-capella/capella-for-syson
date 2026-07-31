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
package org.eclipse.capella.diagram.lab.view.nodes.component;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionNodeDescriptionProvider;
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
 * Component node description.
 *
 * @author frouene
 */
public class ComponentNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "ComponentNodeDescription";

    private static final String COMPONENT_DEFAULT_WIDTH = "180";

    private static final String COMPONENT_DEFAULT_HEIGHT = "70";

    public ComponentNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage());
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .domainType(domainType)
                .insideLabel(new ComponentLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .name(this.getNodeDescriptionName())
                .defaultHeightExpression(COMPONENT_DEFAULT_HEIGHT)
                .defaultWidthExpression(COMPONENT_DEFAULT_WIDTH)
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::allNestedComponents).aqlSelf())
                .style(new ComponentNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createComponentNodeStyle())
                .conditionalStyles(new ComponentConditionalStyleProvider(this.diagramBuilderHelper, this.colorProvider).createActorConditionalStyle())
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

            // First add the node to the diagram to establish EMF containment/resource
            diagramDescription.getNodeDescriptions().add(nodeDescription);

            // Then add children/borders (they will inherit containment from parent)
            cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription.getChildrenDescriptions()::add);
            cache.getNodeDescription(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(componentPortNodeDescription -> nodeDescription.getReusedBorderNodeDescriptions().add(componentPortNodeDescription));

            nodeDescription
                    .setPalette(new ComponentPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));
            nodeDescription.getReusedChildNodeDescriptions().addAll(this.getReusedChildren(cache));
        });
    }

    private List<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var reusedChildren = new ArrayList<NodeDescription>();
        cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(reusedChildren::add);
        return reusedChildren;
    }

}

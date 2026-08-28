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

import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.common.view.nodes.ImageNodeStyleDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.eclipse.syson.util.ViewConstants;

/**
 * Function port node description.
 *
 * @author fbarbin
 */
public class FunctionPortNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "FunctionPortNodeDescription";

    private final ImageNodeStyleDescriptionProvider imageNodeStyleDescriptionProvider = new ImageNodeStyleDescriptionProvider();

    public FunctionPortNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getFeature());
        return this.diagramBuilderHelper.newNodeDescription()
                .defaultHeightExpression("10")
                .defaultWidthExpression("10")
                .domainType(domainType)
                .name(this.getNodeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getFunctionPorts).aqlSelf())
                .style(this.createPortUnsetNodeStyle())
                .conditionalStyles(this.createPortUsageConditionalNodeStyles().toArray(ConditionalNodeStyle[]::new))
                .userResizable(UserResizableDirection.NONE)
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .build();
    }

    private String getNodeDescriptionName() {
        return NODE_DESCRIPTION_NAME;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(nodeDescription -> {
            // NOTE: FunctionPort is contained in FunctionNode.borderNodesDescriptions, so we do NOT add it to 
            // diagramDescription.nodeDescriptions (it would conflict with the containment from Function).
            nodeDescription
                    .setPalette(new FunctionPortPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));
        });
    }

    private NodeStyleDescription createPortUnsetNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderRadius(0)
                .background(this.colorProvider.getColor(LABViewConstants.COMPONENT_PORT_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(LABViewConstants.COMPONENT_PORT_BORDER_COLOR))
                .build();
    }

    private List<ConditionalNodeStyle> createPortUsageConditionalNodeStyles() {
        var borderColor = this.colorProvider.getColor(ViewConstants.DEFAULT_BORDER_COLOR);
        return List.of(
                this.diagramBuilderHelper.newConditionalNodeStyle()
                        .condition(ServiceMethod.of0(TransverseQueryService::isInFeature).aqlSelf())
                        .style(this.imageNodeStyleDescriptionProvider.createImageNodeStyleDescription("icons/full/obj16/FunctionInputPort.svg", borderColor, 0))
                        .build(),
                this.diagramBuilderHelper.newConditionalNodeStyle()
                        .condition(ServiceMethod.of0(TransverseQueryService::isOutFeature).aqlSelf())
                        .style(this.imageNodeStyleDescriptionProvider.createImageNodeStyleDescription("icons/full/obj16/FunctionOutputPort.svg", borderColor, 0))
                        .build(),
                this.diagramBuilderHelper.newConditionalNodeStyle()
                        .condition(ServiceMethod.of0(TransverseQueryService::isInOutFeature).aqlSelf())
                        .style(this.imageNodeStyleDescriptionProvider.createImageNodeStyleDescription("images/feature_inout.svg", borderColor, 1))
                        .build()
        );
    }

}

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
package org.eclipse.capella.diagram.sab.view.nodes.function;

import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.common.view.nodes.ImageNodeStyleDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.SABViewConstants;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.eclipse.syson.util.ViewConstants;

/**
 * Function port border node for SAB.
 *
 * @author mbats
 */
public class FunctionPortNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "SABFunctionPortNodeDescription";

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
                .name(NODE_DESCRIPTION_NAME)
                .semanticCandidatesExpression(ServiceMethod.of0(SAQueryService::getFunctionPorts).aqlSelf())
                .style(this.createPortUnsetNodeStyle())
                .conditionalStyles(this.createPortUsageConditionalNodeStyles().toArray(ConditionalNodeStyle[]::new))
                .userResizable(UserResizableDirection.NONE)
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
            nodeDescription.setPalette(new FunctionPortPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));
        });
    }

    private NodeStyleDescription createPortUnsetNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .borderRadius(0)
                .background(this.colorProvider.getColor(SABViewConstants.COMPONENT_PORT_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(SABViewConstants.COMPONENT_PORT_BORDER_COLOR))
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
                        .build());
    }
}

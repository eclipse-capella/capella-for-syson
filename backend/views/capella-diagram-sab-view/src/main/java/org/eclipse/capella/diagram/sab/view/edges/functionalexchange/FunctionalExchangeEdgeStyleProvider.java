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
package org.eclipse.capella.diagram.sab.view.edges.functionalexchange;

import java.util.Objects;

import org.eclipse.capella.diagram.sab.view.SABViewConstants;
import org.eclipse.capella.model.services.system.analysis.SARepresentationQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.ConditionalEdgeStyle;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.ViewConstants;

/**
 * Style provider for SAB functional exchange edges.
 *
 * @author mbats
 */
public class FunctionalExchangeEdgeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public FunctionalExchangeEdgeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public EdgeStyle createEdgeStyle() {
        return this.diagramBuilderHelper.newEdgeStyle()
                .fontSize(8)
                .borderSize(0)
                .color(this.colorProvider.getColor(SABViewConstants.FUNCTIONAL_EXCHANGE_BACKGROUND_COLOR))
                .edgeWidth(2)
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }

    public ConditionalEdgeStyle[] createConditionalEdgeStyles() {
        return new ConditionalEdgeStyle[] { this.createSeveralFunctionalChainsConditionalEdgeStyle(), this.createFunctionalChainConditionalEdgeStyle(0),
                this.createFunctionalChainConditionalEdgeStyle(1), this.createFunctionalChainConditionalEdgeStyle(2) };
    }

    private ConditionalEdgeStyle createSeveralFunctionalChainsConditionalEdgeStyle() {
        return this.diagramBuilderHelper.newConditionalEdgeStyle()
                .condition(ServiceMethod.<SARepresentationQueryService, FlowUsage, IEditingContext, DiagramContext>of2(SARepresentationQueryService::getImpliedInFunctionalChainIndex)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = 99")
                .edgeWidth(3)
                .fontSize(8)
                .borderSize(0)
                .color(this.colorProvider.getColor(ViewConstants.DEFAULT_EDGE_COLOR))
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }

    private ConditionalEdgeStyle createFunctionalChainConditionalEdgeStyle(int index) {
        return this.diagramBuilderHelper.newConditionalEdgeStyle()
                .condition(ServiceMethod.<SARepresentationQueryService, FlowUsage, IEditingContext, DiagramContext>of2(SARepresentationQueryService::getImpliedInFunctionalChainIndex)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = " + index)
                .edgeWidth(3)
                .fontSize(8)
                .borderSize(0)
                .color(this.colorProvider.getColor(SABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR + "_" + index))
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_ARROW)
                .build();
    }
}

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
package org.eclipse.capella.diagram.lab.view.edges.functionalexchange;

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationQueryService;
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
 * Provide Style for Functional Exchange edges.
 *
 * @author fbarbin
 */
public class FunctionalExchangeEdgeStyleProvider {

    protected final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public FunctionalExchangeEdgeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public EdgeStyle createEdgeStyle() {
        return this.diagramBuilderHelper.newEdgeStyle()
                .fontSize(12)
                .borderSize(0)
                .color(this.colorProvider.getColor(LABViewConstants.FUNCTIONAL_EXCHANGE_BACKGROUND_COLOR))
                .edgeWidth(2)
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.NONE)
                .build();
    }

    public ConditionalEdgeStyle createSeveralFCImpliedInConditionalEdgeStyle() {
        return this.diagramBuilderHelper.newConditionalEdgeStyle()
                .condition(ServiceMethod.<TransverseRepresentationQueryService, FlowUsage, IEditingContext, DiagramContext> of2(TransverseRepresentationQueryService::getImpliedInFunctionalChainIndex)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = 99")
                .edgeWidth(3)
                .fontSize(12)
                .borderSize(0)
                .color(this.colorProvider.getColor(ViewConstants.DEFAULT_EDGE_COLOR))
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.NONE)
                .build();
    }

    public ConditionalEdgeStyle createFCImpliedInConditionalEdgeStyle(int index) {
        return this.diagramBuilderHelper.newConditionalEdgeStyle()
                .condition(ServiceMethod.<TransverseRepresentationQueryService, FlowUsage, IEditingContext, DiagramContext> of2(TransverseRepresentationQueryService::getImpliedInFunctionalChainIndex)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = " + index)
                .edgeWidth(3)
                .fontSize(12)
                .borderSize(0)
                .color(this.colorProvider.getColor(LABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR + "_" + index))
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.NONE)
                .build();
    }

    public ConditionalEdgeStyle[] createConditionalEdgeStyles() {
        return new ConditionalEdgeStyle[] { this.createSeveralFCImpliedInConditionalEdgeStyle(), this.createFCImpliedInConditionalEdgeStyle(0), this.createFCImpliedInConditionalEdgeStyle(1),
                this.createFCImpliedInConditionalEdgeStyle(2) };
    }
}

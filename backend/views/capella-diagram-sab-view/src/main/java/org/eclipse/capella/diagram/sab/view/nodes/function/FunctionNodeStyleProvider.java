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

import java.util.Objects;

import org.eclipse.capella.diagram.sab.view.SABViewConstants;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide style for function nodes.
 *
 * @author mbats
 */
public class FunctionNodeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public FunctionNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createFunctionNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(SABViewConstants.FUNCTION_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(SABViewConstants.FUNCTION_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .build();
    }

    public ConditionalNodeStyle createSeveralFCImpliedInConditionalNodeStyle() {
        return this.diagramBuilderHelper.newConditionalNodeStyle()
                .condition(
                        ServiceMethod.<TransverseRepresentationQueryService, ActionUsage, IEditingContext, DiagramContext> of2(TransverseRepresentationQueryService::getImpliedInFunctionalChainIndex)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = 99")
                .style(this.createFunctionalChainFunctionNodeStyle(SABViewConstants.FUNCTION_BORDER_COLOR))
                .build();
    }

    public ConditionalNodeStyle createFCImpliedInConditionalNodeStyle(int index) {
        return this.diagramBuilderHelper.newConditionalNodeStyle()
                .condition(
                        ServiceMethod.<TransverseRepresentationQueryService, ActionUsage, IEditingContext, DiagramContext> of2(TransverseRepresentationQueryService::getImpliedInFunctionalChainIndex)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = " + index)
                .style(this.createFunctionalChainFunctionNodeStyle(SABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR + "_" + index))
                .build();
    }

    public NodeStyleDescription createFunctionalChainFunctionNodeStyle(String color) {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(SABViewConstants.FUNCTION_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(color))
                .borderRadius(8)
                .borderSize(4)
                .build();
    }

    public ConditionalNodeStyle[] createFunctionConditionalNodeStyles() {
        return new ConditionalNodeStyle[] { this.createSeveralFCImpliedInConditionalNodeStyle(), this.createFCImpliedInConditionalNodeStyle(0),
                this.createFCImpliedInConditionalNodeStyle(1),
                this.createFCImpliedInConditionalNodeStyle(2) };
    }
}

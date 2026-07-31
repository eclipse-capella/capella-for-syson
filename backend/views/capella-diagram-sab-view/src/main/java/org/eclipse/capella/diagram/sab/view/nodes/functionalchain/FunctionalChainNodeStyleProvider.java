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
package org.eclipse.capella.diagram.sab.view.nodes.functionalchain;

import java.util.Objects;

import org.eclipse.capella.diagram.sab.view.SABViewConstants;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides styles for SAB Functional Chain nodes.
 *
 * @author mbats
 */
public class FunctionalChainNodeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public FunctionalChainNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createNodeStyle() {
        return this.createFunctionalChainNodeStyle(SABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR);
    }

    public ConditionalNodeStyle[] createConditionalNodeStyles() {
        return new ConditionalNodeStyle[] { this.createFunctionalChainConditionalNodeStyle(0), this.createFunctionalChainConditionalNodeStyle(1),
                this.createFunctionalChainConditionalNodeStyle(2) };
    }

    private ConditionalNodeStyle createFunctionalChainConditionalNodeStyle(int index) {
        return this.diagramBuilderHelper.newConditionalNodeStyle()
                .condition(ServiceMethod.of2(TransverseRepresentationQueryService::getFunctionalChainIndexInDiagram)
                        .aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = " + index)
                .style(this.createFunctionalChainNodeStyle(SABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR + "_" + index))
                .build();
    }

    private NodeStyleDescription createFunctionalChainNodeStyle(String color) {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(color))
                .borderColor(this.colorProvider.getColor(SABViewConstants.FUNCTIONAL_CHAIN_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .build();
    }
}

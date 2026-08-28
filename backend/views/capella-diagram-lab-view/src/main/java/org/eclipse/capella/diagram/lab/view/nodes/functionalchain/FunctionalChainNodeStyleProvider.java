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
package org.eclipse.capella.diagram.lab.view.nodes.functionalchain;

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide Style for Functional Chain nodes.
 *
 * @author fbarbin
 */
public class FunctionalChainNodeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public FunctionalChainNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createComponentNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(LABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(LABViewConstants.FUNCTIONAL_CHAIN_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .build();
    }

    private ConditionalNodeStyle createFCConditionalNodeStyle(int index) {
        return this.diagramBuilderHelper.newConditionalNodeStyle()
                .condition(ServiceMethod.of2(TransverseRepresentationQueryService::getFunctionalChainIndexInDiagram).aqlSelf(IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT) + " = "
                        + index)
                .style(this.createFunctionalChainNodeStyle(LABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR + "_" + index))
                .build();
    }

    private NodeStyleDescription createFunctionalChainNodeStyle(String color) {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(color))
                .borderColor(this.colorProvider.getColor(LABViewConstants.FUNCTIONAL_CHAIN_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .build();
    }

    public ConditionalNodeStyle[] createConditionalNodeStyles() {
        return new ConditionalNodeStyle[] { this.createFCConditionalNodeStyle(0), this.createFCConditionalNodeStyle(1),
                this.createFCConditionalNodeStyle(2) };
    }

}

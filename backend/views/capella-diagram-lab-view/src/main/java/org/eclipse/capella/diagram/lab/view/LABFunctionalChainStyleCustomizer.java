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
package org.eclipse.capella.diagram.lab.view;

import java.util.Optional;

import org.eclipse.capella.diagram.customization.styles.FunctionalChainStyleConstants;
import org.eclipse.capella.diagram.customization.styles.api.IFunctionalChainStyleCustomizer;
import org.eclipse.capella.diagram.lab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.functionalchain.FunctionalChainNodeDescriptionProvider;
import org.eclipse.sirius.components.diagrams.ArrowStyle;
import org.eclipse.sirius.components.diagrams.EdgeStyle;
import org.eclipse.sirius.components.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.springframework.stereotype.Service;

/**
 * LAB diagram functional chain style customizer.
 *
 * @author Jerome Gout
 */
@Service
public class LABFunctionalChainStyleCustomizer implements IFunctionalChainStyleCustomizer {

    @Override
    public boolean canHandle(DiagramDescription description) {
        return LABViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getName());
    }

    @Override
    public boolean isFunctionalChainNodeDescription(NodeDescription description) {
        return FunctionalChainNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(description.getName());
    }

    @Override
    public boolean isFunctionNodeDescription(NodeDescription description) {
        return FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(description.getName());
    }

    @Override
    public boolean isFunctionalExchangeEdgeDescription(EdgeDescription description) {
        return FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME.equals(description.getName());
    }

    @Override
    public RectangularNodeStyle customizeFunctionStyle(RectangularNodeStyle style, Optional<String> chainColor) {
        return RectangularNodeStyle.newRectangularNodeStyle(style)
                .borderColor(chainColor.orElse(FunctionalChainStyleConstants.OVERLAP_COLOR))
                .borderSize(4)
                .borderRadius(8)
                .build();
    }

    @Override
    public EdgeStyle customizeFunctionalExchangeStyle(EdgeStyle style, Optional<String> chainColor) {
        return EdgeStyle.newEdgeStyle(style)
                .color(chainColor.orElse(FunctionalChainStyleConstants.OVERLAP_COLOR))
                .size(3)
                .targetArrow(ArrowStyle.None)
                .build();
    }
}


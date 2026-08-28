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
package org.eclipse.capella.diagram.ocb.view.edges.generalization;

import java.util.Objects;

import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ArrowStyle;
import org.eclipse.sirius.components.view.diagram.EdgeStyle;
import org.eclipse.sirius.components.view.diagram.LineStyle;
import org.eclipse.syson.util.ViewConstants;

/**
 * Provides the style of Operational Capability generalization edges.
 *
 * @author tbezierslafosse
 */
public class GeneralizationEdgeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public GeneralizationEdgeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public EdgeStyle createEdgeStyle() {
        return this.diagramBuilderHelper.newEdgeStyle()
                .borderSize(0)
                .color(this.colorProvider.getColor(ViewConstants.DEFAULT_EDGE_COLOR))
                .edgeWidth(1)
                .lineStyle(LineStyle.SOLID)
                .sourceArrowStyle(ArrowStyle.NONE)
                .targetArrowStyle(ArrowStyle.INPUT_CLOSED_ARROW)
                .build();
    }
}

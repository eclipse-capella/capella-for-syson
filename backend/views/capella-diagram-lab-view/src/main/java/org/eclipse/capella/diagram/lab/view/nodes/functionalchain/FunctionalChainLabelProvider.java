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
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.OutsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.OutsideLabelStyle;

/**
 * Provide Label for Functional chain nodes.
 *
 * @author fbarbin
 */
public class FunctionalChainLabelProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public FunctionalChainLabelProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public OutsideLabelStyle createOutsideLabelStyle() {
        return this.diagramBuilderHelper.newOutsideLabelStyle()
                .borderSize(0)
                .fontSize(12)
                .labelColor(this.colorProvider.getColor(LABViewConstants.FUNCTIONAL_CHAIN_LABEL_COLOR))
                .showIconExpression("aql:true")
                .labelIcon("/icons/full/obj16/FunctionalChain.svg")
                .build();
    }

    public OutsideLabelDescription createOutsideLabelDescription() {
        return this.diagramBuilderHelper.newOutsideLabelDescription()
                .labelExpression("aql:self.name")
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .textAlign(LabelTextAlign.CENTER)
                .style(this.createOutsideLabelStyle())
                .build();
    }

}

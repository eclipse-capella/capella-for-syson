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
package org.eclipse.capella.diagram.ocb.view.nodes.capability;

import java.util.Objects;

import org.eclipse.capella.diagram.ocb.view.OCBViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.OutsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.OutsideLabelStyle;

/**
 * Provides the external label of an Operational Capability.
 *
 * @author tbezierslafosse
 */
public class CapabilityLabelProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public CapabilityLabelProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public OutsideLabelDescription createOutsideLabelDescription() {
        return this.diagramBuilderHelper.newOutsideLabelDescription()
                .labelExpression("aql:self.name")
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .textAlign(LabelTextAlign.CENTER)
                .style(this.createOutsideLabelStyle())
                .build();
    }

    public InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("aql:'OC'")
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .position(InsideLabelPosition.MIDDLE_CENTER)
                .textAlign(LabelTextAlign.CENTER)
                .style(this.createInsideLabelStyle())
                .build();
    }

    private InsideLabelStyle createInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .bold(true)
                .borderSize(0)
                .fontSize(16)
                .labelColor(this.colorProvider.getColor(OCBViewConstants.CAPABILITY_LABEL_COLOR))
                .showIconExpression("aql:false")
                .withHeader(false)
                .build();
    }

    private OutsideLabelStyle createOutsideLabelStyle() {
        return this.diagramBuilderHelper.newOutsideLabelStyle()
                .borderSize(0)
                .fontSize(12)
                .labelColor(this.colorProvider.getColor(OCBViewConstants.CAPABILITY_LABEL_COLOR))
                .showIconExpression("aql:false")
                .labelIcon("/icons/full/obj16/Capability.svg")
                .build();
    }
}

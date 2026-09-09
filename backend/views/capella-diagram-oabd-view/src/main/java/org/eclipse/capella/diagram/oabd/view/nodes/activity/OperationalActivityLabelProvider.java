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
package org.eclipse.capella.diagram.oabd.view.nodes.activity;

import java.util.Objects;

import org.eclipse.capella.diagram.oabd.view.OABDViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;

/**
 * Provides the operational activity label.
 *
 * @author tbezierslafosse
 */
public class OperationalActivityLabelProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public OperationalActivityLabelProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("aql:self.name")
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .position(InsideLabelPosition.MIDDLE_CENTER)
                .style(this.createInsideLabelStyle())
                .build();
    }

    private InsideLabelStyle createInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .labelColor(this.colorProvider.getColor(OABDViewConstants.ACTIVITY_LABEL_COLOR))
                .showIconExpression("aql:true")
                .labelIcon("/icons/full/obj16/OperationalActivity.svg")
                .withHeader(true)
                .fontSize(12)
                .build();
    }
}

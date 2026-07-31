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
package org.eclipse.capella.diagram.lab.view.nodes.comment;

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;

/**
 * Provide Label for Comment nodes showing the body text.
 *
 * @author vkravchenko
 */
public class CommentLabelProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public CommentLabelProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .labelExpression("aql:self.body")
                .position(InsideLabelPosition.TOP_LEFT)
                .style(this.createInsideLabelStyle())
                .textAlign(LabelTextAlign.LEFT)
                .build();
    }

    private InsideLabelStyle createInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .fontSize(11)
                .italic(true)
                .labelColor(this.colorProvider.getColor(LABViewConstants.COMMENT_LABEL_COLOR))
                .showIconExpression("aql:false")
                .withHeader(false)
                .build();
    }
}

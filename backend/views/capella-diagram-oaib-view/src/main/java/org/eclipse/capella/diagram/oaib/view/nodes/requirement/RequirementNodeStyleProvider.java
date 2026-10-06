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
package org.eclipse.capella.diagram.oaib.view.nodes.requirement;

import java.util.Objects;

import org.eclipse.capella.diagram.oaib.view.OAIBViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

/**
 * Provides the style of Requirement nodes in OAIB diagrams.
 *
 * @author tbezierslafosse
 */
public class RequirementNodeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public RequirementNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createRequirementNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(OAIBViewConstants.REQUIREMENT_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(OAIBViewConstants.REQUIREMENT_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .childrenLayoutStrategy(this.diagramBuilderHelper.newListLayoutStrategyDescription().build())
                .build();
    }
}

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
package org.eclipse.capella.diagram.lab.view.nodes.component;

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

/**
 * Provide Style for Component nodes.
 *
 * @author frouene
 */
public class ComponentNodeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public ComponentNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createComponentNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(LABViewConstants.COMPONENT_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(LABViewConstants.COMPONENT_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .build();
    }

    public NodeStyleDescription createActorNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(LABViewConstants.ACTOR_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(LABViewConstants.ACTOR_BORDER_COLOR))
                .borderRadius(0)
                .borderSize(2)
                .build();
    }
}

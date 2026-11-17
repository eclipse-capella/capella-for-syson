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
package org.eclipse.capella.diagram.sfbd.view.nodes.function;

import java.util.Objects;

import org.eclipse.capella.diagram.sfbd.view.SFBDViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

/**
 * Provides the System Function node style.
 *
 * @author adieumegard
 */
public class SystemFunctionNodeStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public SystemFunctionNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createOperationalActivityNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(SFBDViewConstants.FUNCTION_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(SFBDViewConstants.FUNCTION_BORDER_COLOR))
                .borderSize(2)
                .borderRadius(0)
                .build();
    }
}

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
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;

/**
 * Provides the oval-like style of Operational Capability nodes.
 *
 * @author tbezierslafosse
 */
public class CapabilityNodeStyleProvider {

    private final IColorProvider colorProvider;

    private final DiagramBuilders diagramBuilderHelper;

    public CapabilityNodeStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createNodeStyle() {
        return this.diagramBuilderHelper.newRectangularNodeStyleDescription()
                .background(this.colorProvider.getColor(OCBViewConstants.CAPABILITY_BACKGROUND_COLOR))
                .borderColor(this.colorProvider.getColor(OCBViewConstants.CAPABILITY_BORDER_COLOR))
                .borderSize(2)
                .borderRadius(100)
                .build();
    }
}

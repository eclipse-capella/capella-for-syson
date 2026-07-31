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
package org.eclipse.capella.diagram.common.view.nodes;

import org.eclipse.sirius.components.view.UserColor;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.diagram.ImageNodeStyleDescription;

/**
 * Provide image node style description.
 *
 * @author frouene
 */
public class ImageNodeStyleDescriptionProvider {

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    public ImageNodeStyleDescription createImageNodeStyleDescription(String imagePath, UserColor borderColor, Integer borderSize) {
        return this.diagramBuilderHelper.newImageNodeStyleDescription()
                .borderColor(borderColor)
                .borderSize(borderSize)
                .borderRadius(0)
                .positionDependentRotation(true)
                .shape(imagePath)
                .build();
    }

}

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
package org.eclipse.capella.diagram.lab.view;

import org.eclipse.capella.diagram.common.view.ColorProvider;
import org.eclipse.capella.diagram.common.view.IViewDescriptionProvider;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * Register the LAB diagram in the application.
 *
 * @author frouene
 */
@Service
public class LABViewDescriptionProvider implements IViewDescriptionProvider {

    @Override
    public String getViewDiagramId() {
        return "LogicalArchitectureBlank";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new LABViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        IColorProvider colorProvider = new ColorProvider(view);
        view.getColorPalettes().add(this.createColorPalette());
        return colorProvider;
    }

    private ColorPalette createColorPalette() {
        var colorPalette = ViewFactory.eINSTANCE.createColorPalette();

        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.COMPONENT_BACKGROUND_COLOR, "#7A8CC8"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.COMPONENT_BORDER_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.COMPONENT_LABEL_COLOR, "#FFFFFF"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.DESCRIBES_BACKGROUND_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTION_BACKGROUND_COLOR, "#CBE5AC"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTION_BORDER_COLOR, "#046866"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTION_LABEL_COLOR, "#046866"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR_0, "#FF2F1A"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR_1, "#079459"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTIONAL_CHAIN_BACKGROUND_COLOR_2, "#01C1FF"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTIONAL_CHAIN_BORDER_COLOR, "#18340E"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTIONAL_CHAIN_LABEL_COLOR, "#000000"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.ACTOR_BACKGROUND_COLOR, "#BDEBFA"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.ACTOR_BORDER_COLOR, "#2D5FB2"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.ACTOR_LABEL_COLOR, "#162558"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.COMPONENT_PORT_BACKGROUND_COLOR, "#FFFFFF"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.COMPONENT_PORT_BORDER_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.FUNCTIONAL_EXCHANGE_BACKGROUND_COLOR, "#046866"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.COMPONENT_EXCHANGE_BACKGROUND_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.REQUIREMENT_BACKGROUND_COLOR, "#D8C3D6"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.REQUIREMENT_BORDER_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(LABViewConstants.REQUIREMENT_LABEL_COLOR, "#72496E"));

        return colorPalette;
    }

    private FixedColor createFixedColor(String name, String value) {
        var fixedColor = ViewFactory.eINSTANCE.createFixedColor();
        fixedColor.setName(name);
        fixedColor.setValue(value);

        return fixedColor;
    }
}

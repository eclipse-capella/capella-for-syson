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
package org.eclipse.capella.diagram.oab.view;

import org.eclipse.capella.diagram.common.view.IViewDescriptionProvider;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.syson.services.ColorProvider;
import org.springframework.stereotype.Service;

/**
 * Register the OAB diagram in the application.
 *
 * @author frouene
 */
@Service
public class OABViewDescriptionProvider implements IViewDescriptionProvider {

    @Override
    public String getViewDiagramId() {
        return "OperationalAnalysisBlank";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new OABViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        IColorProvider colorProvider = new ColorProvider(view);
        view.getColorPalettes().add(this.createColorPalette());
        return colorProvider;
    }

    private ColorPalette createColorPalette() {
        var colorPalette = ViewFactory.eINSTANCE.createColorPalette();

        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.COMPONENT_BACKGROUND_COLOR, "#ddddc8"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.COMPONENT_BORDER_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.COMPONENT_LABEL_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.DESCRIBES_BACKGROUND_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.COMPONENT_EXCHANGE_BACKGROUND_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.REQUIREMENT_BACKGROUND_COLOR, "#D8C3D6"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.REQUIREMENT_BORDER_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(OABViewConstants.REQUIREMENT_LABEL_COLOR, "#72496E"));

        return colorPalette;
    }

    private FixedColor createFixedColor(String name, String value) {
        var fixedColor = ViewFactory.eINSTANCE.createFixedColor();
        fixedColor.setName(name);
        fixedColor.setValue(value);

        return fixedColor;
    }
}

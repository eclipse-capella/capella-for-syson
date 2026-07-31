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
package org.eclipse.capella.diagram.ddv.view.view;

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
 * Register the FunctionalContext diagram in the application.
 *
 * @author fbarbin
 */
@Service
public class FunctionalContextViewDescriptionProvider implements IViewDescriptionProvider {

    @Override
    public String getViewDiagramId() {
        return "FunctionalContext";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new FunctionalContextViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        IColorProvider colorProvider = new ColorProvider(view);
        view.getColorPalettes().add(this.createColorPalette());
        return colorProvider;
    }

    private ColorPalette createColorPalette() {
        var colorPalette = ViewFactory.eINSTANCE.createColorPalette();

        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.COMPONENT_BACKGROUND_COLOR, "#7A8CC8"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.COMPONENT_BORDER_COLOR, "#1C2A3F"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.COMPONENT_LABEL_COLOR, "#FFFFFF"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.DESCRIBES_BACKGROUND_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.ROOT_FUNCTION_BACKGROUND_COLOR, "#CBE5AC"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.ROOT_FUNCTION_BORDER_COLOR, "#046866"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.FUNCTION_LABEL_COLOR, "#046866"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.CONTEXTUAL_FUNCTION_BACKGROUND_COLOR, "#BDEBFA"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.CONTEXTUAL_FUNCTION_BORDER_COLOR, "#2D5FB2"));
        colorPalette.getColors().add(this.createFixedColor(DDVViewConstants.FUNCTIONAL_EXCHANGE_BACKGROUND_COLOR, "#1C2A3F"));

        return colorPalette;
    }

    private FixedColor createFixedColor(String name, String value) {
        var fixedColor = ViewFactory.eINSTANCE.createFixedColor();
        fixedColor.setName(name);
        fixedColor.setValue(value);

        return fixedColor;
    }
}

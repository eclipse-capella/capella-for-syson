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
package org.eclipse.capella.diagram.sfbd.view;

import org.eclipse.capella.common.view.diagram.services.api.IDiagramViewDescriptionProvider;
import org.eclipse.sirius.components.view.ColorPalette;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.syson.services.ColorProvider;
import org.springframework.stereotype.Service;

/**
 * Registers the System Function Break Down diagram in the application.
 *
 * @author adieumegard
 */
@Service
public class SFBDViewDescriptionProvider implements IDiagramViewDescriptionProvider {
    @Override
    public String getViewDiagramId() {
        return "SystemFunctionBreakDown";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new SFBDViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        IColorProvider colorProvider = new ColorProvider(view);
        view.getColorPalettes().add(this.createColorPalette());
        return colorProvider;
    }

    private ColorPalette createColorPalette() {
        var colorPalette = ViewFactory.eINSTANCE.createColorPalette();
        colorPalette.getColors().add(this.createFixedColor(SFBDViewConstants.FUNCTION_BACKGROUND_COLOR, "#CBE5AC"));
        colorPalette.getColors().add(this.createFixedColor(SFBDViewConstants.FUNCTION_BORDER_COLOR, "#046866"));
        colorPalette.getColors().add(this.createFixedColor(SFBDViewConstants.FUNCTION_LABEL_COLOR, "#046866"));
        return colorPalette;
    }

    private FixedColor createFixedColor(String name, String value) {
        var fixedColor = ViewFactory.eINSTANCE.createFixedColor();
        fixedColor.setName(name);
        fixedColor.setValue(value);
        return fixedColor;
    }
}

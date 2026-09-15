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
package org.eclipse.capella.diagram.oabd.view;

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
 * Registers the Operational Activity Break Down diagram in the application.
 *
 * @author tbezierslafosse
 */
@Service
public class OABDViewDescriptionProvider implements IViewDescriptionProvider {
    @Override
    public String getViewDiagramId() {
        return "OperationalActivityBreakDown";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new OABDViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        IColorProvider colorProvider = new ColorProvider(view);
        view.getColorPalettes().add(this.createColorPalette());
        return colorProvider;
    }

    private ColorPalette createColorPalette() {
        var colorPalette = ViewFactory.eINSTANCE.createColorPalette();
        colorPalette.getColors().add(this.createFixedColor(OABDViewConstants.ACTIVITY_BACKGROUND_COLOR, "#F7DA74"));
        colorPalette.getColors().add(this.createFixedColor(OABDViewConstants.ACTIVITY_BORDER_COLOR, "#5B4040"));
        colorPalette.getColors().add(this.createFixedColor(OABDViewConstants.ACTIVITY_LABEL_COLOR, "#000000"));
        return colorPalette;
    }

    private FixedColor createFixedColor(String name, String value) {
        var fixedColor = ViewFactory.eINSTANCE.createFixedColor();
        fixedColor.setName(name);
        fixedColor.setValue(value);
        return fixedColor;
    }
}

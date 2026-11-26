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
package org.eclipse.capella.diagram.mcb.view;

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
 * Registers the Missions Capabilities Blank diagram in the application.
 *
 * @author tbezierslafosse
 */
@Service
public class MCBViewDescriptionProvider implements IDiagramViewDescriptionProvider {

    @Override
    public String getViewDiagramId() {
        return "MissionsCapabilitiesBlank";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new MCBViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        IColorProvider colorProvider = new ColorProvider(view);
        view.getColorPalettes().add(this.createColorPalette());
        return colorProvider;
    }

    private ColorPalette createColorPalette() {
        var colorPalette = ViewFactory.eINSTANCE.createColorPalette();
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.REQUIREMENT_BACKGROUND_COLOR, "#D8C3D6"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.REQUIREMENT_BORDER_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.REQUIREMENT_LABEL_COLOR, "#72496E"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.ACTOR_BACKGROUND_COLOR, "#D0F1FE"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.ACTOR_BORDER_COLOR, "#494997"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.ACTOR_LABEL_COLOR, "#000000"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.CAPABILITY_BACKGROUND_COLOR, "#F7DA74"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.CAPABILITY_BORDER_COLOR, "#5B4040"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.CAPABILITY_LABEL_COLOR, "#000000"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.MISSION_BACKGROUND_COLOR, "#F7DA74"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.MISSION_BORDER_COLOR, "#5B4040"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.MISSION_LABEL_COLOR, "#000000"));
        colorPalette.getColors().add(this.createFixedColor(MCBViewConstants.RELATION_COLOR, "#a9a9a9"));
        return colorPalette;
    }

    private FixedColor createFixedColor(String name, String value) {
        var fixedColor = ViewFactory.eINSTANCE.createFixedColor();
        fixedColor.setName(name);
        fixedColor.setValue(value);
        return fixedColor;
    }
}

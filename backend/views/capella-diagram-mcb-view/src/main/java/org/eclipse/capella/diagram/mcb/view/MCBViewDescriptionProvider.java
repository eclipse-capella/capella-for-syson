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

import org.eclipse.capella.diagram.common.view.IViewDescriptionProvider;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.syson.services.ColorProvider;
import org.springframework.stereotype.Service;

/**
 * Registers the Mission and Capabilities Blank diagram in the application.
 *
 * @author tbezierslafosse
 */
@Service
public class MCBViewDescriptionProvider implements IViewDescriptionProvider {

    @Override
    public String getViewDiagramId() {
        return "MissionAndCapabilitiesBlank";
    }

    @Override
    public IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new MCBViewDiagramDescriptionProvider();
    }

    @Override
    public IColorProvider getColorProvider(View view) {
        return new ColorProvider(view);
    }
}

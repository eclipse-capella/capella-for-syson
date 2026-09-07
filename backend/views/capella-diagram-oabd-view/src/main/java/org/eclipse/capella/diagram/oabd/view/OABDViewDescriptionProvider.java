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
import org.eclipse.sirius.components.view.View;
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
        return new ColorProvider(view);
    }
}

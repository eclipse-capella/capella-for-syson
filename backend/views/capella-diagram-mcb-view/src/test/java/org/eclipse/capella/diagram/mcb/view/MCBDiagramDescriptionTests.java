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

import org.eclipse.capella.diagram.common.view.ColorProvider;
import org.eclipse.capella.tests.diagrams.AbstractEditableDiagramDescriptionTests;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.junit.jupiter.api.Disabled;

/**
 * Checks the structure of the Missions Capabilities Blank diagram.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramDescriptionTests extends AbstractEditableDiagramDescriptionTests {

    @Override
    protected IColorProvider getColorProvider(View view) {
        return new ColorProvider(view);
    }

    @Override
    protected IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new MCBViewDiagramDescriptionProvider();
    }

    @Override
    protected IJavaServiceProvider getJavaServiceProvider() {
        return new MCBViewJavaServiceProvider();
    }

    @Disabled("Disabled until MCB has a diagram palette")
    @Override
    public void diagramHasRelatedElementsTools() {
        super.diagramHasRelatedElementsTools();
    }
}

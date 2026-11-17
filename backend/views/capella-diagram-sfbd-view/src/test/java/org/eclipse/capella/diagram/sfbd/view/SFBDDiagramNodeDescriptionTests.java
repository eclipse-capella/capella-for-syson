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

import org.eclipse.capella.diagram.common.view.ColorProvider;
import org.eclipse.capella.diagram.sfbd.view.nodes.function.SystemFunctionNodeDescriptionProvider;
import org.eclipse.capella.tests.diagrams.AbstractDiagramNodeDescriptionTests;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;

/**
 * Checks specific nodes structure on the System Function Break Down diagram.
 *
 * @author adieumegard
 */
public class SFBDDiagramNodeDescriptionTests extends AbstractDiagramNodeDescriptionTests {

    @Override
    protected IColorProvider getColorProvider(View view) {
        return new ColorProvider(view);
    }

    @Override
    protected IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new SFBDViewDiagramDescriptionProvider();
    }

    @Override
    protected String getFunctionNodeDescriptionName() {
        return SystemFunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME;
    }

    @Override
    protected String getComponentNodeDescriptionName() {
        return "";
    }

    @Override
    protected DiagramFamilyEnum getDiagramFamily() {
        return DiagramFamilyEnum.BD;
    }
}

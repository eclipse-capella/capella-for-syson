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

import org.eclipse.capella.diagram.common.view.ColorProvider;
import org.eclipse.capella.diagram.oab.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oab.view.nodes.component.EntityComponentNodeDescriptionProvider;
import org.eclipse.capella.tests.diagrams.AbstractDiagramNodeDescriptionTests;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;

/**
 * Checks specific nodes structure on the Operational Activity Blank diagram.
 *
 * @author adieumegard
 */
public class OABDiagramNodeDescriptionTests extends AbstractDiagramNodeDescriptionTests {

    @Override
    protected IColorProvider getColorProvider(View view) {
        return new ColorProvider(view);
    }

    @Override
    protected IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new OABViewDiagramDescriptionProvider();
    }

    @Override
    protected String getFunctionNodeDescriptionName() {
        return OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME;
    }

    @Override
    protected String getComponentNodeDescriptionName() {
        return EntityComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME;
    }

    @Override
    protected DiagramFamilyEnum getDiagramFamily() {
        return DiagramFamilyEnum.AB;
    }
}

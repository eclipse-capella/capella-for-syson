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

import org.eclipse.capella.diagram.sfbd.view.nodes.function.SystemFunctionToolProvider;
import org.eclipse.capella.diagram.sfbd.view.nodes.requirement.RequirementToolProvider;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.DropToolBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;

/**
 * Provides the SFBD diagram palette.
 *
 * @author adieumegard
 */
public class SFBDDiagramPaletteProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public SFBDDiagramPaletteProvider(DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = new ViewBuilders();
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropTool(new DropToolBuilder().build())
                .nodeTools(new SystemFunctionToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewSystemFunctionNodeTool(cache),
                        new RequirementToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewRequirementNodeTool(cache))
                .build();
    }
}

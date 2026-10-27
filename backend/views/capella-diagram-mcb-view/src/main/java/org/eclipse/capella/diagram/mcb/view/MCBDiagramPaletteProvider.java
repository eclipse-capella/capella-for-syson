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

import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.DropToolBuilder;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;

/**
 * Diagram-level creation tools for MCB.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramPaletteProvider {

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        var tools = new MCBNodeToolProvider();
        return this.diagramBuilders.newDiagramPalette()
                .nodeTools(tools.createRequirementTool(cache))
                .dropTool(new DropToolBuilder().build()) // To be added later
                .build();
    }

}

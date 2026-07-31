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
package org.eclipse.capella.diagram.sab.view;

import org.eclipse.capella.diagram.sab.view.nodes.actor.SystemActorToolProvider;
import org.eclipse.capella.diagram.sab.view.nodes.functionalchain.FunctionalChainToolProvider;
import org.eclipse.capella.diagram.sab.view.nodes.requirement.RequirementToolProvider;
import org.eclipse.capella.model.services.system.analysis.SARepresentationDropServices;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DropTool;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;

/**
 * Provide the minimal palette for the SAB diagram.
 *
 * @author mbats
 */
public class SABDiagramPaletteProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public SABDiagramPaletteProvider(DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = new ViewBuilders();
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropTool(this.createDropFromExplorerTool())
                .nodeTools(
                        new SystemActorToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewSystemActorNodeTool(cache),
                        new RequirementToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewRequirementNodeTool(cache),
                        new FunctionalChainToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewFunctionalChainNodeTool(cache))
                .build();
    }

    public DropTool createDropFromExplorerTool() {
        var dropElementFromExplorer = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of4(SARepresentationDropServices::dropIntoDiagramFromExplorer).aqlSelf(Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT,
                        DiagramContext.DIAGRAM_CONTEXT, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));

        return this.diagramBuilderHelper.newDropTool()
                .name("Drop from Explorer")
                .body(dropElementFromExplorer.build())
                .build();
    }
}

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
package org.eclipse.capella.diagram.oaib.view;

import org.eclipse.capella.diagram.oaib.view.nodes.activity.OperationalActivityToolProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.process.OperationalProcessToolProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.requirement.RequirementToolProvider;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationDropServices;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DropTool;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the creation tools of an OAIB diagram.
 *
 * @author tbezierslafosse
 */
public class OAIBDiagramPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper = new ViewBuilders();

    public OAIBDiagramPaletteProvider(DiagramBuilders diagramBuilderHelper) {
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropTool(this.createDropFromExplorerTool())
                .nodeTools(
                        new OperationalActivityToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewOperationalActivityNodeTool(cache),
                        new OperationalProcessToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewOperationalProcessNodeTool(cache),
                        new RequirementToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewRequirementNodeTool(cache))
                .build();
    }

    private DropTool createDropFromExplorerTool() {
        return this.diagramBuilderHelper.newDropTool()
                .name("Drop from Explorer")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of4(OARepresentationDropServices::dropIntoDiagramFromExplorer)
                                .aqlSelf(Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT,
                                        ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE))
                        .build())
                .build();
    }
}

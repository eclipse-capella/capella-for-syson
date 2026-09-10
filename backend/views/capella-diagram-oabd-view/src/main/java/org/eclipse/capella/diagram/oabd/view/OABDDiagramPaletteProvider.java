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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.oabd.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.activity.OperationalActivityToolProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.requirement.RequirementToolProvider;
import org.eclipse.capella.diagram.oabd.view.services.OABDRepresentationDropService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.DropTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;

/**
 * Provide the palette for the OABD diagram.
 *
 * @author tbezierslafosse
 */
public class OABDDiagramPaletteProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public OABDDiagramPaletteProvider(DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = new ViewBuilders();
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropNodeTool(this.createDropFromDiagramTool(cache))
                .dropTool(this.createDropFromExplorerTool())
                .nodeTools(new OperationalActivityToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewOperationalActivityNodeTool(cache),
                        new RequirementToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewRequirementNodeTool(cache))
                .build();
    }

    public DropTool createDropFromExplorerTool() {
        var dropElementFromExplorer = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of4(OABDRepresentationDropService::dropIntoDiagramFromExplorer)
                        .aqlSelf(Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));

        return this.diagramBuilderHelper.newDropTool()
                .name("Drop from Explorer")
                .body(dropElementFromExplorer.build())
                .build();
    }

    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDropNodeTool()
                .name("Drop from Diagram")
                .acceptedNodeTypes(this.getDroppableNodes(cache).toArray(NodeDescription[]::new))
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression("aql:droppedElement")
                        .build())
                .build();
    }

    private List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        var droppableNodes = new ArrayList<NodeDescription>();
        cache.getNodeDescription(OperationalActivityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        return droppableNodes;
    }
}

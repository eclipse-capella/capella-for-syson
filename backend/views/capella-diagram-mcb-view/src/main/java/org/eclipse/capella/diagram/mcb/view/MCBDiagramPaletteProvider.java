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

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Diagram-level creation and reveal tools for MCB.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramPaletteProvider {

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    private final ViewBuilders viewBuilders = new ViewBuilders();

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        var tools = new MCBNodeToolProvider();
        return this.diagramBuilders.newDiagramPalette()
                .nodeTools(tools.createActorTool(cache), tools.createMissionTool(cache), tools.createCapabilityTool(cache), tools.createRequirementTool(cache))
                .dropTool(this.diagramBuilders.newDropTool().name("Drop from Explorer")
                        .body(this.viewBuilders.newChangeContext()
                                .expression(ServiceMethod.of4(MCBViewDropService::dropFromExplorer).aqlSelf(Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT,
                                        DiagramContext.DIAGRAM_CONTEXT, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE)).build()).build())
                .dropNodeTool(this.diagramBuilders.newDropNodeTool().name("Drop from Diagram")
                        .acceptedNodeTypes(cache.getNodeDescription("SystemActorNodeDescription").orElseThrow(), cache.getNodeDescription("MissionNodeDescription").orElseThrow(),
                                cache.getNodeDescription("CapabilityNodeDescription").orElseThrow(), cache.getNodeDescription("RequirementNodeDescription").orElseThrow())
                        .body(this.viewBuilders.newChangeContext().expression(ServiceMethod.of4(MCBViewDropService::dropOnBackground)
                                .aql("droppedElement", "droppedNode", IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT,
                                        ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE)).build()).build())
                .build();
    }

    public DropNodeTool createActorDropTool(IViewDiagramElementFinder cache) {
        return this.diagramBuilders.newDropNodeTool().name("Drop from Diagram")
                .acceptedNodeTypes(cache.getNodeDescription("SystemActorNodeDescription").orElseThrow())
                .body(this.viewBuilders.newChangeContext().expression(ServiceMethod.of6(MCBViewDropService::dropIntoActor)
                        .aql("droppedElement", "droppedNode", "targetElement", "targetNode", IEditingContext.EDITING_CONTEXT,
                                DiagramContext.DIAGRAM_CONTEXT, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE)).build()).build();
    }
}

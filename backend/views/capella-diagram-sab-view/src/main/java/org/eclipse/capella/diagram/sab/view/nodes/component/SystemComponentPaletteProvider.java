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
package org.eclipse.capella.diagram.sab.view.nodes.component;

import java.util.Objects;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.diagram.sab.view.edges.componentexchange.ComponentExchangeToolProvider;
import org.eclipse.capella.diagram.sab.view.nodes.actor.SystemActorNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.componentport.ComponentPortToolProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionToolProvider;
import org.eclipse.capella.model.services.system.analysis.SARepresentationDropServices;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides palette for SAB System Component nodes.
 *
 * @author mbats
 */
public class SystemComponentPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    public SystemComponentPaletteProvider(DiagramBuilders diagramBuilderHelper, NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = new ViewBuilders();
        this.nodeDeleteFromDiagramToolProvider = Objects.requireNonNull(nodeDeleteFromDiagramToolProvider);
    }

    public NodePalette createNodePalette(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf())
                        .build());
        var labelEditTool = this.diagramBuilderHelper.newLabelEditTool()
                .name("Edit")
                .initialDirectEditLabelExpression(ServiceMethod.<DiagramQueryLabelService, Element>of0(DiagramQueryLabelService::getDefaultInitialDirectEditLabel).aqlSelf())
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(DiagramMutationLabelService::directEditNode).aqlSelf("newLabel"))
                        .build());
        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .labelEditTool(labelEditTool.build())
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .dropNodeTool(this.createDropFromDiagramTool(cache))
                .nodeTools(
                        new SystemComponentToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewSystemComponentNodeTool(cache),
                        new FunctionToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewFunctionNodeTool(cache),
                        new ComponentPortToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewInputComponentPortNodeTool(cache),
                        new ComponentPortToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewOutputComponentPortNodeTool(cache),
                        new ComponentPortToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewInOutComponentPortNodeTool(cache))
                .edgeTools(new ComponentExchangeToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewComponentExchangeTool(cache))
                .build();
    }

    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
        var dropElementFromDiagram = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of6(SARepresentationDropServices::dropIntoComponentFromDiagram)
                        .aql("droppedElement", "droppedNode", "targetElement", "targetNode", IEditingContext.EDITING_CONTEXT,
                                DiagramContext.DIAGRAM_CONTEXT, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));
        return this.diagramBuilderHelper.newDropNodeTool()
                .name("Drop from Diagram")
                .acceptedNodeTypes(this.getDroppableNodes(cache).toArray(NodeDescription[]::new))
                .body(dropElementFromDiagram.build())
                .build();
    }

    private java.util.List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        var droppableNodes = new java.util.ArrayList<NodeDescription>();
        cache.getNodeDescription(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        cache.getNodeDescription(SystemComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        return droppableNodes;
    }
}

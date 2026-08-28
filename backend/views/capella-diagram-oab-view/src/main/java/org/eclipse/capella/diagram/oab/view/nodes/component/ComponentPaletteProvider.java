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
package org.eclipse.capella.diagram.oab.view.nodes.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.diagram.oab.view.edges.componentexchange.CommunicationMeanComponentExchangeToolProvider;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationDropServices;
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
import org.eclipse.syson.diagram.common.view.DiagramDefaultToolsFactory;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide Palette for Component nodes.
 *
 * @author frouene
 */
public class ComponentPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    private final DiagramDefaultToolsFactory diagramDefaultToolsFactory;

    public ComponentPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper, NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.nodeDeleteFromDiagramToolProvider = Objects.requireNonNull(nodeDeleteFromDiagramToolProvider);
        this.diagramDefaultToolsFactory = new DiagramDefaultToolsFactory();
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
                        .expression(ServiceMethod.<DiagramMutationLabelService, Element, String>of1(DiagramMutationLabelService::directEdit)
                                .aqlSelf("newLabel"))
                        .build());

        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .labelEditTool(labelEditTool.build())
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .dropNodeTool(this.createDropFromDiagramTool(cache))
                .nodeTools(
                        new EntityComponentToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewEntityComponentNodeTool(cache),
                        new EntityComponentToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewActorComponentNodeTool(cache))
                .edgeTools(new CommunicationMeanComponentExchangeToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewComponentExchangeTool(cache))
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }

    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
        var dropElementFromDiagram = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of6(OARepresentationDropServices::dropIntoComponentFromDiagram)
                        .aql("droppedElement",
                                "droppedNode", "targetElement", "targetNode", IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT,
                                ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE
                        ));
        return this.diagramBuilderHelper.newDropNodeTool()
                .name("Drop from Diagram")
                .acceptedNodeTypes(this.getDroppableNodes(cache).toArray(NodeDescription[]::new))
                .body(dropElementFromDiagram.build())
                .build();
    }

    private List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        var droppableNodes = new ArrayList<NodeDescription>();
        cache.getNodeDescription(EntityComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        return droppableNodes;
    }


}

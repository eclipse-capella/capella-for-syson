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
package org.eclipse.capella.diagram.ddv.view.view.nodes.function;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.model.services.logical.architecture.LAMutationService;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationDropServices;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.util.AQLConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provide Palette for Function nodes.
 *
 * @author fbarbin
 */
public class RootFunctionPaletteProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    private final DefaultToolsFactory defaultToolsFactory;

    public RootFunctionPaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper, NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.nodeDeleteFromDiagramToolProvider = Objects.requireNonNull(nodeDeleteFromDiagramToolProvider);
        this.defaultToolsFactory = new DefaultToolsFactory();
    }

    public NodePalette createNodePalette(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(LAMutationService::deleteFunction).aqlSelf())
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
                .nodeTools(this.createNewFunctionNodeTool(cache))
                .quickAccessTools(this.nodeDeleteFromDiagramToolProvider.getDeleteFromDiagramTool())
                .dropNodeTool(this.createDropFromDiagramTool(cache))
                .toolSections(this.defaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }

    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
        var dropElementFromDiagram = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of6(LARepresentationDropServices::dropIntoFunctionFromDiagram)
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
        cache.getNodeDescription(RootFunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        return droppableNodes;
    }

    private NodeTool createNewFunctionNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Function")
                .iconURLsExpression("/icons/full/obj16/LogicalFunction.svg");

        cache.getNodeDescription(RootFunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {

            nodeToolBuilder.body(this.viewBuilderHelper.newChangeContext()
                    .expression(ServiceMethod.of0(LARepresentationMutationService::createNewFunctionInFunction).aqlSelf())
                    .children(this.diagramBuilderHelper.newCreateView()
                            .containmentKind(NodeContainmentKind.CHILD_NODE)
                            .elementDescription(nodeDescription)
                            .parentViewExpression("aql:selectedNode")
                            .semanticElementExpression(AQLConstants.AQL_SELF)
                            .variableName("newInstanceView").build())
                    .build());
        });

        return nodeToolBuilder.build();
    }
}

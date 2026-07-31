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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.lab.view.nodes.comment.CommentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.comment.CommentToolProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentToolProvider;
import org.eclipse.capella.diagram.lab.view.nodes.functionalchain.FunctionalChainToolProvider;
import org.eclipse.capella.diagram.lab.view.nodes.packagenode.PackageToolProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementToolProvider;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationDropServices;
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
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide Palette for LAB diagram.
 *
 * @author frouene
 */
public class LABDiagramPaletteProvider {

    private static final LABDescriptionNameGenerator NAME_GENERATOR = new LABDescriptionNameGenerator();

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public LABDiagramPaletteProvider(DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = new ViewBuilders();
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newDiagramPalette()
                .dropNodeTool(this.createDropFromDiagramTool(cache))
                .dropTool(this.createDropFromExplorerTool())
                .nodeTools(new ComponentToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewComponentNodeTool(cache),
                        new ComponentToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewActorComponentNodeTool(cache),
                        new FunctionalChainToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewFunctionalChainNodeTool(cache),
                        new PackageToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewPackageNodeTool(cache),
                        new RequirementToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewRequirementNodeTool(cache),
                        new CommentToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewCommentNodeTool(cache))
                .build();
    }

    public DropTool createDropFromExplorerTool() {
        var dropElementFromExplorer = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of4(LARepresentationDropServices::dropIntoDiagramFromExplorer).aqlSelf(Node.SELECTED_NODE, IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT,
                        ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));

        return this.diagramBuilderHelper.newDropTool()
                .name("Drop from Explorer")
                .body(dropElementFromExplorer.build())
                .build();
    }

    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
        var dropElementFromDiagram = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of5(LARepresentationDropServices::dropIntoDiagram)
                        .aql("droppedElement",
                                "droppedNode", "targetNode", IEditingContext.EDITING_CONTEXT, DiagramContext.DIAGRAM_CONTEXT,
                                ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE));
        return this.diagramBuilderHelper.newDropNodeTool()
                .name("Drop from Diagram")
                .acceptedNodeTypes(this.getDroppableNodes(cache).toArray(NodeDescription[]::new))
                .body(dropElementFromDiagram.build())
                .build();
    }

    private List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        var droppableNodes = new ArrayList<NodeDescription>();
        cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        cache.getNodeDescription(CommentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);
        cache.getNodeDescription(NAME_GENERATOR.getNodeName(SysmlPackage.eINSTANCE.getPackage())).ifPresent(droppableNodes::add);
        return droppableNodes;
    }
}

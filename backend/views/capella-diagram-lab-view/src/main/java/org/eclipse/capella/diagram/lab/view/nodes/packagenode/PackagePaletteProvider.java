/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package org.eclipse.capella.diagram.lab.view.nodes.packagenode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.diagram.lab.view.LABDescriptionNameGenerator;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationDropServices;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DropNodeTool;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.diagram.common.view.DiagramDefaultToolsFactory;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide Palette for Package nodes in LAB diagram.
 * <p>
 * This palette provides drop tools for Package-to-Package and other droppable elements,
 * using LAB-specific drop services instead of SySON's expose-based services.
 * </p>
 *
 * @author vkravchenko
 */
public class PackagePaletteProvider {

    private static final LABDescriptionNameGenerator NAME_GENERATOR = new LABDescriptionNameGenerator();

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    private final NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider;

    private final DiagramDefaultToolsFactory diagramDefaultToolsFactory;

    public PackagePaletteProvider(DiagramBuilders diagramBuilderHelper, ViewBuilders viewBuilderHelper, NodeDeleteFromDiagramToolProvider nodeDeleteFromDiagramToolProvider) {
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
                .nodeTools(this.createNodeTools(cache))
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }

    private DropNodeTool createDropFromDiagramTool(IViewDiagramElementFinder cache) {
        var dropElementFromDiagram = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of6(LARepresentationDropServices::dropIntoPackageFromDiagram)
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

        // Allow dropping RequirementUsage into Package
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);

        // Allow dropping nested Package into Package
        cache.getNodeDescription(NAME_GENERATOR.getNodeName(SysmlPackage.eINSTANCE.getPackage())).ifPresent(droppableNodes::add);

        return droppableNodes;
    }

    private NodeTool[] createNodeTools(IViewDiagramElementFinder cache) {
        var tools = new ArrayList<NodeTool>();

        // Add "New Requirement" tool for creating requirements in the selected package
        tools.add(new PackageToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewRequirementNodeTool(cache));

        // Add "New Package" tool for creating nested packages
        tools.add(new PackageToolProvider(this.viewBuilderHelper, this.diagramBuilderHelper).createNewPackageNodeTool(cache));

        return tools.toArray(NodeTool[]::new);
    }
}

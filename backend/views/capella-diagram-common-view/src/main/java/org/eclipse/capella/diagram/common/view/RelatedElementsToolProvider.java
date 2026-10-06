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
package org.eclipse.capella.diagram.common.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.services.RelatedElementsService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the related elements tools shared by Capella diagrams.
 *
 * @author jgout
 */
public class RelatedElementsToolProvider {

    public static final String RELATED_ELEMENTS_TOOL_SECTION = "Related Elements";

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    private final ViewBuilders viewBuilders = new ViewBuilders();

    public DiagramToolSection createDiagramToolSection() {
        return this.diagramBuilders.newDiagramToolSection()
                .name(RELATED_ELEMENTS_TOOL_SECTION)
                .nodeTools(this.createContainedElementsTool(), this.createRecursiveContainedElementsTool())
                .build();
    }

    public NodeToolSection createNodeToolSection() {
        return this.diagramBuilders.newNodeToolSection()
                .name(RELATED_ELEMENTS_TOOL_SECTION)
                .nodeTools(this.createContainedElementsTool(), this.createRecursiveContainedElementsTool())
                .build();
    }

    public void addNodeToolSections(DiagramDescription diagramDescription) {
        var nodeDescriptions = new ArrayList<NodeDescription>();
        this.collectDescendants(diagramDescription.getNodeDescriptions(), nodeDescriptions);
        nodeDescriptions.forEach(nodeDescription -> {
            if (nodeDescription.getPalette() == null) {
                nodeDescription.setPalette(this.diagramBuilders.newNodePalette().build());
            }
            nodeDescription.getPalette().getToolSections().add(this.createNodeToolSection());
        });
    }

    private void collectDescendants(List<NodeDescription> nodes, List<NodeDescription> descendants) {
        for (NodeDescription node : nodes) {
            descendants.add(node);
            this.collectDescendants(node.getChildrenDescriptions(), descendants);
            this.collectDescendants(node.getBorderNodesDescriptions(), descendants);
        }
    }

    public NodeTool createContainedElementsTool() {
        return this.diagramBuilders.newNodeTool()
                .name("Show all contained elements")
                .iconURLsExpression("/icons/AddExistingElements.svg")
                .body(this.viewBuilders.newChangeContext()
                        .expression(ServiceMethod.of5(RelatedElementsService::showContainedElements).aqlSelf("false", IEditingContext.EDITING_CONTEXT,
                                DiagramContext.DIAGRAM_CONTEXT, Node.SELECTED_NODE, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE))
                        .build())
                .build();
    }

    public NodeTool createRecursiveContainedElementsTool() {
        return this.diagramBuilders.newNodeTool()
                .name("Show all contained elements recursively")
                .iconURLsExpression("/icons/AddExistingElementsRecursive.svg")
                .body(this.viewBuilders.newChangeContext()
                        .expression(ServiceMethod.of5(RelatedElementsService::showContainedElements).aqlSelf("true", IEditingContext.EDITING_CONTEXT,
                                DiagramContext.DIAGRAM_CONTEXT, Node.SELECTED_NODE, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE))
                        .build())
                .build();
    }
}

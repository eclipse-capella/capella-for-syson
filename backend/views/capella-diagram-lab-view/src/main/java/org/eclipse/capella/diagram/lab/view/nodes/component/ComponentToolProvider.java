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
package org.eclipse.capella.diagram.lab.view.nodes.component;

import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.NodeToolBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;

/**
 * Provide tools to create component node.
 *
 * @author frouene
 */
public class ComponentToolProvider {

    protected final ViewBuilders viewBuilderHelper;
    private final DiagramBuilders diagramBuilderHelper;

    public ComponentToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewComponentNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Component")
                .iconURLsExpression("/icons/full/obj16/LogicalComponent.svg");


        return this.configureNewComponentNodeTool(nodeToolBuilder, cache, false);
    }

    public NodeTool createNewActorComponentNodeTool(IViewDiagramElementFinder cache) {

        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Actor")
                .iconURLsExpression("/icons/full/obj16/Actor.svg");

        return this.configureNewComponentNodeTool(nodeToolBuilder, cache, true);
    }

    private NodeTool configureNewComponentNodeTool(NodeToolBuilder nodeToolBuilder, IViewDiagramElementFinder cache, boolean isActor) {

        cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {

            ChangeContextBuilder changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(ServiceMethod.of1(LARepresentationMutationService::createComponent).aqlSelf(String.valueOf(isActor)))
                    .children(
                            this.diagramBuilderHelper.newCreateView()
                                    .containmentKind(NodeContainmentKind.CHILD_NODE)
                                    .elementDescription(nodeDescription)
                                    .parentViewExpression("aql:selectedNode")
                                    .semanticElementExpression(AQLConstants.AQL_SELF)
                                    .variableName("newInstanceView").build());

            nodeToolBuilder.body(changeContextBuilder.build());
        });

        return nodeToolBuilder.build();
    }

}

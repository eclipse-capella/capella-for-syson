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
package org.eclipse.capella.diagram.sab.view.nodes.actor;

import org.eclipse.capella.model.services.system.analysis.SARepresentationMutationService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;

/**
 * Provide tools to create system actors in SAB.
 *
 * @author mbats
 */
public class SystemActorToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public SystemActorToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewSystemActorNodeTool(IViewDiagramElementFinder cache) {
        return this.createActorNodeTool(cache, "New Actor", "/icons/full/obj16/Actor.svg", ServiceMethod.of0(SARepresentationMutationService::createSystemActor).aqlSelf());
    }

    private NodeTool createActorNodeTool(IViewDiagramElementFinder cache, String name, String icon, String expression) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name(name)
                .iconURLsExpression(icon);
        cache.getNodeDescription(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            ChangeContextBuilder changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(expression)
                    .children(this.diagramBuilderHelper.newCreateView()
                            .containmentKind(NodeContainmentKind.CHILD_NODE)
                            .elementDescription(nodeDescription)
                            .parentViewExpression("aql:selectedNode")
                            .semanticElementExpression(AQLConstants.AQL_SELF)
                            .variableName("newInstanceView")
                            .build());
            nodeToolBuilder.body(changeContextBuilder.build());
        });

        return nodeToolBuilder.build();
    }
}

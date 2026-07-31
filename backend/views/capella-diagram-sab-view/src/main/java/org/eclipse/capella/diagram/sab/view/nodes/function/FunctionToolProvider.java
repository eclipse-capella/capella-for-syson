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
package org.eclipse.capella.diagram.sab.view.nodes.function;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide tools to create functions in SAB.
 *
 * @author mbats
 */
public class FunctionToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public FunctionToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewFunctionNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Function")
                .iconURLsExpression("/icons/full/obj16/LogicalFunction.svg");

        cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            ChangeContextBuilder changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(ServiceMethod.of0(TransverseMutationService::createFunction).aqlSelf())
                    .children(this.diagramBuilderHelper.newCreateView()
                            .containmentKind(NodeContainmentKind.CHILD_NODE)
                            .elementDescription(nodeDescription)
                            .parentViewExpression("aql:selectedNode")
                            .semanticElementExpression(AQLConstants.AQL_SELF)
                            .variableName(Node.SELECTED_NODE)
                            .build());
            nodeToolBuilder.body(changeContextBuilder.build());
        });

        return nodeToolBuilder.build();
    }
}

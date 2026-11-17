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
package org.eclipse.capella.diagram.sfbd.view.nodes.function;

import java.util.Objects;

import org.eclipse.capella.model.services.system.analysis.SAMutationService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide tools to create operational activity nodes.
 *
 * @author tbezierslafosse
 */
public class SystemFunctionToolProvider {

    private final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public SystemFunctionToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = Objects.requireNonNull(viewBuilderHelper);
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
    }

    public NodeTool createNewSystemFunctionNodeTool(IViewDiagramElementFinder cache) {

        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New System Function")
                .iconURLsExpression("/icons/full/obj16/SystemFunction.svg");

        cache.getNodeDescription(SystemFunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {

            ChangeContextBuilder changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(ServiceMethod.of0(SAMutationService::createSystemFunctionSA).aqlSelf())
                    .children(
                            this.diagramBuilderHelper.newCreateView()
                                    .containmentKind(NodeContainmentKind.CHILD_NODE)
                                    .elementDescription(nodeDescription)
                                    .parentViewExpression("aql:diagram")
                                    .semanticElementExpression(AQLConstants.AQL_SELF)
                                    .variableName("newInstanceView")
                                    .build());

            nodeToolBuilder.body(changeContextBuilder.build());
        });

        return nodeToolBuilder.build();
    }
}

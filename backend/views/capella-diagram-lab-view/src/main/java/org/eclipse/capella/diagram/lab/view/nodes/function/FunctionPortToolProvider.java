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
package org.eclipse.capella.diagram.lab.view.nodes.function;

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
 * Provide function port creation tools in LAB.
 *
 * @author tbezierslafosse
 */
public class FunctionPortToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public FunctionPortToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewInputFunctionPortNodeTool(IViewDiagramElementFinder cache) {
        return this.createFunctionPortTool(cache, "New Input Function Port", "/icons/full/obj16/FunctionInputPort.svg",
                ServiceMethod.of1(TransverseMutationService::createFunctionPort).aqlSelf("sysml::FeatureDirectionKind::_in"));
    }

    public NodeTool createNewOutputFunctionPortNodeTool(IViewDiagramElementFinder cache) {
        return this.createFunctionPortTool(cache, "New Output Function Port", "/icons/full/obj16/FunctionOutputPort.svg",
                ServiceMethod.of1(TransverseMutationService::createFunctionPort).aqlSelf("sysml::FeatureDirectionKind::out"));
    }

    private NodeTool createFunctionPortTool(IViewDiagramElementFinder cache, String name, String icon, String expression) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool().name(name).iconURLsExpression(icon);
        cache.getNodeDescription(FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            ChangeContextBuilder changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(expression)
                    .children(this.diagramBuilderHelper.newCreateView()
                            .containmentKind(NodeContainmentKind.BORDER_NODE)
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

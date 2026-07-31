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
package org.eclipse.capella.diagram.sab.view.nodes.componentport;

import org.eclipse.capella.model.services.system.analysis.SARepresentationMutationService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ChangeContextBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;

/**
 * Tools to create SAB component ports.
 *
 * @author mbats
 */
public class ComponentPortToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public ComponentPortToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewInputComponentPortNodeTool(IViewDiagramElementFinder cache) {
        return this.createComponentPortTool(cache, "New Input Port", "/icons/full/obj16/InFlowPort.svg", ServiceMethod.of0(SARepresentationMutationService::createInputComponentPort).aqlSelf());
    }

    public NodeTool createNewOutputComponentPortNodeTool(IViewDiagramElementFinder cache) {
        return this.createComponentPortTool(cache, "New Output Port", "/icons/full/obj16/OutFlowPort.svg", ServiceMethod.of0(SARepresentationMutationService::createOutputComponentPort).aqlSelf());
    }

    public NodeTool createNewInOutComponentPortNodeTool(IViewDiagramElementFinder cache) {
        return this.createComponentPortTool(cache, "New InOut Port", "/icons/full/obj16/InOutFlowPort.svg", ServiceMethod.of0(SARepresentationMutationService::createInOutComponentPort).aqlSelf());
    }

    private NodeTool createComponentPortTool(IViewDiagramElementFinder cache, String name, String icon, String expression) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool().name(name).iconURLsExpression(icon);
        cache.getNodeDescription(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
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

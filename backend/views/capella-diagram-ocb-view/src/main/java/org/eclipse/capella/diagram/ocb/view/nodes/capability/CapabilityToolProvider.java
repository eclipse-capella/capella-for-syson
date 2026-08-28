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
package org.eclipse.capella.diagram.ocb.view.nodes.capability;

import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the Operational Capability creation tool.
 *
 * @author tbezierslafosse
 */
public class CapabilityToolProvider {

    private final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public CapabilityToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewCapabilityNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Operational Capability")
                .iconURLsExpression("/icons/full/obj16/Capability.svg");
        cache.getNodeDescription(CapabilityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> nodeToolBuilder.body(
                this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of0(OAMutationService::createOperationalCapabilityOA).aqlSelf())
                        .children(this.diagramBuilderHelper.newCreateView()
                                .containmentKind(NodeContainmentKind.CHILD_NODE)
                                .elementDescription(nodeDescription)
                                .parentViewExpression(AQLUtils.aqlString(Node.SELECTED_NODE))
                                .semanticElementExpression(AQLConstants.AQL_SELF)
                                .variableName("newInstanceView")
                                .build())
                        .build()));
        return nodeToolBuilder.build();
    }
}

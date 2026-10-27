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
package org.eclipse.capella.diagram.mcb.view;

import org.eclipse.capella.diagram.mcb.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
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
 * Creation tools for MCB nodes.
 *
 * @author tbezierslafosse
 */
public class MCBNodeToolProvider {

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    private final ViewBuilders viewBuilders = new ViewBuilders();

    public NodeTool createRequirementTool(IViewDiagramElementFinder cache) {
        return this.createTool(cache, RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME, "New Requirement", "Requirement",
                ServiceMethod.of0(CommonCreationService::createRequirement).aqlSelf());
    }

    private NodeTool createTool(IViewDiagramElementFinder cache, String descriptionName, String name, String icon, String expression) {
        return this.diagramBuilders.newNodeTool().name(name).iconURLsExpression("/icons/full/obj16/" + icon + ".svg")
                .body(this.viewBuilders.newChangeContext().expression(expression)
                        .children(this.diagramBuilders.newCreateView()
                                .containmentKind(NodeContainmentKind.CHILD_NODE)
                                .elementDescription(cache.getNodeDescription(descriptionName).orElseThrow())
                                .parentViewExpression(AQLUtils.aqlString(Node.SELECTED_NODE))
                                .semanticElementExpression(AQLConstants.AQL_SELF).variableName("newInstanceView").build())
                        .build())
                .build();
    }
}

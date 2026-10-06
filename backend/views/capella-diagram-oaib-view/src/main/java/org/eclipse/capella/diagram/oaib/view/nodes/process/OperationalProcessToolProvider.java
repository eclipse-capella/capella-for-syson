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
package org.eclipse.capella.diagram.oaib.view.nodes.process;

import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.capella.model.services.operational.analysis.OAQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DialogDescription;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SelectionDialogTreeDescription;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the Operational Process creation tool of an OAIB diagram.
 *
 * @author tbezierslafosse
 */
public class OperationalProcessToolProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final ViewBuilders viewBuilderHelper;

    public OperationalProcessToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public NodeTool createNewOperationalProcessNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Operational Process")
                .iconURLsExpression("/icons/full/obj16/OperationalProcess.svg")
                .dialogDescription(this.createInteractionsDialogDescription());

        cache.getNodeDescription(OperationalProcessNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> nodeToolBuilder.body(
                this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(OAMutationService::createOperationalProcessOA).aqlSelf("selectedObjects"))
                        .children(this.diagramBuilderHelper.newCreateView()
                                .containmentKind(NodeContainmentKind.CHILD_NODE)
                                .elementDescription(nodeDescription)
                                .parentViewExpression("aql:selectedNode")
                                .semanticElementExpression(AQLConstants.AQL_SELF)
                                .variableName("newInstanceView")
                                .build())
                        .build()));
        return nodeToolBuilder.build();
    }

    private DialogDescription createInteractionsDialogDescription() {
        return this.diagramBuilderHelper.newSelectionDialogDescription()
                .multiple(true)
                .defaultTitleExpression("Interactions")
                .descriptionExpression("Select the involved Interactions")
                .selectionDialogTreeDescription(this.createDialogTreeDescription())
                .build();
    }

    private SelectionDialogTreeDescription createDialogTreeDescription() {
        return this.diagramBuilderHelper.newSelectionDialogTreeDescription()
                .elementsExpression(ServiceMethod.of0(OAQueryService::getFunctionalExchangesOA).aqlSelf())
                .isSelectableExpression(AQLConstants.AQL + "true")
                .childrenExpression(null)
                .build();
    }
}

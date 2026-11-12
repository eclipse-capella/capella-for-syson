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

package org.eclipse.capella.common.view.diagram;

import org.eclipse.capella.common.view.diagram.services.ShowRelatedElementsService;
import org.eclipse.sirius.components.collaborative.diagrams.variables.DiagramVariables;
import org.eclipse.sirius.components.core.api.variables.CoreVariables;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides the add contained elements tools shared by Capella for SysON diagrams.
 *
 * @author gdaniel
 */
public class AllContainedElementsToolProvider {

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    private final ViewBuilders viewBuilders = new ViewBuilders();

    public NodeTool createContainedElementsTool() {
        return this.diagramBuilders.newNodeTool()
                .name("Show all contained elements")
                .iconURLsExpression("/icons/AddExistingElements.svg")
                .body(this.viewBuilders.newChangeContext()
                        .expression(ServiceMethod.of4(ShowRelatedElementsService::showAllContainedElements).aqlSelf(CoreVariables.EDITING_CONTEXT.name(),
                                DiagramVariables.DIAGRAM_CONTEXT.name(), DiagramVariables.SELECTED_NODE.name(), ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE))
                        .build())
                .build();
    }
}

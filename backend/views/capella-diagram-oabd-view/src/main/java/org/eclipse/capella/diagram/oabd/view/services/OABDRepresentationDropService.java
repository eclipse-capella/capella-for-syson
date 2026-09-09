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
package org.eclipse.capella.diagram.oabd.view.services;

import java.util.Map;
import java.util.Objects;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;

/**
 * Reveals reachable OABD nodes without changing semantic ownership or duplicating views.
 *
 * @author tbezierslafosse
 */
public class OABDRepresentationDropService {
    private final IObjectSearchService objectSearchService;

    private final DiagramMutationElementService diagramMutationElementService;

    public OABDRepresentationDropService(IObjectSearchService objectSearchService, DiagramMutationElementService diagramMutationElementService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
    }

    public Element dropIntoDiagramFromExplorer(Element droppedElement, Object selectedNode, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        var context = this.objectSearchService
                .getObject(editingContext, diagramContext.diagram().getTargetObjectId())
                .filter(Element.class::isInstance)
                .map(Element.class::cast);

        if (context.isPresent()) {
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, selectedNode, convertedNodes);
        }
        return droppedElement;
    }
}

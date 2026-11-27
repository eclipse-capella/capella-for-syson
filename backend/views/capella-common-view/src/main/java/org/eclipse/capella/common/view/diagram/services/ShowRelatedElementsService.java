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

package org.eclipse.capella.common.view.diagram.services;

import java.util.Map;
import java.util.Objects;

import org.eclipse.capella.common.view.diagram.services.api.IShowConnectedElementsService;
import org.eclipse.capella.common.view.diagram.services.api.IShowContainedElementsService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.sysml.Element;
import org.springframework.stereotype.Service;

/**
 * The entry point service to show elements related to a provided context.
 *
 * @author Jerome Gout
 */
@Service
public class ShowRelatedElementsService {

    private final IShowContainedElementsService showContainedElementsService;

    private final IShowConnectedElementsService showConnectedElementsService;

    public ShowRelatedElementsService(IShowContainedElementsService showContainedElementsService, IShowConnectedElementsService showConnectedElementsService) {
        this.showContainedElementsService = Objects.requireNonNull(showContainedElementsService);
        this.showConnectedElementsService = Objects.requireNonNull(showConnectedElementsService);
    }

    /**
     * Displays elements contained in the diagram's target object.
     * <p>
     * This method is <i>not</i> recursive: it only displays the direct children of the diagram's target object ({@code self}), and place them on the diagram itself. This method only works on diagrams
     * for now (i.e. {@code selectedNode == null}): calling it on an existing node won't display anything.
     *
     * @param self
     *         the element containing elements to display
     * @param editingContext
     *         the editing context
     * @param diagramContext
     *         the diagram context
     * @param convertedNodes
     *         the mapping from view node descriptions to converted diagram node descriptions
     * @return {@code self}
     */
    public Element showAllContainedElements(Element self, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        return this.showContainedElementsService.showAllContainedElements(self, editingContext, diagramContext, selectedNode, convertedNodes);
    }

    public Element showAllContainedElementsRecursive(Element self, IEditingContext editingContext, DiagramContext diagramContext, Object graphicalParent,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        return this.showContainedElementsService.showAllContainedElementsRecursive(self, editingContext, diagramContext, graphicalParent, convertedNodes);
    }

    public Element showAllConnectedElements(Element self, IEditingContext editingContext, DiagramContext diagramContext, Node selectedNode,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {
        return this.showConnectedElementsService.showAllConnectedElements(self, editingContext, diagramContext, selectedNode, convertedNodes);
    }



}

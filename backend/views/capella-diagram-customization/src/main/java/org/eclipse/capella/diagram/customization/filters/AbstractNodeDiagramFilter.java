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

package org.eclipse.capella.diagram.customization.filters;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent;
import org.eclipse.syson.util.NodeFinder;

/**
 * Abstract filter for hidding/showing node in a diagram.
 *
 * @author Jerome Gout
 */
public abstract class AbstractNodeDiagramFilter extends AbstractDiagramFilter {

    private final IObjectSearchService objectSearchService;

    public AbstractNodeDiagramFilter(String id, IObjectSearchService objectSearchService) {
        this(id, true, objectSearchService);
    }

    public AbstractNodeDiagramFilter(String id, boolean state, IObjectSearchService objectSearchService) {
        super(id, state);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    protected abstract boolean isNodeTargetElement(EObject element);

    @Override
    public void postStateChange(IEditingContext editingContext, DiagramContext diagramContext, boolean state) {
        // create a HideDiagramElementEvent to hide the target element of the node.
        Diagram diagram = diagramContext.diagram();
        NodeFinder nodeFinder = new NodeFinder(diagram);
        Set<String> resolvedIds = new HashSet<>();
        nodeFinder.getAllNodesMatching(node -> this.isTargetNode(node, editingContext)).forEach(node -> {
            resolvedIds.add(node.getId());
        });
        diagramContext.diagramEvents().add(new HideDiagramElementEvent(resolvedIds, !state));
    }

    private boolean isTargetNode(Node node, IEditingContext editingContext) {
        String targetObjectId = node.getTargetObjectId();
        return this.objectSearchService.getObject(editingContext, targetObjectId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .filter(this::isNodeTargetElement)
                .isPresent();
    }
}

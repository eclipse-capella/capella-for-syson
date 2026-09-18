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

package org.eclipse.capella.diagram.customization.services;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.stereotype.Service;

/**
 * Service to filter diagram node base on their semantic target element.
 *
 * @author Jerome Gout 
 */
@Service
public class ElementNodeFilteringService implements org.eclipse.capella.diagram.customization.services.api.IElementNodeFilteringService {

    private final IObjectSearchService objectSearchService;
    private IEditingContext editingContext;
    private DiagramContext diagramContext;
    private Predicate<EObject> elementPredicate;

    public ElementNodeFilteringService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    @Override
    public ElementNodeFilteringService init(IEditingContext eContext, DiagramContext dContext) {
        this.editingContext = Objects.requireNonNull(eContext);
        this.diagramContext = Objects.requireNonNull(dContext);
        return this;
    }

    @Override
    public ElementNodeFilteringService withElementPredicate(Predicate<EObject> eltPredicate) {
        this.elementPredicate = Objects.requireNonNull(eltPredicate);
        return this;
    }

    @Override
    public void filter(boolean state) {
        if (this.editingContext != null && this.diagramContext != null && this.elementPredicate != null) {
            // create a HideDiagramElementEvent to hide the target element of the node.
            Diagram diagram = this.diagramContext.diagram();
            NodeFinder nodeFinder = new NodeFinder(diagram);
            Set<String> resolvedIds = new HashSet<>();
            nodeFinder.getAllNodesMatching(node -> this.isTargetNode(node, this.editingContext)).forEach(node -> {
                resolvedIds.add(node.getId());
            });
            this.diagramContext.diagramEvents().add(new HideDiagramElementEvent(resolvedIds, !state));
        }
    }

    private boolean isTargetNode(Node node, IEditingContext eContext) {
        String targetObjectId = node.getTargetObjectId();
        return this.objectSearchService.getObject(eContext, targetObjectId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .filter(this.elementPredicate)
                .isPresent();
    }
}

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

import org.eclipse.capella.diagram.customization.services.api.IElementNodeFilteringService;
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
public class ElementNodeFilteringService implements IElementNodeFilteringService {

    private final IObjectSearchService objectSearchService;

    public ElementNodeFilteringService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    @Override
    public void filter(IEditingContext editingContext, DiagramContext diagramContext, Predicate<EObject> elementPredicate, boolean state) {
        Objects.requireNonNull(editingContext);
        Objects.requireNonNull(diagramContext);
        Objects.requireNonNull(elementPredicate);

        Diagram diagram = diagramContext.diagram();
        NodeFinder nodeFinder = new NodeFinder(diagram);
        Set<String> resolvedIds = new HashSet<>();
        nodeFinder.getAllNodesMatching(node -> this.isTargetNode(node, editingContext, elementPredicate))
                .forEach(node -> resolvedIds.add(node.getId()));
        if (!resolvedIds.isEmpty()) {
            diagramContext.diagramEvents().add(new HideDiagramElementEvent(resolvedIds, !state));
        }
    }

    private boolean isTargetNode(Node node, IEditingContext editingContext, Predicate<EObject> elementPredicate) {
        String targetObjectId = node.getTargetObjectId();
        return this.objectSearchService.getObject(editingContext, targetObjectId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .filter(elementPredicate)
                .isPresent();
    }
}

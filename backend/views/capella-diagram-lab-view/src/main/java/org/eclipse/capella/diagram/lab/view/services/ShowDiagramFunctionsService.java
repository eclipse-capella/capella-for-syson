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
package org.eclipse.capella.diagram.lab.view.services;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent;
import org.eclipse.syson.util.NodeFinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service class for ShowDiagramFunctions option.
 *
 * @author fbarbin
 */
@Service
public class ShowDiagramFunctionsService {

    private final IObjectSearchService objectSearchService;

    private final LAQueryService laQueryService;

    @Value("${org.eclipse.capella.show.diagram.functions:true}")
    private boolean showFunctions;

    public ShowDiagramFunctionsService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.laQueryService = new LAQueryService();
    }

    public boolean isShowFunctions() {
        return this.showFunctions;
    }

    public void setShowFunctions(boolean showFunctionsValue, IEditingContext editingContext, DiagramContext diagramContext) {
        this.showFunctions = showFunctionsValue;
        Diagram diagram = diagramContext.diagram();
        NodeFinder nodeFinder = new NodeFinder(diagram);
        Set<String> resolvedIds = new HashSet<>();
        nodeFinder.getAllNodesMatching(node -> this.isTargetFunctionNode(node, editingContext)).forEach(node -> {
            resolvedIds.add(node.getId());
        });
        diagramContext.diagramEvents().add(new HideDiagramElementEvent(resolvedIds, !showFunctionsValue));

    }

    private boolean isTargetFunctionNode(Node node, IEditingContext editingContext) {
        String targetObjectId = node.getTargetObjectId();
        return this.objectSearchService.getObject(editingContext, targetObjectId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast)
                .filter(this.laQueryService::isFunction)
                .isPresent();

    }

}

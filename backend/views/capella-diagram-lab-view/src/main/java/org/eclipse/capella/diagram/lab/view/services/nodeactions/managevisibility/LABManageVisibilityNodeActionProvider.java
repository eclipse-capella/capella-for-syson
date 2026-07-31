/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Obeo - initial API and implementation
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view.services.nodeactions.managevisibility;

import java.util.List;
import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABViewDiagramDescriptionProvider;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.diagrams.api.IActionsProvider;
import org.eclipse.sirius.components.collaborative.diagrams.dto.Action;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.IDiagramElement;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.syson.sysml.RequirementUsage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Node action to open the manage visibility modal on LAB diagram nodes.
 * Adds the eye icon to RequirementUsage nodes with compartments.
 *
 * @author vkravchenko
 */
@Service
public class LABManageVisibilityNodeActionProvider implements IActionsProvider {

    private static final String ACTION_ID = "siriusweb_manage_visibility";

    private final IObjectSearchService objectSearchService;

    private final IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService;

    public LABManageVisibilityNodeActionProvider(IObjectSearchService objectSearchService,
            IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.viewRepresentationDescriptionSearchService = Objects.requireNonNull(viewRepresentationDescriptionSearchService);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canHandle(IEditingContext editingContext, DiagramDescription diagramDescription, IDiagramElement diagramElement) {
        if (diagramElement instanceof Node node && !node.getChildNodes().isEmpty()) {
            var semanticObject = this.objectSearchService.getObject(editingContext, node.getTargetObjectId());

            if (semanticObject.isPresent() && semanticObject.get() instanceof EObject eObject) {
                // Only for RequirementUsage elements on LAB diagram
                if (eObject instanceof RequirementUsage) {
                    var viewDiagramDescription = this.viewRepresentationDescriptionSearchService.findById(editingContext, diagramDescription.getId());
                    if (viewDiagramDescription.isPresent()) {
                        return viewDiagramDescription.get().getName().equals(LABViewDiagramDescriptionProvider.DESCRIPTION_NAME);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<Action> handle(IEditingContext editingContext, DiagramDescription diagramDescription, IDiagramElement diagramElement) {
        return List.of(new Action(ACTION_ID, List.of(), ""));
    }
}

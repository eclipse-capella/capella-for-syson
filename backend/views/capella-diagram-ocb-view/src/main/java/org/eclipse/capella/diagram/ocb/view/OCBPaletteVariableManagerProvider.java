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
package org.eclipse.capella.diagram.ocb.view;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.sirius.components.view.emf.diagram.tools.DiagramElementPaletteVariableManagerProvider;
import org.eclipse.sirius.components.view.emf.diagram.tools.api.IDiagramElementPaletteVariableManagerProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Defines an explicit absent connector target in OCB node palettes so their AQL preconditions can also evaluate targets.
 * Sirius Web 2026.9.2 defines {@code semanticEdgeTarget} only in connector palettes. Referencing an undefined variable
 * from a node palette produces an AQL error, whereas an explicit {@code null} can be passed to a service safely.
 * This adapter delegates all standard variables and leaves other diagrams and connector palettes unchanged.
 * See {@code doc/technical/ocb-generalization-precondition.adoc} for the compatibility and removal conditions.
 *
 * @author tbezierslafosse
 */
@Service
@Primary
public class OCBPaletteVariableManagerProvider implements IDiagramElementPaletteVariableManagerProvider {

    private final DiagramElementPaletteVariableManagerProvider delegate;

    private final IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService;

    public OCBPaletteVariableManagerProvider(DiagramElementPaletteVariableManagerProvider delegate, IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService) {
        this.delegate = Objects.requireNonNull(delegate);
        this.viewDiagramDescriptionSearchService = Objects.requireNonNull(viewDiagramDescriptionSearchService);
    }

    @Override
    public Optional<VariableManager> getVariableManager(IEditingContext editingContext, DiagramContext diagramContext, Object diagramElement, Object semanticElement) {
        var result = this.delegate.getVariableManager(editingContext, diagramContext, diagramElement, semanticElement);
        if (this.viewDiagramDescriptionSearchService.findById(editingContext, diagramContext.diagram().getDescriptionId())
                .filter(description -> OCBViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getName()))
                .isPresent()) {
            result.ifPresent(variableManager -> variableManager.put(EdgeDescription.SEMANTIC_EDGE_TARGET, null));
        }
        return result;
    }
}

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

package org.eclipse.capella.diagram.lab.view.filters;

import java.util.Objects;

import org.eclipse.capella.diagram.customization.filters.ShowFunctionsDiagramFilter;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterExecutor;
import org.eclipse.capella.diagram.customization.services.api.IElementNodeFilteringService;
import org.eclipse.capella.diagram.customization.services.api.IRepresentationDescriptionLabelSearchService;
import org.eclipse.capella.diagram.lab.view.LABViewDiagramDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.springframework.stereotype.Service;

/**
 * Filter executor for all filter of LAB diagrams.
 *
 * @author Jerome Gout
 */
@Service
public class LABDiagramFiltersExecutor implements IDiagramFilterExecutor {

    private final IRepresentationDescriptionLabelSearchService representationDescriptionLabelSearchService;

    private final IElementNodeFilteringService elementNodeFilteringService;

    private final CommonQueryService commonQueryService;

    public LABDiagramFiltersExecutor(IRepresentationDescriptionLabelSearchService representationDescriptionLabelSearchService, IElementNodeFilteringService elementNodeFilteringService) {
        this.representationDescriptionLabelSearchService = Objects.requireNonNull(representationDescriptionLabelSearchService);
        this.elementNodeFilteringService =  Objects.requireNonNull(elementNodeFilteringService);
        this.commonQueryService = new CommonQueryService();
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String representationId, String diagramFilterId) {
        return this.representationDescriptionLabelSearchService.findByRepresentationId(editingContext, representationId)
                .filter(label -> Objects.equals(label, LABViewDiagramDescriptionProvider.DESCRIPTION_NAME))
                .isPresent();
    }

    @Override
    public void execute(IEditingContext editingContext, DiagramContext diagramContext, String diagramFilterId, boolean state) {
        if (Objects.equals(diagramFilterId, ShowFunctionsDiagramFilter.ID)) {
            this.elementNodeFilteringService.filter(editingContext, diagramContext, this.commonQueryService::isFunction, state);
        }
    }
}
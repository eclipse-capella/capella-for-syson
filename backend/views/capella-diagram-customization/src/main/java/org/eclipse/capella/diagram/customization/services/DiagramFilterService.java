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

import java.util.List;
import java.util.Objects;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFiltersProvider;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.springframework.stereotype.Service;

/**
 * Implementation of the diagram filter service.
 *
 * @author Jerome Gout
 */
@Service
public class DiagramFilterService implements IDiagramFilterService {

    private final List<IDiagramFilter> filters;

    private final List<IDiagramFiltersProvider> providers;

    public DiagramFilterService(List<IDiagramFilter> filters, List<IDiagramFiltersProvider> providers) {
        this.filters = Objects.requireNonNull(filters);
        this.providers = providers;
    }

    @Override
    public List<IDiagramFilter> getAvailableFilters(IRepresentationDescription representationDescription) {
        return this.providers.stream()
                .filter(provider -> provider.canHandle(representationDescription))
                .findFirst()
                .map(IDiagramFiltersProvider::getDiagramFilters)
                .orElse(List.of());
    }

    @Override
    public boolean isDiagramFilterActive(String diagramFilterId) {
        return this.filters.stream()
                .filter(filter -> Objects.equals(filter.getId(), diagramFilterId))
                .findFirst()
                .map(IDiagramFilter::isActive)
                .orElse(false);
    }

    @Override
    public void setDiagramFilterState(IEditingContext editingContext, DiagramContext diagramContext, String diagramFilterId, boolean state) {
        this.filters.stream()
                .filter(filter -> Objects.equals(filter.getId(), diagramFilterId))
                .findFirst()
                .ifPresent(filter -> filter.setState(editingContext, diagramContext, state));
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterExecutor;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFiltersProvider;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Implementation of the diagram filter service.
 *
 * @author Jerome Gout
 */
@Service
public class DiagramFilterService implements IDiagramFilterService {

    /**
     * RepresentationId to FilterId map
     */
    private final ConcurrentHashMap<String, List<String>> activatedFilters;

    private final List<IDiagramFilter> filters;

    private final List<IDiagramFiltersProvider> providers;

    private final List<IDiagramFilterExecutor> executors;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    public DiagramFilterService(IRepresentationDescriptionSearchService representationDescriptionSearchService, IRepresentationMetadataSearchService representationMetadataSearchService, List<IDiagramFilter> filters, List<IDiagramFiltersProvider> providers, List<IDiagramFilterExecutor> executors) {
        this.filters = Objects.requireNonNull(filters);
        this.providers = Objects.requireNonNull(providers);
        this.executors = Objects.requireNonNull(executors);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.activatedFilters = new ConcurrentHashMap<>();
    }

    @Override
    public List<IDiagramFilter> getAvailableFilters(IEditingContext editingContext, String representationId) {
        return this.representationMetadataSearchService.findMetadataById(AggregateReference.to(UUID.fromString(editingContext.getId())), UUID.fromString(representationId))
                .flatMap(representationMetadata -> this.representationDescriptionSearchService.findById(editingContext, representationMetadata.getDescriptionId()))
                .map(this::getAvailableFilters)
                .orElse(Collections.emptyList());
    }

    private List<IDiagramFilter> getAvailableFilters(IRepresentationDescription representationDescription) {
        return this.providers.stream()
                .filter(provider -> provider.canHandle(representationDescription))
                .findFirst()
                .map(IDiagramFiltersProvider::getDiagramFilters)
                .map(ids -> ids.stream()
                        .map(id -> this.filters.stream()
                                .filter(filter -> Objects.equals(filter.getId(), id))
                                .findFirst()
                                .orElse(null))
                        .filter(Objects::nonNull)
                        .toList())
                .orElse(Collections.emptyList());
    }

    @Override
    public boolean isDiagramFilterActive(String diagramFilterId, String representationId) {
        if (this.activatedFilters.contains(representationId)) {
            return this.activatedFilters.get(representationId).contains(diagramFilterId);
        }
        // initial value of filters
        return this.filters.stream()
                .filter(filter -> Objects.equals(filter.getId(), diagramFilterId))
                .findFirst()
                .map(IDiagramFilter::getInitialState)
                .orElse(false);
    }

    @Override
    public void setDiagramFilterState(IEditingContext editingContext, DiagramContext diagramContext, String diagramFilterId, boolean state) {
        String representationId = diagramContext.diagram().getId();
        if (state) {
            // add the diagram id as an activated filter
            this.activatedFilters.computeIfAbsent(representationId, key -> new ArrayList<String>()).add(diagramFilterId);
        } else {
            // remove the activated filter
            if (this.activatedFilters.contains(representationId)) {
                this.activatedFilters.get(representationId).remove(diagramFilterId);
            }
        }
        // manage filter change execution
        this.executors.stream()
                .filter(executor -> executor.canHandle(editingContext, representationId, diagramFilterId))
                .findFirst()
                .ifPresent(executor -> executor.execute(editingContext, diagramContext, diagramFilterId, state));
    }
}

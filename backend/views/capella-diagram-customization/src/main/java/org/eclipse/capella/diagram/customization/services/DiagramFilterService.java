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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFiltersProvider;
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
     * RepresentationId to filter states map.
     */
    private final ConcurrentHashMap<String, Map<String, Boolean>> filterStates;

    private final List<IDiagramFilter> filters;

    private final List<IDiagramFiltersProvider> providers;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    public DiagramFilterService(IRepresentationDescriptionSearchService representationDescriptionSearchService, IRepresentationMetadataSearchService representationMetadataSearchService, List<IDiagramFilter> filters, List<IDiagramFiltersProvider> providers) {
        this.filters = Objects.requireNonNull(filters);
        this.providers = Objects.requireNonNull(providers);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.filterStates = new ConcurrentHashMap<>();
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
        Map<String, Boolean> representationFilterStates = this.filterStates.get(representationId);
        if (representationFilterStates != null && representationFilterStates.containsKey(diagramFilterId)) {
            return representationFilterStates.get(diagramFilterId);
        }
        // initial value of filters
        return this.filters.stream()
                .filter(filter -> Objects.equals(filter.getId(), diagramFilterId))
                .findFirst()
                .map(IDiagramFilter::getInitialState)
                .orElse(false);
    }

    @Override
    public void initializeFilterStates(IEditingContext editingContext, String representationId) {
        Map<String, Boolean> states = this.filterStates.computeIfAbsent(representationId, key -> new ConcurrentHashMap<>());
        this.getAvailableFilters(editingContext, representationId)
                .forEach(filter ->
                    states.putIfAbsent(filter.getId(), filter.getInitialState()));
    }

    @Override
    public void cleanDiagramFilters(String representationId) {
        this.filterStates.remove(representationId);
    }
}

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

package org.eclipse.capella.diagram.customization.handlers;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.capella.diagram.customization.dto.RepresentationMetadataAvailableDiagramFiltersInput;
import org.eclipse.capella.diagram.customization.dto.RepresentationMetadataAvailableDiagramFiltersSuccessPayload;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterMapper;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInput;
import org.eclipse.sirius.components.collaborative.diagrams.messages.ICollaborativeDiagramMessageService;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks;

/**
 * Event handler for RepresentationMetadata#availableDiagramFilters query.
 *
 * @author Jerome Gout
 */
@Service
public class RepresentationMetadataAvailableDiagramFiltersEventHandler implements IDiagramEventHandler {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IDiagramFilterService diagramFilterService;

    private final ICollaborativeDiagramMessageService messageService;

    private final IDiagramFilterMapper capellaDiagramFilterMapper;

    private final Counter counter;

    public RepresentationMetadataAvailableDiagramFiltersEventHandler(IRepresentationDescriptionSearchService representationDescriptionSearchService, IRepresentationMetadataSearchService representationMetadataSearchService,
            IDiagramFilterService diagramFilterService, ICollaborativeDiagramMessageService messageService, IDiagramFilterMapper capellaDiagramFilterMapper, MeterRegistry meterRegistry) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.diagramFilterService = Objects.requireNonNull(diagramFilterService);
        this.messageService = Objects.requireNonNull(messageService);
        this.capellaDiagramFilterMapper = Objects.requireNonNull(capellaDiagramFilterMapper);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IDiagramInput input) {
        return input instanceof RepresentationMetadataAvailableDiagramFiltersInput;
    }

    @Override
    public void handle(Sinks.One<IPayload> payloadSink, Sinks.Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, DiagramContext diagramContext, IDiagramInput input) {
        this.counter.increment();

        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), CreateRepresentationInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(input.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);

        var optionalSemanticDataId = new UUIDParser().parse(editingContext.getId());
        if (input instanceof RepresentationMetadataAvailableDiagramFiltersInput isShowFunctionsAvailableInput && optionalSemanticDataId.isPresent()) {
            var semanticDataId = optionalSemanticDataId.get();
            var availableDiagramFilters = this.representationMetadataSearchService.findMetadataById(AggregateReference.to(semanticDataId), UUID.fromString(isShowFunctionsAvailableInput.representationId()))
                    .flatMap(representationMetadata -> this.representationDescriptionSearchService.findById(editingContext, representationMetadata.getDescriptionId()))
                    .map(this.diagramFilterService::getAvailableFilters)
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(this.capellaDiagramFilterMapper::toDTO)
                    .toList();

            payload = new RepresentationMetadataAvailableDiagramFiltersSuccessPayload(input.id(), availableDiagramFilters);
        }
        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }
}

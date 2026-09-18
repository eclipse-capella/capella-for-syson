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

import java.util.Objects;

import org.eclipse.capella.diagram.customization.dto.RepresentationMetadataDiagramFiltersInput;
import org.eclipse.capella.diagram.customization.dto.RepresentationMetadataDiagramFiltersSuccessPayload;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterMapper;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInput;
import org.eclipse.sirius.components.collaborative.diagrams.messages.ICollaborativeDiagramMessageService;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks;

/**
 * Event handler for RepresentationMetadata#diagramFilters query.
 *
 * @author Jerome Gout
 */
@Service
public class RepresentationMetadataDiagramFiltersEventHandler implements IDiagramEventHandler {

    private final IDiagramFilterService diagramFilterService;

    private final ICollaborativeDiagramMessageService messageService;

    private final IDiagramFilterMapper capellaDiagramFilterMapper;

    private final Counter counter;

    public RepresentationMetadataDiagramFiltersEventHandler(IDiagramFilterService diagramFilterService, ICollaborativeDiagramMessageService messageService, IDiagramFilterMapper capellaDiagramFilterMapper, MeterRegistry meterRegistry) {
        this.diagramFilterService = Objects.requireNonNull(diagramFilterService);
        this.messageService = Objects.requireNonNull(messageService);
        this.capellaDiagramFilterMapper = Objects.requireNonNull(capellaDiagramFilterMapper);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IDiagramInput input) {
        return input instanceof RepresentationMetadataDiagramFiltersInput;
    }

    @Override
    public void handle(Sinks.One<IPayload> payloadSink, Sinks.Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, DiagramContext diagramContext, IDiagramInput input) {
        this.counter.increment();

        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), RepresentationMetadataDiagramFiltersInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(input.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);

        if (input instanceof RepresentationMetadataDiagramFiltersInput representationMetadataDiagramFiltersInput) {
            var availableDiagramFilters = this.diagramFilterService.getAvailableFilters(editingContext, representationMetadataDiagramFiltersInput.representationId())
                    .stream()
                    .map(filter -> this.capellaDiagramFilterMapper.toDTO(filter, representationMetadataDiagramFiltersInput.representationId()))
                    .toList();
            payload = new RepresentationMetadataDiagramFiltersSuccessPayload(input.id(), availableDiagramFilters);
        }
        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }
}

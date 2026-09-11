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

package org.eclipse.capella.application.configuration.handlers;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.capella.application.configuration.dto.RepresentationMetadataIsShowFunctionsAvailableInput;
import org.eclipse.capella.application.configuration.dto.RepresentationMetadataIsShowFunctionsAvailablePayload;
import org.eclipse.capella.diagram.ocb.view.OCBViewDiagramDescriptionProvider;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventHandler;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.collaborative.diagrams.messages.ICollaborativeDiagramMessageService;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Sinks;

/**
 * Event handler for RepresentationMetadata#isShowFunctionsAvailable query.
 *
 * @author Jerome Gout
 */
@Service
public class RepresentationMetadataIsShowFunctionsAvailableEventHandler implements IEditingContextEventHandler {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final ICollaborativeDiagramMessageService messageService;

    private final Counter counter;

    public RepresentationMetadataIsShowFunctionsAvailableEventHandler(IRepresentationDescriptionSearchService representationDescriptionSearchService, IRepresentationMetadataSearchService representationMetadataSearchService, ICollaborativeDiagramMessageService messageService, MeterRegistry meterRegistry) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IInput input) {
        return input instanceof RepresentationMetadataIsShowFunctionsAvailableInput;
    }

    @Override
    public void handle(Sinks.One<IPayload> payloadSink, Sinks.Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, IInput input) {
        this.counter.increment();

        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), CreateRepresentationInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(input.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);

        var optionalSemanticDataId = new UUIDParser().parse(editingContext.getId());
        if (input instanceof RepresentationMetadataIsShowFunctionsAvailableInput isShowFunctionsAvailableInput && optionalSemanticDataId.isPresent()) {
            var semanticDataId = optionalSemanticDataId.get();
            boolean isAvailable = this.representationMetadataSearchService.findMetadataById(AggregateReference.to(semanticDataId), UUID.fromString(isShowFunctionsAvailableInput.representationId()))
                    .flatMap(representationMetadata -> this.representationDescriptionSearchService.findById(editingContext, representationMetadata.getDescriptionId()))
                    .map(this::isShowFunctionsSupportedByDescription)
                    .orElse(false);

            payload = new RepresentationMetadataIsShowFunctionsAvailablePayload(input.id(), isAvailable);
        }
        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }

    private boolean isShowFunctionsSupportedByDescription(IRepresentationDescription description) {
        // All diagrams may have the Show Functions menu entry but OCB diagram.
        return !OCBViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getLabel());
    }
}

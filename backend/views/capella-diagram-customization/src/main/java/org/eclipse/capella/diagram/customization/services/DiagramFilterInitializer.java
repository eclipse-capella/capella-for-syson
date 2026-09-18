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

import java.util.Objects;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationEventProcessor;
import org.eclipse.sirius.components.collaborative.api.IRepresentationEventProcessorFluxCustomizer;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventProcessor;
import org.eclipse.sirius.components.core.api.IEditingContextSearchService;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

/**
 * Diagram filters initializer.
 * This class is in charge of setting up the diagram filters when a new diagram is created.
 *
 * @author Jerome Gout
 */
@Service
public class DiagramFilterInitializer implements IRepresentationEventProcessorFluxCustomizer {

    private final IDiagramFilterService diagramFilterService;

    private final IEditingContextSearchService editingContextSearchService;

    public DiagramFilterInitializer(IDiagramFilterService diagramFilterService, IEditingContextSearchService editingContextSearchService) {
        this.diagramFilterService = Objects.requireNonNull(diagramFilterService);
        this.editingContextSearchService = Objects.requireNonNull(editingContextSearchService);
    }

    @Override
    public boolean canHandle(String contextId, String representationId, IInput input, IRepresentationEventProcessor processor) {
        return processor instanceof IDiagramEventProcessor;
    }

    @Override
    public Flux<IPayload> customize(String contextId, String representationId, IInput input, IRepresentationEventProcessor processor, Flux<IPayload> events) {

        return events.doOnSubscribe(ignored -> {
            this.editingContextSearchService.findById(contextId)
                    .ifPresent(editingContext -> this.diagramFilterService.initializeFilterStates(editingContext, representationId));
        });
    }
}
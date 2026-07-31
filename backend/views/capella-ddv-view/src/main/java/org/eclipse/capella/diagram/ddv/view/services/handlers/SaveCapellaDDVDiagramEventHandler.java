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
package org.eclipse.capella.diagram.ddv.view.services.handlers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.capella.diagram.ddv.view.services.dto.SaveCapellaDDVDiagramInput;
import org.eclipse.capella.diagram.ddv.view.services.dto.SaveCapellaDDVDiagramSuccessPayload;
import org.eclipse.capella.diagram.ddv.view.view.FunctionalContextViewDiagramDescriptionProvider;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IRepresentationMetadataPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInput;
import org.eclipse.sirius.components.collaborative.diagrams.messages.ICollaborativeDiagramMessageService;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.layoutdata.DiagramLayoutData;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Sinks.Many;
import reactor.core.publisher.Sinks.One;

/**
 * Handle save capella diagram event.
 *
 * @author fbarbin
 */
@Service
public class SaveCapellaDDVDiagramEventHandler implements IDiagramEventHandler {

    private final ICollaborativeDiagramMessageService messageService;

    private final IRepresentationPersistenceService representationPersistenceService;

    private final IRepresentationMetadataPersistenceService representationMetadataPersistenceService;


    public SaveCapellaDDVDiagramEventHandler(ICollaborativeDiagramMessageService messageService, IRepresentationMetadataPersistenceService representationMetadataPersistenceService, IRepresentationPersistenceService representationPersistenceService) {
        this.messageService = Objects.requireNonNull(messageService);
        this.representationPersistenceService = Objects.requireNonNull(representationPersistenceService);
        this.representationMetadataPersistenceService = Objects.requireNonNull(representationMetadataPersistenceService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IDiagramInput diagramInput) {
        return diagramInput instanceof SaveCapellaDDVDiagramInput;
    }

    @Override
    public void handle(One<IPayload> payloadSink, Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, DiagramContext diagramContext, IDiagramInput diagramInput) {
        if (diagramInput instanceof SaveCapellaDDVDiagramInput saveCapellaDDVDiagramInput) {
            var diagram = diagramContext.diagram();
            var createRepresentationInput = new CreateRepresentationInput(saveCapellaDDVDiagramInput.id(), editingContext.getId(), diagram.getDescriptionId(), diagram.getTargetObjectId(),
                    FunctionalContextViewDiagramDescriptionProvider.DESCRIPTION_NAME);

            // We need to generate new UUID, indeed, the representationMetadataPersistenceService and representationPersistenceService expect an UUID.
            var newUUID = UUID.randomUUID().toString();
            var representationMetadata = RepresentationMetadata.newRepresentationMetadata(newUUID).kind(diagram.getKind()).label(FunctionalContextViewDiagramDescriptionProvider.DESCRIPTION_NAME)
                    .descriptionId(diagram.getDescriptionId()).iconURLs(List.of()).build();

            var newDiagram = this.createNewDiagram(newUUID, diagram);
            this.representationMetadataPersistenceService.save(createRepresentationInput, editingContext, representationMetadata, diagram.getTargetObjectId());
            this.representationPersistenceService.save(createRepresentationInput, editingContext, newDiagram);

            IPayload payload = new SaveCapellaDDVDiagramSuccessPayload(diagramInput.id(), newUUID);
            ChangeDescription changeDescription = new ChangeDescription(ChangeKind.REPRESENTATION_CREATION, diagramInput.representationId(), diagramInput);
            payloadSink.tryEmitValue(payload);
            changeDescriptionSink.tryEmitNext(changeDescription);
        } else {
            String message = this.messageService.invalidInput(diagramInput.getClass().getSimpleName(), SaveCapellaDDVDiagramInput.class.getSimpleName());
            IPayload payload = new ErrorPayload(diagramInput.id(), message);
            ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, diagramInput.representationId(), diagramInput);
            payloadSink.tryEmitValue(payload);
            changeDescriptionSink.tryEmitNext(changeDescription);
        }
    }

    private Diagram createNewDiagram(String newId, Diagram currentDiagram) {
        var style = currentDiagram.getStyle();
        if (style == null) {
            style = DiagramStyle.newDiagramStyle().build();
        }
        var layoutData = currentDiagram.getLayoutData();
        if (layoutData == null) {
            layoutData = new DiagramLayoutData(Map.of(), Map.of(), Map.of(), true);
        }

        return Diagram.newDiagram(newId)
                .targetObjectId(currentDiagram.getTargetObjectId())
                .nodes(currentDiagram.getNodes())
                .layoutData(layoutData)
                .edges(currentDiagram.getEdges())
                .descriptionId(currentDiagram.getDescriptionId())
                .style(style)
                .build();
    }
}

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

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.capella.application.configuration.dto.CreateCapellaRepresentationInput;
import org.eclipse.capella.model.services.transverse.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IEditingContextEventHandler;
import org.eclipse.sirius.components.collaborative.api.IRepresentationMetadataPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.api.Monitoring;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.collaborative.diagrams.messages.ICollaborativeDiagramMessageService;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationSuccessPayload;
import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Handler used to create a new Capella diagram.
 *
 * @author frouene
 */
@Service
public class CreateCapellaDiagramEventHandler implements IEditingContextEventHandler {
    private static final String OAB_REPRESENTATION_DESCRIPTION_ID = "OAB";

    private static final String OAB_REPRESENTATION_NAME = "OAB - Operational Analysis Blank";

    private static final String OCB_REPRESENTATION_DESCRIPTION_ID = "OCB";

    private static final String OCB_REPRESENTATION_NAME = "OCB - Operational Capability Blank";

    private static final String LAB_REPRESENTATION_DESCRIPTION_ID = "LAB";

    private static final String LAB_REPRESENTATION_NAME = "LAB - Logical Architecture Blank";

    private static final String SAB_REPRESENTATION_DESCRIPTION_ID = "SAB";

    private static final String SAB_REPRESENTATION_NAME = "SAB - System Analysis Blank";

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationMetadataPersistenceService representationMetadataPersistenceService;

    private final IRepresentationPersistenceService representationPersistenceService;

    private final IDiagramCreationService diagramCreationService;

    private final ICollaborativeDiagramMessageService messageService;

    private final Counter counter;

    public CreateCapellaDiagramEventHandler(IRepresentationDescriptionSearchService representationDescriptionSearchService, IRepresentationMetadataPersistenceService representationMetadataPersistenceService, IRepresentationPersistenceService representationPersistenceService,
            IDiagramCreationService diagramCreationService, ICollaborativeDiagramMessageService messageService, MeterRegistry meterRegistry) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.representationMetadataPersistenceService = Objects.requireNonNull(representationMetadataPersistenceService);
        this.representationPersistenceService = Objects.requireNonNull(representationPersistenceService);
        this.diagramCreationService = Objects.requireNonNull(diagramCreationService);
        this.messageService = Objects.requireNonNull(messageService);

        this.counter = Counter.builder(Monitoring.EVENT_HANDLER)
                .tag(Monitoring.NAME, this.getClass().getSimpleName())
                .register(meterRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, IInput input) {
        return input instanceof CreateCapellaRepresentationInput;
    }

    @Override
    public void handle(Sinks.One<IPayload> payloadSink, Sinks.Many<ChangeDescription> changeDescriptionSink, IEditingContext editingContext, IInput input) {
        this.counter.increment();

        String message = this.messageService.invalidInput(input.getClass().getSimpleName(), CreateRepresentationInput.class.getSimpleName());
        IPayload payload = new ErrorPayload(input.id(), message);
        ChangeDescription changeDescription = new ChangeDescription(ChangeKind.NOTHING, editingContext.getId(), input);

        if (input instanceof CreateCapellaRepresentationInput createRepresentationInput) {
            Optional<DiagramDescription> optionalDiagramDescription = this.findDiagramDescription(editingContext, createRepresentationInput.representationDescriptionId());
            Optional<Package> optionalParentPackage = this.findParentPackage(editingContext, createRepresentationInput.representationDescriptionId());

            if (optionalDiagramDescription.isPresent() && optionalParentPackage.isPresent()) {
                DiagramDescription diagramDescription = optionalDiagramDescription.get();
                Object parentPackage = optionalParentPackage.get();
                var diagramName = this.getRepresentationName(createRepresentationInput.representationDescriptionId());
                var variableManager = new VariableManager();
                variableManager.put(VariableManager.SELF, parentPackage);
                variableManager.put(DiagramDescription.LABEL, diagramName);
                String label = diagramDescription.getLabelProvider().apply(variableManager);
                List<String> iconURLs = diagramDescription.getIconURLsProvider().apply(variableManager);

                Diagram diagram = this.diagramCreationService.create(editingContext, diagramDescription, parentPackage);
                var representationMetadata = RepresentationMetadata.newRepresentationMetadata(diagram.getId())
                        .kind(diagram.getKind())
                        .label(label)
                        .descriptionId(diagram.getDescriptionId())
                        .iconURLs(iconURLs)
                        .build();

                this.representationMetadataPersistenceService.save(createRepresentationInput, editingContext, representationMetadata, diagram.getTargetObjectId());
                this.representationPersistenceService.save(createRepresentationInput, editingContext, diagram);

                payload = new CreateRepresentationSuccessPayload(input.id(), representationMetadata);
                changeDescription = new ChangeDescription(ChangeKind.REPRESENTATION_CREATION, editingContext.getId(), input);
            } else {
                payload = new ErrorPayload(input.id(), this.getCreationErrorMessage(createRepresentationInput.representationDescriptionId(), optionalDiagramDescription, optionalParentPackage));
            }
        }

        payloadSink.tryEmitValue(payload);
        changeDescriptionSink.tryEmitNext(changeDescription);
    }

    private Optional<Package> getPackageInArchitecture(IEditingContext editingContext, ArcadiaEngineeringPerspective engineeringPerspective, String packageName) {
        Package architecturePackage;
        Package targetPackage = null;
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            var resourceSet = emfEditingContext.getDomain().getResourceSet();
            var resources = resourceSet.getResources().stream()
                    .filter(res -> !(ElementUtil.isStandardLibraryResource(res)))
                    .toList();
            for (Resource resource : resources) {
                var contents = resource.getContents();
                architecturePackage = this.findPackageByName(contents, engineeringPerspective.getLabel());
                if (architecturePackage != null) {
                    targetPackage = this.findPackageByName(architecturePackage.eContents(), packageName);
                    if (targetPackage != null) {
                        break;
                    }
                }
            }
        }
        return Optional.ofNullable(targetPackage);
    }

    private Optional<Package> findParentPackage(IEditingContext editingContext, String representationDescriptionId) {
        return switch (representationDescriptionId) {
            case OAB_REPRESENTATION_DESCRIPTION_ID -> this.getPackageInArchitecture(editingContext, ArcadiaEngineeringPerspective.OperationalAnalysis, TransverseQueryService.STRUCTURE_PACKAGE);
            case OCB_REPRESENTATION_DESCRIPTION_ID -> this.getPackageInArchitecture(editingContext, ArcadiaEngineeringPerspective.OperationalAnalysis, TransverseQueryService.CAPABILITIES_PACKAGE);
            case SAB_REPRESENTATION_DESCRIPTION_ID -> this.getPackageInArchitecture(editingContext, ArcadiaEngineeringPerspective.SystemAnalysis, TransverseQueryService.STRUCTURE_PACKAGE);
            case LAB_REPRESENTATION_DESCRIPTION_ID -> this.getPackageInArchitecture(editingContext, ArcadiaEngineeringPerspective.LogicalArchitecture, TransverseQueryService.STRUCTURE_PACKAGE);
            default -> Optional.empty();
        };
    }

    private Package findPackageByName(EList<EObject> eObjects, String packageName) {
        Package result = null;
        for (EObject eObject : eObjects) {
            if (eObject instanceof Package packageElt && this.matchesPackageName(packageElt, packageName)) {
                result = packageElt;
                break;
            }
            EList<EObject> contents = eObject.eContents();
            if (!contents.isEmpty()) {
                result = this.findPackageByName(contents, packageName);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    private Optional<DiagramDescription> findDiagramDescription(IEditingContext editingContext, String representationDescriptionId) {
        String expectedDescriptionName = this.getRepresentationName(representationDescriptionId);

        return this.representationDescriptionSearchService.findAll(editingContext)
                .values()
                .stream()
                .filter(DiagramDescription.class::isInstance)
                .map(DiagramDescription.class::cast)
                .filter(diagramDescription -> this.matchesDiagramDescription(diagramDescription, representationDescriptionId, expectedDescriptionName))
                .findFirst();
    }

    private String getRepresentationName(String representationDescriptionId) {
        return switch (representationDescriptionId) {
            case OAB_REPRESENTATION_DESCRIPTION_ID -> OAB_REPRESENTATION_NAME;
            case OCB_REPRESENTATION_DESCRIPTION_ID -> OCB_REPRESENTATION_NAME;
            case SAB_REPRESENTATION_DESCRIPTION_ID -> SAB_REPRESENTATION_NAME;
            case LAB_REPRESENTATION_DESCRIPTION_ID -> LAB_REPRESENTATION_NAME;
            default -> "";
        };
    }

    private boolean matchesDiagramDescription(DiagramDescription diagramDescription, String representationDescriptionId, String expectedDescriptionName) {
        String label = diagramDescription.getLabel();
        return expectedDescriptionName.equals(label)
                || (label != null && label.startsWith(representationDescriptionId));
    }

    private boolean matchesPackageName(Package packageElt, String packageName) {
        return packageName.equals(packageElt.getDeclaredName()) || packageName.equals(packageElt.getName());
    }

    private String getCreationErrorMessage(String representationDescriptionId, Optional<DiagramDescription> optionalDiagramDescription, Optional<Package> optionalParentPackage) {
        String errorMessage = "The Capella diagram cannot be created";
        if (optionalDiagramDescription.isEmpty()) {
            errorMessage = "No diagram description found for " + representationDescriptionId;
        } else if (optionalParentPackage.isEmpty()) {
            errorMessage = "No Structure package found for " + representationDescriptionId;
        }
        return errorMessage;
    }
}

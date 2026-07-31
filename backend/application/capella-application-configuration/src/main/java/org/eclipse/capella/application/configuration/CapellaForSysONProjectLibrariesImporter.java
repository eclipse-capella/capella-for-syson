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

package org.eclipse.capella.application.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.project.api.ICreateProjectInput;
import org.eclipse.sirius.web.domain.boundedcontexts.library.Library;
import org.eclipse.sirius.web.domain.boundedcontexts.library.services.api.ILibrarySearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.project.events.ProjectCreatedEvent;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.SemanticData;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.events.SemanticDataCreatedEvent;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.services.api.ISemanticDataUpdateService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Imports libraries once the project semantic data has been created.
 * <p>
 * This class handles library ID like {@code namespace-name@version}, and adds the appropriate dependency.
 * </p>
 *
 * @technical-debt this class should be removed once library identifiers are stable (see sirius-web#6743).
 *
 * @author gdaniel
 */
@Service
public class CapellaForSysONProjectLibrariesImporter {

    private final ISemanticDataUpdateService semanticDataUpdateService;

    private final ILibrarySearchService librarySearchService;

    public CapellaForSysONProjectLibrariesImporter(ISemanticDataUpdateService semanticDataUpdateService, ILibrarySearchService librarySearchService) {
        this.semanticDataUpdateService = Objects.requireNonNull(semanticDataUpdateService);
        this.librarySearchService = Objects.requireNonNull(librarySearchService);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void onSemanticDataCreatedEvent(SemanticDataCreatedEvent event) {
        if (event.causedBy() instanceof ProjectCreatedEvent projectCreatedEvent && projectCreatedEvent.causedBy() instanceof ICreateProjectInput createProjectInput) {
            var projectSemanticData = event.semanticData();

            List<Library> libraries = this.librarySearchService.findAllById(this.getLibraryIds(createProjectInput.libraryIds()));

            List<AggregateReference<SemanticData, UUID>> newLibraries = new ArrayList<>();
            for (Library library : libraries) {
                var isAlreadyUsed = projectSemanticData.getDependencies().stream()
                        .anyMatch(dependency -> dependency.dependencySemanticDataId().getId().equals(library.getSemanticData().getId()));
                if (!isAlreadyUsed) {
                    newLibraries.add(library.getSemanticData());
                }
            }
            this.semanticDataUpdateService.addDependencies(event, AggregateReference.to(projectSemanticData.getId()), newLibraries);
        }
    }

    private List<UUID> getLibraryIds(List<String> libraryIds) {
        List<UUID> result = new ArrayList<>();
        for (String libraryId : libraryIds) {
            int dashIndex = libraryId.indexOf('-');
            int atIndex =  libraryId.indexOf('@');
            if (dashIndex != -1 && atIndex != -1) {
                String namespace = libraryId.substring(0, dashIndex);
                String name = libraryId.substring(dashIndex + 1, atIndex);
                String version = libraryId.substring(atIndex + 1);
                this.librarySearchService.findByNamespaceAndNameAndVersion(namespace, name, version)
                        .map(Library::getId)
                        .ifPresent(result::add);
            } else {
                new UUIDParser().parse(libraryId).ifPresent(result::add);
            }
        }
        return result;
    }

}

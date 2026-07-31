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

package org.eclipse.capella.application.configuration.arcadia.services;

import java.util.Objects;

import org.eclipse.sirius.web.domain.boundedcontexts.library.Library;
import org.eclipse.sirius.web.domain.boundedcontexts.library.services.api.ILibraryCreationService;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.events.SemanticDataCreatedEvent;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Creates the Arcadia library from its semantic data.
 *
 * @author gdaniel
 */
@Service
public class ArcadiaLibraryCreator {

    private final ILibraryCreationService libraryCreationService;

    public ArcadiaLibraryCreator(ILibraryCreationService libraryCreationService) {
        this.libraryCreationService = Objects.requireNonNull(libraryCreationService);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void onSemanticDataCreatedEvent(SemanticDataCreatedEvent semanticDataCreatedEvent) {
        if (semanticDataCreatedEvent.causedBy() instanceof PublishArcadiaLibraryCommand publishArcadiaLibraryCommand) {
            Library library = Library.newLibrary()
                    .namespace(publishArcadiaLibraryCommand.namespace())
                    .name(publishArcadiaLibraryCommand.name())
                    .version(publishArcadiaLibraryCommand.version())
                    .semanticData(AggregateReference.to(semanticDataCreatedEvent.semanticData().getId()))
                    .description(publishArcadiaLibraryCommand.description())
                    .build(semanticDataCreatedEvent);
            this.libraryCreationService.createLibrary(library);
        }
    }
}

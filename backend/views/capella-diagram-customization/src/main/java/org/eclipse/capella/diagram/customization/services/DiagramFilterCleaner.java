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
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.events.RepresentationMetadataDeletedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Service used to clean diagram filters when a diagram is deleted.
 *
 * @author Jerome Gout
 */
@Service
public class DiagramFilterCleaner {

    private final IDiagramFilterService diagramFilterService;

    public DiagramFilterCleaner(IDiagramFilterService diagramFilterService) {
        this.diagramFilterService = Objects.requireNonNull(diagramFilterService);
    }

    @TransactionalEventListener
    public void onRepresentationMetadataDeletedEvent(RepresentationMetadataDeletedEvent event) {
        this.diagramFilterService.cleanDiagramFilters(event.representationMetadata().getRepresentationMetadataId().toString());
    }
}

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
import java.util.Optional;
import java.util.UUID;

import org.eclipse.capella.diagram.customization.services.api.IRepresentationDescriptionLabelSearchService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Service to retrieve the name of a representation description.
 *
 * @author Jerome Gout
 */
@Service
public class RepresentationDescriptionLabelSearchService implements IRepresentationDescriptionLabelSearchService {

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    public RepresentationDescriptionLabelSearchService(IRepresentationMetadataSearchService representationMetadataSearchService,
            IRepresentationDescriptionSearchService representationDescriptionSearchService) {
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
    }

    @Override
    public Optional<String> findByRepresentationId(IEditingContext editingContext, String representationId) {
        return this.representationMetadataSearchService.findMetadataById(AggregateReference.to(UUID.fromString(editingContext.getId())), UUID.fromString(representationId))
                .flatMap(representationMetadata -> this.representationDescriptionSearchService.findById(editingContext, representationMetadata.getDescriptionId()))
                .map(IRepresentationDescription::getLabel);
    }
}

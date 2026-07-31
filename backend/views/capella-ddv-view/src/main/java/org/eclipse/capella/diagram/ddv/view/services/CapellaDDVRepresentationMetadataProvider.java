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
package org.eclipse.capella.diagram.ddv.view.services;

import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.URLParser;
import org.eclipse.sirius.components.core.api.IRepresentationMetadataProvider;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A Capella specific {@link IRepresentationMetadataProvider} for the DDV view.
 *
 * @author fbarbin
 */
@Service
public class CapellaDDVRepresentationMetadataProvider implements IRepresentationMetadataProvider {

    private final URLParser urlParser;

    public CapellaDDVRepresentationMetadataProvider(URLParser urlParser) {
        this.urlParser = Objects.requireNonNull(urlParser);
    }

    @Override
    public Optional<RepresentationMetadata> getMetadata(String editingContextId, String representationId) {

        return Optional.ofNullable(representationId)
                .filter(id -> id.startsWith(CapellaDDVDiagramEventProcessorFactory.CAPELLA_DDV_PREFIX))
                .flatMap(this::createRepresentationMetadata);
    }

    private Optional<RepresentationMetadata> createRepresentationMetadata(String representationId) {
        var values = this.urlParser.getParameterValues(representationId)
                .getOrDefault(CapellaDDVDiagramEventProcessorFactory.REPRESENTATION_ID_PARAMETER, List.of());
        if (!values.isEmpty()) {
            String representationDescriptionId = values.get(0);
            var representationMetadata = RepresentationMetadata.newRepresentationMetadata(representationId)
                    .descriptionId(representationDescriptionId)
                    .kind(Diagram.KIND)
                    .label("")
                    .iconURLs(List.of())
                    .build();
            return Optional.of(representationMetadata);
        }
        return Optional.empty();
    }
}

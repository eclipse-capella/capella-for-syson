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
package org.eclipse.capella.application.configuration.details.view.semanticbrowser;

import org.eclipse.sirius.components.core.RepresentationMetadata;
import org.eclipse.sirius.components.core.api.IRepresentationMetadataProvider;
import org.eclipse.sirius.components.forms.Form;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Provides metadata for the semantic browser (related elements) representation.
 *
 * @author ntinsalhi
 */
@Service
public class SemanticBrowserMetadataProvider implements IRepresentationMetadataProvider {

    @Override
    public Optional<RepresentationMetadata> getMetadata(String editingContextId, String representationId) {
        if (representationId.startsWith("semanticBrowser://")) {
            var representationMetadata = RepresentationMetadata.newRepresentationMetadata(representationId)
                    .kind(Form.KIND)
                    .label(SemanticBrowserDescriptionProvider.FORM_TITLE)
                    .descriptionId(SemanticBrowserDescriptionProvider.FORM_DESCRIPTION_ID)
                    .iconURLs(List.of("related-elements/related_elements.svg"))
                    .build();
            return Optional.of(representationMetadata);
        }
        return Optional.empty();
    }
}

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
package org.eclipse.capella.form.view;

import org.eclipse.sirius.components.collaborative.api.IRepresentationDescriptionsProvider;
import org.eclipse.sirius.components.collaborative.dto.RepresentationDescriptionMetadataDTO;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Used to make the form description editor work.
 *
 * @technical-debt This implementation has been developed in the context of a POC.
 * As such, it focuses on validating functional ideas rather than providing a fully
 * generic, extensible, or optimized solution. Several parts of this service rely
 * on hard-coded concepts, assumptions on SysML/Arcadia structures, and duplicated
 * traversal logic, which may limit reuse and scalability. A future industrialization
 * phase should consider refactoring toward more generic mechanisms, improved separation
 * of concerns, and better configurability.
 *
 * @author ntinsalhi
 */
@Service
public class CapellaFormRepresentationDescriptionMetaDataProvider implements IRepresentationDescriptionsProvider {

    @Override
    public boolean canHandle(IRepresentationDescription representationDescription) {
        return CapellaFormRepresentationDescriptionProvider.FORM_DESCRIPTION_ID.equals(representationDescription.getId());
    }

    @Override
    public List<RepresentationDescriptionMetadataDTO> handle(IEditingContext editingContext, Object object, IRepresentationDescription representationDescription) {
        return List.of(new RepresentationDescriptionMetadataDTO(representationDescription.getId(), representationDescription.getLabel(), representationDescription.getLabel(), ""));
    }
}

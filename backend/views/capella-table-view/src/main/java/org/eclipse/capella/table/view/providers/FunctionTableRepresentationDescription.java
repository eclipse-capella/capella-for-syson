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
package org.eclipse.capella.table.view.providers;

import java.util.List;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.table.view.FunctionTableRepresentationDescriptionProvider;
import org.eclipse.sirius.components.collaborative.api.IRepresentationDescriptionsProvider;
import org.eclipse.sirius.components.collaborative.dto.RepresentationDescriptionMetadataDTO;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.syson.sysml.Element;
import org.springframework.stereotype.Service;

/**
 * Provides the {@link RepresentationDescriptionMetadataDTO} for function table.
 *
 * @author ntinsalhi
 */
@Service
public class FunctionTableRepresentationDescription implements IRepresentationDescriptionsProvider {

    private final CommonQueryService commonQueryService;

    public FunctionTableRepresentationDescription() {
        this.commonQueryService = new CommonQueryService();
    }

    @Override
    public boolean canHandle(IRepresentationDescription representationDescription) {
        return representationDescription.getId().equals(FunctionTableRepresentationDescriptionProvider.TABLE_DESCRIPTION_ID);
    }

    @Override
    public List<RepresentationDescriptionMetadataDTO> handle(IEditingContext editingContext, Object object, IRepresentationDescription representationDescription) {
        String representationDescriptionLabel = representationDescription.getLabel();
        if (object instanceof Element element && this.commonQueryService.isInPerspective(element, ArcadiaEngineeringPerspective.OperationalAnalysis)) {
            // Functions are called "Activities" in the Operational Analysis perspective, so we rename the representation description accordingly
            representationDescriptionLabel = representationDescriptionLabel.replace("Function", "Activity");
        }
        return List.of(new RepresentationDescriptionMetadataDTO(representationDescription.getId(),
                representationDescriptionLabel,
                representationDescription.getLabel(),
                ""));
    }
}

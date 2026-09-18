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

package org.eclipse.capella.diagram.lab.view.filters;

import java.util.List;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFiltersProvider;
import org.eclipse.capella.diagram.lab.view.LABViewDiagramDescriptionProvider;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.springframework.stereotype.Service;

/**
 * Filters provider for LAB representation.
 * This class is responsible for providing all diagram filters available on this diagram.
 *
 * @author Jerome Gout
 */
@Service
public class LABDiagramFiltersProvider implements IDiagramFiltersProvider {

    @Override
    public boolean canHandle(IRepresentationDescription representationDescription) {
        return LABViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(representationDescription.getLabel());
    }

    @Override
    public List<String> getDiagramFilters() {
        return List.of(LABShowFunctionsDiagramFilter.ID);
    }
}

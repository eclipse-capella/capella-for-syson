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

import org.eclipse.capella.diagram.customization.dto.DiagramFilterDTO;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterMapper;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.springframework.stereotype.Service;

/**
 * The mapper for diagram filter to DTO.
 *
 * @author Jerome Gout
 */
@Service
public class DiagramFilterMapper implements IDiagramFilterMapper {

    private final IDiagramFilterService diagramFilterService;

    public DiagramFilterMapper(IDiagramFilterService diagramFilterService) {
        this.diagramFilterService = Objects.requireNonNull(diagramFilterService);
    }

    @Override
    public DiagramFilterDTO toDTO(IDiagramFilter diagramFilter, String representationId) {

        return new DiagramFilterDTO(diagramFilter.getId(),
                diagramFilter.getLabel(),
                this.diagramFilterService.isDiagramFilterActive(diagramFilter.getId(), representationId),
                diagramFilter.getActiveTooltip(),
                diagramFilter.getInactiveTooltip(),
                diagramFilter.getURLParam());
    }
}

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

import org.eclipse.capella.diagram.customization.dto.DiagramFilterDTO;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterMapper;
import org.springframework.stereotype.Service;

/**
 * The mapper for diagram filter to DTO.
 *
 * @author Jerome Gout
 */
@Service
public class DiagramFilterMapper implements IDiagramFilterMapper {
    @Override
    public DiagramFilterDTO toDTO(IDiagramFilter diagramFilter) {

        return new DiagramFilterDTO(diagramFilter.getId(), diagramFilter.getLabel(), diagramFilter.isActive(), diagramFilter.getActiveTooltip(), diagramFilter.getInactiveTooltip(),
                diagramFilter.getURLParam());
    }
}

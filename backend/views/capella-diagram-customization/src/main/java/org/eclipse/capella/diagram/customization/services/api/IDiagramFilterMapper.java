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

package org.eclipse.capella.diagram.customization.services.api;

import org.eclipse.capella.diagram.customization.dto.DiagramFilterDTO;

/**
 * Used to convert a Capella diagram filter to a DTO.
 *
 * @author Jerome Gout
 */
public interface IDiagramFilterMapper {

    DiagramFilterDTO toDTO(IDiagramFilter diagramFilter, String representationId);
}

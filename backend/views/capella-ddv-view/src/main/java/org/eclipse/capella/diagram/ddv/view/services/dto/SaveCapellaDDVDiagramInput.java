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
package org.eclipse.capella.diagram.ddv.view.services.dto;

import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInput;

import java.util.UUID;

/**
 * The input object of the save the Capella DDV diagram mutation.
 *
 * @author fbarbin
 */
public record SaveCapellaDDVDiagramInput(UUID id, String editingContextId, String representationId) implements IDiagramInput {

}

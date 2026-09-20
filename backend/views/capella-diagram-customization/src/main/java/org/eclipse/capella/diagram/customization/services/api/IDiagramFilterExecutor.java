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

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Interface for executing a filter of a representation.
 *
 * @author Jerome Gout
 */
public interface IDiagramFilterExecutor {

    boolean canHandle(IEditingContext editingContext, String representationId, String diagramFilterId);

    void  execute(IEditingContext editingContext, DiagramContext diagramContext, String diagramFilterId, boolean state);
}

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
 * Interface for Capella diagram filters.
 *
 * @author Jerome Gout
 */
public interface IDiagramFilter {

    /**
     * Return the current activation state of the filter.
     */
    boolean isActive();

    /**
     * Actual implementors should not override this api.
     * State change is handled by {@link org.eclipse.capella.diagram.customization.filters.AbstractDiagramFilter}
     */
    void setState(IEditingContext editingContext, DiagramContext diagramContext, boolean state);

    /**
     * Implementors should provide additional process that should be to reflect the filter change in the diagram.
     */
    void postStateChange(IEditingContext editingContext, DiagramContext diagramContext, boolean state);

    String getId();

    String getLabel();

    String getActiveTooltip();

    String getInactiveTooltip();

    String getURLParam();

}

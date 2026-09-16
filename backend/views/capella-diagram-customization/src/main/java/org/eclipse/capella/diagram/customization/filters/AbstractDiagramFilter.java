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

package org.eclipse.capella.diagram.customization.filters;

import java.util.Objects;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Abstract diagram filter dealing with the internal state of the filter.
 * Implementors should handle the filter availability in diagrams.
 *
 * @author Jerome Gout
 */
public abstract class AbstractDiagramFilter implements IDiagramFilter {

    private final String id;
    private boolean state;

    public AbstractDiagramFilter(String id) {
        this(id, true);
    }

    public AbstractDiagramFilter(String id, boolean initialState) {
        this.id = Objects.requireNonNull(id);
        this.state = initialState;
    }

    @Override
    public boolean isActive() {
        return this.state;
    }

    @Override
    public void setState(IEditingContext editingContext, DiagramContext diagramContext, boolean newState) {
        this.state = newState;
        this.postStateChange(editingContext, diagramContext, newState);
    }

    @Override
    public String getId() {
        return this.id;
    }

}

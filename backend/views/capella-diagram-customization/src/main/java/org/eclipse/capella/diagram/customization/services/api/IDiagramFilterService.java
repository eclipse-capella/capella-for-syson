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

import java.util.List;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Interface of the diagram filter service.
 *
 * @author Jerome Gout
 */
public interface IDiagramFilterService {

    List<IDiagramFilter> getAvailableFilters(IEditingContext editingContext, String representationId);

    boolean isDiagramFilterActive(String diagramFilterId, String representationId);

    void setDiagramFilterState(IEditingContext editingContext, DiagramContext diagramContext, String diagramFilterId, boolean state);

    /**
     * Implementation which does nothing, used for mocks in unit tests.
     *
     * @author Jerome Gout
     */
    class NoOp implements IDiagramFilterService {

        @Override
        public List<IDiagramFilter> getAvailableFilters(IEditingContext editingContext, String representationId) {
            return List.of();
        }

        @Override
        public boolean isDiagramFilterActive(String diagramFilterId, String representationId) {
            return false;
        }

        @Override
        public void setDiagramFilterState(IEditingContext editingContext, DiagramContext diagramContext, String diagramFilterId, boolean state) { }
    }
}

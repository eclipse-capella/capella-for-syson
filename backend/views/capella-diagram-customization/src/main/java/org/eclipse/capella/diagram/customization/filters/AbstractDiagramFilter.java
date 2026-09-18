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

/**
 * Abstract diagram filter dealing with the internal id of the filter.
 *
 * @author Jerome Gout
 */
public abstract class AbstractDiagramFilter implements IDiagramFilter {

    private final String id;

    public AbstractDiagramFilter(String id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public String getId() {
        return this.id;
    }

}

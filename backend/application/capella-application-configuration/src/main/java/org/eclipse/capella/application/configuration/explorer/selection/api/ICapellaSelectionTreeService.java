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
package org.eclipse.capella.application.configuration.explorer.selection.api;

import java.util.List;

import org.eclipse.sirius.components.representations.VariableManager;

/**
 * Provides Capella selection-tree navigation independently of interactive Explorer filters.
 *
 * @author tbezierslafosse
 */
public interface ICapellaSelectionTreeService {

    /**
     * Returns the default Explorer roots.
     */
    List<Object> getRoots(VariableManager variables);

    /**
     * Returns visible children independently of whether the item is expanded.
     */
    List<Object> getChildren(VariableManager variables);

    /**
     * Returns the visible parent, or null when none is available.
     */
    Object getParent(VariableManager variables);
}

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
package org.eclipse.capella.application.configuration.explorer.services.api;

import java.util.List;

import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Services to apply filters on Capella explorer.
 *
 * @author frouene
 */
public interface ICapellaExplorerFilterService {

    boolean isKerMLStandardLibrary(Object object);

    boolean isSysMLStandardLibrary(Object object);

    boolean isArcadiaLibrary(Object object);

    boolean isUserLibrary(IEditingContext editingContext, Object object);

    List<Object> hideKerMLStandardLibraries(List<Object> elements);

    List<Object> hideSysMLStandardLibraries(List<Object> elements);

    List<Object> hideUserLibraries(IEditingContext editingContext, List<Object> elements);

    List<Object> hideMemberships(List<Object> elements);

    List<Object> hideRootNamespace(List<Object> elements);

    /**
     * Filters visibility without expanding memberships or root namespaces, so this method can also filter explorer ancestor paths.
     * Hides standard and imported user libraries, applies active optional filters and mandatory rules contributed by explorer filter providers.
     * Preserves the order and identity of retained elements without modifying the input list or semantic model.
     *
     * @param editingContext
     *         the editing context used to identify imported user libraries
     * @param elements
     *         the elements whose visibility is evaluated
     * @param activeFilterIds
     *         the identifiers of active optional explorer filters
     * @return a new list containing the visible elements in their original order
     */
    List<Object> applyVisibilityFilters(IEditingContext editingContext, List<?> elements, List<String> activeFilterIds);

    /**
     * Prepares child elements for display in the explorer: expands memberships and root namespaces into their owned elements,
     * retains supported Capella elements and representations, then applies {@link #applyVisibilityFilters(IEditingContext, List, List)}.
     * Does not modify the input list or semantic model. Use {@code applyVisibilityFilters} for ancestor paths, which must not be expanded.
     *
     * @param editingContext
     *         the editing context used to identify imported user libraries
     * @param elements
     *         the raw child elements to display
     * @param activeFilterIds
     *         the identifiers of active optional explorer filters
     * @return a new list containing the visible explorer children
     */
    List<Object> applyFilters(IEditingContext editingContext, List<?> elements, List<String> activeFilterIds);
}

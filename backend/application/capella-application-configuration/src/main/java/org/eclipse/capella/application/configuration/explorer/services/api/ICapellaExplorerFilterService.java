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

    List<Object> applyFilters(IEditingContext editingContext, List<?> elements, List<String> activeFilterIds);
}

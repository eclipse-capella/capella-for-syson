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
package org.eclipse.capella.model.transverse.services.api;

import java.util.function.Predicate;

/**
 * Contributes perspective-specific visibility rules to the model explorer.
 *
 * @author tbezierslafosse
 */
public interface IExplorerFilterServiceProvider {

    /**
     * Returns a read-only predicate accepting visible elements. Elements outside the provider's scope must be accepted.
     * All contributed predicates must accept an element for it to remain visible, independently of optional explorer filters.
     *
     * @return the visibility predicate
     */
    Predicate<Object> getFilter();
}

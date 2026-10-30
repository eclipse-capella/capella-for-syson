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

import org.eclipse.sirius.components.trees.description.TreeDescription;

/**
 * Converts a tree to the Capella Explorer presentation while preserving its identifiers,
 * root scope and selection rules.
 *
 * @author tbezierslafosse
 */
public interface ICapellaTreeDescriptionConverter {

    TreeDescription convert(TreeDescription original);
}

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
 * Tests whether an editing context belongs to a project with the Capella nature,
 * independently of model contents and engineering perspectives.
 * Unknown editing contexts and projects return false.
 *
 * @author tbezierslafosse
 */
public interface ICapellaCapableEditingContextPredicate extends Predicate<String> {
}

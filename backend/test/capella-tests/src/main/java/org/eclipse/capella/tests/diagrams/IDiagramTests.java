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

package org.eclipse.capella.tests.diagrams;

/**
 * Marker interface for diagram-related tests.
 * <p>
 * Architecture rules can use this interface to identify diagram tests independently of their superclass.
 * Note that the name of classes implementing this interface must follow this pattern {@code <Service class name>Tests}, e.g. {@code LABDiagramServiceTests}.
 * <p>
 * This interface may later define accessors for diagram fixtures.
 *
 * @author gdaniel
 */
public interface IDiagramTests {
}

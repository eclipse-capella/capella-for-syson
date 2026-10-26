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
package org.eclipse.capella.diagram.customization.styles.api;

import java.util.List;

import org.eclipse.sirius.components.view.FixedColor;

/**
 * Creates the shared Functional Chain palette entries for a diagram.
 *
 * @author Jerome Gout
 */
public interface IFunctionalChainColorPaletteProvider {

    /**
     * Creates fresh EMF colors in palette order, named with the shared Functional Chain prefix and their zero-based index.
     * Each call returns independent instances suitable for containment in a different diagram palette.
     *
     * @return the ordered Functional Chain colors
     */
    List<FixedColor> createColors();
}


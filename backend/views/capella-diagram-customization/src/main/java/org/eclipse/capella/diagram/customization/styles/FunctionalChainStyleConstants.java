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
package org.eclipse.capella.diagram.customization.styles;

import java.util.List;

/**
 * Shared automatic Functional Chain style constants.
 *
 * @author Jerome Gout
 */
public final class FunctionalChainStyleConstants {

    public static final String FUNCTIONAL_CHAIN_COLOR_PREFIX = "FUNCTIONAL_CHAIN_COLOR";

    public static final String BLUE = "#1864D9";

    public static final String PURPLE = "#7B2CBF";

    public static final String ORANGE = "#C65D00";

    public static final String GREEN = "#237A3B";

    public static final String TURQUOISE = "#007F86";

    public static final String YELLOW_OCHRE = "#9A7B00";

    public static final String GRAY = "#666666";

    public static final List<String> COLORS = List.of(BLUE, PURPLE, ORANGE, GREEN, TURQUOISE, YELLOW_OCHRE, GRAY);

    public static final String OVERLAP_COLOR = "#000000";

    private FunctionalChainStyleConstants() {
        // Prevent instantiation.
    }
}

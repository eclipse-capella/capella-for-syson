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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Allocates colors deterministically while retaining observable surviving assignments.
 *
 * @author Jerome Gout
 */
public class FunctionalChainColorAllocator {

    public Map<String, String> allocate(Collection<String> chainIds, Map<String, String> previousColors, List<String> palette) {
        Map<String, String> colors = new HashMap<>();
        if (!palette.isEmpty()) {
            List<String> orderedIds = chainIds.stream().distinct().sorted().toList();
            for (String id : orderedIds) {
                String previousColor = previousColors.get(id);
                if (previousColor != null && palette.contains(previousColor)) {
                    colors.put(id, previousColor);
                }
            }
            var usedColors = new HashSet<>(colors.values());
            for (int index = 0; index < orderedIds.size(); index++) {
                String id = orderedIds.get(index);
                if (!colors.containsKey(id)) {
                    String color = palette.stream()
                            .filter(candidate -> !usedColors.contains(candidate))
                            .findFirst()
                            .orElse(palette.get(index % palette.size()));
                    colors.put(id, color);
                    usedColors.add(color);
                }
            }
        }
        return colors;
    }
}

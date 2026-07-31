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
package org.eclipse.capella.application.configuration.explorer.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.capella.application.configuration.explorer.CapellaExplorerTreeDescriptionProvider;
import org.eclipse.sirius.components.collaborative.trees.api.ITreeFilterProvider;
import org.eclipse.sirius.components.collaborative.trees.api.TreeFilter;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.springframework.stereotype.Service;

/**
 * Specific tree filter provider for Capella.
 *
 * @author fbarbin
 */
@Service
public class CapellaTreeFilterProvider implements ITreeFilterProvider {

    public static final String HIDE_PORTS_TREE_ITEM_FILTER_ID = UUID.nameUUIDFromBytes("CapellaTreeItemPortsFilter".getBytes()).toString();


    @Override
    public List<TreeFilter> get(String editingContextId, TreeDescription treeDescription) {
        List<TreeFilter> filters = new ArrayList<>();
        if (CapellaExplorerTreeDescriptionProvider.CAPELLA_EXPLORER.equals(treeDescription.getLabel())) {
            filters.add(new TreeFilter(HIDE_PORTS_TREE_ITEM_FILTER_ID, "Hide Ports", false));
        }
        return filters;
    }
}

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

import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerFilterService;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerTreeItemAlteredContentProvider;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * An implementation of {@link IExplorerTreeItemAlteredContentProvider} allowing to hide port tree items from
 * Explorer tree without hide their children.
 *
 * @author fbarbin
 */
@Service
public class HidePortsTreeItemAlteredContentProvider implements IExplorerTreeItemAlteredContentProvider {

    private final ICapellaExplorerFilterService filterService;

    public HidePortsTreeItemAlteredContentProvider(ICapellaExplorerFilterService filterService) {
        this.filterService = filterService;
    }

    @Override
    public boolean canHandle(Object object, List<String> activeFilterIds) {
        return activeFilterIds.contains(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID);
    }

    @Override
    public List<Object> apply(List<Object> computedChildren, VariableManager variableManager) {
        return this.filterService.hideMemberships(computedChildren);
    }

}

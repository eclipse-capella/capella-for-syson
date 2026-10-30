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
package org.eclipse.capella.application.configuration.explorer.selection;

import java.util.List;
import java.util.Objects;

import org.eclipse.capella.application.configuration.explorer.selection.api.ICapellaSelectionTreeService;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.variables.CoreVariables;
import org.eclipse.sirius.components.representations.RepresentationVariables;
import org.eclipse.sirius.components.representations.VariableManager;
import org.springframework.stereotype.Service;

/**
 * Provides Capella selection-tree navigation independently of interactive Explorer filters.
 *
 * @author tbezierslafosse
 */
@Service
public class CapellaSelectionTreeService implements ICapellaSelectionTreeService {

    private final ICapellaExplorerService explorerService;

    public CapellaSelectionTreeService(ICapellaExplorerService explorerService) {
        this.explorerService = Objects.requireNonNull(explorerService);
    }

    /**
     * Returns the default Explorer roots, independently of interactive Explorer filters.
     */
    @Override
    public List<Object> getRoots(VariableManager variables) {
        return variables.get(CoreVariables.EDITING_CONTEXT.name(), IEditingContext.class)
                .map(context -> this.explorerService.getElements(context, List.of()))
                .orElseGet(List::of);
    }

    @Override
    public List<Object> getChildren(VariableManager variables) {
        var self = variables.getVariables().get(RepresentationVariables.SELF.name());
        return variables.get(CoreVariables.EDITING_CONTEXT.name(), IEditingContext.class)
                .map(context -> this.explorerService.getChildren(self, context, List.of(), List.of(this.explorerService.getTreeItemId(self)), List.of()))
                .orElseGet(List::of);
    }

    @Override
    public Object getParent(VariableManager variables) {
        var self = variables.getVariables().get(RepresentationVariables.SELF.name());
        return variables.get(CoreVariables.EDITING_CONTEXT.name(), IEditingContext.class)
                .map(context -> this.explorerService.getParent(self, this.explorerService.getTreeItemId(self), context))
                .orElse(null);
    }
}

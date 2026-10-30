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

import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerLabelService;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.trees.renderer.TreeRenderer;
import org.springframework.stereotype.Service;

/**
 * Applies the Capella Explorer presentation to selection trees without changing their selection rules.
 *
 * @author tbezierslafosse
 */
@Service
public class CapellaSelectionTreeService {

    private final ICapellaExplorerLabelService labelService;

    private final ICapellaExplorerService explorerService;

    public CapellaSelectionTreeService(ICapellaExplorerLabelService labelService, ICapellaExplorerService explorerService) {
        this.labelService = Objects.requireNonNull(labelService);
        this.explorerService = Objects.requireNonNull(explorerService);
    }

    /**
     * Copies a native tree, preserving its identifiers, root scope and selectable candidates.
     */
    public TreeDescription adapt(TreeDescription original) {
        return TreeDescription.newTreeDescription(original)
                .elementsProvider(variables -> this.filterRoots(variables, original.getElementsProvider().apply(variables)))
                .childrenProvider(variables -> this.getChildren(original, variables))
                .hasChildrenProvider(variables -> !this.getVisibleChildren(variables).isEmpty())
                .parentObjectProvider(this::getParent)
                .treeItemLabelProvider(variables -> StyledString.of(this.labelService.getLabel(variables.getVariables().get(VariableManager.SELF))))
                .treeItemIconURLsProvider(variables -> this.labelService.getImageURL(variables.getVariables().get(VariableManager.SELF)))
                .build();
    }

    /**
     * Returns the default Explorer roots, independently of interactive Explorer filters.
     */
    public List<Object> getRoots(VariableManager variables) {
        return variables.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class)
                .map(context -> this.explorerService.getElements(context, List.of()))
                .orElseGet(List::of);
    }

    private List<?> filterRoots(VariableManager variables, List<?> roots) {
        var explorerRoots = this.getRoots(variables);
        return explorerRoots.stream().filter(roots::contains).toList();
    }

    private List<Object> getChildren(TreeDescription original, VariableManager variables) {
        var expanded = variables.getVariables().get(TreeRenderer.EXPANDED);
        if (expanded instanceof List<?> ids && ids.contains(original.getTreeItemIdProvider().apply(variables))) {
            return this.getVisibleChildren(variables);
        }
        return List.of();
    }

    private List<Object> getVisibleChildren(VariableManager variables) {
        var self = variables.getVariables().get(VariableManager.SELF);
        return variables.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class)
                .map(context -> this.explorerService.getChildren(self, context, List.of(), List.of(this.explorerService.getTreeItemId(self)), List.of()))
                .orElseGet(List::of);
    }

    private Object getParent(VariableManager variables) {
        var self = variables.getVariables().get(VariableManager.SELF);
        return variables.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class)
                .map(context -> this.explorerService.getParent(self, this.explorerService.getTreeItemId(self), context))
                .orElse(null);
    }
}

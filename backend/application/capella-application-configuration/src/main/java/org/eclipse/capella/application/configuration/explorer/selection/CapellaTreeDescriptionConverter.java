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
import org.eclipse.capella.application.configuration.explorer.selection.api.ICapellaTreeDescriptionConverter;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerLabelService;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.representations.RepresentationVariables;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.trees.renderer.TreeRenderer;
import org.springframework.stereotype.Service;

/**
 * Applies the Capella Explorer presentation to tree descriptions without changing their selection rules.
 *
 * @author tbezierslafosse
 */
@Service
public class CapellaTreeDescriptionConverter implements ICapellaTreeDescriptionConverter {

    private final ICapellaExplorerLabelService labelService;

    private final ICapellaSelectionTreeService treeService;

    public CapellaTreeDescriptionConverter(ICapellaExplorerLabelService labelService, ICapellaSelectionTreeService treeService) {
        this.labelService = Objects.requireNonNull(labelService);
        this.treeService = Objects.requireNonNull(treeService);
    }

    @Override
    public TreeDescription convert(TreeDescription original) {
        return TreeDescription.newTreeDescription(original)
                .elementsProvider(variables -> this.filterRoots(variables, original.getElementsProvider().apply(variables)))
                .childrenProvider(variables -> this.getChildren(original, variables))
                .hasChildrenProvider(variables -> !this.treeService.getChildren(variables).isEmpty())
                .parentObjectProvider(this.treeService::getParent)
                .treeItemLabelProvider(variables -> StyledString.of(this.labelService.getLabel(variables.getVariables().get(RepresentationVariables.SELF.name()))))
                .treeItemIconURLsProvider(variables -> this.labelService.getImageURL(variables.getVariables().get(RepresentationVariables.SELF.name())))
                .build();
    }

    private List<?> filterRoots(VariableManager variables, List<?> roots) {
        return this.treeService.getRoots(variables).stream().filter(roots::contains).toList();
    }

    private List<Object> getChildren(TreeDescription original, VariableManager variables) {
        var expanded = variables.getVariables().get(TreeRenderer.EXPANDED);
        if (expanded instanceof List<?> ids && ids.contains(original.getTreeItemIdProvider().apply(variables))) {
            return this.treeService.getChildren(variables);
        }
        return List.of();
    }
}

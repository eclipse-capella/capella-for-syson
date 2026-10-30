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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextRepresentationDescriptionProvider;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.web.application.browser.DefaultModelBrowsersTreeDescriptionProvider;
import org.eclipse.syson.tree.explorer.view.duplicate.SysONDuplicateTargetBrowserTreeDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * Contributes Capella presentations of the native reference, container and duplication browsers.
 *
 * @author tbezierslafosse
 */
@Service
public class CapellaModelBrowserTreeDescriptionProvider implements IEditingContextRepresentationDescriptionProvider {

    private final DefaultModelBrowsersTreeDescriptionProvider defaultProvider;

    // Duplication window that appears with CTRL / CMD + D
    private final SysONDuplicateTargetBrowserTreeDescriptionProvider duplicateProvider;

    private final CapellaSelectionTreeService treeService;

    public CapellaModelBrowserTreeDescriptionProvider(DefaultModelBrowsersTreeDescriptionProvider defaultProvider,
            SysONDuplicateTargetBrowserTreeDescriptionProvider duplicateProvider, CapellaSelectionTreeService treeService) {
        this.defaultProvider = Objects.requireNonNull(defaultProvider);
        this.duplicateProvider = Objects.requireNonNull(duplicateProvider);
        this.treeService = Objects.requireNonNull(treeService);
    }

    @Override
    public List<IRepresentationDescription> getRepresentationDescriptions(IEditingContext editingContext) {
        var descriptions = new ArrayList<>(this.defaultProvider.getRepresentationDescriptions(editingContext));
        descriptions.addAll(this.duplicateProvider.getRepresentationDescriptions(editingContext));
        return descriptions.stream()
                .filter(TreeDescription.class::isInstance)
                .map(TreeDescription.class::cast)
                .map(this.treeService::adapt)
                .map(tree -> TreeDescription.newTreeDescription(tree)
                        .id(this.getDescriptionId(tree.getId()))
                        .build())
                .map(IRepresentationDescription.class::cast)
                .toList();
    }

    /**
     * Returns a stable Capella-specific identifier for a native browser description.
     */
    public String getDescriptionId(String nativeId) {
        return "capella_selection_" + nativeId;
    }
}

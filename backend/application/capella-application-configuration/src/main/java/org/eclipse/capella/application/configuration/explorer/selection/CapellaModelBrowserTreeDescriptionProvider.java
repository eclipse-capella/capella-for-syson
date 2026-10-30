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
import java.util.UUID;

import org.eclipse.capella.application.configuration.explorer.selection.api.ICapellaTreeDescriptionConverter;
import org.eclipse.capella.model.transverse.services.api.ICapellaCapableEditingContextPredicate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextRepresentationDescriptionProvider;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.web.application.browser.DefaultModelBrowsersTreeDescriptionProvider;
import org.eclipse.syson.tree.explorer.view.duplicate.SysONDuplicateTargetBrowserTreeDescriptionProvider;
import org.eclipse.syson.tree.explorer.view.reference.SysONReferenceWidgetModelBrowserTreeDescriptionProvider;
import org.springframework.stereotype.Service;

/**
 * Registers dedicated Capella model-browser descriptions alongside the native SysON descriptions.
 *
 * @author tbezierslafosse
 */
@Service
public class CapellaModelBrowserTreeDescriptionProvider implements IEditingContextRepresentationDescriptionProvider {

    public static final String REFERENCE_DESCRIPTION_ID = UUID.nameUUIDFromBytes("capella_reference_widget_model_browser_tree_description".getBytes()).toString();

    public static final String CONTAINER_DESCRIPTION_ID = UUID.nameUUIDFromBytes("capella_reference_widget_container_tree_description".getBytes()).toString();

    public static final String DUPLICATION_DESCRIPTION_ID = UUID.nameUUIDFromBytes("capella_duplicate_target_browser_tree_description".getBytes()).toString();

    private final ICapellaCapableEditingContextPredicate capellaCapableEditingContextPredicate;

    private final ICapellaTreeDescriptionConverter treeDescriptionConverter;

    private final IEditingContextRepresentationDescriptionProvider defaultProvider;

    private final IEditingContextRepresentationDescriptionProvider referenceProvider;

    private final IEditingContextRepresentationDescriptionProvider duplicationProvider;

    public CapellaModelBrowserTreeDescriptionProvider(ICapellaCapableEditingContextPredicate capellaCapableEditingContextPredicate,
            ICapellaTreeDescriptionConverter treeDescriptionConverter, DefaultModelBrowsersTreeDescriptionProvider defaultProvider,
            SysONReferenceWidgetModelBrowserTreeDescriptionProvider referenceProvider, SysONDuplicateTargetBrowserTreeDescriptionProvider duplicationProvider) {
        this.capellaCapableEditingContextPredicate = Objects.requireNonNull(capellaCapableEditingContextPredicate);
        this.treeDescriptionConverter = Objects.requireNonNull(treeDescriptionConverter);
        this.defaultProvider = Objects.requireNonNull(defaultProvider);
        this.referenceProvider = Objects.requireNonNull(referenceProvider);
        this.duplicationProvider = Objects.requireNonNull(duplicationProvider);
    }

    @Override
    public List<IRepresentationDescription> getRepresentationDescriptions(IEditingContext editingContext) {
        if (!this.capellaCapableEditingContextPredicate.test(editingContext.getId())) {
            return List.of();
        }

        var capellaDescriptions = new ArrayList<IRepresentationDescription>();

        // container description
        capellaDescriptions.add(this.convertDescription(this.defaultProvider.getRepresentationDescriptions(editingContext),
            DefaultModelBrowsersTreeDescriptionProvider.CONTAINER_DESCRIPTION_ID, CONTAINER_DESCRIPTION_ID));

        // reference description
        capellaDescriptions.add(this.convertDescription(this.referenceProvider.getRepresentationDescriptions(editingContext),
            SysONReferenceWidgetModelBrowserTreeDescriptionProvider.DESCRIPTION_ID, REFERENCE_DESCRIPTION_ID));

        // duplication description
        capellaDescriptions.add(this.convertDescription(this.duplicationProvider.getRepresentationDescriptions(editingContext),
            SysONDuplicateTargetBrowserTreeDescriptionProvider.SYSON_DUPLICATE_TARGET_BROWSER_TREE_REPRESENTATION_ID, DUPLICATION_DESCRIPTION_ID));

        return capellaDescriptions;
    }

    /**
     * Creates a separate Capella browser description. An EditingContextProcessor cannot modify the shared tree directly:
     * Core features must keep the default SysON browser, while other features use the Capella browser.
     */
    private TreeDescription convertDescription(List<IRepresentationDescription> descriptions, String originalId, String capellaId) {
        return descriptions.stream()
                .filter(description -> originalId.equals(description.getId()))
                .filter(TreeDescription.class::isInstance)
                .map(TreeDescription.class::cast)
                .map(tree -> TreeDescription.newTreeDescription(this.treeDescriptionConverter.convert(tree))
                        .id(capellaId)
                        .build())
                .findFirst().orElseThrow();
    }
}

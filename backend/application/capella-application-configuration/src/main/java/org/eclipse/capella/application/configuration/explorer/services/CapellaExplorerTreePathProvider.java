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
package org.eclipse.capella.application.configuration.explorer.services;

import org.eclipse.capella.application.configuration.explorer.CapellaExplorerTreeDescriptionProvider;
import org.eclipse.capella.application.configuration.explorer.filters.CapellaTreeFilterProvider;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.trees.api.ITreePathProvider;
import org.eclipse.sirius.components.collaborative.trees.dto.TreePath;
import org.eclipse.sirius.components.collaborative.trees.dto.TreePathInput;
import org.eclipse.sirius.components.collaborative.trees.dto.TreePathSuccessPayload;
import org.eclipse.sirius.components.collaborative.trees.services.api.ITreeNavigationService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * ITreePathProvider implementation for the Capella Explorer tree.
 *
 * @author frouene
 */
@Service
public class CapellaExplorerTreePathProvider implements ITreePathProvider {

    private final ITreeNavigationService treeNavigationService;

    private final IURLParser urlParser;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final LAQueryService laQueryService;

    private final TransverseQueryService transverseQueryService;

    public CapellaExplorerTreePathProvider(ITreeNavigationService treeNavigationService, IURLParser urlParser,
            IRepresentationDescriptionSearchService representationDescriptionSearchService) {
        this.treeNavigationService = Objects.requireNonNull(treeNavigationService);
        this.urlParser = Objects.requireNonNull(urlParser);
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.laQueryService = new LAQueryService();
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public boolean canHandle(Tree tree) {
        if (tree != null) {
            var sysonExplorerId = UUID.nameUUIDFromBytes(CapellaExplorerTreeDescriptionProvider.CAPELLA_EXPLORER.getBytes()).toString();
            var optionalParameters = Optional.ofNullable(this.urlParser.getParameterValues(tree.getDescriptionId()));
            String sourceId = optionalParameters.map(parameters -> parameters.get("sourceId"))
                    .filter(list -> !list.isEmpty())
                    .map(list -> list.get(0))
                    .orElse("");
            return Objects.equals(sysonExplorerId, sourceId);
        }
        return false;
    }

    @Override
    public IPayload handle(IEditingContext editingContext, Tree tree, TreePathInput input) {
        int maxDepth = 0;
        Set<String> allAncestors = new LinkedHashSet<>();
        for (String selectionEntryId : input.selectionEntryIds()) {
            var itemAncestors = this.treeNavigationService.getAncestors(editingContext, tree, selectionEntryId);
            var optTreeDescription = this.getTreeDescription(editingContext, tree.getDescriptionId());
            if (!itemAncestors.isEmpty() && optTreeDescription.isPresent()) {
                var parameters = this.urlParser.getParameterValues(tree.getId());
                var activeFilterIdsParam = parameters.get("activeFilterIds").get(0);
                var activeFilterIds = this.urlParser.getParameterEntries(activeFilterIdsParam);
                var itemAncestorsObjects = new ArrayList<>();
                for (String itemAncestor : itemAncestors) {
                    this.getTreeItemObject(editingContext, optTreeDescription.get(), tree, itemAncestor).ifPresent(itemAncestorsObjects::add);
                }
                var filteredItemAncestorsIds = new ArrayList<String>();
                var filteredItemAncestorsObjects = this.applyFilters(itemAncestorsObjects, activeFilterIds);
                for (Object filteredItemAncestorsObject : filteredItemAncestorsObjects) {
                    this.getItemId(editingContext, optTreeDescription.get(), tree, filteredItemAncestorsObject).ifPresent(filteredItemAncestorsIds::add);
                }
                allAncestors.addAll(filteredItemAncestorsIds);
                maxDepth = Math.max(maxDepth, filteredItemAncestorsIds.size());
            }
        }
        return new TreePathSuccessPayload(input.id(), new TreePath(allAncestors.stream().toList(), maxDepth));
    }

    private Optional<String> getItemId(IEditingContext editingContext, TreeDescription treeDescription, Tree tree, Object object) {
        if (treeDescription != null) {
            var variableManager = new VariableManager();
            variableManager.put(VariableManager.SELF, object);
            return Optional.of(treeDescription.getTreeItemIdProvider().apply(variableManager));
        }
        return Optional.empty();
    }

    private Optional<Object> getTreeItemObject(IEditingContext editingContext, TreeDescription treeDescription, Tree tree, String id) {
        if (treeDescription != null) {
            var variableManager = new VariableManager();
            variableManager.put(IEditingContext.EDITING_CONTEXT, editingContext);
            variableManager.put(TreeDescription.ID, id);
            return Optional.ofNullable(treeDescription.getTreeItemObjectProvider().apply(variableManager));
        }
        return Optional.empty();
    }

    private Optional<TreeDescription> getTreeDescription(IEditingContext editingContext, String descriptionId) {
        return this.representationDescriptionSearchService.findById(editingContext, descriptionId)
                .filter(TreeDescription.class::isInstance)
                .map(TreeDescription.class::cast);
    }

    private List<Object> applyFilters(List<Object> elements, List<String> activeFilterIds) {
        var alteredElements = new ArrayList<Object>(elements);
        if (activeFilterIds.contains(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID)) {
            alteredElements.removeIf(element -> element instanceof EObject eObject && (this.transverseQueryService.isComponentPort(eObject) || this.laQueryService.isFunctionPort(eObject)));
        }
        return alteredElements;
    }
}

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
package org.eclipse.capella.application.configuration.explorer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.application.configuration.template.CapellaProjectTemplateProvider;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.view.emf.IViewRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.view.emf.tree.ITreeIdProvider;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerTreeDescriptionProvider;
import org.eclipse.sirius.web.domain.boundedcontexts.project.Nature;
import org.eclipse.sirius.web.domain.boundedcontexts.project.Project;
import org.eclipse.sirius.web.domain.boundedcontexts.project.services.api.IProjectSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.ProjectSemanticData;
import org.eclipse.sirius.web.domain.boundedcontexts.projectsemanticdata.services.api.IProjectSemanticDataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Provides the {@link TreeDescription}s to contribute to the Capella explorer.
 *
 * @author frouene
 */
@Service
public class CapellaExplorerContributionProvider implements IExplorerTreeDescriptionProvider {

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService;

    private final IProjectSearchService projectSearchService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    public CapellaExplorerContributionProvider(IRepresentationDescriptionSearchService representationDescriptionSearchService,
            IViewRepresentationDescriptionSearchService viewRepresentationDescriptionSearchService, IProjectSearchService projectSearchService, IProjectSemanticDataSearchService projectSemanticDataSearchService) {
        this.representationDescriptionSearchService = Objects.requireNonNull(representationDescriptionSearchService);
        this.viewRepresentationDescriptionSearchService = Objects.requireNonNull(viewRepresentationDescriptionSearchService);
        this.projectSearchService = Objects.requireNonNull(projectSearchService);
        this.projectSemanticDataSearchService = Objects.requireNonNull(projectSemanticDataSearchService);
    }

    @Override
    public List<TreeDescription> getDescriptions(IEditingContext editingContext) {
        return this.getCapellaExplorerTreeDescription(editingContext).map(List::of).orElseGet(List::of);
    }

    private Optional<TreeDescription> getCapellaExplorerTreeDescription(IEditingContext editingContext) {
        Optional<TreeDescription> result = Optional.empty();
        if (this.isCapellaProject(editingContext.getId())) {
            result = this.representationDescriptionSearchService
                    .findAll(editingContext).values().stream()
                    .filter(TreeDescription.class::isInstance)
                    .map(TreeDescription.class::cast)
                    .filter(td -> this.isCapellaExplorerView(td, editingContext))
                    .findFirst();
        }
        return result;
    }

    private boolean isCapellaExplorerView(TreeDescription treeDescription, IEditingContext editingContext) {
        if (treeDescription.getId().startsWith(ITreeIdProvider.TREE_DESCRIPTION_KIND)) {
            // This tree description comes from a tree DSL
            var optionalViewTreeDescription = this.viewRepresentationDescriptionSearchService.findById(editingContext, treeDescription.getId());
            if (optionalViewTreeDescription.isPresent()) {
                return optionalViewTreeDescription.get().getName().equals(CapellaExplorerTreeDescriptionProvider.CAPELLA_EXPLORER);
            }
        }
        return false;
    }

    private boolean isCapellaProject(String editingContextId) {
        return new UUIDParser().parse(editingContextId)
                .flatMap(semanticDataId -> this.projectSemanticDataSearchService.findBySemanticDataId(AggregateReference.to(semanticDataId)))
                .map(ProjectSemanticData::getProject)
                .map(AggregateReference::getId)
                .flatMap(this.projectSearchService::findById)
                .filter(this::isCapella)
                .isPresent();
    }

    private boolean isCapella(Project project) {
        return project.getNatures().stream()
                .map(Nature::name)
                .anyMatch(CapellaProjectTemplateProvider.CAPELLA_NATURE::equals);
    }

}

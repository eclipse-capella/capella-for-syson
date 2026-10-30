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
package org.eclipse.capella.model.transverse.services;

import java.util.Objects;

import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.projects.domain.Nature;
import org.eclipse.sirius.web.projects.domain.services.api.IProjectSearchService;
import org.eclipse.sirius.web.projects.semanticdata.domain.ProjectSemanticData;
import org.eclipse.sirius.web.projects.semanticdata.domain.services.api.IProjectSemanticDataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Queries the Capella project nature independently of model contents and engineering perspectives.
 *
 * @author tbezierslafosse
 */
@Service
public class CapellaProjectService {

    public static final String CAPELLA_NATURE = "siriusWeb://nature?kind=capella";

    private final IProjectSearchService projectSearchService;

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService;

    public CapellaProjectService(IProjectSearchService projectSearchService, IProjectSemanticDataSearchService projectSemanticDataSearchService) {
        this.projectSearchService = Objects.requireNonNull(projectSearchService);
        this.projectSemanticDataSearchService = Objects.requireNonNull(projectSemanticDataSearchService);
    }

    /**
     * Returns whether the editing context belongs to a project with the Capella nature.
     * Unknown editing contexts and projects return false.
     */
    public boolean isCapellaProject(String editingContextId) {
        return new UUIDParser().parse(editingContextId)
                .flatMap(semanticDataId -> this.projectSemanticDataSearchService.findBySemanticDataId(AggregateReference.to(semanticDataId)))
                .map(ProjectSemanticData::getProject)
                .map(AggregateReference::getId)
                .flatMap(this.projectSearchService::findById)
                .filter(project -> project.getNatures().stream().map(Nature::name).anyMatch(CAPELLA_NATURE::equals))
                .isPresent();
    }
}

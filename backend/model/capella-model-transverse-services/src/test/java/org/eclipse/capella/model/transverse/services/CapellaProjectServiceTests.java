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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.sirius.web.projects.domain.Project;
import org.eclipse.sirius.web.projects.domain.services.api.IProjectSearchService;
import org.eclipse.sirius.web.projects.semanticdata.domain.ProjectSemanticData;
import org.eclipse.sirius.web.projects.semanticdata.domain.services.api.IProjectSemanticDataSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.data.jdbc.core.mapping.AggregateReference;

/**
 * Tests project nature detection with minimal projects and simulated persistence lookups.
 *
 * @author tbezierslafosse
 */
public class CapellaProjectServiceTests {

    private final UUID contextId = UUID.randomUUID();

    private final IProjectSearchService projectSearchService = mock(IProjectSearchService.class);

    private final IProjectSemanticDataSearchService projectSemanticDataSearchService = mock(IProjectSemanticDataSearchService.class);

    private final CapellaProjectService service = new CapellaProjectService(this.projectSearchService, this.projectSemanticDataSearchService);

    @Test
    public void isCapellaProjectShouldReturnTrueForCapellaProject() {
        var project = Project.newProject().name("Capella").natures(List.of(CapellaProjectService.CAPELLA_NATURE)).build(() -> this.contextId);
        this.givenProject(project);

        assertThat(this.service.isCapellaProject(this.contextId.toString())).isTrue();
    }

    @Test
    public void isCapellaProjectShouldReturnFalseForSysMLProject() {
        var project = Project.newProject().name("SysML").natures(List.of("siriusWeb://nature?kind=sysml")).build(() -> this.contextId);
        this.givenProject(project);

        assertThat(this.service.isCapellaProject(this.contextId.toString())).isFalse();
    }

    private void givenProject(Project project) {
        var link = ProjectSemanticData.newProjectSemanticData()
                .project(AggregateReference.to(project.getId()))
                .semanticData(AggregateReference.to(this.contextId))
                .name("main")
                .build(() -> this.contextId);
        when(this.projectSemanticDataSearchService.findBySemanticDataId(AggregateReference.to(this.contextId))).thenReturn(Optional.of(link));
        when(this.projectSearchService.findById(project.getId())).thenReturn(Optional.of(project));
    }
}

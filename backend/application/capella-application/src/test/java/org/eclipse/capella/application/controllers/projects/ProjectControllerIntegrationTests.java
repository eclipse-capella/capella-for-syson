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
package org.eclipse.capella.application.controllers.projects;

import com.jayway.jsonpath.JsonPath;
import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.sirius.web.tests.graphql.ProjectsQueryRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests of the project controllers.
 *
 * @author frouene
 */
@Transactional
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private ProjectsQueryRunner projectsQueryRunner;

    @Test
    @DisplayName("Given an empty database, when a valid first query is performed, then zero project is returned")
    public void givenSetOfProjectsWhenValidFirstQueryIsPerformedThenTheProjectsAreReturned() {
        Map<String, Object> variables = Map.of("first", 2);
        var result = this.projectsQueryRunner.run(variables).data();

        boolean hasPreviousPage = JsonPath.read(result, "$.data.viewer.projects.pageInfo.hasPreviousPage");
        assertThat(hasPreviousPage).isFalse();

        boolean hasNextPage = JsonPath.read(result, "$.data.viewer.projects.pageInfo.hasNextPage");
        assertThat(hasNextPage).isFalse();

        String startCursor = JsonPath.read(result, "$.data.viewer.projects.pageInfo.startCursor");
        assertThat(startCursor).isBlank();

        String endCursor = JsonPath.read(result, "$.data.viewer.projects.pageInfo.endCursor");
        assertThat(endCursor).isBlank();

        int count = JsonPath.read(result, "$.data.viewer.projects.pageInfo.count");
        assertThat(count).isEqualTo(0);

        List<String> projectIds = JsonPath.read(result, "$.data.viewer.projects.edges[*].node.id");
        assertThat(projectIds).hasSize(0);
    }

}

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

package org.eclipse.capella.application.controllers.diagram;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.diagram.lab.view.filters.LABShowFunctionsDiagramFilter;
import org.eclipse.capella.tests.graphql.CreateCapellaRepresentationMutationRunner;
import org.eclipse.capella.tests.graphql.RepresentationMetadataAvailableFiltersQueryRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for Capella diagram Show Functions filter availability tests.
 *
 * @author Jerome Gout
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class ShowFunctionsMenuAvailabilityTests extends AbstractIntegrationTests {

    @Autowired
    private CreateCapellaRepresentationMutationRunner createCapellaRepresentationMutationRunner;

    @Autowired
    private RepresentationMetadataAvailableFiltersQueryRunner representationMetadataAvailableFiltersQueryRunner;

    @Test
    @DisplayName("GIVEN a Capella project with a LAB diagram, WHEN querying if Show Functions is available, THEN true is returned")
    public void showFunctionsAvailabilityInLABDiagram() {
        Map<String, Object> variables = Map.of(
                "editingContextId", CapellaProjectData.EDITING_CONTEXT_ID,
                "representationId", CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID,
                "filterId", LABShowFunctionsDiagramFilter.ID,
                "active", true
        );
        var availableFiltersQueryResult = this.representationMetadataAvailableFiltersQueryRunner.run(variables).data();
        List<Map<String, Object>> availableFilters = JsonPath.read(availableFiltersQueryResult, "$.data.viewer.editingContext.representation.availableDiagramFilters[*]");
        assertThat(availableFilters)
                .extracting(filter -> filter.get("id"))
                .contains(LABShowFunctionsDiagramFilter.ID);
    }

    @Test
    @DisplayName("GIVEN a Capella project with a OCB diagram, WHEN querying if Show Functions is available, THEN false is returned")
    public void showFunctionsAvailabilityInOCBDiagram() {
        Map<String, Object> variables = Map.of(
                "editingContextId", CapellaProjectData.EDITING_CONTEXT_ID,
                "representationId", CapellaProjectData.GraphicalIds.OCB_OPERATIONAL_CAPABILITY_BLANK_DIAGRAM_ID,
                "filterId", LABShowFunctionsDiagramFilter.ID,
                "active", true
        );
        var availableFiltersQueryResult = this.representationMetadataAvailableFiltersQueryRunner.run(variables).data();
        List<Map<String, Object>> availableFilters = JsonPath.read(availableFiltersQueryResult, "$.data.viewer.editingContext.representation.availableDiagramFilters[*]");
        assertThat(availableFilters)
                .extracting(filter -> filter.get("id"))
                .doesNotContain(LABShowFunctionsDiagramFilter.ID);    }
}

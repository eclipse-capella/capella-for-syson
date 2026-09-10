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

import java.util.Map;
import java.util.UUID;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaIdentifiers;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.application.configuration.dto.CreateCapellaRepresentationInput;
import org.eclipse.capella.tests.graphql.CreateCapellaRepresentationMutationRunner;
import org.eclipse.capella.tests.graphql.RepresentationMetadataIShowFunctionsAvailableQueryRunner;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationSuccessPayload;
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
    private RepresentationMetadataIShowFunctionsAvailableQueryRunner representationMetadataIShowFunctionsAvailableQueryRunner;

    @Test
    @DisplayName("GIVEN a Capella project with a LAB diagram, WHEN querying if Show Functions is available, THEN true is returned")
    public void showFunctionsAvailabilityInLABDiagram() {
        var labCreationInput = new CreateCapellaRepresentationInput(UUID.randomUUID(), CapellaIdentifiers.EDITING_CONTEXT_ID, "LAB");
        var result = this.createCapellaRepresentationMutationRunner.run(labCreationInput).data();

        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        assertThat(typename).withFailMessage(result).isEqualTo(CreateRepresentationSuccessPayload.class.getSimpleName());
        String representationId = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.id");

        Map<String, Object> variables = Map.of(
                "editingContextId", CapellaIdentifiers.EDITING_CONTEXT_ID,
                "representationId", representationId
        );
        var showFunctionQueryResult = this.representationMetadataIShowFunctionsAvailableQueryRunner.run(variables).data();
        var isShowFunctionsAvailable = JsonPath.read(showFunctionQueryResult, "$.data.viewer.editingContext.representation.isShowFunctionsAvailable");
        assertThat(isShowFunctionsAvailable).isEqualTo(true);
    }

    @Test
    @DisplayName("GIVEN a Capella project with a OCB diagram, WHEN querying if Show Functions is available, THEN false is returned")
    public void showFunctionsAvailabilityInOCBDiagram() {
        var labCreationInput = new CreateCapellaRepresentationInput(UUID.randomUUID(), CapellaIdentifiers.EDITING_CONTEXT_ID, "OCB");
        var result = this.createCapellaRepresentationMutationRunner.run(labCreationInput).data();

        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        assertThat(typename).withFailMessage(result).isEqualTo(CreateRepresentationSuccessPayload.class.getSimpleName());
        String representationId = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.id");

        Map<String, Object> variables = Map.of(
                "editingContextId", CapellaIdentifiers.EDITING_CONTEXT_ID,
                "representationId", representationId
        );
        var showFunctionQueryResult = this.representationMetadataIShowFunctionsAvailableQueryRunner.run(variables).data();
        var isShowFunctionsAvailable = JsonPath.read(showFunctionQueryResult, "$.data.viewer.editingContext.representation.isShowFunctionsAvailable");
        assertThat(isShowFunctionsAvailable).isEqualTo(false);
    }
}

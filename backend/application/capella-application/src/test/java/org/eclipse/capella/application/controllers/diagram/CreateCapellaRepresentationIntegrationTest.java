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

import java.util.UUID;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaIdentifiers;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.application.configuration.dto.CreateCapellaRepresentationInput;
import org.eclipse.capella.tests.graphql.CreateCapellaRepresentationMutationRunner;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationSuccessPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for Capella diagram creation through GraphQL.
 *
 * @author tbezierslafosse
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class CreateCapellaRepresentationIntegrationTest extends AbstractIntegrationTests {

    @Autowired
    private CreateCapellaRepresentationMutationRunner createCapellaRepresentationMutationRunner;

    @Test
    @DisplayName("Given a Capella project, when an LAB representation is created, then its metadata is returned")
    public void createLABRepresentation() {
        this.assertRepresentationCreation("LAB", "LAB - Logical Architecture Blank");
    }

    @Test
    @DisplayName("Given a Capella project, when an OAB representation is created, then its metadata is returned")
    public void createOABRepresentation() {
        this.assertRepresentationCreation("OAB", "OAB - Operational Analysis Blank");
    }

    @Test
    @DisplayName("Given a Capella project, when an OCB representation is created, then its metadata is returned")
    public void createOCBRepresentation() {
        this.assertRepresentationCreation("OCB", "OCB - Operational Capability Blank");
    }

    @Test
    @DisplayName("Given a Capella project, when an SAB representation is created, then its metadata is returned")
    public void createSABRepresentation() {
        this.assertRepresentationCreation("SAB", "SAB - System Analysis Blank");
    }

    @Test
    @DisplayName("Given a Capella project, when an unknown representation is requested, then an error is returned")
    public void rejectUnknownRepresentation() {
        var input = new CreateCapellaRepresentationInput(UUID.randomUUID(), CapellaIdentifiers.EDITING_CONTEXT_ID, "unknown");
        var result = this.createCapellaRepresentationMutationRunner.run(input).data();

        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        String message = JsonPath.read(result, "$.data.createCapellaRepresentation.message");
        assertThat(typename).isEqualTo("ErrorPayload");
        assertThat(message).isEqualTo("No diagram description found for unknown");
    }

    private void assertRepresentationCreation(String descriptionId, String expectedLabel) {
        var input = new CreateCapellaRepresentationInput(UUID.randomUUID(), CapellaIdentifiers.EDITING_CONTEXT_ID, descriptionId);
        var result = this.createCapellaRepresentationMutationRunner.run(input).data();

        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        assertThat(typename).withFailMessage(result).isEqualTo(CreateRepresentationSuccessPayload.class.getSimpleName());
        String representationId = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.id");
        String label = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.label");
        String kind = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.kind");
        assertThat(representationId).isNotBlank();
        assertThat(label).isEqualTo(expectedLabel);
        assertThat(kind).isEqualTo("siriusComponents://representation?type=Diagram");
    }
}

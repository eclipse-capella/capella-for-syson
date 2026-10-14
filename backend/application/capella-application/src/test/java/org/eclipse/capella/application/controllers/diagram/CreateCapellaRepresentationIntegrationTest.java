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
import static org.eclipse.sirius.components.diagrams.tests.DiagramEventPayloadConsumer.assertRefreshedDiagramThat;

import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.UUID;
import java.time.Duration;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.application.configuration.dto.CreateCapellaRepresentationInput;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.tests.graphql.CreateCapellaRepresentationMutationRunner;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationSuccessPayload;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
import org.eclipse.sirius.components.diagrams.tests.graphql.DiagramEventSubscriptionRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

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

    @Autowired
    private DiagramEventSubscriptionRunner diagramEventSubscriptionRunner;

    @Test
    public void createLABRepresentationShouldReturnMetadata() {
        this.assertRepresentationCreation("LAB", "LAB - Logical Architecture Blank");
    }

    @Test
    public void createOABRepresentationShouldReturnMetadata() {
        this.assertRepresentationCreation("OAB", "OAB - Operational Analysis Blank");
    }

    @Test
    public void createOABDRepresentationShouldReturnMetadata() {
        this.assertRepresentationCreation("OABD", "OABD - Operational Activity Break Down");
    }

    @Test
    public void createOAIBRepresentationShouldReturnMetadata() {
        this.assertRepresentationCreation("OAIB", "OAIB - Operational Activity Interaction Blank");
    }

    @Test
    public void createOCBRepresentationShouldReturnMetadata() {
        this.assertRepresentationCreation("OCB", "OCB - Operational Capability Blank");
    }

    @Test
    public void createMCBRepresentationShouldOpenAnEmptyDiagramOnSystemAnalysisCapabilities() {
        var representationId = this.assertRepresentationCreation("MCB", "MCB - Mission and Capabilities Blank");
        var input = new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);

        StepVerifier.create(this.diagramEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedDiagramThat(diagram -> {
                    assertThat(diagram.getTargetObjectId()).isEqualTo(CapellaProjectData.SemanticIds.CAPABILITIES_PAC_ID_2);
                    assertThat(diagram.getNodes()).isEmpty();
                    assertThat(diagram.getEdges()).isEmpty();
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    public void createSABRepresentationShouldReturnMetadata() {
        this.assertRepresentationCreation("SAB", "SAB - System Analysis Blank");
    }

    @Test
    public void createRepresentationWithUnknownDescriptionShouldReturnError() {
        var input = new CreateCapellaRepresentationInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, "unknown");
        var result = this.createCapellaRepresentationMutationRunner.run(input).data();

        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        List<String> messages = JsonPath.read(result, "$.data.createCapellaRepresentation.messages[*].body");
        assertThat(typename).isEqualTo("ErrorPayload");
        assertThat(messages).contains("No diagram description found for unknown");
    }

    private String assertRepresentationCreation(String descriptionId, String expectedLabel) {
        var input = new CreateCapellaRepresentationInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, descriptionId);
        var result = this.createCapellaRepresentationMutationRunner.run(input).data();

        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        assertThat(typename).withFailMessage(result).isEqualTo(CreateRepresentationSuccessPayload.class.getSimpleName());
        String representationId = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.id");
        String label = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.label");
        String kind = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.kind");
        assertThat(representationId).isNotBlank();
        assertThat(label).isEqualTo(expectedLabel);
        assertThat(kind).isEqualTo("siriusComponents://representation?type=Diagram");
        return representationId;
    }
}

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

import java.time.Duration;
import java.util.UUID;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.ToolTester;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
import org.eclipse.sirius.components.diagrams.tests.graphql.DiagramEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Integration tests of the <i>show all contained elements</i> tools.
 *
 * @author gdaniel
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class ShowAllContainedElementsIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private DiagramEventSubscriptionRunner diagramEventSubscriptionRunner;

    @Autowired
    private ToolTester toolTester;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("gg")
    public void test() {
        var input = new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID);
        var subscriptionResult = this.diagramEventSubscriptionRunner.run(input);
        assertThat(subscriptionResult.errors()).isEmpty();
        var diagramFlux = subscriptionResult.flux();

        Runnable executeShowAllContainedElementsOnDiagram = () -> {
            this.toolTester.invokeDiagramTool(CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID, "Show all contained elements");
        };
        
        var checkDiagramContent = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(1);
            assertThat(diagram.getNodes().getFirst().getInsideLabel().getText()).isEqualTo("system");
        });

        StepVerifier.create(diagramFlux)
                .consumeNextWith(assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty()))
                .then(executeShowAllContainedElementsOnDiagram)
                .consumeNextWith(checkDiagramContent)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}

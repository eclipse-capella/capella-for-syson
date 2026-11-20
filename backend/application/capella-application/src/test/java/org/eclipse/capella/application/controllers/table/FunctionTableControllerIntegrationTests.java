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

package org.eclipse.capella.application.controllers.table;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sirius.components.tables.tests.TableEventPayloadConsumer.assertRefreshedTableThat;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.table.view.FunctionTableRepresentationDescriptionProvider;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationInput;
import org.eclipse.sirius.components.tables.Line;
import org.eclipse.sirius.web.tests.services.api.IGivenCreatedTableSubscription;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.test.StepVerifier;

/**
 * Integration tests of the function table controllers.
 *
 * @author gdaniel
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class FunctionTableControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IGivenCreatedTableSubscription givenCreatedTableSubscription;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("Given a table representation on the LA functions package, when we subscribe to its events, then the representation data are received")
    public void createFunctionTableOnLAFunction() {
        var input = new CreateRepresentationInput(
                UUID.randomUUID(),
                CapellaProjectData.EDITING_CONTEXT_ID,
                FunctionTableRepresentationDescriptionProvider.TABLE_DESCRIPTION_ID,
                CapellaProjectData.SemanticIds.FUNCTIONS_PAC_ID_2,
                "Function Table"
        );

        var subscriptionResult = this.givenCreatedTableSubscription.createAndSubscribe(input);
        assertThat(subscriptionResult.errors()).isEmpty();
        var flux = subscriptionResult.flux();

        Consumer<Object> initialTableContentConsumer = assertRefreshedTableThat(table -> {
            assertThat(table.getColumns()).hasSize(4);
            assertThat(table.getLines()).hasSize(1);
            Line firstLine = table.getLines().getFirst();
            assertThat(firstLine.getTargetObjectId()).isEqualTo(CapellaProjectData.SemanticIds.ROOT_FUNCTION_ACT_ID_2);
            assertThat(firstLine.getHeaderLabel()).isEqualTo("Root Function");
        });

        StepVerifier.create(flux)
                .consumeNextWith(initialTableContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Given a table representation on the OA functions package, when we subscribe to its events, then the representation data are received")
    public void createActionTableOnOAAction() {
        var input = new CreateRepresentationInput(
              UUID.randomUUID(),
              CapellaProjectData.EDITING_CONTEXT_ID,
              FunctionTableRepresentationDescriptionProvider.TABLE_DESCRIPTION_ID,
                CapellaProjectData.SemanticIds.FUNCTIONS_PAC_ID,
              "Action Table"
        );

        var subscriptionResult = this.givenCreatedTableSubscription.createAndSubscribe(input);
        assertThat(subscriptionResult.errors()).isEmpty();
        var flux = subscriptionResult.flux();

        Consumer<Object> initialTableContentConsumer = assertRefreshedTableThat(table -> {
            assertThat(table.getColumns()).hasSize(4);
            assertThat(table.getLines()).hasSize(1);
            Line firstLine = table.getLines().getFirst();
            assertThat(firstLine.getTargetObjectId()).isEqualTo(CapellaProjectData.SemanticIds.ROOT_FUNCTION_ACT_ID);
            assertThat(firstLine.getHeaderLabel()).isEqualTo("Root Function");
        });

        StepVerifier.create(flux)
                .consumeNextWith(initialTableContentConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}

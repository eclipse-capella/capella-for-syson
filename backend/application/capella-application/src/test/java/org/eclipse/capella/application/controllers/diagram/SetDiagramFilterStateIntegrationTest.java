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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.ToolTester;
import org.eclipse.capella.diagram.customization.dto.SetDiagramFilterStateInput;
import org.eclipse.capella.diagram.customization.dto.SetDiagramFilterStateSuccessPayload;
import org.eclipse.capella.diagram.customization.filters.ShowFunctionsDiagramFilter;
import org.eclipse.capella.tests.graphql.RepresentationMetadataDiagramFiltersQueryRunner;
import org.eclipse.capella.diagram.customization.filters.ShowActivitiesDiagramFilter;
import org.eclipse.capella.tests.graphql.SetDiagramFilterStateMutationRunner;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
import org.eclipse.sirius.components.diagrams.ViewModifier;
import org.eclipse.sirius.components.diagrams.tests.graphql.DiagramEventSubscriptionRunner;
import org.eclipse.sirius.components.diagrams.tests.navigation.DiagramNavigator;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Integration tests for the diagram filter state mutation.
 *
 * @author Jerome Gout
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class SetDiagramFilterStateIntegrationTest extends AbstractIntegrationTests {

    @Autowired
    private DiagramEventSubscriptionRunner diagramEventSubscriptionRunner;

    @Autowired
    private ToolTester toolTester;

    @Autowired
    private SetDiagramFilterStateMutationRunner setDiagramFilterStateMutationRunner;

    @Autowired
    private RepresentationMetadataDiagramFiltersQueryRunner representationMetadataDiagramFiltersQueryRunner;

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @BeforeEach
    void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("GIVEN a LAB diagram containing a function, WHEN the Show Functions filter is deactivated, THEN the function is hidden")
    public void deactivateLABShowFunctionsDiagramFilter() {
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        String filterId = ShowFunctionsDiagramFilter.ID;

        var diagramEventInput = new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);
        var diagramEvents = this.diagramEventSubscriptionRunner.run(diagramEventInput).flux();

        // TODO : Add a component and a function in LAB diagram in capella-project.sql
        // Ideally, the test project should contain a component and a function that way, this test
        // could focus on checking the Show Functions filter without creating new elements.

        AtomicReference<String> componentNodeId = new AtomicReference<>();
        AtomicReference<String> functionNodeId = new AtomicReference<>();

        Consumer<Object> initialDiagramConsumer = assertRefreshedDiagramThat(initialDiagram -> assertThat(initialDiagram.getNodes()).isEmpty());

        Runnable createComponent = () -> {
            this.toolTester.invokeDiagramTool(CapellaProjectData.EDITING_CONTEXT_ID, representationId, "New Component");
        };
        Consumer<Object> componentCreationConsumer = assertRefreshedDiagramThat(updatedDiagram -> {
            assertThat(updatedDiagram.getNodes()).hasSize(1);
            componentNodeId.set(updatedDiagram.getNodes().getFirst().getId());
        });

        Runnable createFunction = () -> {
            this.toolTester.invokeTool(CapellaProjectData.EDITING_CONTEXT_ID, representationId, componentNodeId.get(), "New Function");
        };
        Consumer<Object> functionCreationConsumer = assertRefreshedDiagramThat(updatedDiagram -> {
            var componentNode = new DiagramNavigator(updatedDiagram).nodeWithId(componentNodeId.get()).getNode();
            assertThat(componentNode.getChildNodes()).hasSize(1);
            var functionNode = componentNode.getChildNodes().getFirst();
            assertThat(functionNode.getState()).isEqualTo(ViewModifier.Normal);
            functionNodeId.set(functionNode.getId());
        });

        Runnable deactivateFilter = () -> {
            assertThat(this.getDiagramFilterState(representationId, filterId)).isTrue();
            var input = new SetDiagramFilterStateInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId, filterId, false);
            var result = this.setDiagramFilterStateMutationRunner.run(input).data();

            String typename = JsonPath.read(result, "$.data.setDiagramFilterState.__typename");
            Boolean active = JsonPath.read(result, "$.data.setDiagramFilterState.active");
            assertThat(typename).withFailMessage(result).isEqualTo(SetDiagramFilterStateSuccessPayload.class.getSimpleName());
            assertThat(active).isFalse();
            assertThat(this.getDiagramFilterState(representationId, filterId)).isFalse();
        };
        Consumer<Object> hiddenFunctionConsumer = assertRefreshedDiagramThat(updatedDiagram -> {
            var functionNode = new DiagramNavigator(updatedDiagram).nodeWithId(functionNodeId.get()).getNode();
            assertThat(functionNode.getState()).isEqualTo(ViewModifier.Hidden);
        });

        StepVerifier.create(diagramEvents)
                .consumeNextWith(initialDiagramConsumer)
                .then(createComponent)
                .consumeNextWith(componentCreationConsumer)
                .then(createFunction)
                .consumeNextWith(functionCreationConsumer)
                .then(deactivateFilter)
                .consumeNextWith(hiddenFunctionConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("GIVEN an active OAB Show Activities filter, WHEN it is deactivated, THEN its new state is returned and exposed")
    public void deactivateOABShowActivitiesDiagramFilter() {
        String representationId = CapellaProjectData.GraphicalIds.OAB_OPERATIONAL_ANALYSIS_BLANK_DIAGRAM_ID;
        String filterId = ShowActivitiesDiagramFilter.ID;

        var diagramEventInput = new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);
        var diagramEvents = this.diagramEventSubscriptionRunner.run(diagramEventInput).flux();

        Runnable deactivateFilter = () -> {
            assertThat(this.getDiagramFilterState(representationId, filterId)).isTrue();
            var input = new SetDiagramFilterStateInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId, filterId, false);
            var result = this.setDiagramFilterStateMutationRunner.run(input).data();

            String typename = JsonPath.read(result, "$.data.setDiagramFilterState.__typename");
            Boolean active = JsonPath.read(result, "$.data.setDiagramFilterState.active");
            assertThat(typename).withFailMessage(result).isEqualTo(SetDiagramFilterStateSuccessPayload.class.getSimpleName());
            assertThat(active).isFalse();
            assertThat(this.getDiagramFilterState(representationId, filterId)).isFalse();
        };

        StepVerifier.create(diagramEvents)
                .then(deactivateFilter)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    private boolean getDiagramFilterState(String representationId, String filterId) {
        Map<String, Object> variables = Map.of(
                "editingContextId", CapellaProjectData.EDITING_CONTEXT_ID,
                "representationId", representationId
        );
        var result = this.representationMetadataDiagramFiltersQueryRunner.run(variables).data();
        List<Boolean> states = JsonPath.read(result, "$.data.viewer.editingContext.representation.diagramFilters[?(@.id == '" + filterId + "')].state");
        assertThat(states).hasSize(1);
        return states.get(0);
    }
}

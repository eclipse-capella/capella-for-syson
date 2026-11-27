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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.ToolTester;
import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.tests.graphql.DiagramEventSubscriptionRunner;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.graphql.tests.ExecuteEditingContextFunctionInput;
import org.eclipse.sirius.components.graphql.tests.api.IExecuteEditingContextFunctionRunner;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
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

    @Autowired
    private IExecuteEditingContextFunctionRunner executeEditingContextFunctionRunner;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("Given an empty LAB diagram, when the show all contained elements is invoked, then the appropriate elements are displayed on the diagram")
    public void showAllContainedElementsOnLABDiagram() {

        // This initialization should be removed once #378 is fixed: we should have all the elements in our test model.
        ExecuteEditingContextFunctionInput semanticInitializationInput = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, (editingContext, input) -> {
            IPayload result = new SuccessPayload(input.id());
            CommonQueryService commonQueryService = new CommonQueryService();
            CommonCreationService commonCreationService = new CommonCreationService();
            if (editingContext instanceof EditingContext siriusWebEditingContext) {
                Optional<Resource> optionalCapellaResource = siriusWebEditingContext.getDomain().getResourceSet().getResources().stream()
                        .filter(resource -> resource.eAdapters().stream()
                                .filter(ResourceMetadataAdapter.class::isInstance)
                                .map(ResourceMetadataAdapter.class::cast)
                                .anyMatch(resourceMetadata -> Objects.equals(resourceMetadata.getName(), "CapellaTest.sysml")))
                        .findFirst();
                if (optionalCapellaResource.isPresent()) {
                    Resource capellaResource = optionalCapellaResource.get();
                    Optional<PartUsage> optionalSystemComponent = commonQueryService.getArcadiaPerspectivePackage((Element) capellaResource.getContents().getFirst(),
                                    ArcadiaEngineeringPerspective.LogicalArchitecture)
                            .flatMap(commonQueryService::getStructurePackage)
                            .flatMap(structurePackage -> structurePackage.getOwnedMember().stream()
                                    .filter(commonQueryService::isComponent)
                                    .map(PartUsage.class::cast)
                                    .filter(member -> Objects.equals(member.getDeclaredName(), "system"))
                                    .findFirst());
                    if (optionalSystemComponent.isPresent()) {
                        PartUsage systemComponent = optionalSystemComponent.get();
                        // Create 2 components in the system component
                        commonCreationService.createComponent(systemComponent);
                        commonCreationService.createComponent(systemComponent);
                    } else {
                        result = new ErrorPayload(input.id(), "Cannot find LAB system component");
                    }
                } else {
                    result = new ErrorPayload(input.id(), "Cannot find Capella resource");
                }
            }
            return result;
        });

        assertThat(this.executeEditingContextFunctionRunner.execute(semanticInitializationInput).block(Duration.ofSeconds(10))).isInstanceOf(SuccessPayload.class);


        var input = new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID);
        var subscriptionResult = this.diagramEventSubscriptionRunner.run(input);
        assertThat(subscriptionResult.errors()).isEmpty();
        var diagramFlux = subscriptionResult.flux();

        Runnable executeShowAllContainedElementsOnDiagram = () -> {
            this.toolTester.invokeDiagramTool(CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID, "Show all contained elements");
        };

        AtomicReference<String> systemComponentId = new AtomicReference<>();

        var checkDiagramContentAfterDiagramTool = assertRefreshedDiagramThat(diagram -> {
            // This test should be completed to check for additional elements once #378 is fixed.
            assertThat(diagram.getNodes()).hasSize(1);
            assertThat(diagram.getNodes().getFirst().getInsideLabel().getText()).isEqualTo("system");
            systemComponentId.set(diagram.getNodes().getFirst().getId());
        });

        Runnable executeShowAllContainedElementsOnSystemComponent = () -> {
            this.toolTester.invokeTool(CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID, systemComponentId.get(),
                    "Show all contained elements");
        };

        var checkDiagramContentAfterNodeTool = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(1);
            Node systemComponentNode = diagram.getNodes().getFirst();
            assertThat(systemComponentNode.getInsideLabel().getText()).isEqualTo("system");
            assertThat(systemComponentNode.getChildNodes()).hasSize(2);
            assertThat(systemComponentNode.getChildNodes())
                    .map(childNode -> childNode.getInsideLabel().getText())
                    .containsExactlyInAnyOrder("C 1", "C 2");
        });

        StepVerifier.create(diagramFlux)
                .consumeNextWith(assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty()))
                .then(executeShowAllContainedElementsOnDiagram)
                .consumeNextWith(checkDiagramContentAfterDiagramTool)
                .then(executeShowAllContainedElementsOnSystemComponent)
                .consumeNextWith(checkDiagramContentAfterNodeTool)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}

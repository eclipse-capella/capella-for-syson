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
import org.eclipse.sirius.components.diagrams.Edge;
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
 * Integration tests of the <i>show all connected elements</i> tools.
 *
 * @author gdaniel
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class ShowAllConnectedElementsIntegrationTests extends AbstractIntegrationTests {

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
    @DisplayName("Given a LAB diagram with a component, when the show all connected elements is invoked on the component, then the appropriate elements are displayed on the diagram")
    public void showAllConnectedElementsOnComponentInLABDiagram() {
        this.initializeSemanticData();

        var input = new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID);
        var subscriptionResult = this.diagramEventSubscriptionRunner.run(input);
        assertThat(subscriptionResult.errors()).isNotNull();
        var diagramFlux = subscriptionResult.flux();

        // Executing the show all contained elements and checking the result should be removed once #378 is fixed: the diagram should contain enough data to perform this test without this initialization.
        Runnable executeShowAllContainedElementsRecursiveOnDiagram = () -> {
            this.toolTester.invokeDiagramTool(CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID,
                    "Show all contained elements (recursive)");
        };

        AtomicReference<String> component1Id = new AtomicReference<>();

        var checkDiagramContentAfterShowAllContainedElementsRecursive = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(1);
            assertThat(diagram.getEdges()).hasSize(0);
            assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(2);
            component1Id.set(diagram.getNodes().getFirst().getChildNodes().getFirst().getId());
        });

        Runnable executeShowAllConnectedElementsOnComponent = () -> {
            this.toolTester.invokeTool(CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID, component1Id.get(), "Show all connected elements");
        };

        var checkDiagramContentAfterShowAllConnectedElements = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(2);
            Node requirementNode = diagram.getNodes().stream()
                    .filter(node -> Objects.equals(node.getInsideLabel().getText(), "Requirement"))
                    .findFirst()
                    .get();
            Node componentNode = diagram.getNodes().getFirst().getChildNodes().getFirst();
            assertThat(diagram.getEdges()).hasSize(1);
            Edge describesEdge = diagram.getEdges().getFirst();
            assertThat(describesEdge.getSourceId()).isEqualTo(requirementNode.getId());
            assertThat(describesEdge.getTargetId()).isEqualTo(componentNode.getId());
        });

        StepVerifier.create(diagramFlux)
                .consumeNextWith(assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty()))
                .then(executeShowAllContainedElementsRecursiveOnDiagram)
                .consumeNextWith(checkDiagramContentAfterShowAllContainedElementsRecursive)
                .then(executeShowAllConnectedElementsOnComponent)
                .consumeNextWith(checkDiagramContentAfterShowAllConnectedElements)
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    private void initializeSemanticData() {
        // This initialization method should be removed once #378 is fixed: we should have all the elements in our test model.
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
                        var systemSubComponent1 = commonCreationService.createComponent(systemComponent);
                        var systemSubComponent2 = commonCreationService.createComponent(systemComponent);
                        // Create 2 requirements connected to each component
                        var requirement1 = commonCreationService.createRequirement(systemComponent);
                        var requirement2 = commonCreationService.createRequirement(systemComponent);
                        commonCreationService.createDescribes(requirement1, systemSubComponent1);
                        commonCreationService.createDescribes(requirement2, systemSubComponent2);
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
    }
}

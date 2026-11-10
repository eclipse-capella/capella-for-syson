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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.ToolTester;
import org.eclipse.capella.application.configuration.dto.CreateCapellaRepresentationInput;
import org.eclipse.capella.tests.graphql.CreateCapellaRepresentationMutationRunner;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
import org.eclipse.sirius.components.collaborative.dto.CreateRepresentationSuccessPayload;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.tests.graphql.DiagramEventSubscriptionRunner;
import org.eclipse.sirius.components.diagrams.tests.navigation.DiagramNavigator;
import org.eclipse.sirius.components.graphql.tests.ExecuteEditingContextFunctionInput;
import org.eclipse.sirius.components.graphql.tests.api.IExecuteEditingContextFunctionRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Regression tests for Describes creation through the connector palette.
 *
 * @author Jerome Gout
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class DescribesIntegrationTests extends AbstractIntegrationTests {

    private static final String EDITING_CONTEXT_ID = CapellaProjectData.EDITING_CONTEXT_ID;

    private static final String REPRESENTATION_ID = CapellaProjectData.GraphicalIds.SAB_SYSTEM_ANALYSIS_BLANK_DIAGRAM_ID;

    private static final String NEW_REQUIREMENT = "New Requirement";

    private static final String NEW_DESCRIBES = "New Describes";

    private static final String NEW_OPERATIONAL_ENTITY = "New Operational Entity";

    @Autowired
    private DiagramEventSubscriptionRunner diagramEventSubscriptionRunner;

    @Autowired
    private ToolTester toolTester;

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IExecuteEditingContextFunctionRunner executeEditingContextFunctionRunner;

    @Autowired
    private IObjectSearchService objectSearchService;

    @Autowired
    private CreateCapellaRepresentationMutationRunner createCapellaRepresentationMutationRunner;

    @BeforeEach
    void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    public void createDescribesToActorThroughConnectorPalette() {
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, REPRESENTATION_ID)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var actor = new AtomicReference<Node>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, "New Actor"))
                .consumeNextWith(this.captureNewRootNode(diagram, actor))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, requirement.get().getId(), actor.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), actor.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), actor.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createDescribesToComponentExchangeThroughConnectorPalette() {
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, REPRESENTATION_ID)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstActor = new AtomicReference<Node>();
        var secondActor = new AtomicReference<Node>();
        var componentExchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, "New Actor"))
                .consumeNextWith(this.captureNewRootNode(diagram, firstActor))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, "New Actor"))
                .consumeNextWith(this.captureNewRootNode(diagram, secondActor))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, firstActor.get().getId(), secondActor.get().getId(), "New Component Exchange"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    componentExchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, requirement.get().getId(), componentExchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), componentExchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), componentExchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createDescribesToFunctionalExchangeThroughConnectorPalette() {
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, REPRESENTATION_ID)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstActor = new AtomicReference<Node>();
        var secondActor = new AtomicReference<Node>();
        var firstFunction = new AtomicReference<Node>();
        var secondFunction = new AtomicReference<Node>();
        var functionalExchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, "New Actor"))
                .consumeNextWith(this.captureNewRootNode(diagram, firstActor))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, "New Actor"))
                .consumeNextWith(this.captureNewRootNode(diagram, secondActor))
                .then(() -> this.toolTester.invokeTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, firstActor.get().getId(), "New Function"))
                .consumeNextWith(this.captureFunction(firstActor, firstFunction))
                .then(() -> this.toolTester.invokeTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, secondActor.get().getId(), "New Function"))
                .consumeNextWith(this.captureFunction(secondActor, secondFunction))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, firstFunction.get().getId(), secondFunction.get().getId(), "New Functional Exchange"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    functionalExchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, REPRESENTATION_ID, requirement.get().getId(), functionalExchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), functionalExchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), functionalExchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createLABDescribesToComponentExchangeThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), secondEndpoint.get().getId(), "New Component Exchange"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, requirement.get().getId(), exchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createLABDescribesToFunctionalExchangeThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var firstFunction = new AtomicReference<Node>();
        var secondFunction = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), "New Function"))
                .consumeNextWith(this.captureFunction(firstEndpoint, firstFunction))
                .then(() -> this.toolTester.invokeTool(EDITING_CONTEXT_ID, representationId, secondEndpoint.get().getId(), "New Function"))
                .consumeNextWith(this.captureFunction(secondEndpoint, secondFunction))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstFunction.get().getId(), secondFunction.get().getId(), "New Functional Exchange"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, requirement.get().getId(), exchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createOABDescribesToCommunicationMeanThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.OAB_OPERATIONAL_ANALYSIS_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_OPERATIONAL_ENTITY))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_OPERATIONAL_ENTITY))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), secondEndpoint.get().getId(), "New Communication Mean"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, requirement.get().getId(), exchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createOCBDescribesToCommunicationMeanThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.OCB_OPERATIONAL_CAPABILITY_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_OPERATIONAL_ENTITY))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_OPERATIONAL_ENTITY))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), secondEndpoint.get().getId(), "New Communication Mean"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, requirement.get().getId(), exchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void createOAIBDescribesToInteractionThroughConnectorPalette() {
        var input = new CreateCapellaRepresentationInput(UUID.randomUUID(), EDITING_CONTEXT_ID, "OAIB");
        var result = this.createCapellaRepresentationMutationRunner.run(input).data();
        String typename = JsonPath.read(result, "$.data.createCapellaRepresentation.__typename");
        assertThat(typename).withFailMessage(result).isEqualTo(CreateRepresentationSuccessPayload.class.getSimpleName());
        String representationId = JsonPath.read(result, "$.data.createCapellaRepresentation.representation.id");
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Operational Activity"))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Operational Activity"))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), secondEndpoint.get().getId(), "New Interaction"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, requirement.get().getId(), exchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }


    @Test
    public void createOABDescribesToFunctionalExchangeThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.OAB_OPERATIONAL_ANALYSIS_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var requirement = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var firstActivity = new AtomicReference<Node>();
        var secondActivity = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var describes = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_REQUIREMENT))
                .consumeNextWith(this.captureNewRootNode(diagram, requirement))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_OPERATIONAL_ENTITY))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, NEW_OPERATIONAL_ENTITY))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), "New Operational Activity"))
                .consumeNextWith(this.captureFunction(firstEndpoint, firstActivity))
                .then(() -> this.toolTester.invokeTool(EDITING_CONTEXT_ID, representationId, secondEndpoint.get().getId(), "New Operational Activity"))
                .consumeNextWith(this.captureFunction(secondEndpoint, secondActivity))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstActivity.get().getId(), secondActivity.get().getId(), "New Functional Exchange"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, requirement.get().getId(), exchange.get().getId(), NEW_DESCRIBES))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    describes.set(this.assertDescribesEdge(updated, requirement.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(describes.get(), requirement.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    private Edge assertDescribesEdge(Diagram diagram, Node requirement, String targetId) {
        var links = diagram.getEdges().stream().filter(edge -> edge.getSourceId().equals(requirement.getId())).toList();
        assertThat(links).singleElement().satisfies(edge -> assertThat(edge.getTargetId()).isEqualTo(targetId));
        return links.getFirst();
    }

    private Consumer<Object> captureNewRootNode(AtomicReference<Diagram> previousDiagram, AtomicReference<Node> node) {
        return assertRefreshedDiagramThat(updated -> {
            var previousIds = previousDiagram.get().getNodes().stream().map(Node::getId).toList();
            var newNodes = updated.getNodes().stream().filter(candidate -> !previousIds.contains(candidate.getId())).toList();
            assertThat(newNodes).hasSize(1);
            node.set(newNodes.getFirst());
            previousDiagram.set(updated);
        });
    }

    private Consumer<Object> captureFunction(AtomicReference<Node> actor, AtomicReference<Node> function) {
        return assertRefreshedDiagramThat(updated -> {
            var children = new DiagramNavigator(updated).nodeWithId(actor.get().getId()).getNode().getChildNodes();
            assertThat(children).hasSize(1);
            function.set(children.getFirst());
        });
    }

    private void assertSemanticEndpoints(Edge describes, String sourceId, String targetId) {
        var input = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), EDITING_CONTEXT_ID, (editingContext, request) -> {
            var source = this.objectSearchService.getObject(editingContext, sourceId).map(Element.class::cast).orElseThrow();
            var target = this.objectSearchService.getObject(editingContext, targetId).map(Element.class::cast).orElseThrow();
            var relationship = this.objectSearchService.getObject(editingContext, describes.getTargetObjectId()).orElseThrow();
            assertThat(relationship).isInstanceOfSatisfying(AllocationUsage.class, allocation -> {
                assertThat(allocation.getSource()).containsExactly(source);
                assertThat(allocation.getTarget()).containsExactly(target);
            });
            return new SuccessPayload(request.id());
        });
        assertThat(this.executeEditingContextFunctionRunner.execute(input).block(Duration.ofSeconds(5))).isInstanceOf(SuccessPayload.class);
    }
}

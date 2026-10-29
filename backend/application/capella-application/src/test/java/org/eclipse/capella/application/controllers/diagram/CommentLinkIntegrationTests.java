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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.ToolTester;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
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
import org.eclipse.syson.sysml.Annotation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Regression tests for LAB comment links created through the connector palette.
 *
 * @author Jerome Gout
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class CommentLinkIntegrationTests extends AbstractIntegrationTests {

    private static final String EDITING_CONTEXT_ID = CapellaProjectData.EDITING_CONTEXT_ID;

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

    @BeforeEach
    void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    public void linkCommentToComponentThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var comment = new AtomicReference<Node>();
        var component = new AtomicReference<Node>();
        var annotation = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Comment"))
                .consumeNextWith(this.captureNewRootNode(diagram, comment))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, component))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, comment.get().getId(), component.get().getId(), "Link to Element"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    annotation.set(this.assertAnnotationEdge(updated, comment.get(), component.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(annotation.get(), comment.get().getTargetObjectId(), component.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void linkCommentToComponentExchangeThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var comment = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var annotation = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Comment"))
                .consumeNextWith(this.captureNewRootNode(diagram, comment))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, firstEndpoint))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Component"))
                .consumeNextWith(this.captureNewRootNode(diagram, secondEndpoint))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, firstEndpoint.get().getId(), secondEndpoint.get().getId(), "New Component Exchange"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(1);
                    exchange.set(updated.getEdges().getFirst());
                }))
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, comment.get().getId(), exchange.get().getId(), "Link to Element"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    annotation.set(this.assertAnnotationEdge(updated, comment.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(annotation.get(), comment.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    @Test
    public void linkCommentToFunctionalExchangeThroughConnectorPalette() {
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.diagramEventSubscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), EDITING_CONTEXT_ID, representationId)).flux();
        var diagram = new AtomicReference<Diagram>();
        var comment = new AtomicReference<Node>();
        var firstEndpoint = new AtomicReference<Node>();
        var secondEndpoint = new AtomicReference<Node>();
        var firstFunction = new AtomicReference<Node>();
        var secondFunction = new AtomicReference<Node>();
        var exchange = new AtomicReference<Edge>();
        var annotation = new AtomicReference<Edge>();

        StepVerifier.create(events)
                .consumeNextWith(assertRefreshedDiagramThat(diagram::set))
                .then(() -> this.toolTester.invokeDiagramTool(EDITING_CONTEXT_ID, representationId, "New Comment"))
                .consumeNextWith(this.captureNewRootNode(diagram, comment))
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
                .then(() -> this.toolTester.invokeConnectorTool(EDITING_CONTEXT_ID, representationId, comment.get().getId(), exchange.get().getId(), "Link to Element"))
                .consumeNextWith(assertRefreshedDiagramThat(updated -> {
                    assertThat(updated.getEdges()).hasSize(2);
                    annotation.set(this.assertAnnotationEdge(updated, comment.get(), exchange.get().getId()));
                }))
                .then(() -> this.assertSemanticEndpoints(annotation.get(), comment.get().getTargetObjectId(), exchange.get().getTargetObjectId()))
                .thenCancel()
                .verify(Duration.ofSeconds(30));
    }

    private Edge assertAnnotationEdge(Diagram diagram, Node comment, String targetId) {
        var links = diagram.getEdges().stream().filter(edge -> edge.getSourceId().equals(comment.getId())).toList();
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

    private Consumer<Object> captureFunction(AtomicReference<Node> component, AtomicReference<Node> function) {
        return assertRefreshedDiagramThat(updated -> {
            var children = new DiagramNavigator(updated).nodeWithId(component.get().getId()).getNode().getChildNodes();
            assertThat(children).hasSize(1);
            function.set(children.getFirst());
        });
    }

    private void assertSemanticEndpoints(Edge annotation, String sourceId, String targetId) {
        var input = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), EDITING_CONTEXT_ID, (editingContext, request) -> {
            var source = this.objectSearchService.getObject(editingContext, sourceId).orElseThrow();
            var target = this.objectSearchService.getObject(editingContext, targetId).orElseThrow();
            var relationship = this.objectSearchService.getObject(editingContext, annotation.getTargetObjectId()).orElseThrow();
            assertThat(relationship).isInstanceOfSatisfying(Annotation.class, link -> {
                assertThat(link.getAnnotatingElement()).isSameAs(source);
                assertThat(link.getAnnotatedElement()).isSameAs(target);
            });
            return new SuccessPayload(request.id());
        });
        assertThat(this.executeEditingContextFunctionRunner.execute(input).block(Duration.ofSeconds(5))).isInstanceOf(SuccessPayload.class);
    }
}

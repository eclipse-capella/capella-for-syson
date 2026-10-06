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
import java.util.Set;
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
import org.eclipse.capella.tests.graphql.SetDiagramFilterStateMutationRunner;
import org.eclipse.sirius.components.collaborative.diagrams.dto.DiagramEventInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.ViewModifier;
import org.eclipse.sirius.components.diagrams.tests.graphql.DiagramEventSubscriptionRunner;
import org.eclipse.sirius.components.diagrams.tests.navigation.DiagramNavigator;
import org.eclipse.sirius.components.graphql.tests.ExecuteEditingContextFunctionInput;
import org.eclipse.sirius.components.graphql.tests.api.IExecuteEditingContextFunctionRunner;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Checks related element palettes and invokes population tools through GraphQL.
 *
 * @author jgout
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class RelatedElementsIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private DiagramEventSubscriptionRunner subscriptionRunner;

    @Autowired
    private ToolTester toolTester;

    @Autowired
    private SetDiagramFilterStateMutationRunner setDiagramFilterStateMutationRunner;

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IExecuteEditingContextFunctionRunner executeEditingContextFunctionRunner;

    @BeforeEach
    void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void diagramPopulationRestoresComponentsAccordingToRecursionMode(boolean recursive) {
        String editingContextId = CapellaProjectData.EDITING_CONTEXT_ID;
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), editingContextId, representationId)).flux();
        var parentNodeId = new AtomicReference<String>();
        String tool = "Show all contained elements";
        if (recursive) {
            tool += " recursively";
        }
        String toolLabel = tool;

        Consumer<Object> emptyDiagramConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty());

        Runnable createComponent = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, "New Component");

        Consumer<Object> componentCreationConsumer = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(1);
            parentNodeId.set(diagram.getNodes().getFirst().getId());
        });

        Runnable createChildComponent = () -> this.toolTester.invokeTool(editingContextId, representationId, parentNodeId.get(), "New Component");

        Consumer<Object> childComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(1));

        Runnable deleteComponent = () -> this.toolTester.invokeTool(editingContextId, representationId, parentNodeId.get(), "Delete from Diagram");

        Runnable populateDiagram = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, toolLabel);

        Consumer<Object> populationConsumer = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(1);
            int expectedChildren = 0;
            if (recursive) {
                expectedChildren = 1;
            }
            assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(expectedChildren);
        });

        StepVerifier.create(events)
                .consumeNextWith(emptyDiagramConsumer)
                .then(createComponent)
                .consumeNextWith(componentCreationConsumer)
                .then(createChildComponent)
                .consumeNextWith(childComponentCreationConsumer)
                .then(deleteComponent)
                .consumeNextWith(emptyDiagramConsumer)
                .then(populateDiagram)
                .consumeNextWith(populationConsumer)
                .then(populateDiagram)
                .consumeNextWith(populationConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    @Test
    public void recursivePopulationPreservesTheFunctionsFilter() {
        String editingContextId = CapellaProjectData.EDITING_CONTEXT_ID;
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), editingContextId, representationId)).flux();
        var firstComponentId = new AtomicReference<String>();
        var secondComponentId = new AtomicReference<String>();
        var deletedFunctionId = new AtomicReference<String>();

        Consumer<Object> emptyDiagramConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty());

        Runnable createComponent = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, "New Component");

        Consumer<Object> firstComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> firstComponentId.set(diagram.getNodes().getFirst().getId()));

        Runnable createFirstFunction = () -> this.toolTester.invokeTool(editingContextId, representationId, firstComponentId.get(), "New Function");

        Consumer<Object> firstFunctionCreationConsumer = assertRefreshedDiagramThat(diagram -> {
            var component = new DiagramNavigator(diagram).nodeWithId(firstComponentId.get()).getNode();
            assertThat(component.getChildNodes()).hasSize(1);
            assertThat(component.getChildNodes().getFirst().getState()).isEqualTo(ViewModifier.Normal);
        });

        Consumer<Object> secondComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes()).hasSize(2);
            secondComponentId.set(diagram.getNodes().stream().filter(node -> !node.getId().equals(firstComponentId.get())).findFirst().orElseThrow().getId());
        });

        Runnable createSecondFunction = () -> this.toolTester.invokeTool(editingContextId, representationId, secondComponentId.get(), "New Function");

        Consumer<Object> secondFunctionCreationConsumer = assertRefreshedDiagramThat(diagram -> {
            var component = new DiagramNavigator(diagram).nodeWithId(secondComponentId.get()).getNode();
            assertThat(component.getChildNodes()).hasSize(1);
            deletedFunctionId.set(component.getChildNodes().getFirst().getId());
        });

        Runnable deleteSecondFunction = () -> this.toolTester.invokeTool(editingContextId, representationId, deletedFunctionId.get(), "Delete from Diagram");

        Consumer<Object> functionDeletionConsumer = assertRefreshedDiagramThat(diagram -> {
            var component = new DiagramNavigator(diagram).nodeWithId(secondComponentId.get()).getNode();
            assertThat(component.getChildNodes()).isEmpty();
        });

        Runnable deactivateFunctionsFilter = () -> this.setDiagramFilterState(representationId, ShowFunctionsDiagramFilter.ID, false);

        Consumer<Object> hiddenFunctionConsumer = assertRefreshedDiagramThat(diagram -> {
            var component = new DiagramNavigator(diagram).nodeWithId(firstComponentId.get()).getNode();
            assertThat(component.getChildNodes().getFirst().getState()).isEqualTo(ViewModifier.Hidden);
        });

        Runnable populateDiagram = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, "Show all contained elements recursively");

        Consumer<Object> hiddenFunctionsPopulationConsumer = this.getFunctionsVisibilityConsumer(firstComponentId, secondComponentId, ViewModifier.Hidden);

        Runnable activateFunctionsFilter = () -> this.setDiagramFilterState(representationId, ShowFunctionsDiagramFilter.ID, true);

        Consumer<Object> visibleFunctionsConsumer = this.getFunctionsVisibilityConsumer(firstComponentId, secondComponentId, ViewModifier.Normal);

        StepVerifier.create(events)
                .consumeNextWith(emptyDiagramConsumer)
                .then(createComponent)
                .consumeNextWith(firstComponentCreationConsumer)
                .then(createFirstFunction)
                .consumeNextWith(firstFunctionCreationConsumer)
                .then(createComponent)
                .consumeNextWith(secondComponentCreationConsumer)
                .then(createSecondFunction)
                .consumeNextWith(secondFunctionCreationConsumer)
                .then(deleteSecondFunction)
                .consumeNextWith(functionDeletionConsumer)
                .then(deactivateFunctionsFilter)
                .consumeNextWith(hiddenFunctionConsumer)
                .then(populateDiagram)
                .consumeNextWith(hiddenFunctionsPopulationConsumer)
                .then(activateFunctionsFilter)
                .consumeNextWith(visibleFunctionsConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    private Consumer<Object> getFunctionsVisibilityConsumer(AtomicReference<String> firstComponentId,  AtomicReference<String> secondComponentId, ViewModifier modifier) {
        return assertRefreshedDiagramThat(diagram -> {
            assertThat(List.of(firstComponentId.get(), secondComponentId.get())).allSatisfy(componentId -> {
                var component = new DiagramNavigator(diagram).nodeWithId(componentId).getNode();
                assertThat(component.getChildNodes()).hasSize(1);
                assertThat(component.getChildNodes().getFirst().getState()).isEqualTo(modifier);
            });
        });
    }

    @Test
    public void editableDiagramNodesSupportingChildrenOfferPopulationTools() {
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID,
                CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID)).flux();

        Runnable checkNodePalettes = () -> this.checkEditingContext(editingContext -> {
            assertThat(editingContext).isInstanceOf(EditingContext.class);
            var diagrams = ((EditingContext) editingContext).getViews().stream().flatMap(view -> view.getDescriptions().stream())
                    .filter(DiagramDescription.class::isInstance).map(DiagramDescription.class::cast)
                    .filter(diagram -> Set.of("LAB", "SAB", "OAB", "OABD", "OAIB", "OCB").contains(diagram.getName().split(" - ")[0])).toList();
            assertThat(diagrams).hasSize(6);
            diagrams.forEach(diagram -> {
                var nodes = EMFUtils.allContainedObjectOfType(diagram, NodeDescription.class)
                        .filter(node -> !node.getChildrenDescriptions().isEmpty() || !node.getReusedChildNodeDescriptions().isEmpty()).toList();
                assertThat(nodes).isNotEmpty().allSatisfy(node -> {
                    assertThat(node.getPalette()).as("Palette of %s in %s", node.getName(), diagram.getName()).isNotNull();
                    assertThat(node.getPalette().getToolSections()).filteredOn(section -> "Related Elements".equals(section.getName()))
                            .singleElement().satisfies(section -> assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                                    .containsExactly("Show all contained elements", "Show all contained elements recursively"));
                });
            });
        });

        StepVerifier.create(events)
                // Check that nodes supporting children offer population tools in all six editable diagram types.
                .then(checkNodePalettes)
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    @Test
    public void editableDiagramNodesWithoutChildrenDoNotOfferPopulationTools() {
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID,
                CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID)).flux();

        Runnable checkLeafNodePalettes = () -> this.checkEditingContext(editingContext -> {
            assertThat(editingContext).isInstanceOf(EditingContext.class);
            var diagrams = ((EditingContext) editingContext).getViews().stream().flatMap(view -> view.getDescriptions().stream())
                    .filter(DiagramDescription.class::isInstance).map(DiagramDescription.class::cast)
                    .filter(diagram -> Set.of("LAB", "SAB", "OAB", "OABD", "OAIB", "OCB").contains(diagram.getName().split(" - ")[0])).toList();
            assertThat(diagrams).hasSize(6);
            diagrams.forEach(diagram -> {
                var nodes = EMFUtils.allContainedObjectOfType(diagram, NodeDescription.class)
                        .filter(node -> node.getChildrenDescriptions().isEmpty() && node.getReusedChildNodeDescriptions().isEmpty()).toList();
                assertThat(nodes).isNotEmpty();
                var sections = nodes.stream().map(NodeDescription::getPalette).filter(palette -> palette != null)
                        .flatMap(palette -> palette.getToolSections().stream()).toList();
                assertThat(sections).as("Sections of nodes without children in %s", diagram.getName())
                        .noneMatch(section -> "Related Elements".equals(section.getName()));
            });
        });

        StepVerifier.create(events)
                // Check that leaf nodes in all six editable diagram types have no related elements section.
                .then(checkLeafNodePalettes)
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    @Test
    public void directPopulationRestoresOnlySelectedComponentChildren() {
        String editingContextId = CapellaProjectData.EDITING_CONTEXT_ID;
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), editingContextId, representationId)).flux();
        var selectedId = new AtomicReference<String>();
        var childId = new AtomicReference<String>();
        var siblingId = new AtomicReference<String>();
        Consumer<Object> initialDiagramConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty());

        Runnable createComponent = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, "New Component");

        Consumer<Object> selectedComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> selectedId.set(diagram.getNodes().getFirst().getId()));

        Runnable createSelectedChild = () -> this.toolTester.invokeTool(editingContextId, representationId, selectedId.get(), "New Component");

        Consumer<Object> selectedChildCreationConsumer = assertRefreshedDiagramThat(diagram -> childId.set(diagram.getNodes().getFirst().getChildNodes().getFirst().getId()));

        Runnable createGrandchild = () -> this.toolTester.invokeTool(editingContextId, representationId, childId.get(), "New Component");

        Consumer<Object> grandchildCreationConsumer = assertRefreshedDiagramThat(diagram -> assertThat(new DiagramNavigator(diagram).nodeWithId(childId.get()).getNode().getChildNodes()).hasSize(1));

        Runnable deleteChild = () -> this.toolTester.invokeTool(editingContextId, representationId, childId.get(), "Delete from Diagram");

        Consumer<Object> selectedChildDeletionConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes()).isEmpty());

        Consumer<Object> siblingComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> siblingId.set(diagram.getNodes().stream()
                .filter(node -> !node.getId().equals(selectedId.get())).findFirst().orElseThrow().getId()));

        Runnable createSiblingChild = () -> this.toolTester.invokeTool(editingContextId, representationId, siblingId.get(), "New Component");

        Consumer<Object> siblingChildCreationConsumer = assertRefreshedDiagramThat(diagram -> childId.set(new DiagramNavigator(diagram).nodeWithId(siblingId.get()).getNode().getChildNodes().getFirst().getId()));

        Consumer<Object> siblingChildDeletionConsumer = assertRefreshedDiagramThat(diagram -> assertThat(new DiagramNavigator(diagram).nodeWithId(siblingId.get()).getNode().getChildNodes()).isEmpty());

        Runnable populateSelectedComponent = () -> this.toolTester.invokeTool(editingContextId, representationId, selectedId.get(), "Show all contained elements");

        Consumer<Object> populationConsumer = assertRefreshedDiagramThat(diagram -> {
            // Check that only the direct child of the selected component is restored, with no grandchild or sibling child.
            assertThat(diagram.getNodes()).hasSize(2);
            var selected = new DiagramNavigator(diagram).nodeWithId(selectedId.get()).getNode();
            assertThat(selected.getChildNodes()).hasSize(1);
            assertThat(selected.getChildNodes().getFirst().getChildNodes()).isEmpty();
            assertThat(new DiagramNavigator(diagram).nodeWithId(siblingId.get()).getNode().getChildNodes()).isEmpty();
        });

        StepVerifier.create(events)
                .consumeNextWith(initialDiagramConsumer)
                .then(createComponent)
                .consumeNextWith(selectedComponentCreationConsumer)
                .then(createSelectedChild)
                .consumeNextWith(selectedChildCreationConsumer)
                .then(createGrandchild)
                .consumeNextWith(grandchildCreationConsumer)
                .then(deleteChild)
                .consumeNextWith(selectedChildDeletionConsumer)
                .then(createComponent)
                .consumeNextWith(siblingComponentCreationConsumer)
                .then(createSiblingChild)
                .consumeNextWith(siblingChildCreationConsumer)
                .then(deleteChild)
                .consumeNextWith(siblingChildDeletionConsumer)
                .then(populateSelectedComponent)
                .consumeNextWith(populationConsumer)
                .then(populateSelectedComponent)
                .consumeNextWith(populationConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    @Test
    public void recursivePopulationRestoresOnlySelectedComponentDescendants() {
        String editingContextId = CapellaProjectData.EDITING_CONTEXT_ID;
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), editingContextId, representationId)).flux();
        var selectedId = new AtomicReference<String>();
        var childId = new AtomicReference<String>();
        var siblingId = new AtomicReference<String>();
        Consumer<Object> initialDiagramConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty());

        Runnable createComponent = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, "New Component");

        Consumer<Object> selectedComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> selectedId.set(diagram.getNodes().getFirst().getId()));

        Runnable createSelectedChild = () -> this.toolTester.invokeTool(editingContextId, representationId, selectedId.get(), "New Component");

        Consumer<Object> selectedChildCreationConsumer = assertRefreshedDiagramThat(diagram -> childId.set(diagram.getNodes().getFirst().getChildNodes().getFirst().getId()));

        Runnable createGrandchild = () -> this.toolTester.invokeTool(editingContextId, representationId, childId.get(), "New Component");

        Consumer<Object> grandchildCreationConsumer = assertRefreshedDiagramThat(diagram -> assertThat(new DiagramNavigator(diagram).nodeWithId(childId.get()).getNode().getChildNodes()).hasSize(1));

        Runnable deleteChild = () -> this.toolTester.invokeTool(editingContextId, representationId, childId.get(), "Delete from Diagram");

        Consumer<Object> selectedChildDeletionConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes()).isEmpty());

        Consumer<Object> siblingComponentCreationConsumer = assertRefreshedDiagramThat(diagram -> siblingId.set(diagram.getNodes().stream()
                .filter(node -> !node.getId().equals(selectedId.get())).findFirst().orElseThrow().getId()));

        Runnable createSiblingChild = () -> this.toolTester.invokeTool(editingContextId, representationId, siblingId.get(), "New Component");

        Consumer<Object> siblingChildCreationConsumer = assertRefreshedDiagramThat(diagram -> childId.set(new DiagramNavigator(diagram).nodeWithId(siblingId.get()).getNode().getChildNodes().getFirst().getId()));

        Consumer<Object> siblingChildDeletionConsumer = assertRefreshedDiagramThat(diagram -> assertThat(new DiagramNavigator(diagram).nodeWithId(siblingId.get()).getNode().getChildNodes()).isEmpty());

        Runnable populateSelectedComponent = () -> this.toolTester.invokeTool(editingContextId, representationId, selectedId.get(), "Show all contained elements recursively");

        Consumer<Object> populationConsumer = assertRefreshedDiagramThat(diagram -> {
            // Check that the child and grandchild of the selected component are restored while the sibling still has no children.
            assertThat(diagram.getNodes()).hasSize(2);
            var selected = new DiagramNavigator(diagram).nodeWithId(selectedId.get()).getNode();
            assertThat(selected.getChildNodes()).hasSize(1);
            assertThat(selected.getChildNodes().getFirst().getChildNodes()).hasSize(1);
            assertThat(new DiagramNavigator(diagram).nodeWithId(siblingId.get()).getNode().getChildNodes()).isEmpty();
        });

        StepVerifier.create(events)
                .consumeNextWith(initialDiagramConsumer)
                .then(createComponent)
                .consumeNextWith(selectedComponentCreationConsumer)
                .then(createSelectedChild)
                .consumeNextWith(selectedChildCreationConsumer)
                .then(createGrandchild)
                .consumeNextWith(grandchildCreationConsumer)
                .then(deleteChild)
                .consumeNextWith(selectedChildDeletionConsumer)
                .then(createComponent)
                .consumeNextWith(siblingComponentCreationConsumer)
                .then(createSiblingChild)
                .consumeNextWith(siblingChildCreationConsumer)
                .then(deleteChild)
                .consumeNextWith(siblingChildDeletionConsumer)
                .then(populateSelectedComponent)
                .consumeNextWith(populationConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    @Test
    public void recursiveSelectedComponentPopulationPreservesTheFunctionsFilter() {
        String editingContextId = CapellaProjectData.EDITING_CONTEXT_ID;
        String representationId = CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID;
        String filterId = ShowFunctionsDiagramFilter.ID;
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), editingContextId, representationId)).flux();
        var componentId = new AtomicReference<String>();
        var removedFunctionId = new AtomicReference<String>();
        Consumer<Diagram> checkPopulation = diagram -> {
            assertThat(diagram.getNodes()).hasSize(1);
            var component = new DiagramNavigator(diagram).nodeWithId(componentId.get()).getNode();
            assertThat(component.getChildNodes()).hasSize(2).allSatisfy(function -> assertThat(function.getState()).isEqualTo(ViewModifier.Hidden));
        };
        Consumer<Object> initialDiagramConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes()).isEmpty());

        Runnable createComponent = () -> this.toolTester.invokeDiagramTool(editingContextId, representationId, "New Component");

        Consumer<Object> componentCreationConsumer = assertRefreshedDiagramThat(diagram -> componentId.set(diagram.getNodes().getFirst().getId()));

        Runnable createFunction = () -> this.toolTester.invokeTool(editingContextId, representationId, componentId.get(), "New Function");

        Consumer<Object> firstFunctionCreationConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(1));

        Consumer<Object> secondFunctionCreationConsumer = assertRefreshedDiagramThat(diagram -> {
            assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(2);
            removedFunctionId.set(diagram.getNodes().getFirst().getChildNodes().getLast().getId());
        });

        Runnable deleteFunction = () -> this.toolTester.invokeTool(editingContextId, representationId, removedFunctionId.get(), "Delete from Diagram");

        Consumer<Object> functionDeletionConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(1));

        Runnable deactivateFunctionsFilter = () -> this.setDiagramFilterState(representationId, filterId, false);

        Consumer<Object> hiddenFunctionConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes().getFirst().getState()).isEqualTo(ViewModifier.Hidden));

        Runnable populateComponent = () -> this.toolTester.invokeTool(editingContextId, representationId, componentId.get(), "Show all contained elements recursively");

        Consumer<Object> populationConsumer = assertRefreshedDiagramThat(checkPopulation);

        Runnable activateFunctionsFilter = () -> this.setDiagramFilterState(representationId, filterId, true);

        Consumer<Object> visibleFunctionsConsumer = assertRefreshedDiagramThat(diagram -> assertThat(diagram.getNodes().getFirst().getChildNodes()).hasSize(2)
                .allSatisfy(function -> assertThat(function.getState()).isEqualTo(ViewModifier.Normal)));

        StepVerifier.create(events)
                .consumeNextWith(initialDiagramConsumer)
                .then(createComponent)
                .consumeNextWith(componentCreationConsumer)
                .then(createFunction)
                .consumeNextWith(firstFunctionCreationConsumer)
                .then(createFunction)
                .consumeNextWith(secondFunctionCreationConsumer)
                .then(deleteFunction)
                .consumeNextWith(functionDeletionConsumer)
                .then(deactivateFunctionsFilter)
                .consumeNextWith(hiddenFunctionConsumer)
                .then(populateComponent)
                .consumeNextWith(populationConsumer)
                .then(activateFunctionsFilter)
                .consumeNextWith(visibleFunctionsConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    private void checkEditingContext(Consumer<IEditingContext> check) {
        var input = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, (editingContext, executeInput) -> {
            check.accept(editingContext);
            return new SuccessPayload(executeInput.id());
        });
        assertThat(this.executeEditingContextFunctionRunner.execute(input).block()).isInstanceOf(SuccessPayload.class);
    }

    private void setDiagramFilterState(String representationId, String filterId, boolean state) {
        var input = new SetDiagramFilterStateInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId, filterId, state);
        var result = this.setDiagramFilterStateMutationRunner.run(input).data();
        String typename = JsonPath.read(result, "$.data.setDiagramFilterState.__typename");
        Boolean active = JsonPath.read(result, "$.data.setDiagramFilterState.active");
        assertThat(typename).withFailMessage(result).isEqualTo(SetDiagramFilterStateSuccessPayload.class.getSimpleName());
        assertThat(active).isEqualTo(state);
    }
}

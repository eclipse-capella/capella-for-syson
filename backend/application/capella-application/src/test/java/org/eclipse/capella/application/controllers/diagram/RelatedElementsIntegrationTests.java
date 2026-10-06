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
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("GIVEN existing nested components, WHEN the diagram is populated, THEN components are restored according to the population mode")
    public void populateExistingComponents(boolean recursive) {
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
                // Check that the diagram is initially empty.
                .consumeNextWith(emptyDiagramConsumer)
                // Create the parent component at the top level of the diagram.
                .then(createComponent)
                // Check that the parent is present and remember its ID.
                .consumeNextWith(componentCreationConsumer)
                // Create a child inside the parent component.
                .then(createChildComponent)
                // Check that the parent contains one child.
                .consumeNextWith(childComponentCreationConsumer)
                // Remove the parent and its child from the diagram while keeping them in the model.
                .then(deleteComponent)
                // Check that removing the parent leaves the diagram empty.
                .consumeNextWith(emptyDiagramConsumer)
                // Populate the diagram using the selected direct or recursive mode.
                .then(populateDiagram)
                // Check that the parent is restored and its child appears only in recursive mode.
                .consumeNextWith(populationConsumer)
                // Populate the diagram again to check that repeated population does not create duplicates.
                .then(populateDiagram)
                // Check that the same parent and expected number of children are still present.
                .consumeNextWith(populationConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    @Test
    @DisplayName("GIVEN a LAB diagram with hidden functions, WHEN the diagram is populated recursively, THEN the Show Functions filter is preserved")
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

        Consumer<Object> hiddenFunctionsPopulationConsumer = this.getFunctionsVisiblityConsumer(firstComponentId, secondComponentId, ViewModifier.Hidden);

        Runnable activateFunctionsFilter = () -> this.setDiagramFilterState(representationId, ShowFunctionsDiagramFilter.ID, true);

        Consumer<Object> visibleFunctionsConsumer = this.getFunctionsVisiblityConsumer(firstComponentId, secondComponentId, ViewModifier.Normal);

        StepVerifier.create(events)
                .consumeNextWith(emptyDiagramConsumer)
                // Create the first top-level component.
                .then(createComponent)
                // Remember the first component ID for the following steps.
                .consumeNextWith(firstComponentCreationConsumer)
                // Create a function allocated to the first component.
                .then(createFirstFunction)
                // Check that the first component contains one visible function.
                .consumeNextWith(firstFunctionCreationConsumer)
                // Create a second top-level component as a sibling of the first component.
                .then(createComponent)
                // Check that both sibling components are present and remember the second component ID.
                .consumeNextWith(secondComponentCreationConsumer)
                // Create a function allocated to the second component.
                .then(createSecondFunction)
                // Check that the second component contains one function and remember its ID.
                .consumeNextWith(secondFunctionCreationConsumer)
                // Remove the second function from the diagram while keeping it in the model.
                .then(deleteSecondFunction)
                // Check that the second component no longer shows a function.
                .consumeNextWith(functionDeletionConsumer)
                // Turn off Show Functions to hide allocated functions.
                .then(deactivateFunctionsFilter)
                // Check that the function of the first component is now hidden.
                .consumeNextWith(hiddenFunctionConsumer)
                // Recursively populate the diagram to restore the function of the second component.
                .then(populateDiagram)
                // Check that both components contain one function and that both functions remain hidden.
                .consumeNextWith(hiddenFunctionsPopulationConsumer)
                // Turn on Show Functions to display allocated functions again.
                .then(activateFunctionsFilter)
                // Check that the functions of both sibling components are visible.
                .consumeNextWith(visibleFunctionsConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    private Consumer<Object> getFunctionsVisiblityConsumer(AtomicReference<String> firstComponentId,  AtomicReference<String> secondComponentId, ViewModifier modifier) {
        return assertRefreshedDiagramThat(diagram -> {
            assertThat(List.of(firstComponentId.get(), secondComponentId.get())).allSatisfy(componentId -> {
                var component = new DiagramNavigator(diagram).nodeWithId(componentId).getNode();
                assertThat(component.getChildNodes()).hasSize(1);
                assertThat(component.getChildNodes().getFirst().getState()).isEqualTo(modifier);
            });
        });
    }

    @Test
    @DisplayName("GIVEN editable Capella diagrams, WHEN node palettes are inspected, THEN every node offers direct and recursive population tools")
    public void allEditableDiagramNodesOfferPopulationTools() {
        var events = this.subscriptionRunner.run(new DiagramEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID,
                CapellaProjectData.GraphicalIds.LAB_LOGICAL_ARCHITECTURE_BLANK_DIAGRAM_ID)).flux();

        Runnable checkNodePalettes = () -> this.checkEditingContext(editingContext -> {
            assertThat(editingContext).isInstanceOf(EditingContext.class);
            var diagrams = ((EditingContext) editingContext).getViews().stream().flatMap(view -> view.getDescriptions().stream())
                    .filter(DiagramDescription.class::isInstance).map(DiagramDescription.class::cast)
                    .filter(diagram -> Set.of("LAB", "SAB", "OAB", "OABD", "OAIB", "OCB").contains(diagram.getName().split(" - ")[0])).toList();
            assertThat(diagrams).hasSize(6);
            diagrams.forEach(diagram -> {
                var nodes = EMFUtils.allContainedObjectOfType(diagram, NodeDescription.class).toList();
                assertThat(nodes).isNotEmpty().allSatisfy(node -> {
                    assertThat(node.getPalette()).as("Palette of %s in %s", node.getName(), diagram.getName()).isNotNull();
                    assertThat(node.getPalette().getToolSections()).filteredOn(section -> "Related Elements".equals(section.getName()))
                            .singleElement().satisfies(section -> assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                                    .containsExactly("Show all contained elements", "Show all contained elements recursively"));
                });
            });
        });

        StepVerifier.create(events)
                // Check that every node palette in the six editable diagram types offers direct and recursive population tools.
                .then(checkNodePalettes)
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    @Test
    @DisplayName("GIVEN nested components, WHEN the selected component is populated directly, THEN only its direct children are restored")
    public void populateOnlyTheSelectedComponent() {
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
                // Check that the diagram is initially empty.
                .consumeNextWith(initialDiagramConsumer)
                // Create the top-level component that will be selected for population.
                .then(createComponent)
                // Remember the selected component ID.
                .consumeNextWith(selectedComponentCreationConsumer)
                // Create a direct child inside the selected component.
                .then(createSelectedChild)
                // Remember the child ID so a grandchild can be created inside it.
                .consumeNextWith(selectedChildCreationConsumer)
                // Create a grandchild inside that child.
                .then(createGrandchild)
                // Check that the child contains one grandchild.
                .consumeNextWith(grandchildCreationConsumer)
                // Remove the selected child and its grandchild from the diagram while keeping them in the model.
                .then(deleteChild)
                // Check that the selected component no longer shows any children.
                .consumeNextWith(selectedChildDeletionConsumer)
                // Create another top-level component as a sibling of the selected component.
                .then(createComponent)
                // Remember the sibling component ID to check that population leaves it unchanged.
                .consumeNextWith(siblingComponentCreationConsumer)
                // Create a child inside the sibling component.
                .then(createSiblingChild)
                // Remember the sibling child ID for the next removal step.
                .consumeNextWith(siblingChildCreationConsumer)
                // Remove the sibling child from the diagram while keeping it in the model.
                .then(deleteChild)
                // Check that the sibling component no longer shows any children.
                .consumeNextWith(siblingChildDeletionConsumer)
                // Populate only the direct children of the selected component.
                .then(populateSelectedComponent)
                // Check that its child is restored, its grandchild is absent, and the sibling still has no children.
                .consumeNextWith(populationConsumer)
                // Populate the selected component again to check that no duplicate children are created.
                .then(populateSelectedComponent)
                // Check that the selected component still has only its direct child and the sibling remains unchanged.
                .consumeNextWith(populationConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    @Test
    @DisplayName("GIVEN nested components, WHEN the selected component is populated recursively, THEN only its descendants are restored")
    public void recursivelyPopulateOnlyTheSelectedComponent() {
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
                // Check that the diagram is initially empty.
                .consumeNextWith(initialDiagramConsumer)
                // Create the top-level component that will be selected for population.
                .then(createComponent)
                // Remember the selected component ID.
                .consumeNextWith(selectedComponentCreationConsumer)
                // Create a direct child inside the selected component.
                .then(createSelectedChild)
                // Remember the child ID so a grandchild can be created inside it.
                .consumeNextWith(selectedChildCreationConsumer)
                // Create a grandchild inside that child.
                .then(createGrandchild)
                // Check that the child contains one grandchild.
                .consumeNextWith(grandchildCreationConsumer)
                // Remove the selected child and its grandchild from the diagram while keeping them in the model.
                .then(deleteChild)
                // Check that the selected component no longer shows any children.
                .consumeNextWith(selectedChildDeletionConsumer)
                // Create another top-level component as a sibling of the selected component.
                .then(createComponent)
                // Remember the sibling component ID to check that population leaves it unchanged.
                .consumeNextWith(siblingComponentCreationConsumer)
                // Create a child inside the sibling component.
                .then(createSiblingChild)
                // Remember the sibling child ID for the next removal step.
                .consumeNextWith(siblingChildCreationConsumer)
                // Remove the sibling child from the diagram while keeping it in the model.
                .then(deleteChild)
                // Check that the sibling component no longer shows any children.
                .consumeNextWith(siblingChildDeletionConsumer)
                // Recursively populate the selected component to restore its child and grandchild.
                .then(populateSelectedComponent)
                // Check that both descendants are restored and the sibling still has no children.
                .consumeNextWith(populationConsumer)
                .thenCancel()
                .verify(Duration.ofSeconds(40));
    }

    @Test
    @DisplayName("GIVEN a LAB component with hidden allocated functions, WHEN the component is populated, THEN the Show Functions filter is preserved")
    public void populateAllocatedFunctionsOnSelectedComponentPreservesFilters() {
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
                // Check that the diagram is initially empty.
                .consumeNextWith(initialDiagramConsumer)
                // Create the top-level component whose allocated functions will be populated.
                .then(createComponent)
                // Remember the component ID for the following steps.
                .consumeNextWith(componentCreationConsumer)
                // Create the first function allocated to the component.
                .then(createFunction)
                // Check that the component contains one allocated function.
                .consumeNextWith(firstFunctionCreationConsumer)
                // Create a second allocated function as a sibling of the first function inside the same component.
                .then(createFunction)
                // Check that both functions are present and remember the second function ID.
                .consumeNextWith(secondFunctionCreationConsumer)
                // Remove the second function from the diagram while keeping its allocation in the model.
                .then(deleteFunction)
                // Check that only the first function remains in the diagram.
                .consumeNextWith(functionDeletionConsumer)
                // Turn off Show Functions to hide allocated functions.
                .then(deactivateFunctionsFilter)
                // Check that the remaining function is now hidden.
                .consumeNextWith(hiddenFunctionConsumer)
                // Recursively populate the component to restore its second allocated function.
                .then(populateComponent)
                // Check that both allocated functions are present and remain hidden.
                .consumeNextWith(populationConsumer)
                // Turn on Show Functions to display allocated functions again.
                .then(activateFunctionsFilter)
                // Check that both allocated functions are visible.
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

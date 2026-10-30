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
package org.eclipse.capella.application.controllers.selection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sirius.components.trees.tests.TreeEventPayloadConsumer.assertRefreshedTreeThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.diagram.lab.view.LABViewDiagramDescriptionProvider;
import org.eclipse.sirius.components.collaborative.selection.dto.SelectionDialogTreeEventInput;
import org.eclipse.sirius.components.collaborative.trees.services.api.ITreeQueryService;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.graphql.tests.ExecuteEditingContextFunctionInput;
import org.eclipse.sirius.components.graphql.tests.api.IExecuteEditingContextFunctionRunner;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SelectionDialogDescription;
import org.eclipse.sirius.components.view.emf.diagram.IDiagramIdProvider;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.eclipse.sirius.web.tests.services.selection.SelectionDialogTreeEventSubscriptionRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Checks that diagram selection dialogs display the Capella model hierarchy.
 *
 * @author tbezierslafosse
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CapellaSelectionDialogTreeTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private IExecuteEditingContextFunctionRunner executeEditingContextFunctionRunner;

    @Autowired
    private IRepresentationDescriptionSearchService representationDescriptionSearchService;

    @Autowired
    private IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService;

    @Autowired
    private IDiagramIdProvider diagramIdProvider;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @Autowired
    private SelectionDialogTreeEventSubscriptionRunner selectionDialogTreeEventSubscriptionRunner;

    @Autowired
    private ITreeQueryService treeQueryService;

    @BeforeEach
    public void initializeServer() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenCapellaServer
    public void openingFunctionalChainSelectionDialogShouldDisplayCapellaTree() {
        var treeDescriptionId = new AtomicReference<String>();
        var descriptionInput = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, (editingContext, input) -> {
            var diagramDescription = this.representationDescriptionSearchService.findAll(editingContext).values().stream()
                    .filter(DiagramDescription.class::isInstance)
                    .map(DiagramDescription.class::cast)
                    .filter(description -> LABViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getLabel()))
                    .findFirst().orElseThrow();
            var viewDiagram = this.viewDiagramDescriptionSearchService.findById(editingContext, diagramDescription.getId()).orElseThrow();
            var dialog = viewDiagram.getPalette().getNodeTools().stream()
                    .filter(tool -> "New Functional Chain".equals(tool.getName()))
                    .map(NodeTool::getDialogDescription)
                    .filter(SelectionDialogDescription.class::isInstance)
                    .map(SelectionDialogDescription.class::cast)
                    .findFirst().orElseThrow();
            treeDescriptionId.set(this.diagramIdProvider.getId(dialog.getSelectionDialogTreeDescription()));
            return new SuccessPayload(input.id());
        });
        assertThat(this.executeEditingContextFunctionRunner.execute(descriptionInput).block(Duration.ofSeconds(10))).isInstanceOf(SuccessPayload.class);

        var representationId = this.representationIdBuilder.buildSelectionRepresentationId(treeDescriptionId.get(), CapellaProjectData.SemanticIds.LOGICAL_ARCHITECTURE_PAC_ID,
                List.of(CapellaProjectData.CAPELLA_DOCUMENT_ID, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID,
                        CapellaProjectData.SemanticIds.LOGICAL_ARCHITECTURE_PAC_ID, CapellaProjectData.SemanticIds.FUNCTIONS_PAC_ID_3));
        var input = new SelectionDialogTreeEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);

        StepVerifier.create(this.selectionDialogTreeEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(tree.getChildren()).extracting(TreeItem::getId).containsExactly(CapellaProjectData.CAPELLA_DOCUMENT_ID);
                    var model = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID).orElseThrow();
                    assertThat(model.getChildren()).extracting(item -> item.getLabel().toString())
                            .contains("Operational Analysis", "System Analysis", "Logical Architecture");
                    var architecture = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.LOGICAL_ARCHITECTURE_PAC_ID).orElseThrow();
                    assertThat(architecture.getIconURL()).containsExactly("/icons/full/obj16/LogicalArchitecture.svg");
                    assertThat(architecture.isSelectable()).isFalse();
                    var rootFunction = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.ROOT_FUNCTION_ACT_ID_3).orElseThrow();
                    assertThat(rootFunction.getLabel().toString()).isEqualTo("Root Function");
                    assertThat(rootFunction.getIconURL()).containsExactly("/icons/full/obj16/LogicalFunction.svg");
                    assertThat(rootFunction.isSelectable()).isFalse();
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}

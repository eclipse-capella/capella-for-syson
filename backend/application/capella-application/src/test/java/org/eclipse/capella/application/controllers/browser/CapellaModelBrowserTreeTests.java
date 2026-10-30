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
package org.eclipse.capella.application.controllers.browser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sirius.components.trees.tests.TreeEventPayloadConsumer.assertRefreshedTreeThat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.application.configuration.details.view.CapellaDetailsViewProvider;
import org.eclipse.sirius.components.collaborative.browser.dto.ModelBrowserEventInput;
import org.eclipse.sirius.components.collaborative.trees.services.api.ITreeQueryService;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.forms.description.IfDescription;
import org.eclipse.sirius.components.graphql.tests.ExecuteEditingContextFunctionInput;
import org.eclipse.sirius.components.graphql.tests.api.IExecuteEditingContextFunctionRunner;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.modelbrowser.ModelBrowserEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.eclipse.syson.tree.explorer.view.reference.SysONReferenceWidgetModelBrowserTreeDescriptionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Checks the Capella hierarchy and icons in reference and duplication browsers.
 * SysON reference widgets retain their native hierarchy alongside Capella-specific widgets.
 *
 * @author tbezierslafosse
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CapellaModelBrowserTreeTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @Autowired
    private ModelBrowserEventSubscriptionRunner modelBrowserEventSubscriptionRunner;

    @Autowired
    private ITreeQueryService treeQueryService;

    @Autowired
    private CapellaDetailsViewProvider detailsViewProvider;

    @Autowired
    private IExecuteEditingContextFunctionRunner executeEditingContextFunctionRunner;

    @BeforeEach
    public void initializeServer() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("Given a Capella project, when opening a Details reference browser, then the Capella model hierarchy and icons are displayed")
    @GivenCapellaServer
    public void openingReferenceBrowserShouldDisplayCapellaTree() {
        var input = this.getCapellaReferenceBrowserInput("reference");

        StepVerifier.create(this.modelBrowserEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    var model = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID).orElseThrow();
                    var architecture = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.LOGICAL_ARCHITECTURE_PAC_ID).orElseThrow();

                    assertThat(tree.getChildren()).extracting(TreeItem::getId).containsExactly(CapellaProjectData.CAPELLA_DOCUMENT_ID);
                    assertThat(model.getChildren()).extracting(item -> item.getLabel().toString())
                            .contains("Operational Analysis", "System Analysis", "Logical Architecture");
                    assertThat(architecture.getIconURL()).containsExactly("/icons/full/obj16/LogicalArchitecture.svg");
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Given a Capella reference widget, when choosing a creation container, then the Capella model hierarchy is displayed")
    @GivenCapellaServer
    public void openingCapellaReferenceCreationBrowserShouldDisplayCapellaContainers() {
        var input = this.getCapellaReferenceBrowserInput("container");

        StepVerifier.create(this.modelBrowserEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    var model = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID).orElseThrow();
                    assertThat(model.getChildren()).extracting(item -> item.getLabel().toString())
                            .contains("Operational Analysis", "System Analysis", "Logical Architecture");
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Given a Capella project, when opening a Core reference browser, then the SysON tree remains available")
    @GivenCapellaServer
    public void openingCoreReferenceBrowserShouldPreserveSysONTreeAndLibraries() {
        var representationId = this.representationIdBuilder.buildModelBrowserRepresentationId(
                "reference",
                "siriusComponents://semantic?domain=sysml&entity=OccurrenceDefinition",
                "siriusComponents://semantic?domain=sysml&entity=Package",
                CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID,
                "core-reference-widget",
                false,
                List.of(CapellaProjectData.CAPELLA_DOCUMENT_ID));
        var input = new ModelBrowserEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);

        StepVerifier.create(this.modelBrowserEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(tree.getDescriptionId()).isEqualTo(SysONReferenceWidgetModelBrowserTreeDescriptionProvider.DESCRIPTION_ID);
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Given a Capella function, when opening the duplication browser, then Capella containers are displayed")
    @GivenCapellaServer
    public void openingDuplicationBrowserShouldDisplayCapellaContainers() {
        var representationId = this.representationIdBuilder.buildModelBrowserRepresentationId(
                "container",
                "siriusComponents://semantic?domain=sysml&entity=ActionUsage",
                "siriusComponents://semantic?domain=sysml&entity=Namespace",
                CapellaProjectData.SemanticIds.SEND_DATA_FUNCTION_ID,
                "duplicate-target-browser",
                true,
                List.of(CapellaProjectData.CAPELLA_DOCUMENT_ID, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID,
                        CapellaProjectData.SemanticIds.LOGICAL_ARCHITECTURE_PAC_ID, CapellaProjectData.SemanticIds.FUNCTIONS_PAC_ID_3));
        var input = new ModelBrowserEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);

        StepVerifier.create(this.modelBrowserEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    var container = this.treeQueryService.findTreeItem(tree, CapellaProjectData.SemanticIds.ROOT_FUNCTION_ACT_ID_3).orElseThrow();
                    assertThat(container.getLabel().toString()).isEqualTo("Root Function");
                    assertThat(container.getIconURL()).containsExactly("/icons/full/obj16/LogicalFunction.svg");
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }

    private ModelBrowserEventInput getCapellaReferenceBrowserInput(String browserType) {
        var descriptionId = new AtomicReference<String>();
        var input = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, (editingContext, functionInput) -> {
            descriptionId.set(this.detailsViewProvider.getPageDescriptions(editingContext).stream()
                .flatMap(page -> page.getGroupDescriptions().stream())
                .flatMap(group -> group.getControlDescriptions().stream())
                .filter(IfDescription.class::isInstance)
                .map(IfDescription.class::cast)
                .flatMap(conditional -> conditional.getControlDescriptions().stream())
                .filter(ReferenceWidgetDescription.class::isInstance)
                .map(ReferenceWidgetDescription.class::cast)
                .map(ReferenceWidgetDescription::getId)
                .findFirst().orElseThrow());
            return new SuccessPayload(functionInput.id());
        });
        assertThat(this.executeEditingContextFunctionRunner.execute(input).block(Duration.ofSeconds(10))).isInstanceOf(SuccessPayload.class);
        var representationId = this.representationIdBuilder.buildModelBrowserRepresentationId(
                browserType,
                "siriusComponents://semantic?domain=sysml&entity=OccurrenceDefinition",
                "siriusComponents://semantic?domain=sysml&entity=Package",
                CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID,
                URLEncoder.encode(descriptionId.get(), StandardCharsets.UTF_8),
                false,
                List.of(CapellaProjectData.CAPELLA_DOCUMENT_ID, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID));
        return new ModelBrowserEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);
    }
}

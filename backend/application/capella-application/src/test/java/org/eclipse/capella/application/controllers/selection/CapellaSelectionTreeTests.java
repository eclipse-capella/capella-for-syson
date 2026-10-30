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

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.sirius.components.collaborative.browser.dto.ModelBrowserEventInput;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.modelbrowser.ModelBrowserEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import reactor.test.StepVerifier;

/**
 * Checks that opening a Details reference browser displays the Capella model hierarchy and icons.
 * This ensures that the standard SysON tree view is properly redefined.
 *
 * @author tbezierslafosse
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CapellaSelectionTreeTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @Autowired
    private ModelBrowserEventSubscriptionRunner modelBrowserEventSubscriptionRunner;

    @BeforeEach
    public void initializeServer() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenCapellaServer
    public void openingReferenceBrowserShouldDisplayCapellaTree() {
        var representationId = this.representationIdBuilder.buildModelBrowserRepresentationId(
                "reference",
                "siriusComponents://semantic?domain=sysml&entity=OccurrenceDefinition",
                "siriusComponents://semantic?domain=sysml&entity=Package",
                CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID,
                "reference-widget",
                false,
                List.of(CapellaProjectData.CAPELLA_DOCUMENT_ID, CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID));
        var input = new ModelBrowserEventInput(UUID.randomUUID(), CapellaProjectData.EDITING_CONTEXT_ID, representationId);

        StepVerifier.create(this.modelBrowserEventSubscriptionRunner.run(input).flux())
                .consumeNextWith(assertRefreshedTreeThat(tree -> {
                    assertThat(tree.getChildren()).extracting(TreeItem::getId).containsExactly(CapellaProjectData.CAPELLA_DOCUMENT_ID);
                    var document = tree.getChildren().getFirst();
                    assertThat(document.getChildren()).extracting(TreeItem::getId).containsExactly(CapellaProjectData.SemanticIds.CAPELLA_MODEL_OCC_ID);
                    var model = document.getChildren().getFirst();
                    assertThat(model.getChildren()).hasSize(5);
                    assertThat(model.getChildren().stream().map(item -> item.getLabel().toString()))
                            .contains("Operational Analysis", "System Analysis", "Logical Architecture");
                    assertThat(model.getChildren()).anySatisfy(item -> {
                        assertThat(item.getLabel().toString()).isEqualTo("Logical Architecture");
                        assertThat(item.getIconURL()).containsExactly("/icons/full/obj16/LogicalArchitecture.svg");
                    });
                }))
                .thenCancel()
                .verify(Duration.ofSeconds(10));
    }
}

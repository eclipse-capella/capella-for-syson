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

package org.eclipse.capella.application.controllers.explorer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.sirius.components.trees.tests.TreeEventPayloadConsumer.assertRefreshedTreeThat;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaIdentifiers;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.application.configuration.explorer.CapellaExplorerTreeItemContextMenuEntryProvider;
import org.eclipse.capella.application.configuration.explorer.CapellaTreeViewDescriptionProvider;
import org.eclipse.sirius.components.trees.tests.graphql.TreeItemPaletteExecutor;
import org.eclipse.sirius.web.application.views.explorer.ExplorerEventInput;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerTreeItemContextMenuEntryProvider;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.sirius.web.tests.services.explorer.ExplorerEventSubscriptionRunner;
import org.eclipse.sirius.web.tests.services.representation.RepresentationIdBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import reactor.test.StepVerifier;

/**
 * Integration tests of the tree item context menu controllers.
 *
 * @author gdaniel
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreeItemContextMenuControllerTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private TreeItemPaletteExecutor treeItemPaletteExecutor;

    @Autowired
    private RepresentationIdBuilder representationIdBuilder;

    @Autowired
    private CapellaTreeViewDescriptionProvider capellaTreeViewDescriptionProvider;

    @Autowired
    private ExplorerEventSubscriptionRunner explorerEventSubscriptionRunner;

    @BeforeEach
    public void setup() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenCapellaServer
    @DisplayName("Given a Capella project, when the context menu is requested on an object, then the correct items are returned")
    public void givenCapellaProjectWhenContextMenuIsRequestedOnObjectThenCorrectItemsAreReturned()  {
        List<String> expandedIds = List.of(CapellaIdentifiers.CAPELLA_DOCUMENT_ID);
        var explorerRepresentationId = this.representationIdBuilder.buildExplorerRepresentationId(this.capellaTreeViewDescriptionProvider.getDescriptionId(), expandedIds, List.of());
        var input = new ExplorerEventInput(UUID.randomUUID(), CapellaIdentifiers.EDITING_CONTEXT_ID, explorerRepresentationId);
        var flux = this.explorerEventSubscriptionRunner.run(input).flux();

        var treeId = new AtomicReference<String>();
        Consumer<Object> initialTreeContentConsumer = assertRefreshedTreeThat(tree -> treeId.set(tree.getId()));

        Runnable getDocumentContextMenuItems = () -> {
            this.treeItemPaletteExecutor.execute(CapellaIdentifiers.EDITING_CONTEXT_ID, treeId.get(), CapellaIdentifiers.CAPELLA_DOCUMENT_ID)
                    .hasPaletteEntriesIds(entries -> assertThat(entries)
                            .contains(
                                    ExplorerTreeItemContextMenuEntryProvider.NEW_ROOT_OBJECT,
                                    ExplorerTreeItemContextMenuEntryProvider.DOWNLOAD_DOCUMENT,
                                    ExplorerTreeItemContextMenuEntryProvider.EXPAND_ALL
                            ));
        };

        Runnable getRootContextMenuItems = () -> {
            this.treeItemPaletteExecutor.execute(CapellaIdentifiers.EDITING_CONTEXT_ID, treeId.get(), CapellaIdentifiers.ROOT_OCCURRENCE_DEFINITION_ID)
                    .hasPaletteEntriesIds(entries -> assertThat(entries)
                            .contains(
                                    ExplorerTreeItemContextMenuEntryProvider.NEW_OBJECT,
                                    ExplorerTreeItemContextMenuEntryProvider.NEW_REPRESENTATION,
                                    CapellaExplorerTreeItemContextMenuEntryProvider.NEW_OBJECTS_FROM_TEXT_MENU_ENTRY_CONTRIBUTION_ID,
                                    ExplorerTreeItemContextMenuEntryProvider.EXPAND_ALL
                            ));
        };

        StepVerifier.create(flux)
                .consumeNextWith(initialTreeContentConsumer)
                .then(getDocumentContextMenuItems)
                .then(getRootContextMenuItems)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }
}

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

package org.eclipse.capella.application.controllers.projects;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.capella.application.configuration.template.CapellaProjectTemplateProvider;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.components.collaborative.dto.EditingContextEventInput;
import org.eclipse.sirius.components.core.api.ErrorPayload;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.core.api.SuccessPayload;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.graphql.tests.EditingContextEventSubscriptionRunner;
import org.eclipse.sirius.components.graphql.tests.ExecuteEditingContextFunctionInput;
import org.eclipse.sirius.components.graphql.tests.api.IExecuteEditingContextFunctionRunner;
import org.eclipse.sirius.web.application.library.services.LibraryMetadataAdapter;
import org.eclipse.sirius.web.application.project.dto.CreateProjectInput;
import org.eclipse.sirius.web.application.project.services.api.IProjectEditingContextService;
import org.eclipse.sirius.web.domain.boundedcontexts.library.services.api.ILibrarySearchService;
import org.eclipse.sirius.web.tests.graphql.CreateProjectExecutor;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.helper.EMFUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.transaction.annotation.Transactional;

import reactor.test.StepVerifier;

/**
 * Integration tests of the project templates controllers.
 *
 * @author gdaniel
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProjectTemplateControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private CreateProjectExecutor createProjectExecutor;

    @Autowired
    private ILibrarySearchService librarySearchService;

    @Autowired
    private IProjectEditingContextService projectEditingContextService;

    @Autowired
    private IExecuteEditingContextFunctionRunner executeEditingContextFunctionRunner;

    @Autowired
    private EditingContextEventSubscriptionRunner editingContextEventSubscriptionRunner;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenCapellaServer
    @DisplayName("Given a Capella project to create from a template with Arcadia dependency, when the mutation is performed, then the project is created")
    public void createCapellaProjectFromTemplate(CapturedOutput capturedOutput) {
        var optionalArcadiaLibrary = this.librarySearchService.findByNamespaceAndNameAndVersion("capella", "arcadia", "0.0.1");
        assertThat(optionalArcadiaLibrary).isPresent();
        var input = new CreateProjectInput(UUID.randomUUID(), "capella", CapellaProjectTemplateProvider.CAPELLA_TEMPLATE_ID, List.of(optionalArcadiaLibrary.get().getId().toString()));
        String projectId = this.createProjectExecutor.execute(input, capturedOutput).getProjectId();

        var optionalEditingContextId = this.projectEditingContextService.getEditingContextId(projectId);
        assertThat(optionalEditingContextId).isPresent();
        String editingContextId = optionalEditingContextId.get();

        var editingContextEventInput = new EditingContextEventInput(UUID.randomUUID(), editingContextId);
        var flux = this.editingContextEventSubscriptionRunner.run(editingContextEventInput).flux();


        BiFunction<IEditingContext, IInput, IPayload> checkEditingContextFunction = (editingContext, executeEditingContextFunctionInput) -> {
            if (editingContext instanceof IEMFEditingContext emfEditingContext) {
                ResourceSet resourceSet = emfEditingContext.getDomain().getResourceSet();
                assertThat(getLibraryResource(resourceSet, "capella", "arcadia")).isPresent();
                Optional<Resource> optionalProjectResource = getResourceWithName(resourceSet, "capella.sysml");
                assertThat(optionalProjectResource).isPresent();
                Resource projectResource = optionalProjectResource.get();

                Optional<ActionUsage> optionalRootFunction = EMFUtils.eAllContentStreamWithSelf(projectResource)
                        .filter(ActionUsage.class::isInstance)
                        .map(ActionUsage.class::cast)
                        .filter(actionUsage -> Objects.equals(actionUsage.getName(), "Root Function"))
                        .findFirst();

                assertThat(optionalRootFunction).isPresent();
                ActionUsage rootFunction = optionalRootFunction.get();
                assertThat(rootFunction.getOwnedRelationship())
                        .hasSize(1)
                        .first()
                        .isInstanceOfSatisfying(FeatureTyping.class, featureTyping -> {
                            // Ensure the Arcadia library is correctly loaded and references pointing to it aren't proxies.
                            assertThat(featureTyping.getType().eIsProxy()).isFalse();
                            assertThat(featureTyping.getType().getQualifiedName()).isEqualTo("Arcadia::Function");
                        });
                return new SuccessPayload(executeEditingContextFunctionInput.id());
            }
            return new ErrorPayload(executeEditingContextFunctionInput.id(), "Invalid editing context");
        };

        Runnable checkEditingContext = () -> {
            var executeEditingContextFunctionInput = new ExecuteEditingContextFunctionInput(UUID.randomUUID(), editingContextId, checkEditingContextFunction);
            var payload = this.executeEditingContextFunctionRunner.execute(executeEditingContextFunctionInput).block();
            assertThat(payload).isInstanceOf(SuccessPayload.class);
        };

        StepVerifier.create(flux)
                .then(checkEditingContext)
                .thenCancel()
                .verify(Duration.ofSeconds(5));
    }

    private Optional<Resource> getLibraryResource(ResourceSet resourceSet, String namespace, String name) {
        return resourceSet.getResources().stream()
                .filter(resource -> resource.eAdapters().stream()
                        .filter(LibraryMetadataAdapter.class::isInstance)
                        .map(LibraryMetadataAdapter.class::cast)
                        .anyMatch(libraryMetadataAdapter -> Objects.equals(libraryMetadataAdapter.getNamespace(), namespace)
                                && Objects.equals(libraryMetadataAdapter.getName(), name)))
                .findFirst();
    }

    private Optional<Resource> getResourceWithName(ResourceSet resourceSet, String name) {
        return resourceSet.getResources().stream()
                .filter(resource -> resource.eAdapters().stream()
                        .filter(ResourceMetadataAdapter.class::isInstance)
                        .map(ResourceMetadataAdapter.class::cast)
                        .anyMatch(resourceMetadataAdapter -> Objects.equals(resourceMetadataAdapter.getName(), name)))
                .findFirst();
    }
}

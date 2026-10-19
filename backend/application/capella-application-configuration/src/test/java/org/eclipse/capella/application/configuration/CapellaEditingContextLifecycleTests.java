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
package org.eclipse.capella.application.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.capella.model.transverse.services.ArcadiaElementContentAdapter;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.core.api.IEditingContextRepresentationDescriptionProvider;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.application.editingcontext.services.EditingContextLoader;
import org.eclipse.sirius.web.application.editingcontext.services.ResourceLoader;
import org.eclipse.sirius.web.application.editingcontext.services.api.IEditingContextDependencyLoader;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.Document;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.SemanticData;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.services.api.ISemanticDataSearchService;
import org.eclipse.sirius.web.library.domain.services.api.ILibrarySearchService;
import org.eclipse.syson.application.configuration.SysMLEditingContextProcessor;
import org.eclipse.syson.application.services.api.IStandardLibrariesLoader;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Exercises the Sirius Web loader and real editing context disposal with serialized SysML documents.
 * Only persistence lookups and standard library loading are simulated.
 *
 * @author Jerome Gout
 */
public class CapellaEditingContextLifecycleTests {

    private static final String COMPONENT_TYPE = "Arcadia::Component";

    private final CommonQueryService queryService = new CommonQueryService();

    private Document document;

    @BeforeEach
    public void beforeEach() throws IOException {
        var documentId = UUID.randomUUID();
        var resource = new JSONResourceFactory().createResourceFromPath(documentId.toString());
        var library = SysmlFactory.eINSTANCE.createPackage();
        library.setDeclaredName("Arcadia");
        var type = SysmlFactory.eINSTANCE.createPartDefinition();
        type.setDeclaredName("Component");
        var membership = SysmlFactory.eINSTANCE.createOwningMembership();
        membership.getOwnedRelatedElement().add(type);
        library.getOwnedRelationship().add(membership);
        resource.getContents().add(library);
        this.addComponent(resource, type);
        var output = new ByteArrayOutputStream();
        resource.save(output, null);
        this.document = Document.newDocument(documentId).name("model.sysml").content(output.toString(StandardCharsets.UTF_8)).build();
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void loadingShouldExposeCompleteTypesBeforeAndAfterIndexProcessor(boolean capellaFirst) {
        var context = this.newContext(UUID.randomUUID().toString());
        var probes = new ArrayList<String>();
        var sysMLProcessor = this.newSysMLProcessor();
        var capellaProcessor = new CapellaEditingContextProcessor();
        var processors = new ArrayList<IEditingContextProcessor>();
        processors.add(this.typeProbe(context, probes, "before", false));
        if (capellaFirst) {
            processors.add(capellaProcessor);
            processors.add(sysMLProcessor);
        } else {
            processors.add(sysMLProcessor);
            processors.add(capellaProcessor);
        }
        processors.add(this.typeProbe(context, probes, "after", true));
        IEditingContextRepresentationDescriptionProvider descriptions = editingContext -> {
            this.assertLoadedComponent(context, false);
            probes.add("descriptions");
            return List.of();
        };
        try {
            this.newLoader(processors, List.of(descriptions)).load(context, this.semanticData(context.getId()));

            assertThat(probes).containsExactly("descriptions", "before", "after");
            this.assertLoadedComponent(context, true);
        } finally {
            context.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void loadingShouldIndexElementsCreatedByOtherPostProcessors(boolean initializeFirst) {
        var context = this.newContext(UUID.randomUUID().toString());
        IEditingContextProcessor initializer = new IEditingContextProcessor() {
            @Override
            public void postProcess(IEditingContext editingContext) {
                var component = CapellaEditingContextLifecycleTests.this.loadedComponent(context);
                var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
                CapellaEditingContextLifecycleTests.this.addComponent(component.eResource(), (PartDefinition) typing.getType());
            }
        };
        var processors = new ArrayList<IEditingContextProcessor>();
        processors.add(this.newSysMLProcessor());
        if (initializeFirst) {
            processors.add(initializer);
            processors.add(new CapellaEditingContextProcessor());
        } else {
            processors.add(new CapellaEditingContextProcessor());
            processors.add(initializer);
        }
        try {
            this.newLoader(processors, List.of()).load(context, this.semanticData(context.getId()));

            var component = this.loadedComponent(context);
            assertThat(this.queryService.getComponents(component)).hasSize(2);
            assertThat(this.index(context).getType(component)).contains(COMPONENT_TYPE);
        } finally {
            context.dispose();
        }
    }

    @Test
    public void disposingContextShouldClearItsIndexWithoutAffectingAnotherProject() {
        var first = this.loadContext(UUID.randomUUID().toString());
        var second = this.loadContext(UUID.randomUUID().toString());
        var firstComponent = this.loadedComponent(first);
        var secondComponent = this.loadedComponent(second);
        var firstIndex = this.index(first);
        var secondIndex = this.index(second);
        var firstResource = firstComponent.eResource();
        try {
            assertThat(firstComponent.getElementId()).isEqualTo(secondComponent.getElementId());
            assertThat(firstResource.getURI()).isEqualTo(secondComponent.eResource().getURI());
            assertThat(firstComponent).isNotSameAs(secondComponent);
            assertThat(firstIndex).isNotSameAs(secondIndex);

            first.dispose();

            assertThat(firstResource.getContents()).isEmpty();
            assertThat(firstComponent.eAdapters()).doesNotContain(firstIndex);
            assertThat(firstIndex.getType(firstComponent)).isEmpty();
            assertThat(firstIndex.getElements(firstResource, SysmlPackage.eINSTANCE.getPartUsage(), COMPONENT_TYPE)).isEmpty();
            assertThat(secondIndex.getType(secondComponent)).contains(COMPONENT_TYPE);
            assertThat(this.queryService.getComponents(secondComponent)).containsExactly(secondComponent);
        } finally {
            first.dispose();
            second.dispose();
        }
    }

    @Test
    public void reopeningContextShouldRebuildIndexWithNewEObjects() {
        var id = UUID.randomUUID().toString();
        var original = this.loadContext(id);
        var originalComponent = this.loadedComponent(original);
        var originalIndex = this.index(original);
        original.dispose();

        var reopened = this.loadContext(id);
        try {
            var reloadedComponent = this.loadedComponent(reopened);
            assertThat(reloadedComponent.getElementId()).isEqualTo(originalComponent.getElementId());
            assertThat(reloadedComponent).isNotSameAs(originalComponent);
            assertThat(this.index(reopened)).isNotSameAs(originalIndex);
            assertThat(this.queryService.getComponents(reloadedComponent)).containsExactly(reloadedComponent);
            assertThat(originalIndex.getType(originalComponent)).isEmpty();
            assertThat(originalIndex.getType(reloadedComponent)).isEmpty();
        } finally {
            reopened.dispose();
        }
    }

    @Test
    public void loadingDocumentAfterInitializationShouldIndexItAndKeepResourceScope() {
        var context = this.loadContext(UUID.randomUUID().toString());
        try {
            var existingComponent = this.loadedComponent(context);
            var loaded = new ResourceLoader(List.of()).toResource(context.getDomain().getResourceSet(), UUID.randomUUID().toString(),
                    this.document.getName(), this.document.getContent(), false, false).orElseThrow();
            var newComponent = loaded.getContents().stream().filter(PartUsage.class::isInstance).map(PartUsage.class::cast).findFirst().orElseThrow();

            assertThat(this.index(context).getType(newComponent)).contains(COMPONENT_TYPE);
            assertThat(this.queryService.getComponents(newComponent)).containsExactly(newComponent);
            assertThat(this.queryService.getComponents(existingComponent)).containsExactly(existingComponent);
        } finally {
            context.dispose();
        }
    }

    private EditingContext newContext(String id) {
        var resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put(SysmlPackage.eNS_URI, SysmlPackage.eINSTANCE);
        var domain = new AdapterFactoryEditingDomain(new ComposedAdapterFactory(), new BasicCommandStack(), resourceSet);
        return new EditingContext(id, domain, new HashMap<>(), new ArrayList<>());
    }

    private EditingContext loadContext(String id) {
        var context = this.newContext(id);
        this.newLoader(List.of(this.newSysMLProcessor(), new CapellaEditingContextProcessor()), List.of()).load(context, this.semanticData(id));
        return context;
    }

    private SysMLEditingContextProcessor newSysMLProcessor() {
        return new SysMLEditingContextProcessor(mock(IStandardLibrariesLoader.class), id -> false);
    }

    private SemanticData semanticData(String id) {
        var semanticData = mock(SemanticData.class);
        when(semanticData.getId()).thenReturn(UUID.fromString(id));
        when(semanticData.getDocuments()).thenReturn(Set.of(this.document));
        return semanticData;
    }

    private EditingContextLoader newLoader(List<IEditingContextProcessor> processors, List<IEditingContextRepresentationDescriptionProvider> descriptions) {
        return new EditingContextLoader(new ResourceLoader(List.of()), mock(IEditingContextDependencyLoader.class), descriptions, processors, List.of(),
                mock(ILibrarySearchService.class), mock(ISemanticDataSearchService.class));
    }

    private IEditingContextProcessor typeProbe(EditingContext context, List<String> probes, String label, boolean indexExpected) {
        return new IEditingContextProcessor() {
            @Override
            public void postProcess(IEditingContext editingContext) {
                CapellaEditingContextLifecycleTests.this.assertLoadedComponent(context, indexExpected);
                probes.add(label);
            }
        };
    }

    private void assertLoadedComponent(EditingContext context, boolean indexExpected) {
        var component = this.loadedComponent(context);
        assertThat(this.queryService.getComponents(component)).containsExactly(component);
        assertThat(this.queryService.getArcadiaType(component)).contains(COMPONENT_TYPE);
        var adapter = EcoreUtil.getAdapter(context.getDomain().getResourceSet().eAdapters(), ArcadiaElementContentAdapter.class);
        assertThat(adapter != null).isEqualTo(indexExpected);
    }

    private PartUsage loadedComponent(EditingContext context) {
        return context.getDomain().getResourceSet().getResources().stream()
                .flatMap(resource -> resource.getContents().stream())
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .findFirst().orElseThrow();
    }

    private ArcadiaElementContentAdapter index(EditingContext context) {
        return (ArcadiaElementContentAdapter) EcoreUtil.getAdapter(context.getDomain().getResourceSet().eAdapters(), ArcadiaElementContentAdapter.class);
    }

    private void addComponent(Resource resource, PartDefinition type) {
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        component.setDeclaredName("Component");
        var typing = SysmlFactory.eINSTANCE.createFeatureTyping();
        typing.setTypedFeature(component);
        typing.setType(type);
        component.getOwnedRelationship().add(typing);
        resource.getContents().add(component);
    }
}

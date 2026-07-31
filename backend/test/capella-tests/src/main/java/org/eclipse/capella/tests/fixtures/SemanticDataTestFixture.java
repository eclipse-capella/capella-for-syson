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

package org.eclipse.capella.tests.fixtures;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.syson.application.configuration.SysONDefaultLibrariesConfiguration;
import org.eclipse.syson.application.configuration.SysONLoadDefaultLibrariesOnApplicationStartConfiguration;
import org.eclipse.syson.application.libraries.SysONLibraryLoader;
import org.eclipse.syson.application.libraries.SysONLibraryLoadingDefinition;
import org.eclipse.syson.sysml.helper.EMFUtils;
import org.eclipse.syson.util.SysONEContentAdapter;
import org.springframework.core.io.ClassPathResource;

/**
 * Provides the semantic data used by semantic tests.
 *
 * @author gdaniel
 */
public class SemanticDataTestFixture {

    private static final String ARCADIA_LIBRARY_PATH = "arcadia/library";

    private static final String ARCADIA_DEFAULT_STRUCTURE_PATH = "arcadia/structure/arcadiaDefaultStructure.json";

    private static final String ARCADIA_DEFAULT_STRUCTURE_NAME = "Arcadia Default Structure";

    private final ResourceSet resourceSet;

    public SemanticDataTestFixture() {
        var defaultLibrariesConfiguration = new SysONDefaultLibrariesConfiguration(new SysONLoadDefaultLibrariesOnApplicationStartConfiguration());
        this.resourceSet = defaultLibrariesConfiguration.getLibrariesResourceSet();
        this.resourceSet.eAdapters().add(new SysONEContentAdapter());

        var loadingDefinition = new SysONLibraryLoadingDefinition(
                "Arcadia Library",
                IEMFEditingContext.RESOURCE_SCHEME,
                ARCADIA_LIBRARY_PATH,
                List.of(JsonResourceFactoryImpl.EXTENSION),
                new JSONResourceFactory()::createResource);
        var loadedResources = new SysONLibraryLoader().loadLibraryResources(this.resourceSet, loadingDefinition);
        if (loadedResources.isEmpty()) {
            fail("No resource found at " + ARCADIA_LIBRARY_PATH);
        }

        EMFUtils.resolveAllNonDerived(this.resourceSet);
        this.resourceSet.eAdapters().add(new ECrossReferenceAdapter());
    }

    public ResourceSet getResourceSet() {
        return this.resourceSet;
    }

    public CapellaModel createCapellaModel() {
        this.resourceSet.getResources().removeIf(this::isCapellaModel);

        var resource = new JSONResourceFactory().createResourceFromPath(UUID.randomUUID().toString());
        resource.eAdapters().add(new ResourceMetadataAdapter(ARCADIA_DEFAULT_STRUCTURE_NAME));
        this.resourceSet.getResources().add(resource);

        try (var inputStream = new ClassPathResource(ARCADIA_DEFAULT_STRUCTURE_PATH).getInputStream()) {
            resource.load(inputStream, null);
        } catch (IOException exception) {
            fail("Could not load " + ARCADIA_DEFAULT_STRUCTURE_PATH, exception);
        }

        // We should only resolve on the created resource, but SysON's EMFUtils#resolveAllNonDerived(Resource) isn't public.
        EMFUtils.resolveAllNonDerived(this.resourceSet);
        return CapellaModel.from(resource);
    }

    private boolean isCapellaModel(Resource resource) {
        return resource.eAdapters().stream()
                .filter(ResourceMetadataAdapter.class::isInstance)
                .map(ResourceMetadataAdapter.class::cast)
                .map(ResourceMetadataAdapter::getName)
                .anyMatch(name -> Objects.equals(name, ARCADIA_DEFAULT_STRUCTURE_NAME));
    }
}

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

package org.eclipse.capella.application.configuration.arcadia.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.application.configuration.arcadia.services.api.IArcadiaLibraryPublisher;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.emfjson.resource.JsonResourceFactoryImpl;
import org.eclipse.sirius.web.application.editingcontext.services.EPackageEntry;
import org.eclipse.sirius.web.application.editingcontext.services.api.IResourceToDocumentService;
import org.eclipse.sirius.web.domain.boundedcontexts.library.services.api.ILibrarySearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.services.api.ISemanticDataCreationService;
import org.eclipse.syson.application.libraries.SysONLibraryLoader;
import org.eclipse.syson.application.libraries.SysONLibraryLoadingDefinition;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.helper.EMFUtils;
import org.springframework.stereotype.Service;

/**
 * Publishes the Arcadia library.
 *
 * @author gdaniel
 */
@Service
public class ArcadiaLibraryPublisher implements IArcadiaLibraryPublisher {

    private final ILibrarySearchService librarySearchService;

    private final IResourceToDocumentService resourceToDocumentService;

    private final ISemanticDataCreationService semanticDataCreationService;

    public ArcadiaLibraryPublisher(ILibrarySearchService librarySearchService, IResourceToDocumentService resourceToDocumentService,
            ISemanticDataCreationService semanticDataCreationService) {
        this.librarySearchService = Objects.requireNonNull(librarySearchService);
        this.resourceToDocumentService = Objects.requireNonNull(resourceToDocumentService);
        this.semanticDataCreationService = Objects.requireNonNull(semanticDataCreationService);
    }

    @Override
    public void publish(PublishArcadiaLibraryCommand command) {
        if (this.librarySearchService.existsByNamespaceAndNameAndVersion(command.namespace(), command.name(), command.version())) {
            return;
        }

        ResourceSet arcadiaLibraryResourceSet = this.loadArcadiaLibraryResourceSet();
        Optional<Resource> optionalArcadiaResource = arcadiaLibraryResourceSet.getResources().stream()
                .filter(resource -> Objects.equals(resource.getURI().scheme(), "arcadialibrary"))
                .findFirst();

        if (optionalArcadiaResource.isPresent()) {
            this.resourceToDocumentService.toDocument(optionalArcadiaResource.get(), false).ifPresent(documentData -> {
                this.semanticDataCreationService.create(command, List.of(documentData.document()), documentData.ePackageEntries().stream().map(EPackageEntry::nsURI).toList(), List.of());
            });
        }

    }

    private ResourceSet loadArcadiaLibraryResourceSet() {
        EPackage.Registry ePackageRegistry = new EPackageRegistryImpl();
        ePackageRegistry.put(SysmlPackage.eNS_URI, SysmlPackage.eINSTANCE);
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.setPackageRegistry(ePackageRegistry);
        SysONLibraryLoader sysONLibraryLoader = new SysONLibraryLoader();
        SysONLibraryLoadingDefinition arcadiaLibraryLoadingDefinition = new SysONLibraryLoadingDefinition(
                "Arcadia Library",
                "arcadialibrary",
                "arcadia/library",
                Collections.singletonList(JsonResourceFactoryImpl.EXTENSION),
                new JSONResourceFactory()::createResource);
        sysONLibraryLoader.loadLibraryResources(resourceSet, arcadiaLibraryLoadingDefinition);
        EMFUtils.resolveAllNonDerived(resourceSet);
        return resourceSet;
    }
}

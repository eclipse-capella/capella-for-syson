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
package org.eclipse.capella.application.configuration.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.collaborative.trees.dto.palette.SingleClickTreeItemTool;
import org.eclipse.sirius.components.collaborative.trees.palette.api.ITreeItemPaletteCustomizer;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.palette.dto.IPaletteEntry;
import org.eclipse.sirius.components.palette.dto.Palette;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.library.services.LibraryMetadataAdapter;
import org.eclipse.sirius.web.application.messages.ISiriusWebApplicationMessageService;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerDescriptionProvider;
import org.eclipse.sirius.web.application.views.explorer.services.ExplorerTreeItemContextMenuEntryProvider;
import org.eclipse.sirius.web.domain.boundedcontexts.library.Library;
import org.eclipse.sirius.web.domain.boundedcontexts.library.services.api.ILibrarySearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.services.api.ISemanticDataSearchService;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Customization of {@link ExplorerTreeItemContextMenuEntryProvider} for Capella to provide the contextual menu entries in
 * the 'Explorer' view.
 *
 * @author frouene
 */
@Service
public class CapellaExplorerTreeItemContextMenuEntryProvider implements ITreeItemPaletteCustomizer {

    public static final String NEW_OBJECTS_FROM_TEXT_MENU_ENTRY_CONTRIBUTION_ID = "newObjectsFromText";

    private final IObjectSearchService objectSearchService;

    private final ILibrarySearchService librarySearchService;

    private final ISemanticDataSearchService semanticDataSearchService;

    private final CapellaTreeViewDescriptionProvider capellaTreeViewDescriptionProvider;

    private final ISiriusWebApplicationMessageService messageService;

    public CapellaExplorerTreeItemContextMenuEntryProvider(IObjectSearchService objectSearchService, ILibrarySearchService librarySearchService, ISemanticDataSearchService semanticDataSearchService,
            CapellaTreeViewDescriptionProvider capellaTreeViewDescriptionProvider, ISiriusWebApplicationMessageService messageService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.librarySearchService = Objects.requireNonNull(librarySearchService);
        this.semanticDataSearchService = Objects.requireNonNull(semanticDataSearchService);
        this.capellaTreeViewDescriptionProvider = Objects.requireNonNull(capellaTreeViewDescriptionProvider);
        this.messageService = Objects.requireNonNull(messageService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, TreeDescription treeDescription, Tree tree, TreeItem treeItem) {
        return tree.getId().startsWith(ExplorerDescriptionProvider.PREFIX)
                && Objects.equals(tree.getDescriptionId(), this.capellaTreeViewDescriptionProvider.getDescriptionId());
    }

    @Override
    public Palette customize(IEditingContext editingContext, TreeDescription treeDescription, Tree tree, TreeItem treeItem, Palette palette) {
        List<IPaletteEntry> paletteEntries = new ArrayList<>();
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            paletteEntries.addAll(this.getDocumentContextMenuEntries(emfEditingContext, treeItem));
            paletteEntries.addAll(this.getObjectContextMenuEntries(emfEditingContext, treeItem));
            paletteEntries.addAll(this.getRepresentationContextMenuEntries(emfEditingContext, treeItem));
            paletteEntries.addAll(this.getLibraryRelatedEntries(emfEditingContext, treeItem));
            paletteEntries.add(new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.EXPAND_ALL, this.messageService.treeToolExpandAll(), List.of(), false, List.of()));
        }
        return new Palette("", List.of(), paletteEntries);
    }

    private List<IPaletteEntry> getDocumentContextMenuEntries(IEMFEditingContext editingContext, TreeItem treeItem) {
        var optionalResource = this.objectSearchService.getObject(editingContext, treeItem.getId())
                .filter(Resource.class::isInstance)
                .map(Resource.class::cast);
        if (optionalResource.isPresent()) {
            var resource = optionalResource.get();

            List<IPaletteEntry> entries = new ArrayList<>();
            entries.add(new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.NEW_ROOT_OBJECT, this.messageService.treeToolNewObject(), List.of(), false, List.of()));
            entries.add(new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.DOWNLOAD_DOCUMENT, this.messageService.treeToolDownload(), List.of(), false, List.of()));
            return entries;
        }
        return List.of();
    }

    private List<IPaletteEntry> getObjectContextMenuEntries(IEMFEditingContext editingContext, TreeItem treeItem) {
        var optionalEObject = this.objectSearchService.getObject(editingContext, treeItem.getId())
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);
        if (optionalEObject.isPresent()) {
            return List.of(
                    new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.NEW_OBJECT, this.messageService.treeToolNewObject(), List.of(), false, List.of()),
                    new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.NEW_REPRESENTATION, this.messageService.treeToolNewRepresentation(), List.of(), false, List.of()),
                    new SingleClickTreeItemTool(NEW_OBJECTS_FROM_TEXT_MENU_ENTRY_CONTRIBUTION_ID, "", List.of(), false, List.of()));
        }
        return List.of();
    }

    private List<IPaletteEntry> getRepresentationContextMenuEntries(IEMFEditingContext editingContext, TreeItem treeItem) {
        var optionalRepresentationMetadata = this.objectSearchService.getObject(editingContext, treeItem.getId())
                .filter(RepresentationMetadata.class::isInstance)
                .map(RepresentationMetadata.class::cast);
        if (optionalRepresentationMetadata.isPresent()) {
            return List.of(
                    new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.DUPLICATE_REPRESENTATION, this.messageService.treeToolDuplicateRepresentation(), List.of(), false,
                            List.of()));
        }
        return List.of();
    }

    private List<IPaletteEntry> getLibraryRelatedEntries(IEMFEditingContext editingContext, TreeItem treeItem) {
        List<IPaletteEntry> result = new ArrayList<>();
        var optionalNotifier = this.objectSearchService.getObject(editingContext, treeItem.getId())
                .filter(Notifier.class::isInstance)
                .map(Notifier.class::cast);

        if (optionalNotifier.isEmpty()) {
            optionalNotifier = editingContext.getDomain().getResourceSet().getResources().stream()
                    .filter(resource -> resource.getURI().toString().contains(treeItem.getId()))
                    .map(Notifier.class::cast)
                    .findFirst();
        }

        var optionalLibraryMetadataAdapter = optionalNotifier.stream()
                .map(Notifier::eAdapters)
                .flatMap(Collection::stream)
                .filter(LibraryMetadataAdapter.class::isInstance)
                .map(LibraryMetadataAdapter.class::cast)
                .findFirst();

        if (optionalLibraryMetadataAdapter.isPresent()) {
            var libraryMetadataAdapter = optionalLibraryMetadataAdapter.get();
            if (this.isDirectDependency(editingContext, libraryMetadataAdapter)) {
                // We do not support the update or removal of a transitive dependency for the moment.
                result.add(new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.UPDATE_LIBRARY, this.messageService.treeToolUpdateLibrary(), List.of(), true, List.of()));
                result.add(new SingleClickTreeItemTool(ExplorerTreeItemContextMenuEntryProvider.REMOVE_LIBRARY, this.messageService.treeToolRemoveLibrary(),
                        List.of("/icons/remove_library.svg"), true, List.of()));
            }
        }
        return result;
    }

    private boolean isDirectDependency(IEMFEditingContext emfEditingContext, LibraryMetadataAdapter libraryMetadataAdapter) {
        var editingContextDependencies = new UUIDParser().parse(emfEditingContext.getId())
                .map(this.semanticDataSearchService::findAllDependenciesIdById)
                .stream()
                .flatMap(Collection::stream)
                .map(AggregateReference::getId)
                .toList();
        return this.librarySearchService.findByNamespaceAndNameAndVersion(libraryMetadataAdapter.getNamespace(), libraryMetadataAdapter.getName(), libraryMetadataAdapter.getVersion())
                .map(Library::getSemanticData)
                .map(AggregateReference::getId)
                .map(editingContextDependencies::contains)
                .orElse(false);
    }
}

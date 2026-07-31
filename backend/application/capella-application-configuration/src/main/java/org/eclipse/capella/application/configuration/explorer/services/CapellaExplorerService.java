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
package org.eclipse.capella.application.configuration.explorer.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerFilterService;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerFragment;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerLabelService;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IContentService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerServices;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.services.api.ISysONResourceService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link ICapellaExplorerService}.
 *
 * @author gdaniel
 */
@Service
public class CapellaExplorerService implements ICapellaExplorerService {

    public static final String ARCADIA_TYPE_PARAMETER = "arcadiaType";

    public static final String CAPELLA_PKG_PARAMETER = "capellaPackage";

    private final IIdentityService identityService;

    private final IContentService contentService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IExplorerServices explorerServices;

    private final ICapellaExplorerFilterService filterService;

    private final UtilService utilService = new UtilService();

    private final ISysONResourceService sysONResourceService;

    private final ICapellaExplorerLabelService capellaExplorerLabelService;

    private final TransverseQueryService transverseQueryService;

    public CapellaExplorerService(IIdentityService identityService, IContentService contentService, IRepresentationMetadataSearchService representationMetadataSearchService,
            IExplorerServices explorerServices, ICapellaExplorerFilterService filterService,
            ISysONResourceService sysONResourceService, ICapellaExplorerLabelService capellaExplorerLabelService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.contentService = Objects.requireNonNull(contentService);
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.explorerServices = Objects.requireNonNull(explorerServices);
        this.filterService = Objects.requireNonNull(filterService);
        this.sysONResourceService = Objects.requireNonNull(sysONResourceService);
        this.capellaExplorerLabelService = Objects.requireNonNull(capellaExplorerLabelService);
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public List<Object> getElements(IEditingContext editingContext, List<String> activeFilterIds) {
        List<Object> results = new ArrayList<>();
        if (editingContext instanceof EditingContext siriusWebContext) {
            siriusWebContext.getDomain().getResourceSet().getResources().stream()
                    .filter(r -> !this.filterService.isSysMLStandardLibrary(r))
                    .filter(r -> !this.filterService.isArcadiaLibrary(r))
                    .filter(r -> !this.filterService.isKerMLStandardLibrary(r))
                    .filter(r -> !this.sysONResourceService.isImported(siriusWebContext, r) || this.utilService.getLibraries(r, false).isEmpty())
                    .forEach(results::add);
        }
        return results;
    }

    @Override
    public String getTreeItemId(Object self) {
        String id = null;
        if (self instanceof ICapellaExplorerFragment fragment) {
            id = fragment.getId();
        } else {
            id = this.explorerServices.getTreeItemId(self);
        }
        return id;
    }

    @Override
    public String getKind(Object self) {
        final String result;
        if (self instanceof ICapellaExplorerFragment fragment) {
            result = fragment.getKind();
        } else if (self instanceof Element element && this.transverseQueryService.isArcadiaElement(element)) {
            String kind = this.explorerServices.getKind(self);
            result = kind + "&" + ARCADIA_TYPE_PARAMETER + "=" + this.transverseQueryService.getArcadiaType(element).get();
        } else if (self instanceof org.eclipse.syson.sysml.Package pkg) {
            String kind = this.explorerServices.getKind(self);
            result = kind + "&" + CAPELLA_PKG_PARAMETER + "=" + pkg.getDeclaredName();
        } else {
            result = this.explorerServices.getKind(self);
        }
        return result;
    }

    @Override
    public boolean hasChildren(Object self, IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations, List<String> expandedIds, List<String> activeFilterIds) {
        boolean hasChildren = false;
        if (self instanceof ICapellaExplorerFragment fragment) {
            hasChildren = fragment.hasChildren(editingContext, existingRepresentations, expandedIds, activeFilterIds);
        } else if (self instanceof Resource resource) {
            hasChildren = !this.filterService.applyFilters(editingContext, resource.getContents(), activeFilterIds).isEmpty();
        } else if (self instanceof Element element) {
            List<Object> contents = this.filterService.applyFilters(editingContext, this.contentService.getContents(self), activeFilterIds);
            hasChildren = !contents.isEmpty() && contents.stream().anyMatch(e -> !(e instanceof EAnnotation))
                    || this.hasRepresentation(element, editingContext);
        } else {
            hasChildren = this.explorerServices.hasChildren(self, editingContext, existingRepresentations);
        }
        return hasChildren;
    }

    private boolean hasRepresentation(EObject self, IEditingContext editingContext) {
        var optionalSemanticDataId = new UUIDParser().parse(editingContext.getId());
        if (optionalSemanticDataId.isPresent()) {
            String id = this.identityService.getId(self);
            return this.representationMetadataSearchService.existAnyRepresentationMetadataForSemanticDataAndTargetObjectId(AggregateReference.to(optionalSemanticDataId.get()), id);
        }
        return false;
    }

    @Override
    public List<Object> getChildren(Object self, IEditingContext editingContext, List<RepresentationMetadata> existingRepresentations, List<String> expandedIds, List<String> activeFilterIds) {
        List<Object> result = new ArrayList<>();
        String id = this.getTreeItemId(self);
        if (self instanceof ICapellaExplorerFragment fragment) {
            if (expandedIds.contains(id)) {
                result.addAll(fragment.getChildren(editingContext, existingRepresentations, expandedIds, activeFilterIds));
            }
        } else {
            result.addAll(this.explorerServices.getDefaultChildren(self, editingContext, expandedIds, existingRepresentations));
        }

        result = this.filterService.applyFilters(editingContext, result, activeFilterIds);

        // Remove annotations: they aren't part of the SysML standard and shouldn't be visible to the user.
        return result.stream()
                .filter(element -> !(element instanceof EAnnotation))
                .toList();
    }

    @Override
    public boolean canExpandAll(TreeItem treeItem, IEditingContext editingContext) {
        return treeItem.isHasChildren();
    }

    @Override
    public boolean canCreateNewObjectsFromText(Object self) {
        return self instanceof Element && this.isEditable(self);
    }

    @Override
    public Object getParent(Object self, String treeItemId, IEditingContext editingContext) {
        final Object result;
        if (self instanceof ICapellaExplorerFragment fragment) {
            result = fragment.getParent();
        } else {
            result = this.explorerServices.getParent(self, treeItemId, editingContext);
        }
        return result;
    }

    @Override
    public Object getTreeItemObject(String treeItemId, IEditingContext editingContext) {
        final Object result;
        Optional<Resource> optionalResource = Optional.ofNullable(editingContext)
                .filter(IEMFEditingContext.class::isInstance)
                .map(IEMFEditingContext.class::cast)
                .flatMap(emfEditingContext -> {
                    return emfEditingContext.getDomain().getResourceSet().getResources().stream()
                            .filter(resource -> resource.getURI().toString().contains(treeItemId))
                            .findFirst();
                });
        if (optionalResource.isPresent()) {
            result = optionalResource.get();
        } else {
            result = this.explorerServices.getTreeItemObject(treeItemId, editingContext);
        }
        return result;
    }

    @Override
    public boolean isEditable(Object self) {
        boolean result = true;
        if (self instanceof ICapellaExplorerFragment fragment) {
            result = fragment.isEditable();
        } else if (self instanceof Namespace namespace) {
            if (this.utilService.isRootNamespace(namespace)) {
                result = namespace.getOwnedElement().stream().noneMatch(ownedElement -> ElementUtil.isFromStandardLibrary(ownedElement));
            } else {
                result = !ElementUtil.isFromStandardLibrary(namespace);
            }
        } else if (self instanceof Element element) {
            result = !ElementUtil.isFromStandardLibrary(element);
        } else if (self instanceof Resource resource) {
            result = resource.getContents().stream()
                    .filter(Namespace.class::isInstance)
                    .map(Namespace.class::cast)
                    .flatMap(namespace -> namespace.getOwnedElement().stream())
                    .noneMatch(ElementUtil::isFromStandardLibrary);
        }
        return result;
    }

    @Override
    public boolean isDeletable(Object self) {
        boolean result = true;
        if (self instanceof ICapellaExplorerFragment fragment) {
            result = fragment.isEditable();
        } else if (self instanceof Namespace namespace) {
            if (this.utilService.isRootNamespace(namespace)) {
                result = namespace.getOwnedElement().stream().noneMatch(ownedElement -> ElementUtil.isFromStandardLibrary(ownedElement));
            } else {
                result = !ElementUtil.isFromStandardLibrary(namespace);
            }
        } else if (self instanceof Element element) {
            result = !ElementUtil.isFromStandardLibrary(element);
        } else if (self instanceof Resource resource) {
            // Allow to delete resources containing user libraries, users may want to remove an imported library from
            // their project.
            result = resource.getContents().stream()
                    .filter(Namespace.class::isInstance)
                    .map(Namespace.class::cast)
                    .flatMap(namespace -> namespace.getOwnedElement().stream())
                    .noneMatch(ElementUtil::isFromStandardLibrary);
        }
        return result;
    }

    @Override
    public boolean isSelectable(Object self) {
        return true;
    }

    @Override
    public String getLabel(Object self) {
        return this.capellaExplorerLabelService.getLabel(self);
    }

    @Override
    public List<String> getImageURL(Object self) {
        return this.capellaExplorerLabelService.getImageURL(self);
    }
}

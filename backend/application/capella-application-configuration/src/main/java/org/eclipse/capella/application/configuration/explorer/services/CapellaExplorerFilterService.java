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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.application.configuration.explorer.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.capella.application.configuration.explorer.filters.CapellaTreeFilterProvider;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerFilterService;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.services.api.ISysONResourceService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.util.ElementUtil;
import org.springframework.stereotype.Service;

/**
 * Services to apply filters on Capella explorer.
 *
 * @author frouene
 */
@Service
public class CapellaExplorerFilterService implements ICapellaExplorerFilterService {

    private final UtilService utilService = new UtilService();

    private final ISysONResourceService sysONResourceService;

    private final TransverseQueryService transverseQueryService;

    private final LAQueryService laQueryService;

    public CapellaExplorerFilterService(final ISysONResourceService sysONResourceService) {
        this.sysONResourceService = Objects.requireNonNull(sysONResourceService);
        this.laQueryService = new LAQueryService();
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public boolean isKerMLStandardLibrary(Object object) {
        return object instanceof Resource res && res.getURI() != null && res.getURI().toString().startsWith(ElementUtil.KERML_LIBRARY_SCHEME);
    }

    @Override
    public boolean isSysMLStandardLibrary(Object object) {
        return object instanceof Resource res && res.getURI() != null && res.getURI().toString().startsWith(ElementUtil.SYSML_LIBRARY_SCHEME);
    }

    @Override
    public boolean isArcadiaLibrary(Object object) {
        return object instanceof Resource res && res.getURI() != null && res.getURI().toString().startsWith("arcadialibrary");
    }

    @Override
    public boolean isUserLibrary(IEditingContext editingContext, Object object) {
        return object instanceof Resource res
                && this.sysONResourceService.isImported(editingContext, res)
                && !new UtilService().getLibraries(res, false).isEmpty();
    }

    @Override
    public List<Object> hideKerMLStandardLibraries(List<Object> elements) {
        return elements.stream().filter(element -> !this.isKerMLStandardLibrary(element)).toList();
    }

    @Override
    public List<Object> hideSysMLStandardLibraries(List<Object> elements) {
        return elements.stream().filter(element -> !this.isSysMLStandardLibrary(element)).toList();
    }

    @Override
    public List<Object> hideUserLibraries(IEditingContext editingContext, List<Object> elements) {
        return elements.stream()
                .filter(element -> !this.isUserLibrary(editingContext, element))
                .toList();
    }

    @Override
    public List<Object> hideMemberships(List<Object> elements) {
        List<Object> alteredElements = new ArrayList<>();
        elements.forEach(child -> {
            if (child instanceof Membership membership) {
                alteredElements.addAll(membership.getOwnedRelatedElement());
            } else {
                alteredElements.add(child);
            }
        });
        return alteredElements;
    }

    @Override
    public List<Object> hideRootNamespace(List<Object> elements) {
        List<Object> alteredElements = new ArrayList<>();
        elements.forEach(child -> {
            if (child instanceof Namespace namespace && this.utilService.isRootNamespace(namespace)) {
                alteredElements.addAll(namespace.getOwnedElement());
            } else {
                alteredElements.add(child);
            }
        });
        return alteredElements;
    }

    @Override
    public List<Object> applyFilters(IEditingContext editingContext, List<?> elements, List<String> activeFilterIds) {
        List<Object> alteredElements = new ArrayList<>(elements);
        alteredElements = this.hideMemberships(alteredElements);
        alteredElements = this.hideKerMLStandardLibraries(alteredElements);
        alteredElements = this.hideSysMLStandardLibraries(alteredElements);
        alteredElements = this.hideUserLibraries(editingContext, alteredElements);
        alteredElements = this.hideRootNamespace(alteredElements);
        alteredElements = this.keepCapellaElementsAndRepresentations(alteredElements);
        alteredElements = this.hidePorts(alteredElements, activeFilterIds);
        return alteredElements;
    }

    private List<Object> hidePorts(List<Object> elements, List<String> activeFilterIds) {
        var alteredElements = new ArrayList<>(elements);
        if (activeFilterIds.contains(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID)) {
            alteredElements.removeIf(object -> object instanceof EObject eObject && (this.transverseQueryService.isComponentPort(eObject) || this.transverseQueryService.isFunctionPort(eObject)));
        }
        return alteredElements;
    }

    private List<Object> keepCapellaElementsAndRepresentations(List<Object> elements) {
        return new ArrayList<>(elements.stream().filter(this::capellaElementOrRepresentation).toList());
    }

    private boolean capellaElementOrRepresentation(Object object) {
        if (object instanceof Element element) {
            boolean isSupportedSysMLElement = element instanceof org.eclipse.syson.sysml.OccurrenceDefinition
                    || element instanceof org.eclipse.syson.sysml.Package
                    || element instanceof org.eclipse.syson.sysml.RequirementUsage;
            return isSupportedSysMLElement
                    // Describes is represented as an AllocationUsage, which isn't an Arcadia element.
                    || this.transverseQueryService.isDescribes(element)
                    // PerformActionUsage are Arcadia elements (allocations) but should not be displayed in the explorer.
                    || (this.transverseQueryService.isArcadiaElement(element) && !(element instanceof PerformActionUsage));
        }
        return object instanceof RepresentationMetadata;
    }

}

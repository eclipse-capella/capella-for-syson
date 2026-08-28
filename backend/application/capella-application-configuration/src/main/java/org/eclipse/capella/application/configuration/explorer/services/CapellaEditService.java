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

import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.capella.application.configuration.label.services.CapellaImagePathsService;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.sirius.components.core.api.IEditServiceDelegate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.application.services.SysMLv2EditService;
import org.eclipse.syson.sysml.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * A specific Capella Edit Service to customize children creation in the Model explorer.
 *
 * @author fbarbin
 */
@Service
@Order(0)
public class CapellaEditService implements IEditServiceDelegate {

    public static final String ID_PREFIX = "CapellaEditService-";

    private static final String DATA = "Data";

    private final TransverseQueryService transverseQueryService;

    private final SysMLv2EditService sysMLv2EditService;

    private final CapellaImagePathsService capellaImagePathsService;

    private final TransverseMutationService transverseMutationService;

    private final IObjectSearchService objectSearchService;

    public CapellaEditService(SysMLv2EditService sysMLv2EditService, CapellaImagePathsService capellaImagePathsService, IObjectSearchService objectSearchService) {
        this.sysMLv2EditService = Objects.requireNonNull(sysMLv2EditService);
        this.capellaImagePathsService = Objects.requireNonNull(capellaImagePathsService);
        this.transverseMutationService = new TransverseMutationService();
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public boolean canHandle(Object object) {
        if (object instanceof Element element && this.transverseQueryService.isArcadiaElement(element)) {
            return true;
        }
        return this.sysMLv2EditService.canHandle(object);
    }

    @Override
    public List<ChildCreationDescription> getChildCreationDescriptions(IEditingContext editingContext, String containerId, String referenceKind) {
        var optionalContainer = this.objectSearchService.getObject(editingContext, containerId)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);

        List<ChildCreationDescription> childCreationDescriptions = new ArrayList<>();
        if (optionalContainer.isPresent()) {
            EObject container = optionalContainer.get();
            if (this.transverseQueryService.isArcadiaElement(container)) {
                // To implement specific children creation description for Arcadia elements.

            } else if (container instanceof org.eclipse.syson.sysml.Package pkg) {
                String packageName = pkg.getDeclaredName();
                if (DATA.equals(packageName)) {
                    // We add the entry for Exchange Items
                    List<String> iconURL = new ArrayList<>();
                    this.capellaImagePathsService.getImageFromArcadiaType(null, null, ARCADIA_EXCHANGE_ITEM).ifPresent(iconURL::add);
                    ChildCreationDescription childCreationDescription = new ChildCreationDescription(ID_PREFIX +
                            ARCADIA_EXCHANGE_ITEM, ARCADIA_EXCHANGE_ITEM, iconURL);
                    childCreationDescriptions.add(childCreationDescription);
                }
            } else {
                childCreationDescriptions = this.sysMLv2EditService.getChildCreationDescriptions(editingContext, containerId, referenceKind);
            }
        }
        return childCreationDescriptions;
    }

    @Override
    public boolean canHandle(IEditingContext editingContext) {
        return this.sysMLv2EditService.canHandle(editingContext);
    }

    @Override
    public List<ChildCreationDescription> getRootCreationDescriptions(IEditingContext editingContext, String domainId, boolean suggested, String referenceKind) {
        return this.sysMLv2EditService.getRootCreationDescriptions(editingContext, domainId, suggested, referenceKind);
    }

    @Override
    public Optional<Object> createChild(IEditingContext editingContext, Object object, String childCreationDescriptionId) {
        Optional<Object> value = Optional.empty();
        if (childCreationDescriptionId.startsWith(ID_PREFIX) && object instanceof Element container) {
            String arcadiaType = childCreationDescriptionId.substring(ID_PREFIX.length());
            if (ARCADIA_EXCHANGE_ITEM.equals(arcadiaType)) {
                value = Optional.ofNullable(this.transverseMutationService.createNewExchangeItem(container));
            }
        } else {
            value = this.sysMLv2EditService.createChild(editingContext, object, childCreationDescriptionId);
        }
        return value;
    }

    @Override
    public Optional<Object> createRootObject(IEditingContext editingContext, UUID documentId, String domainId, String rootObjectCreationDescriptionId) {
        return this.sysMLv2EditService.createRootObject(editingContext, documentId, domainId, rootObjectCreationDescriptionId);
    }

    @Override
    public void delete(Object object) {
        this.sysMLv2EditService.delete(object);
    }
}

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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.api.ICapellaCapableEditingContextPredicate;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.sirius.components.core.api.IEditServiceDelegate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.application.services.SysMLv2EditService;
import org.eclipse.syson.sysml.Element;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * A specific Capella Edit Service to customize children creation and semantic deletion in the Model explorer.
 *
 * @author fbarbin
 */
@Service
@Order(0)
public class CapellaEditService implements IEditServiceDelegate {

    public static final String ID_PREFIX = "CapellaEditService-";

    private final CommonQueryService commonQueryService;

    private final SysMLv2EditService sysMLv2EditService;

    private final CapellaChildCreationService childCreationService;

    private final CommonDeletionService commonDeletionService;

    private final IObjectSearchService objectSearchService;

    private final ICapellaCapableEditingContextPredicate capellaCapableEditingContextPredicate;

    public CapellaEditService(SysMLv2EditService sysMLv2EditService, IObjectSearchService objectSearchService,
            ICapellaCapableEditingContextPredicate capellaCapableEditingContextPredicate) {
        this.sysMLv2EditService = Objects.requireNonNull(sysMLv2EditService);
        this.childCreationService = new CapellaChildCreationService();
        this.commonDeletionService = new CommonDeletionService();
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.commonQueryService = new CommonQueryService();
        this.capellaCapableEditingContextPredicate = Objects.requireNonNull(capellaCapableEditingContextPredicate);
    }

    @Override
    public boolean canHandle(Object object) {
        if (object instanceof Element element && this.commonQueryService.isArcadiaElement(element)) {
            return true;
        }
        return this.sysMLv2EditService.canHandle(object);
    }

    @Override
    public List<ChildCreationDescription> getChildCreationDescriptions(IEditingContext editingContext, String containerId, String referenceKind) {
        var container = this.objectSearchService.getObject(editingContext, containerId).orElse(null);
        if (this.capellaCapableEditingContextPredicate.test(editingContext.getId())
                && container instanceof Element element && this.commonQueryService.getArcadiaPerspective(element).isPresent()) {
            return new ArcadiaChildCreationDescriptionSwitch().doSwitch(element);
        }
        return this.sysMLv2EditService.getChildCreationDescriptions(editingContext, containerId, referenceKind);
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
        Optional<Object> result = Optional.empty();
        if (this.capellaCapableEditingContextPredicate.test(editingContext.getId())
                && object instanceof Element container && this.commonQueryService.getArcadiaPerspective(container).isPresent()) {
            if (childCreationDescriptionId.startsWith(ID_PREFIX)) {
                String type = childCreationDescriptionId.substring(ID_PREFIX.length());
                result = Optional.ofNullable(this.childCreationService.createCapellaChild(container, type));
            }
        } else if (!childCreationDescriptionId.startsWith(ID_PREFIX)) {
            result = this.sysMLv2EditService.createChild(editingContext, object, childCreationDescriptionId);
        }
        return result;
    }

    @Override
    public Optional<Object> createRootObject(IEditingContext editingContext, UUID documentId, String domainId, String rootObjectCreationDescriptionId) {
        return this.sysMLv2EditService.createRootObject(editingContext, documentId, domainId, rootObjectCreationDescriptionId);
    }

    @Override
    public void delete(Object object) {
        if (object instanceof Element element) {
            this.commonDeletionService.delete(element);
        } else {
            this.sysMLv2EditService.delete(object);
        }
    }
}

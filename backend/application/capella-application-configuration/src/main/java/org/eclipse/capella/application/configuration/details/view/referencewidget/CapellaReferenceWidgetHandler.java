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
package org.eclipse.capella.application.configuration.details.view.referencewidget;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetAddElementHandler;
import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetClearHandler;
import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetRemoveHandler;
import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetSetElementHandler;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.forms.description.FormDescription;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.view.emf.form.api.IViewFormDescriptionSearchService;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.widget.reference.ReferenceWidget;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Resolves reference action inputs and delegates virtual reference editing to the matching Capella provider.
 *
 * @author tbezierslafosse
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaReferenceWidgetHandler implements IReferenceWidgetAddElementHandler, IReferenceWidgetSetElementHandler,
        IReferenceWidgetRemoveHandler, IReferenceWidgetClearHandler {

    private static final String CANNOT_BE_EDITED_STATUS_MESSAGE = "This reference cannot be edited";

    private final IObjectSearchService objectSearchService;

    private final IViewFormDescriptionSearchService viewFormDescriptionSearchService;

    private final List<ICapellaReferenceWidgetProvider> capellaReferenceWidgetProviders;

    public CapellaReferenceWidgetHandler(IObjectSearchService objectSearchService, IViewFormDescriptionSearchService viewFormDescriptionSearchService,
            List<ICapellaReferenceWidgetProvider> capellaReferenceWidgetProviders) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.viewFormDescriptionSearchService = Objects.requireNonNull(viewFormDescriptionSearchService);
        this.capellaReferenceWidgetProviders = Objects.requireNonNull(capellaReferenceWidgetProviders);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget referenceWidget) {
        return referenceWidget.getReferenceName().startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX);
    }

    @Override
    public IStatus add(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget widget, List<String> newValueIds) {
        var description = this.findDescription(editingContext, widget).filter(candidate -> widget.isSelectable() && candidate.getAdd() != null);
        var owner = this.objectSearchService.getObject(editingContext, widget.getOwnerId())
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);
        IStatus status = new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE);
        if (description.isPresent() && owner.isPresent() && !widget.isReadOnly()) {
            var ids = newValueIds.stream().distinct().toList();
            var values = ids.stream()
                    .flatMap(id -> this.objectSearchService.getObject(editingContext, id).stream())
                    .toList();
            // Reject the whole selection if any ID is missing
            if (values.size() == ids.size()) {
                status = this.capellaReferenceWidgetProviders.stream()
                        .filter(provider -> provider.canHandle(description.get()))
                        .findFirst()
                        .map(provider -> provider.handleAdd(description.get(), owner.get(), values))
                        .orElse(new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE));
            }
        }
        return status;
    }

    @Override
    public IStatus set(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget widget, String newValueId) {
        var description = this.findDescription(editingContext, widget).filter(candidate -> widget.isSelectable() && candidate.getSet() != null);
        var owner = this.objectSearchService.getObject(editingContext, widget.getOwnerId())
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);
        var value = this.objectSearchService.getObject(editingContext, newValueId);
        IStatus status = new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE);
        if (description.isPresent() && owner.isPresent() && !widget.isReadOnly()) {
            status = new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE);
            if (value.isPresent()) {
                status = this.capellaReferenceWidgetProviders.stream()
                        .filter(provider -> provider.canHandle(description.get()))
                        .findFirst()
                        .map(provider -> provider.handleSet(description.get(), owner.get(), value.get()))
                        .orElse(new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE));
            }
        }
        return status;
    }

    @Override
    public IStatus remove(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget widget, String itemId) {
        var description = this.findDescription(editingContext, widget).filter(candidate -> widget.isRemovable() && candidate.getRemove() != null);
        var owner = this.objectSearchService.getObject(editingContext, widget.getOwnerId())
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);
        var value = this.objectSearchService.getObject(editingContext, itemId);
        IStatus status = new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE);
        if (description.isPresent() && owner.isPresent() && !widget.isReadOnly()) {
            status = new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE);
            if (value.isPresent()) {
                status = this.capellaReferenceWidgetProviders.stream()
                        .filter(provider -> provider.canHandle(description.get()))
                        .findFirst()
                        .map(provider -> provider.handleRemove(description.get(), owner.get(), value.get()))
                        .orElse(new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE));
            }
        }
        return status;
    }

    @Override
    public IStatus clear(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget widget) {
        var description = this.findDescription(editingContext, widget).filter(candidate -> widget.getClearButton() != null && candidate.getClearButton() != null);
        var owner = this.objectSearchService.getObject(editingContext, widget.getOwnerId())
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);
        IStatus status = new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE);
        if (description.isPresent() && owner.isPresent() && !widget.isReadOnly()) {
            status = this.capellaReferenceWidgetProviders.stream()
                    .filter(provider -> provider.canHandle(description.get()))
                    .findFirst()
                    .map(provider -> provider.handleClear(description.get(), owner.get()))
                    .orElse(new Failure(CANNOT_BE_EDITED_STATUS_MESSAGE));
        }
        return status;
    }

    private Optional<ReferenceWidgetDescription> findDescription(IEditingContext editingContext, ReferenceWidget widget) {
        return this.viewFormDescriptionSearchService.findFormElementDescriptionById(editingContext, widget.getDescriptionId())
                .filter(ReferenceWidgetDescription.class::isInstance)
                .map(ReferenceWidgetDescription.class::cast);
    }
}

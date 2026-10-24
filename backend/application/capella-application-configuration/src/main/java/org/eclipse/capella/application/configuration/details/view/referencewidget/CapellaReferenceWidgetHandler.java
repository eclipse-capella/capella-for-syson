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
import java.util.stream.Stream;

import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetAddElementHandler;
import org.eclipse.sirius.components.collaborative.widget.reference.api.IReferenceWidgetClearHandler;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.forms.description.FormDescription;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.widget.reference.ReferenceWidget;
import org.eclipse.sirius.components.widget.reference.ReferenceValue;
import org.eclipse.sirius.web.application.views.details.services.DetailsViewReferenceWidgetAddElementHandler;
import org.eclipse.sirius.web.application.views.details.services.DetailsViewReferenceWidgetClearHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Routes Arcadia reference additions and clearing through their converted callbacks instead of EMF references.
 * The reference name prefix identifies virtual Capella references; ordinary Details widgets retain Sirius Web handling.
 *
 * @author tbezierslafosse
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaReferenceWidgetHandler implements IReferenceWidgetAddElementHandler, IReferenceWidgetClearHandler {

    private final IObjectSearchService objectSearchService;

    private final DetailsViewReferenceWidgetAddElementHandler addElementHandler;

    private final DetailsViewReferenceWidgetClearHandler clearHandler;

    public CapellaReferenceWidgetHandler(IObjectSearchService objectSearchService, DetailsViewReferenceWidgetAddElementHandler addElementHandler,
            DetailsViewReferenceWidgetClearHandler clearHandler) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.addElementHandler = Objects.requireNonNull(addElementHandler);
        this.clearHandler = Objects.requireNonNull(clearHandler);
    }

    @Override
    public boolean canHandle(FormDescription formDescription) {
        return this.addElementHandler.canHandle(formDescription);
    }

    @Override
    public IStatus add(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget referenceWidget, List<String> newValueIds) {
        if (!referenceWidget.getReferenceName().startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX)) {
            return this.addElementHandler.add(editingContext, formDescription, referenceWidget, newValueIds);
        }
        IStatus status = new Failure("This reference cannot be edited.");
        if (!referenceWidget.isReadOnly() && referenceWidget.getSetHandler() != null) {
            if (newValueIds.isEmpty()) {
                status = new Success();
            } else {
                var ids = Stream.concat(referenceWidget.getReferenceValues().stream().map(ReferenceValue::getId), newValueIds.stream()).distinct().toList();
                List<Object> values = ids.stream().flatMap(id -> this.objectSearchService.getObject(editingContext, id).stream()).toList();
                if (values.size() == ids.size()) {
                    status = referenceWidget.getSetHandler().apply(values);
                } else {
                    status = new Failure("The referenced elements could not be found.");
                }
            }
        }
        return status;
    }

    @Override
    public IStatus clear(IEditingContext editingContext, FormDescription formDescription, ReferenceWidget referenceWidget) {
        if (!referenceWidget.getReferenceName().startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX)) {
            return this.clearHandler.clear(editingContext, formDescription, referenceWidget);
        }
        IStatus status = new Failure("This reference cannot be edited.");
        if (!referenceWidget.isReadOnly() && referenceWidget.getSetHandler() != null) {
            status = referenceWidget.getSetHandler().apply(List.of());
        }
        return status;
    }
}

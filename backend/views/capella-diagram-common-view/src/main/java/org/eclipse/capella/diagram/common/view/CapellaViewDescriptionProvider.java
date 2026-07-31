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
package org.eclipse.capella.diagram.common.view;

import java.util.List;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.springframework.stereotype.Service;

/**
 * The editing context processor contributing Capella views to the editing context.
 * Views registered here will be converted in representation description by the {@link org.eclipse.sirius.web.application.studio.services.ViewBasedRepresentationDescriptionConverter}
 *
 * @author frouene
 */
@Service
public class CapellaViewDescriptionProvider implements IEditingContextProcessor {

    private final List<View> views;

    public CapellaViewDescriptionProvider(List<IViewDescriptionProvider> viewDescriptionProviders) {
        this.views = viewDescriptionProviders.stream()
                .flatMap(viewDescriptionProvider -> viewDescriptionProvider.getRepresentationDescriptions().stream())
                .toList();
    }

    @Override
    public void preProcess(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext siriusWebEditingContext) {
            siriusWebEditingContext.getViews().addAll(this.views);
        }
    }
}

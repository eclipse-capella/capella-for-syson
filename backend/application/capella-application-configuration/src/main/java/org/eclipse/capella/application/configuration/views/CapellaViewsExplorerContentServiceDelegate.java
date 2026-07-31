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
package org.eclipse.capella.application.configuration.views;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.RepresentationKind;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.api.IDefaultViewsExplorerContentService;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.api.IViewsExplorerContentServiceDelegate;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Customize the retrieval of the content of the views explorer for Capella.
 *
 * @author frouene
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaViewsExplorerContentServiceDelegate implements IViewsExplorerContentServiceDelegate {

    private final IDefaultViewsExplorerContentService defaultViewsExplorerContentService;

    public CapellaViewsExplorerContentServiceDelegate(IDefaultViewsExplorerContentService defaultViewsExplorerContentService) {
        this.defaultViewsExplorerContentService = Objects.requireNonNull(defaultViewsExplorerContentService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext) {
        return true;
    }

    @Override
    public List<RepresentationKind> getContents(IEditingContext editingContext, List<RepresentationMetadata> representationMetadata,
            Map<String, IRepresentationDescription> representationDescriptions) {
        return this.defaultViewsExplorerContentService.getContents(editingContext, representationMetadata, representationDescriptions);
    }
}

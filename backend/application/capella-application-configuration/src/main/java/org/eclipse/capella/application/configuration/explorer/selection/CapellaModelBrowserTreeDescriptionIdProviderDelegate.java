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
package org.eclipse.capella.application.configuration.explorer.selection;

import java.util.List;
import java.util.Objects;

import org.eclipse.capella.application.configuration.details.view.CapellaDetailsViewDescriptionProvider;
import org.eclipse.capella.model.transverse.services.api.ICapellaCapableEditingContextPredicate;
import org.eclipse.sirius.components.collaborative.browser.api.IModelBrowserTreeDescriptionIdProviderDelegate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.view.emf.IRepresentationDescriptionIdProvider;
import org.eclipse.sirius.web.application.browser.DefaultModelBrowsersTreeDescriptionProvider;
import org.eclipse.syson.tree.explorer.view.duplicate.SysONDuplicateTargetBrowserTreeDescriptionIdProviderDelegate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Selects dedicated Capella browsers for Capella Details widgets and model-element duplication.
 * Other reference widgets retain the SysON browser, including those in the Core tab.
 *
 * @author tbezierslafosse
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaModelBrowserTreeDescriptionIdProviderDelegate implements IModelBrowserTreeDescriptionIdProviderDelegate {

    private final ICapellaCapableEditingContextPredicate capellaCapableEditingContextPredicate;

    private final IURLParser urlParser;

    private final IModelBrowserTreeDescriptionIdProviderDelegate duplicateTargetBrowserDelegate;

    public CapellaModelBrowserTreeDescriptionIdProviderDelegate(ICapellaCapableEditingContextPredicate capellaCapableEditingContextPredicate, IURLParser urlParser,
            SysONDuplicateTargetBrowserTreeDescriptionIdProviderDelegate duplicateTargetBrowserDelegate) {
        this.capellaCapableEditingContextPredicate = Objects.requireNonNull(capellaCapableEditingContextPredicate);
        this.urlParser = Objects.requireNonNull(urlParser);
        this.duplicateTargetBrowserDelegate = Objects.requireNonNull(duplicateTargetBrowserDelegate);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String modelBrowserId) {
        if (!this.capellaCapableEditingContextPredicate.test(editingContext.getId())) {
            return false;
        }

        var isReferenceOrContainerBrowser = modelBrowserId.startsWith(DefaultModelBrowsersTreeDescriptionProvider.MODEL_BROWSER_REFERENCE_PREFIX)
                || modelBrowserId.startsWith(DefaultModelBrowsersTreeDescriptionProvider.MODEL_BROWSER_CONTAINER_PREFIX);
        return isReferenceOrContainerBrowser
                && (this.isCapellaWidget(modelBrowserId) || (this.duplicateTargetBrowserDelegate.canHandle(editingContext, modelBrowserId)));
    }

    @Override
    public String getModelBrowserTreeDescriptionId(IEditingContext editingContext, String modelBrowserId) {
        var descriptionId = CapellaModelBrowserTreeDescriptionProvider.REFERENCE_DESCRIPTION_ID;
        if (this.duplicateTargetBrowserDelegate.canHandle(editingContext, modelBrowserId)) {
            descriptionId = CapellaModelBrowserTreeDescriptionProvider.DUPLICATION_DESCRIPTION_ID;
        } else if (modelBrowserId.startsWith(DefaultModelBrowsersTreeDescriptionProvider.MODEL_BROWSER_CONTAINER_PREFIX)) {
            descriptionId = CapellaModelBrowserTreeDescriptionProvider.CONTAINER_DESCRIPTION_ID;
        }
        return descriptionId;
    }

    private boolean isCapellaWidget(String modelBrowserId) {
        return this.urlParser.getParameterValues(modelBrowserId)
                .getOrDefault("descriptionId", List.of())
                .stream()
                .map(this.urlParser::getParameterValues)
                .anyMatch(parameters -> parameters.getOrDefault(IRepresentationDescriptionIdProvider.SOURCE_ID, List.of())
                        .contains(CapellaDetailsViewDescriptionProvider.SOURCE_ID));
    }
}

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

import java.util.Objects;

import org.eclipse.capella.model.transverse.services.CapellaProjectService;
import org.eclipse.sirius.components.collaborative.browser.api.IModelBrowserTreeDescriptionIdProviderDelegate;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.application.browser.DefaultModelBrowserTreeDescriptionIdProvider;
import org.eclipse.sirius.web.application.browser.DefaultModelBrowsersTreeDescriptionProvider;
import org.eclipse.syson.tree.explorer.view.duplicate.SysONDuplicateTargetBrowserTreeDescriptionIdProviderDelegate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Routes Capella model browsers before the SysON browser delegates.
 * <p>
 * Spring orders the injected delegates using {@link Order}. Sirius Web's
 * {@link org.eclipse.sirius.components.collaborative.browser.ComposedModelBrowserTreeDescriptionIdProvider}
 * selects the first delegate whose {@code canHandle} method returns {@code true}.
 * This delegate therefore has the highest precedence so that Capella projects use their own tree presentation
 * before the generic SysON reference-browser delegate can handle them.
 * </p>
 *
 * @author tbezierslafosse
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaModelBrowserTreeDescriptionIdProviderDelegate implements IModelBrowserTreeDescriptionIdProviderDelegate {

    private final CapellaProjectService capellaProjectService;

    private final DefaultModelBrowserTreeDescriptionIdProvider defaultProvider;

    private final SysONDuplicateTargetBrowserTreeDescriptionIdProviderDelegate duplicateDelegate;

    private final CapellaModelBrowserTreeDescriptionProvider capellaDescriptionProvider;

    public CapellaModelBrowserTreeDescriptionIdProviderDelegate(CapellaProjectService capellaProjectService,
            DefaultModelBrowserTreeDescriptionIdProvider defaultProvider, SysONDuplicateTargetBrowserTreeDescriptionIdProviderDelegate duplicateDelegate,
            CapellaModelBrowserTreeDescriptionProvider capellaDescriptionProvider) {
        this.capellaProjectService = Objects.requireNonNull(capellaProjectService);
        this.defaultProvider = Objects.requireNonNull(defaultProvider);
        this.duplicateDelegate = Objects.requireNonNull(duplicateDelegate);
        this.capellaDescriptionProvider = Objects.requireNonNull(capellaDescriptionProvider);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String modelBrowserId) {
        return (modelBrowserId.startsWith(DefaultModelBrowsersTreeDescriptionProvider.MODEL_BROWSER_REFERENCE_PREFIX)
                || modelBrowserId.startsWith(DefaultModelBrowsersTreeDescriptionProvider.MODEL_BROWSER_CONTAINER_PREFIX))
                && this.capellaProjectService.isCapellaProject(editingContext.getId());
    }

    @Override
    public String getModelBrowserTreeDescriptionId(IEditingContext editingContext, String modelBrowserId) {
        var nativeId = this.defaultProvider.getModelBrowserTreeDescriptionId(editingContext, modelBrowserId);
        if (this.duplicateDelegate.canHandle(editingContext, modelBrowserId)) {
            // If we are performing a duplication, we first rely on the SysON browser, then we replace its tree with Capella's
            nativeId = this.duplicateDelegate.getModelBrowserTreeDescriptionId(editingContext, modelBrowserId);
        }
        return this.capellaDescriptionProvider.getDescriptionId(nativeId);
    }
}

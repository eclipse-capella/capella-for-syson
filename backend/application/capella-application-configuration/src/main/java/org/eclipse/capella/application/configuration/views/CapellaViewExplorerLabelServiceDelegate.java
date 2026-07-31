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

import java.util.Objects;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.api.IDefaultViewsExplorerLabelService;
import org.eclipse.sirius.web.application.views.viewsexplorer.services.api.IViewsExplorerLabelServiceDelegate;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Provide the behavior of the views explorer for Capella.
 *
 * @author frouene
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaViewExplorerLabelServiceDelegate implements IViewsExplorerLabelServiceDelegate {

    private final IDefaultViewsExplorerLabelService defaultExplorerLabelService;

    public CapellaViewExplorerLabelServiceDelegate(IDefaultViewsExplorerLabelService defaultExplorerLabelService) {
        this.defaultExplorerLabelService = Objects.requireNonNull(defaultExplorerLabelService);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext) {
        return true;
    }

    @Override
    public boolean isEditable(Object self) {
        return this.defaultExplorerLabelService.isEditable(self);
    }

    @Override
    public StyledString getLabel(Object self) {
        return this.defaultExplorerLabelService.getLabel(self);
    }

    @Override
    public IStatus editLabel(IEditingContext editingContext, Tree tree, TreeItem treeItem, String newValue) {
        return this.defaultExplorerLabelService.editLabel(editingContext, tree, treeItem, newValue);
    }
}

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
package org.eclipse.capella.application.configuration.explorer;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.tree.ITreeIdProvider;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.springframework.stereotype.Service;

/**
 * The editing context processor contributing Capella tree views to the editing context.
 *
 * @author frouene
 */
@Service
public class CapellaTreeViewDescriptionProvider implements IEditingContextProcessor {

    private final View view;

    private final String descriptionId;

    public CapellaTreeViewDescriptionProvider(ITreeIdProvider treeIdProvider) {
        this.view = new CapellaExplorerTreeDescriptionProvider().createView();
        this.descriptionId = treeIdProvider.getId((TreeDescription) this.view.getDescriptions().get(0));
    }

    @Override
    public void preProcess(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext siriusWebEditingContext) {
            siriusWebEditingContext.getViews().add(this.view);
        }
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

}

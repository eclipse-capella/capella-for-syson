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

import org.eclipse.sirius.components.collaborative.trees.api.IDropTreeItemHandler;
import org.eclipse.sirius.components.collaborative.trees.dto.DropTreeItemInput;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.trees.Tree;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Handles drop operations in the Capella explorer tree by delegating execution
 * to the {@link CapellaExplorerDropTreeItemExecutor}.
 *
 * @author ntinsalhi
 */
@Service
public class CapellaExplorerDropTreeItemHandler implements IDropTreeItemHandler {

    private final CapellaTreeViewDescriptionProvider treeViewDescriptionProvider;

    private final CapellaExplorerDropTreeItemExecutor explorerDropTreeItemExecutor;


    public  CapellaExplorerDropTreeItemHandler(CapellaExplorerDropTreeItemExecutor explorerDropTreeItemExecutor,
                                               CapellaTreeViewDescriptionProvider treeViewDescriptionProvider) {
        this.treeViewDescriptionProvider = Objects.requireNonNull(treeViewDescriptionProvider);
        this.explorerDropTreeItemExecutor = Objects.requireNonNull(explorerDropTreeItemExecutor);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, Tree tree) {
        return this.treeViewDescriptionProvider.getDescriptionId().equals(tree.getDescriptionId());
    }

    @Override
    public IStatus handle(IEditingContext editingContext, Tree tree, DropTreeItemInput input) {
        return explorerDropTreeItemExecutor.drop(editingContext, tree, input.droppedElementIds(),
                input.targetElementId(), input.index());
    }
}

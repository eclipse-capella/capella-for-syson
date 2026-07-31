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
package org.eclipse.capella.table.view.providers;

import java.util.Map;
import java.util.Objects;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.tables.api.IRowContextMenuEntryExecutor;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.tables.Line;
import org.eclipse.sirius.components.tables.Table;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.eclipse.syson.sysml.ActionUsage;
import org.springframework.stereotype.Service;

/**
 * Executor for adding a new row in the Function Table using the context menu.
 * Creates a new {@link ActionUsage} as a sub-function of the selected function.
 *
 * @author ntinsalhi
 */
@Service
public class AddSubFunctionContextMenuEntryExecutor implements IRowContextMenuEntryExecutor {

    private final IObjectSearchService objectSearchService;

    private final TransverseMutationService transverseMutationService;

    public AddSubFunctionContextMenuEntryExecutor(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.transverseMutationService = new TransverseMutationService();
    }

    @Override
    public boolean canExecute(IEditingContext editingContext, TableDescription tableDescription, Table table, Line row, String rowMenuContextEntryId) {
        return FunctionTableRowContextMenuProvider.ADD_SUB_FUNCTION_ID.equals(rowMenuContextEntryId);
    }

    @Override
    public IStatus execute(IEditingContext editingContext, TableDescription tableDescription, Table table, Line row, String rowMenuContextEntryId) {

        this.objectSearchService.getObject(editingContext, row.getTargetObjectId())
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .ifPresent(this.transverseMutationService::createFunction);

        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
    }
}

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

import org.eclipse.capella.table.view.FunctionTableRepresentationDescriptionProvider;
import org.eclipse.sirius.components.collaborative.tables.api.IRowContextMenuEntryProvider;
import org.eclipse.sirius.components.collaborative.tables.dto.RowContextMenuEntry;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.tables.Line;
import org.eclipse.sirius.components.tables.Table;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Provides row context menu entries used in function table.
 *
 * @author ntinsalhi
 */
@Service
public class FunctionTableRowContextMenuProvider implements IRowContextMenuEntryProvider {

    public static final String DELETE_ID = "function-table-delete-row";

    public static final String DELETE_LABEL = "Delete function";

    public static final String ADD_SUB_FUNCTION_ID = "function-table-add-sub-function";

    public static final String ADD_SUB_FUNCTION_LABEL = "Add subfunction";

    public static final String ADD_FUNCTION_ID = "function-table-add-row";

    public static final String ADD_FUNCTION_LABEL = "Add function";

    @Override
    public boolean canHandle(IEditingContext editingContext, TableDescription tableDescription, Table table, Line row) {
        return Objects.equals(tableDescription.getId(), FunctionTableRepresentationDescriptionProvider.TABLE_DESCRIPTION_ID);
    }

    @Override
    public List<RowContextMenuEntry> getRowContextMenuEntries(IEditingContext editingContext, TableDescription tableDescription, Table table, Line row) {
        return List.of(
                new RowContextMenuEntry(DELETE_ID, DELETE_LABEL,
                        List.of("/icons/full/obj16/TableDeleteRow.svg")),
                new RowContextMenuEntry(ADD_FUNCTION_ID, ADD_FUNCTION_LABEL,
                        List.of("/icons/full/obj16/TableAddRow.svg")),
                new RowContextMenuEntry(ADD_SUB_FUNCTION_ID, ADD_SUB_FUNCTION_LABEL,
                        List.of("/icons/full/obj16/TableAddSubRow.svg"))
        );
    }
}

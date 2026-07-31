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
import org.eclipse.sirius.components.collaborative.tables.api.IRowFilterProvider;
import org.eclipse.sirius.components.collaborative.tables.api.RowFilter;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Provides row filters used in function table.
 *
 * @author ntinsalhi
 */
@Service
public class FunctionTableRowFiltersProvider implements IRowFilterProvider {

    public static final String OPEN_STATUS_ROW_FILTER_ID = "open-filter";
    public static final String TBD_STATUS_ROW_FILTER_ID = "tbd-filter";
    public static final String TBR_STATUS_ROW_FILTER_ID = "tbr-filter";
    public static final String TBC_STATUS_ROW_FILTER_ID = "tbc-filter";
    public static final String DONE_STATUS_ROW_FILTER_ID = "done-filter";
    public static final String CLOSED_STATUS_ROW_FILTER_ID = "closed-filter";
    public static final String NO_STATUS_ROW_FILTER_ID = "none-filter";

    @Override
    public boolean canHandle(IEditingContext editingContext, TableDescription tableDescription, String representationId) {
        return Objects.equals(tableDescription.getId(), FunctionTableRepresentationDescriptionProvider.TABLE_DESCRIPTION_ID);
    }

    @Override
    public List<RowFilter> get(IEditingContext editingContext, TableDescription tableDescription, String representationId) {
        return List.of(
                new RowFilter(OPEN_STATUS_ROW_FILTER_ID, "Open", true),
                new RowFilter(TBD_STATUS_ROW_FILTER_ID, "To Be Determined", true),
                new RowFilter(TBR_STATUS_ROW_FILTER_ID, "To Be Resolved", true),
                new RowFilter(TBC_STATUS_ROW_FILTER_ID, "To Be Confirmed", true),
                new RowFilter(DONE_STATUS_ROW_FILTER_ID, "Done", true),
                new RowFilter(CLOSED_STATUS_ROW_FILTER_ID, "Closed", true),
                new RowFilter(NO_STATUS_ROW_FILTER_ID, "None", true)
        );
    }
}

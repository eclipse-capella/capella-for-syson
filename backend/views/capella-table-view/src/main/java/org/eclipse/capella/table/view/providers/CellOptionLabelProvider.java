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

import java.util.function.Function;

import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.tables.components.SelectCellComponent;

/**
 * Provides label of an option in function table select cells.
 *
 * @author ntinsalhi
 */
public class CellOptionLabelProvider implements Function<VariableManager, String> {

    @Override
    public String apply(VariableManager variableManager) {
        Object candidate = variableManager.getVariables().get(SelectCellComponent.CANDIDATE_VARIABLE);
        return candidate.toString();

    }
}

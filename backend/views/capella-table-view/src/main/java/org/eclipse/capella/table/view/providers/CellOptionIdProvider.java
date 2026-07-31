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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.tables.descriptions.ColumnDescription;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.SysmlPackage;

import java.util.function.Function;

/**
 * Provides the ID of a cell option for function table columns.
 *
 * @author ntinsalhi
 */
public class CellOptionIdProvider implements Function<VariableManager, String> {

    @Override
    public String apply(VariableManager variableManager) {

        EObject self = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        Object columnTargetObject = variableManager.get(ColumnDescription.COLUMN_TARGET_OBJECT, Object.class).orElse(null);
        String cellOptionCandidate = variableManager.getVariables().get("candidate").toString();

        if (self instanceof ActionUsage && columnTargetObject == SysmlPackage.eINSTANCE.getOwningMembership()) {
            return cellOptionCandidate;
        }
        return "";
    }
}

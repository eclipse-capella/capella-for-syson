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

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.tables.IconLabelCell;
import org.eclipse.sirius.components.tables.descriptions.ColumnDescription;
import org.eclipse.sirius.components.tables.elements.SelectCellElementProps;
import org.eclipse.sirius.components.tables.elements.TextareaCellElementProps;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Provides function table cell type.
 *
 * @author ntinsalhi
 */
public class CellTypePredicate {

    private String getCellType(VariableManager variableManager) {
        String type = IconLabelCell.TYPE;

        Optional<EObject> optionalEObject = variableManager.get(VariableManager.SELF, EObject.class);
        Optional<EObject> optionalColumnTargetObject = variableManager
                .get(ColumnDescription.COLUMN_TARGET_OBJECT, Object.class)
                .filter(EObject.class::isInstance)
                .map(EObject.class::cast);

        if (optionalEObject.isPresent() && optionalColumnTargetObject.isPresent()) {
            EObject columnTargetObject = optionalColumnTargetObject.get();

            if (columnTargetObject == SysmlPackage.eINSTANCE.getOwningMembership()) {
                type = SelectCellElementProps.TYPE;

            } else if (columnTargetObject == SysmlPackage.eINSTANCE.getLiteralString()) {
                type = TextareaCellElementProps.TYPE;

            }
        }

        return type;
    }

    public Predicate<VariableManager> isTextareaCell() {
        return (variableManager) -> this.getCellType(variableManager).equals(TextareaCellElementProps.TYPE);
    }

    public Predicate<VariableManager> isSelectCell() {
        return (variableManager) -> this.getCellType(variableManager).equals(SelectCellElementProps.TYPE);
    }

    public Predicate<VariableManager> isLabelCell() {
        return (variableManager) -> this.getCellType(variableManager).equals(IconLabelCell.TYPE);
    }
}

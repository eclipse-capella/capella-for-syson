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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Provides cell options in function table select cells.
 *
 * @author ntinsalhi
 */
public class CellOptionsProvider implements BiFunction<VariableManager, Object, List<Object>> {

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    @Override
    public List<Object> apply(VariableManager variableManager, Object columnTargetObject) {
        EObject self = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (self instanceof ActionUsage function
                && columnTargetObject == SysmlPackage.eINSTANCE.getOwningMembership()) {
            List<String> statusLiterals = this.transverseQueryService.getStatusKindEnumLiterals(function);

            return new ArrayList<>(statusLiterals);

        }
        return List.of();
    }
}

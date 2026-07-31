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

import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.representations.VariableManager;

import java.util.function.Function;

/**
 * Provides columns targetObjectId in function table.
 *
 * @author ntinsalhi
 */
public class ColumnTargetObjectIdProvider implements Function<VariableManager, String> {

    @Override
    public String apply(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, ENamedElement.class)
                .map(eNamedElement -> EcoreUtil.getURI(eNamedElement).toString())
                .orElse("");
    }
}

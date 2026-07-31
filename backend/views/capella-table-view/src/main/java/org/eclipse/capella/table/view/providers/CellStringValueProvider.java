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

import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Provides string value of a cell in function table.
 *
 * @author ntinsalhi
 */
public class CellStringValueProvider implements BiFunction<VariableManager, Object, String> {

    private final LAQueryService laQueryService;

    private final TransverseQueryService transverseQueryService;

    public CellStringValueProvider() {
        this.laQueryService = new LAQueryService();
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public String apply(VariableManager variableManager, Object columnTargetObject) {
        EObject self = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (self == null || columnTargetObject == null) {
            return "";
        }

        String cellValue = "";

        if (columnTargetObject == SysmlPackage.eINSTANCE.getPartUsage()) {
            var optComponent = this.laQueryService.getAllocatingComponent((ActionUsage) self);
            cellValue = optComponent
                    .map(Element::getDeclaredName)
                    .orElse("");

        } else if (columnTargetObject == SysmlPackage.eINSTANCE.getReferenceUsage()) {
            cellValue = formatSysmlElements(this.laQueryService.getFunctionPorts(self));

        } else if (columnTargetObject == SysmlPackage.eINSTANCE.getLiteralString()) {
            cellValue = this.transverseQueryService.getArcadiaElementDescription(self);

        } else if (columnTargetObject == SysmlPackage.eINSTANCE.getOwningMembership()) {
            var optionalFunctionStatus = Optional.ofNullable(this.laQueryService.getStatus((ActionUsage) self));
            cellValue = optionalFunctionStatus.map(Element::getDeclaredName).orElse("");
        }

        return cellValue;
    }

    private String formatSysmlElements(List<? extends Element > elements) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }

        return elements.stream()
                .map(Element::getDeclaredName)
                .collect(Collectors.joining(", ", "", ""));
    }
}

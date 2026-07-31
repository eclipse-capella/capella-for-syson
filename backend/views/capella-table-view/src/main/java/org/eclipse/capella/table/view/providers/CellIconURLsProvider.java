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
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;

import java.util.function.BiFunction;
import java.util.List;
import java.util.Optional;

/**
 *  * Provides a list of icon URLs to decorate table cells.
 *
 * @author ntinsalhi
 */
public class CellIconURLsProvider implements BiFunction<VariableManager, Object, List<String>> {

    private final LAQueryService laQueryService;

    private final TransverseQueryService transverseQueryService;

    public CellIconURLsProvider() {
        this.laQueryService = new LAQueryService();
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public List<String> apply(VariableManager variableManager, Object columnTargetObject) {
        // @technical-debt
        List<String> iconPath = List.of();

        EObject self = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (self == null || columnTargetObject == null) {
            return iconPath;
        }

        if (columnTargetObject == SysmlPackage.eINSTANCE.getPartUsage()) {
            iconPath = this.resolveComponentIcon(self);
        } else if (columnTargetObject == SysmlPackage.eINSTANCE.getReferenceUsage()) {
            iconPath = this.resolvePortIcon(self);
        }

        return iconPath;
    }

    private List<String> resolveComponentIcon(EObject self) {
        List<String> iconPath = List.of();
        Optional<PartUsage> component = this.laQueryService.getAllocatingComponent((ActionUsage) self);
        if (component.isPresent()) {
            iconPath = this.getAllocatingComponentIcon(component.get());
        }
        return iconPath;
    }

    private List<String> resolvePortIcon(EObject self) {
        List<String> iconPath = List.of();
        List<Feature> ports = this.laQueryService.getFunctionPorts(self);

        if (ports.size() == 1) {
            iconPath = this.getFunctionPortIcon(ports.get(0));
        } else if (ports.size() > 1) {
            iconPath = List.of("/icons/full/obj16/FlowPort.svg");
        }

        return iconPath;
    }

    private List<String> getAllocatingComponentIcon(PartUsage component) {
        List<String> iconPath = List.of();

        if (this.transverseQueryService.isComponentHumanActor(component)) {
            iconPath = List.of("/icons/full/obj16/LogicalComponentHuman.svg");
        } else if (this.transverseQueryService.isComponentActor(component)) {
            iconPath = List.of("/icons/full/obj16/LogicalActor.svg");
        } else if (this.transverseQueryService.isComponent(component)) {
            iconPath = List.of("/icons/full/obj16/LogicalComponent.svg");
        }

        return iconPath;
    }

    private List<String> getFunctionPortIcon(Feature port) {
        List<String> iconPath = List.of();

        if (this.transverseQueryService.isInFeature(port)) {
            iconPath = List.of("/icons/full/obj16/FunctionInputPort.svg");
        } else if (this.transverseQueryService.isOutFeature(port)) {
            iconPath = List.of("/icons/full/obj16/FunctionOutputPort.svg");
        }

        return iconPath;
    }
}

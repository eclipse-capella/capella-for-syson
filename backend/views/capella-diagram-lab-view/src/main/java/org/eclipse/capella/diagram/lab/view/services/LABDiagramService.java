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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view.services;

import java.util.Objects;

import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * A Service class specific to the LAB Diagram. This class is intended to be used for services specific to the view
 * without relation with the model or the business layer.
 *
 * @author fbarbin
 */
public class LABDiagramService {

    private final ShowDiagramFunctionsService showDiagramFunctionsService;

    public LABDiagramService(ShowDiagramFunctionsService diagramFunctionsService) {
        this.showDiagramFunctionsService = Objects.requireNonNull(diagramFunctionsService);
    }

    public boolean isFunctionHidden(Object self) {
        return !this.showDiagramFunctionsService.isShowFunctions();
    }

    /**
     * Determines if a compartment should be hidden by default for RequirementUsage elements.
     * <p>
     * For RequirementUsage, only the documentation compartment is visible by default.
     * All other compartments (nestedAttribute, actorParameter, assumedConstraint, requiredConstraint, nestedPort)
     * are hidden by default.
     * </p>
     *
     * @param self
     *            the element owning the compartment
     * @param referenceName
     *            the name of the EReference corresponding to the compartment
     * @return {@code true} if the compartment should be hidden by default, {@code false} otherwise
     */
    public boolean isHiddenByDefault(Element self, String referenceName) {
        if (self instanceof RequirementUsage) {
            // Only show documentation compartment by default
            return !SysmlPackage.eINSTANCE.getElement_Documentation().getName().equals(referenceName);
        }
        // For other elements, use default behavior (not hidden)
        return false;
    }
}

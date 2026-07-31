/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package org.eclipse.capella.diagram.lab.view;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.DescriptionNameGenerator;

/**
 * Name generator for LAB (Logical Architecture Blank) diagram description providers.
 *
 * @author vkravchenko
 */
public class LABDescriptionNameGenerator extends DescriptionNameGenerator {

    /**
     * Prefix for LAB diagram node descriptions.
     */
    public static final String PREFIX = "LAB";

    public LABDescriptionNameGenerator() {
        super(PREFIX);
    }

    @Override
    public String getCreationToolName(EReference eReference) {
        String name = super.getCreationToolName(eReference);
        if (SysmlPackage.eINSTANCE.getRequirementUsage_AssumedConstraint().equals(eReference)
                || SysmlPackage.eINSTANCE.getRequirementDefinition_AssumedConstraint().equals(eReference)) {
            name = "New Assume constraint";
        } else if (SysmlPackage.eINSTANCE.getRequirementUsage_RequiredConstraint().equals(eReference)
                || SysmlPackage.eINSTANCE.getRequirementDefinition_RequiredConstraint().equals(eReference)) {
            name = "New Require constraint";
        }
        return name;
    }
}

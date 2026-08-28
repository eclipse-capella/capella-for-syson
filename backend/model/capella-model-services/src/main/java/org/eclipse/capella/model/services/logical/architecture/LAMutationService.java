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
package org.eclipse.capella.model.services.logical.architecture;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Logical Architecture (LA) related mutation service. It is important to note that this service must retain its empty
 * constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class LAMutationService {

    private final TransverseQueryService transverseQueryService;

    private final TransverseMutationService transverseMutationService;

    public LAMutationService() {
        this.transverseQueryService = new TransverseQueryService();
        this.transverseMutationService = new TransverseMutationService();
    }

    /**
     * Creates a logical component in the selected component or, from the LAB root, in the logical system.
     *
     * @param parent
     *         the selected semantic element
     * @return the newly created logical component
     * @throws IllegalStateException
     *         if the logical system is missing from the Structure package
     */
    public PartUsage createComponentLA(Element parent) {
        Element targetContainer = parent;
        if (!this.transverseQueryService.isComponent(parent)) {
            Package structurePackage = this.transverseQueryService.getStructurePackage(parent)
                    .orElseThrow(() -> new IllegalStateException("The logical architecture Structure package is missing"));
            targetContainer = structurePackage.getOwnedElement().stream()
                    .filter(this.transverseQueryService::isComponent)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("The logical system is missing from the Structure package"));
        }
        return this.transverseMutationService.createComponent(targetContainer);
    }

}

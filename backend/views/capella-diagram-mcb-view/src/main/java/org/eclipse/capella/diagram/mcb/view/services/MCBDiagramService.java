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
package org.eclipse.capella.diagram.mcb.view.services;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.syson.sysml.Package;

/**
 * Provides services specific to the Mission and Capabilities Blank diagram.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    /**
     * Checks whether the context is the System Analysis Capabilities package or one of its nested packages.
     *
     * @param element
     *         the diagram context
     * @return whether an MCB diagram can be created on the context
     */
    public boolean isSystemAnalysisCapabilitiesPackage(Object element) {
        if (element instanceof Package packageElt
                && this.commonQueryService.getArcadiaPerspective(packageElt).filter(ArcadiaEngineeringPerspective.SystemAnalysis::equals).isPresent()) {
            return this.commonQueryService.getCapabilitiesPackage(packageElt)
                    .filter(capabilitiesPackage -> EcoreUtil.isAncestor(capabilitiesPackage, packageElt))
                    .isPresent();
        }
        return false;
    }
}

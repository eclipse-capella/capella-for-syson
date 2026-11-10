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

package org.eclipse.capella.diagram.sab.view.services;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;

/**
 * Services of the SAB diagram.
 *
 * @author gdaniel
 */
public class SABDiagramService {

    private final CommonQueryService commonQueryService;

    public SABDiagramService() {
        this.commonQueryService = new CommonQueryService();
    }

    /**
     * Returns {@code true} if a SAB diagram can be created on {@code self}.
     * <p>
     * SAB diagrams can be created on the structure package, its sub-packages, or any component of the <i>system analysis</i> perspective.
     *
     * @param self
     *         the element to check
     * @return {@code true} if the diagram can be created, {@code false} otherwise
     */
    public boolean canCreateSABDiagram(Element self) {
        return this.commonQueryService.isInPerspective(self, ArcadiaEngineeringPerspective.SystemAnalysis)
                && (this.commonQueryService.isComponent(self)
                || this.commonQueryService.isStructurePackage(self)
                || this.isStructureSubPackage(self));
    }

    private boolean isStructureSubPackage(Element self) {
        return self instanceof Package
                && EMFUtils.getFirstAncestor(Package.class, self, this.commonQueryService::isStructurePackage).isPresent();
    }
}

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

package org.eclipse.capella.diagram.lab.view.services;

import java.util.List;

import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Semantic candidate services of the LAB diagram.
 *
 * @author gdaniel
 */
public class LABDiagramSemanticCandidatesService {

    private final LAQueryService laQueryService;

    private final CommonQueryService commonQueryService;

    public LABDiagramSemanticCandidatesService() {
        this.laQueryService = new LAQueryService();
        this.commonQueryService = new CommonQueryService();
    }

    public List<ActionUsage> getFunctionCandidates(Element self) {
        List<ActionUsage> result = List.of();
        if (this.commonQueryService.isComponent(self)) {
            result = this.commonQueryService.getAllocatedFunctions((PartUsage) self);
        } else if (this.commonQueryService.isFunction(self)) {
            result = this.commonQueryService.getSubFunctions(self);
        }
        return result;
    }

    public List<PartUsage> getComponentCandidates(Element self) {
        List<PartUsage> result = List.of();
        if (self instanceof Package) {
            result = this.laQueryService.getComponents(self);
        } else if (this.commonQueryService.isComponent(self)) {
            result = this.commonQueryService.getSubComponents((PartUsage) self);
        }
        return result;
    }

}

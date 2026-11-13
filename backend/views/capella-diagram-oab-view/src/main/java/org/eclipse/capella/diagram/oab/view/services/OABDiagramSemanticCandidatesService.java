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

package org.eclipse.capella.diagram.oab.view.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.model.services.operational.analysis.OAQueryService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Semantic candidate services of the OAB diagram.
 *
 * @author gdaniel
 */
public class OABDiagramSemanticCandidatesService {

    private final OAQueryService oaQueryService;

    private final CommonQueryService commonQueryService;

    public OABDiagramSemanticCandidatesService() {
        this.oaQueryService = new OAQueryService();
        this.commonQueryService = new CommonQueryService();
    }

    public List<PartUsage> getComponentCandidates(Element self) {
        List<PartUsage> result = new ArrayList<>();
        if (self instanceof Package) {
            result = this.oaQueryService.getComponents(self);
        } else if (this.commonQueryService.isComponent(self)) {
            result = this.commonQueryService.getSubComponents((PartUsage) self);
        }
        return result;
    }
}

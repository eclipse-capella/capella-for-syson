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

import java.util.List;

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Semantic candidate services of the SAB diagram.
 *
 * @author gdaniel
 */
public class SABDiagramSemanticCandidatesService {

    private final SAQueryService saQueryService;

    private final CommonQueryService commonQueryService;

    public SABDiagramSemanticCandidatesService() {
        this.saQueryService = new SAQueryService();
        this.commonQueryService = new CommonQueryService();
    }

    public PartUsage getSystemOfInterestCandidate(Element self) {
        return this.saQueryService.getSystemOfInterest(self)
                .orElse(null);
    }

    public List<PartUsage> getSystemComponentCandidates(Element self) {
        List<PartUsage> result = List.of();
        if (self instanceof PartUsage partUsage) {
            result = this.saQueryService.getSubSystemComponents(partUsage);
        }
        return result;
    }

    public List<PartUsage> getActorCandidates(Element self) {
        return this.saQueryService.getSubActors(self);
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
}

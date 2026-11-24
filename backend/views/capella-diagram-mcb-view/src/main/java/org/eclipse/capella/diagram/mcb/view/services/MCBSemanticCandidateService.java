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

import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Provides semantic candidates for MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class MCBSemanticCandidateService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final SAQueryService saQueryService = new SAQueryService();

    public List<PartUsage> getActorCandidates(Element self) {
        List<PartUsage> result = List.of();
        if (self instanceof PartUsage) {
            if (this.commonQueryService.isComponentActor(self) && !this.commonQueryService.isComponentHumanActor(self)) {
                // Unsynchronized views are created under the closest represented ancestor, which may not be the direct parent.
                result = this.saQueryService.getActors(self).stream()
                        .filter(actor -> actor != self && EcoreUtil.isAncestor(self, actor))
                        .toList();
            }
        } else {
            result = this.saQueryService.getActors(self);
        }
        return result;
    }

}

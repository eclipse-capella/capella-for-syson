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
import java.util.stream.Stream;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.OccurrenceUsage;

/**
 * Semantic candidate services of the MCB diagram.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramSemanticCandidatesService {

    private final CommonQueryService commonQueryService;

    public MCBDiagramSemanticCandidatesService() {
        this.commonQueryService = new CommonQueryService();
    }

    public List<OccurrenceUsage> getInvolvementCandidates(Element self) {
        return Stream.concat(this.commonQueryService.getMissions(self).stream(),
                    this.commonQueryService.getCapabilities(self).stream())
                .distinct().toList();
    }
}

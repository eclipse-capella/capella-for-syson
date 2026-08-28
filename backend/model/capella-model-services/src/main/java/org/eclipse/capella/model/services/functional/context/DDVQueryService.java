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

package org.eclipse.capella.model.services.functional.context;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;

/**
 * Functional Context related query service. It is important to note that this service must retain its empty
 * constructor and should not have constructors with parameters.
 *
 * @author gdaniel
 */
public class DDVQueryService {

    private final TransverseQueryService transverseQueryService;

    public DDVQueryService() {
        this.transverseQueryService = new TransverseQueryService();
    }

    public List<FlowUsage> getRelatedFunctionalExchanges(EObject self) {
        if (self instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(actionUsage)) {
            var referencingFunctionalExchanges = this.transverseQueryService.getIncomingFunctionalExchanges(actionUsage)
                    .stream();

            var referencedFunctionalExchanges = this.transverseQueryService.getOutgoingFunctionalExchanges(actionUsage)
                    .stream();

            return Stream.concat(referencingFunctionalExchanges, referencedFunctionalExchanges)
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    public List<ActionUsage> getReferencedAndReferencingFunctions(EObject self) {
        if (self instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(actionUsage)) {
            var referencingFunctions = this.transverseQueryService.getIncomingFunctionalExchanges(actionUsage)
                    .stream()
                    .map(this.transverseQueryService::getFunctionalExchangeSourceFunction);

            var referencedFunctions = this.transverseQueryService.getOutgoingFunctionalExchanges(actionUsage)
                    .stream()
                    .map(this.transverseQueryService::getFunctionalExchangeTargetFunction);

            return Stream.concat(referencedFunctions, referencingFunctions)
                    .distinct()
                    .toList();
        }
        return List.of();
    }
}

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

import org.eclipse.capella.model.transverse.services.CommonQueryService;
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

    private final CommonQueryService commonQueryService;

    public DDVQueryService() {
        this.commonQueryService = new CommonQueryService();
    }

    public List<FlowUsage> getRelatedFunctionalExchanges(EObject self) {
        if (self instanceof ActionUsage actionUsage && this.commonQueryService.isFunction(actionUsage)) {
            var referencingFunctionalExchanges = this.commonQueryService.getIncomingFunctionalExchanges(actionUsage)
                    .stream();

            var referencedFunctionalExchanges = this.commonQueryService.getOutgoingFunctionalExchanges(actionUsage)
                    .stream();

            return Stream.concat(referencingFunctionalExchanges, referencedFunctionalExchanges)
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    public List<ActionUsage> getReferencedAndReferencingFunctions(EObject self) {
        if (self instanceof ActionUsage actionUsage && this.commonQueryService.isFunction(actionUsage)) {
            var referencingFunctions = this.commonQueryService.getIncomingFunctionalExchanges(actionUsage)
                    .stream()
                    .map(this.commonQueryService::getFunctionalExchangeSourceFunction);

            var referencedFunctions = this.commonQueryService.getOutgoingFunctionalExchanges(actionUsage)
                    .stream()
                    .map(this.commonQueryService::getFunctionalExchangeTargetFunction);

            return Stream.concat(referencedFunctions, referencingFunctions)
                    .distinct()
                    .toList();
        }
        return List.of();
    }
}

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
package org.eclipse.capella.model.services.operational.analysis;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.InterfaceUsage;

/**
 * Operational Analysis (OA) related mutation service.
 * It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class OAMutationService {

    private final TransverseMutationService transverseMutationService;

    private final TransverseQueryService transverseQueryService;

    public OAMutationService() {
        this.transverseMutationService = new TransverseMutationService();
        this.transverseQueryService = new TransverseQueryService();
    }

    public InterfaceUsage createCommunicationMeanComponentExchangeOA(Feature source, Feature target) {
        var componentExchange = this.transverseMutationService.createComponentExchange(source, target);
        if (componentExchange != null) {
            long existingElementsCount = this.transverseQueryService.existingElementsCount(componentExchange);
            componentExchange.setDeclaredName("CommunicationMean " + existingElementsCount);
        }
        return componentExchange;
    }

}

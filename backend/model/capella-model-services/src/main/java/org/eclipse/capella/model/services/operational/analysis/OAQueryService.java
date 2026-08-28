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

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.PartUsage;

import java.util.Optional;

/**
 * Operational Analysis (OA) related query service. It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class OAQueryService {

    private final TransverseQueryService transverseQueryService;

    public OAQueryService() {
        this.transverseQueryService = new TransverseQueryService();
    }

    /**
     * Returns the component source of a component exchange.
     * <p>
     * OA diagrams do not display ports on components, which are the actual sources of component exchange. This method allows to represent component exchange between components instead of ports.
     * </p>
     *
     * @param interfaceUsage
     *         the component exchange the return the source from
     * @return the component source of the provided component exchange
     */
    public PartUsage getComponentExchangeSourceOA(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(this.transverseQueryService.getComponentExchangeSource(interfaceUsage))
                .map(Element::getOwner)
                .filter(this.transverseQueryService::isComponent)
                .map(PartUsage.class::cast)
                .orElse(null);
    }

    /**
     * Returns the component target of a component exchange.
     * <p>
     * OA diagrams do not display ports on components, which are the actual targets of component exchange. This method allows to represent component exchange between components instead of ports.
     * </p>
     *
     * @param interfaceUsage
     *         the component exchange the return the target from
     * @return the component target of the provided component exchange
     */
    public PartUsage getComponentExchangeTargetOA(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(this.transverseQueryService.getComponentExchangeTarget(interfaceUsage))
                .map(Element::getOwner)
                .filter(this.transverseQueryService::isComponent)
                .map(PartUsage.class::cast)
                .orElse(null);
    }
}

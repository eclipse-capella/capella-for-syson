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
package org.eclipse.capella.model.transverse.services;

import org.eclipse.syson.sysml.Element;

/**
 * Dispatches elements by their Arcadia library type rather than their concrete SysML EClass.
 * Subclasses override only the cases they handle. A null result continues to
 * {@link #defaultCase(Element)}; a non-null result, including an empty collection, stops dispatch.
 * Actor flags, package roles and engineering perspectives are interpreted by consumers.
 *
 * @param <T> the result type
 * @author tbezierslafosse
 */
public abstract class AbstractArcadiaSwitch<T> {

    private final CommonQueryService queryService = new CommonQueryService();

    /**
     * Dispatches an element using the Arcadia predicates of {@link CommonQueryService}.
     *
     * @param element the element to interpret, or null
     * @return the case result, or the default result
     */
    public T doSwitch(Element element) {
        T result = switch (element) {
            case Element e when this.queryService.isComponent(e) -> this.caseComponent(e);
            case Element e when this.queryService.isFunction(e) -> this.caseFunction(e);
            case Element e when this.queryService.isCapability(e) -> this.caseCapability(e);
            case Element e when this.queryService.isMission(e) -> this.caseMission(e);
            case Element e when this.queryService.isComponentPort(e) -> this.caseComponentPort(e);
            case Element e when this.queryService.isComponentExchange(e) -> this.caseComponentExchange(e);
            case Element e when this.queryService.isFunctionalExchange(e) -> this.caseFunctionalExchange(e);
            case Element e when this.queryService.isFunctionalChain(e) -> this.caseFunctionalChain(e);
            case Element e when this.queryService.isExchangeItem(e) -> this.caseExchangeItem(e);
            case Element e when this.queryService.isArcadiaRequirement(e) -> this.caseRequirement(e);
            case null, default -> null;
        };
        if (result == null) {
            result = this.defaultCase(element);
        }
        return result;
    }

    protected T caseComponent(Element element) {
        return null;
    }

    protected T caseFunction(Element element) {
        return null;
    }

    protected T caseCapability(Element element) {
        return null;
    }

    protected T caseMission(Element element) {
        return null;
    }

    protected T caseComponentPort(Element element) {
        return null;
    }

    protected T caseComponentExchange(Element element) {
        return null;
    }

    protected T caseFunctionalExchange(Element element) {
        return null;
    }

    protected T caseFunctionalChain(Element element) {
        return null;
    }

    protected T caseExchangeItem(Element element) {
        return null;
    }

    protected T caseRequirement(Element element) {
        return null;
    }

    protected T defaultCase(Element element) {
        return null;
    }
}

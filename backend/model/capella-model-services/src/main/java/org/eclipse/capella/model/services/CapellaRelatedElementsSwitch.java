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

package org.eclipse.capella.model.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.syson.services.RelatedElementsSwitch;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureChaining;
import org.eclipse.syson.sysml.FlowEnd;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.util.SysmlSwitch;

/**
 * Provides the related elements of a given element.
 * <p>
 * This class returns both the related elements from a Capella perspective (e.g. the ComponentExchange related to a Component) as well as the related elements from a SysON perspective (e.g. the
 * dependencies associated to an element).
 * </p>
 *
 * @author gdaniel
 */
public class CapellaRelatedElementsSwitch extends SysmlSwitch<Set<EObject>> {

    private final EStructuralFeature eStructuralFeature;

    private final RelatedElementsSwitch relatedElementsSwitch;

    private final TransverseQueryService transverseQueryService;

    public CapellaRelatedElementsSwitch(EStructuralFeature eStructuralFeature) {
        this.eStructuralFeature = eStructuralFeature;
        this.relatedElementsSwitch = new RelatedElementsSwitch(eStructuralFeature);
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public Set<EObject> caseFeatureChaining(FeatureChaining featureChaining) {
        Set<EObject> relatedElements = new HashSet<>(Objects.requireNonNullElseGet(super.caseFeatureChaining(featureChaining), Collections::emptySet));
        if (this.transverseQueryService.isComponentPort(featureChaining.getChainingFeature())) {
            // Delete the ComponentExchange connected to the port being deleted.
            Optional.ofNullable(featureChaining.getFeatureChained())
                    .map(Element::getOwner)
                    // The unnamed PortUsage directly contained in the ComponentExchange.
                    .filter(PortUsage.class::isInstance)
                    .map(Element::getOwner)
                    .filter(this.transverseQueryService::isComponentExchange)
                    .ifPresent(relatedElements::add);
        } else if (this.transverseQueryService.isFunctionPort(featureChaining.getChainingFeature())) {
            // Delete the FunctionalExchange connected to the port being deleted.
            Optional.ofNullable(featureChaining.getFeatureChained())
                    .map(Element::getOwner)
                    // The unnamed FlowEnd directly contained in the FunctionalExchange.
                    .filter(FlowEnd.class::isInstance)
                    .map(Element::getOwner)
                    .filter(this.transverseQueryService::isFunctionalExchange)
                    .ifPresent(relatedElements::add);
        }
        return relatedElements;
    }

    @Override
    public Set<EObject> defaultCase(EObject object) {
        // Do not call defaultCase on the SysON switch, otherwise it won't get SysON-specific related elements for elements not covered by the Capella switch.
        return this.relatedElementsSwitch.doSwitch(object);
    }
}

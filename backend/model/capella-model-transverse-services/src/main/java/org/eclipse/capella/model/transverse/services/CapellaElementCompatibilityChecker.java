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

import java.util.List;
import java.util.Optional;

import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;

/**
 * Checks that Capella elements are compatible with a given semantic context.
 * <p>
 * This checker validates owners, relationship ends, and content before an operation mutates the semantic model.
 *
 * @author gdaniel
 */
public class CapellaElementCompatibilityChecker {

    private final TransverseQueryService transverseQueryService;

    public CapellaElementCompatibilityChecker() {
        this.transverseQueryService = new TransverseQueryService();
    }

    public boolean isValidActorOwner(Element owner) {
        return this.transverseQueryService.isComponent(owner) || this.transverseQueryService.getStructurePackage(owner).isPresent();
    }

    public boolean isValidComponentOwner(Element owner) {
        return this.transverseQueryService.isComponent(owner) || this.transverseQueryService.getStructurePackage(owner).isPresent();
    }

    public boolean areValidComponentExchangeEnds(Element source, Element target) {
        boolean result = false;
        // TODO PR#103 changes this feature
        Optional<Package> optionalSourceStructurePackage = this.transverseQueryService.getStructurePackage(source);
        Optional<Package> optionalTargetStructurePackage = this.transverseQueryService.getStructurePackage(target);
        if (optionalSourceStructurePackage.isPresent() && optionalSourceStructurePackage.equals(optionalTargetStructurePackage)) {
            Optional<PartUsage> sourceComponent = EMFUtils.getFirstAncestor(PartUsage.class, source, this.transverseQueryService::isComponent);
            Optional<PartUsage> targetComponent = EMFUtils.getFirstAncestor(PartUsage.class, target, this.transverseQueryService::isComponent);
            result = sourceComponent.isPresent() && targetComponent.isPresent() && !sourceComponent.equals(targetComponent);
        }
        return result;
    }

    public boolean isValidComponentPortOwner(Element owner) {
        return this.transverseQueryService.isComponent(owner);
    }

    public boolean areValidDescribesEnds(Element source, Element target) {
        return this.transverseQueryService.isRequirement(source);
    }

    public boolean isValidFunctionOwner(Element owner) {
        return this.transverseQueryService.isFunction(owner) || this.transverseQueryService.getRootFunction(owner).isPresent();
    }

    public boolean isValidFunctionalChain(Element owner, List<Object> content) {
        // A functional chain is created in the common ancestor of the functional exchanges involved in it, so it cannot be created if there is no content.
        return !content.isEmpty();
    }

    public boolean areValidFunctionalExchangeEnds(Element source, Element target) {
        boolean result = false;
        // TODO PR#103 changes this feature
        Optional<Package> optionalSourceFunctionsPackage = this.transverseQueryService.getFunctionsPackage(source);
        Optional<Package> optionalTargetFunctionsPackage = this.transverseQueryService.getFunctionsPackage(target);
        if (optionalSourceFunctionsPackage.isPresent() && optionalSourceFunctionsPackage.equals(optionalTargetFunctionsPackage)) {
            Optional<ActionUsage> sourceFunction = EMFUtils.getFirstAncestor(ActionUsage.class, source, this.transverseQueryService::isFunction);
            Optional<ActionUsage> targetFunction = EMFUtils.getFirstAncestor(ActionUsage.class, target, this.transverseQueryService::isFunction);
            result = sourceFunction.isPresent() && targetFunction.isPresent() && !sourceFunction.equals(targetFunction);
        }
        return result;
    }

    public boolean isValidFunctionPortOwner(Element owner) {
        return this.transverseQueryService.isFunction(owner);
    }

    public boolean isValidOperationalCapabilityOwner(Element owner) {
        return this.transverseQueryService.getCapabilitiesPackage(owner).isPresent();
    }

    public boolean isValidRequirementOwner(Element owner) {
        return this.transverseQueryService.getRequirementsPackage(owner).isPresent();
    }
}

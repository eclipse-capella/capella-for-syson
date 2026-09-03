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
package org.eclipse.capella.model.services.system.analysis;

import java.util.Optional;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;

/**
 * System Analysis semantic mutation service.
 *
 * @author tbezierslafosse
 */
public class SAMutationService {

    private final SAQueryService saQueryService;

    private final TransverseQueryService transverseQueryService;

    private final TransverseMutationService transverseMutationService;

    public SAMutationService() {
        this.saQueryService = new SAQueryService();
        this.transverseQueryService = new TransverseQueryService();
        this.transverseMutationService = new TransverseMutationService();
    }

    public PartUsage createActorSA(Element parent) {
        PartUsage result = null;
        Optional<Element> targetContainer = Optional.empty();
        if (parent instanceof PartUsage partUsage && !this.saQueryService.isSystemOfInterest(partUsage)) {
            targetContainer = Optional.of(parent);
        } else {
            targetContainer = this.transverseQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (targetContainer.isPresent()) {
            result = this.transverseMutationService.createActor(targetContainer.get());
        }
        return result;
    }

    public PartUsage createComponentSA(Element parent) {
        PartUsage result = null;
        Element targetContainer = null;
        if (parent instanceof PartUsage partUsage && this.saQueryService.isSystemOfInterest(partUsage)) {
            targetContainer = parent;
        } else if (parent instanceof PartUsage partUsage && partUsage.getOwner() instanceof PartUsage && this.saQueryService.isSystemComponent(partUsage)) {
            targetContainer = parent;
        }
        if (targetContainer != null) {
            result = this.transverseMutationService.createComponent(targetContainer);
        }
        return result;
    }

    public void moveFunctionToComponent(ActionUsage function, Object previousParent, PartUsage targetComponent) {
        if (previousParent != targetComponent && previousParent instanceof PartUsage previousParentPartUsage && this.transverseQueryService.isComponent(previousParentPartUsage)) {
            this.transverseMutationService.deletePerformedActionUsage(previousParentPartUsage, function);
            this.transverseMutationService.setPerformAction(targetComponent, function);
        }
    }
}

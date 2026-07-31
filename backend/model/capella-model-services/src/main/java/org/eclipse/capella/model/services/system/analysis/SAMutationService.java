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

import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;

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

    public void moveFunctionToComponent(ActionUsage function, PartUsage targetComponent) {
        var root = this.getRoot(function);
        var components = new ArrayList<PartUsage>();
        root.eAllContents().forEachRemaining(child -> {
            if (child instanceof PartUsage partUsage) {
                components.add(partUsage);
            }
        });
        components.forEach(component -> this.transverseMutationService.deletePerformedActionUsage(component, function));
        this.createPerformActionUsage(targetComponent, function);
    }

    private EObject getRoot(EObject eObject) {
        EObject root = eObject;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        return root;
    }

    private void createPerformActionUsage(PartUsage component, ActionUsage function) {
        var membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        component.getOwnedRelationship().add(membership);
        var performActionUsage = SysmlFactory.eINSTANCE.createPerformActionUsage();
        membership.getOwnedRelatedElement().add(performActionUsage);
        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(function);
        performActionUsage.getOwnedRelationship().add(referenceSubsetting);
    }
}

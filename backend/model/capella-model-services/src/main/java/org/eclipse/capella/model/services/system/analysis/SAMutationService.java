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

import static org.eclipse.capella.model.transverse.services.CommonNamingService.SYSTEM_FUNCTION_DEFAULT_DECLAREDNAME_PREFIX;

import java.util.List;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonMoveService;
import org.eclipse.capella.model.transverse.services.CommonNamingService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;

/**
 * System Analysis semantic mutation service.
 *
 * @author tbezierslafosse
 */
public class SAMutationService {

    private final SAQueryService saQueryService;

    private final CommonQueryService commonQueryService;

    private final CommonCreationService commonCreationService;

    private final CommonUpdateService commonUpdateService;

    private final CommonDeletionService commonDeletionService;

    private final CommonMoveService commonMoveService;

    private final CommonNamingService commonNamingService;

    public SAMutationService() {
        this.saQueryService = new SAQueryService();
        this.commonQueryService = new CommonQueryService();
        this.commonCreationService = new CommonCreationService();
        this.commonUpdateService = new CommonUpdateService();
        this.commonDeletionService = new CommonDeletionService();
        this.commonMoveService = new CommonMoveService();
        this.commonNamingService = new CommonNamingService();
    }

    public Usage createCapabilityExploitation(Usage mission, Usage capability) {
        if (this.saQueryService.canCreateCapabilityExploitation(mission, capability)) {
            this.commonUpdateService.addFeatureReferenceValue(mission, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_MISSION,
                    CommonQueryService.ARCADIA_EXPLOITED_CAPABILITIES, capability, SysmlPackage.eINSTANCE.getOccurrenceUsage());
        }
        return mission;
    }

    public Usage deleteCapabilityExploitation(Usage mission, Usage capability) {
        this.commonUpdateService.deleteFeaturesFromReference(mission, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_MISSION,
                CommonQueryService.ARCADIA_EXPLOITED_CAPABILITIES, SysmlPackage.eINSTANCE.getOccurrenceUsage(), List.of(capability));
        if (this.saQueryService.getExploitedCapabilities(mission).isEmpty()) {
            this.commonUpdateService.deleteReference(mission, CommonQueryService.ARCADIA_EXPLOITED_CAPABILITIES);
        }
        return mission;
    }

    public Usage setCapabilityExploitationTarget(Usage mission, Usage oldCapability, Usage newCapability) {
        if (this.saQueryService.canCreateCapabilityExploitation(mission, newCapability)) {
            this.createCapabilityExploitation(mission, newCapability);
            this.deleteCapabilityExploitation(mission, oldCapability);
        }
        return mission;
    }

    public Usage setCapabilityExploitationSource(Usage oldMission, Usage newMission, Usage capability) {
        if (this.saQueryService.canCreateCapabilityExploitation(newMission, capability)) {
            this.createCapabilityExploitation(newMission, capability);
            this.deleteCapabilityExploitation(oldMission, capability);
            return newMission;
        }
        return oldMission;
    }

    public PartUsage createActorSA(Element parent) {
        PartUsage result = null;
        Optional<Element> targetContainer = Optional.empty();
        if (parent instanceof PartUsage partUsage && !this.saQueryService.isSystemOfInterest(partUsage)) {
            targetContainer = Optional.of(parent);
        } else {
            targetContainer = this.commonQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (targetContainer.isPresent()) {
            result = this.commonCreationService.createActor(targetContainer.get());
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
            result = this.commonCreationService.createComponent(targetContainer);
        }
        return result;
    }

    public void moveFunctionToComponent(ActionUsage function, Object previousParent, PartUsage targetComponent) {
        if (previousParent != targetComponent && previousParent instanceof PartUsage previousParentPartUsage && this.commonQueryService.isComponent(previousParentPartUsage)) {
            this.commonDeletionService.deletePerformedActionUsage(previousParentPartUsage, function);
            this.commonUpdateService.setPerformAction(targetComponent, function);
        }
    }

    public ActionUsage createSystemFunctionSA(Element parent) {
        ActionUsage activity = null;
        if (this.saQueryService.isSystemFunction(parent)
                || this.commonQueryService.isComponent(parent) && this.commonQueryService.isInPerspective(parent, ArcadiaEngineeringPerspective.SystemAnalysis)) {
            activity = this.commonCreationService.createFunction(parent);
            if (activity != null) {
                this.commonNamingService.setElementDefaultDeclaredName(activity, SYSTEM_FUNCTION_DEFAULT_DECLAREDNAME_PREFIX);
            }
        }
        return activity;
    }

    /**
     * Returns self. If self is a SystemFunction, moves self under the root SystemFunction.
     *
     * @param self
     *            The element to move
     * @return The element passed as input
     */
    public Element moveSFToRootFunction(Element self) {
        if (this.saQueryService.isSystemFunction(self)) {
            var targetParentFunctionsPackage = this.commonQueryService.getFunctionsPackage(self);
            if (targetParentFunctionsPackage.isPresent()) {
                Optional<ActionUsage> rootFunction = this.commonQueryService.getRootFunction(targetParentFunctionsPackage.get());
                if (rootFunction.isPresent()) {
                    this.commonMoveService.moveSemanticElement(self, rootFunction.get());
                }
            }
        }
        return self;
    }
}

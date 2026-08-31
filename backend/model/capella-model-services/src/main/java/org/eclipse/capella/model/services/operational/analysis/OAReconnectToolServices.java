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

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_INVOLVED_COMPONENTS;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;

import java.util.ArrayList;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;


/**
 * Provides Operational Analysis diagram reconnection tools.
 *
 * @author tbezierslafosse
 */
public class OAReconnectToolServices {

    private final TransverseQueryService transverseQueryService;

    private final TransverseMutationService transverseMutationService;

    public OAReconnectToolServices() {
        this.transverseQueryService = new TransverseQueryService();
        this.transverseMutationService = new TransverseMutationService();
    }

    public Usage reconnectCapabilityInvolvement(Usage capability, PartUsage oldComponent, PartUsage newComponent) {
        var reconnectedComponents = new ArrayList<Feature>(this.transverseQueryService.getInvolvedComponents(capability));
        reconnectedComponents.set(reconnectedComponents.indexOf(oldComponent), newComponent);
        this.transverseMutationService.setFeatureReferenceValues(capability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, reconnectedComponents, SysmlPackage.eINSTANCE.getPartUsage());
        return capability;
    }

    public Usage reconnectCapabilityInvolvementSource(Usage oldCapability, Usage newCapability, PartUsage component) {
        var remainingComponents = new ArrayList<Feature>(this.transverseQueryService.getInvolvedComponents(oldCapability));
        remainingComponents.remove(component);
        if (remainingComponents.isEmpty()) {
            this.transverseMutationService.deleteReference(oldCapability, ARCADIA_INVOLVED_COMPONENTS);
        } else {
            this.transverseMutationService.setFeatureReferenceValues(oldCapability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                    ARCADIA_INVOLVED_COMPONENTS, remainingComponents, SysmlPackage.eINSTANCE.getPartUsage());
        }
        this.transverseMutationService.setArcadiaReferenceFeature(newCapability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, component, SysmlPackage.eINSTANCE.getPartUsage().getName());
        return newCapability;
    }

}

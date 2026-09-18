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

import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_INVOLVED_COMPONENTS;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;

/**
 * Common semantic element deletion service.
 *
 * @author gdaniel
 */
public class CommonDeletionService {

    private final TransverseQueryService transverseQueryService;

    private final CapellaDeleteService capellaDeleteService;

    public CommonDeletionService() {
        this.transverseQueryService = new TransverseQueryService();
        this.capellaDeleteService = new CapellaDeleteService();
    }

    public Element delete(Element element) {
        return this.capellaDeleteService.deleteFromModel(element);
    }

    public Usage deleteCapabilityInvolvement(Usage capability, PartUsage component) {
        var involvedComponents = this.transverseQueryService.getInvolvedComponents(capability);
        if (!involvedComponents.contains(component)) {
            return null;
        }
        var commonUpdateService = new CommonUpdateService();
        List<Feature> remainingComponents = new ArrayList<>(involvedComponents);
        remainingComponents.remove(component);
        if (remainingComponents.isEmpty()) {
            commonUpdateService.deleteReference(capability, ARCADIA_INVOLVED_COMPONENTS);
        } else {
            commonUpdateService.setFeatureReferenceValues(capability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                    ARCADIA_INVOLVED_COMPONENTS, remainingComponents, SysmlPackage.eINSTANCE.getPartUsage());
        }
        return capability;
    }

    public void deletePerformedActionUsage(PartUsage usage, ActionUsage actionUsage) {
        this.getPerformActionUsage(usage, actionUsage::equals)
                .forEach(this::delete);
    }

    private List<? extends ActionUsage> getPerformActionUsage(PartUsage partUsage, Predicate<? super ActionUsage> predicate) {
        return partUsage.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .filter(performActionUsage -> predicate.test(this.transverseQueryService.getPerformedAction(performActionUsage).orElse(null)))
                .toList();
    }
}

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

import java.util.List;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;

/**
 * System Analysis (SA) related mutation service.
 * It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class SAQueryService {

    private final CommonQueryService commonQueryService;

    public SAQueryService() {
        this.commonQueryService = new CommonQueryService();
    }

    public List<PartUsage> getComponents(Element self) {
        return this.commonQueryService.getComponents(self).stream()
                .filter(component -> this.commonQueryService.isInPerspective(component, ArcadiaEngineeringPerspective.SystemAnalysis))
                .toList();
    }

    /**
     * Returns the system of interest.
     * <p>
     * Note that the <i>System Analysis</i> perspective contains a unique system of interest (the first non actor component under the structure package).
     *
     * @param self
     *         the contextual element
     * @return the system of interest
     */
    public Optional<PartUsage> getSystemOfInterest(Element self) {
        return this.getComponents(self).stream()
                .filter(this::isSystemOfInterest)
                .findFirst();
    }

    public List<PartUsage> getActors(Element self) {
        return this.getComponents(self).stream()
                .filter(this.commonQueryService::isComponentActor)
                .toList();
    }

    public List<PartUsage> getSubActors(Element parent) {
        List<PartUsage> result = List.of();
        if (this.commonQueryService.isStructurePackage(parent)
                || this.commonQueryService.isComponentActor(parent)) {
            result = this.commonQueryService.getSubComponents(parent);
        }
        return result;
    }

    public List<PartUsage> getSystemComponents(Element self) {
        return this.getComponents(self).stream()
                .filter(this::isSystemComponent)
                .toList();
    }

    public List<PartUsage> getSubSystemComponents(PartUsage parent) {
        List<PartUsage> result = List.of();
        if (this.isSystemOfInterest(parent)
                || this.isSystemComponent(parent)) {
            result = this.commonQueryService.getSubComponents(parent).stream()
                    .filter(this::isSystemComponent)
                    .toList();
        }
        return result;
    }

    public boolean isSystemComponent(PartUsage partUsage) {
        return this.commonQueryService.isComponent(partUsage)
                && !this.commonQueryService.isComponentActor(partUsage)
                && !this.isSystemOfInterest(partUsage)
                && this.commonQueryService.isInPerspective(partUsage, ArcadiaEngineeringPerspective.SystemAnalysis);
    }

    /**
     * Indicates whether the provided {@code partUsage} is the system of interest.
     * <p>
     * Note that the <i>System Analysis</i> perspective contains a unique system of interest (the first non actor component under the structure package).
     *
     * @param partUsage
     *         the element to check
     * @return {@code true} if the element is the system of interest, {@code false} otherwise.
     */
    public boolean isSystemOfInterest(PartUsage partUsage) {
        return this.commonQueryService.isComponent(partUsage)
                && !this.commonQueryService.isComponentActor(partUsage)
                && this.commonQueryService.isStructurePackage(partUsage.getOwner())
                && this.isFirstDirectNonActorSubComponent(partUsage);
    }

    private boolean isFirstDirectNonActorSubComponent(PartUsage partUsage) {
        boolean result = false;
        if (partUsage.getOwner() instanceof Package packageElement) {
            result = packageElement.getOwnedElement().stream()
                    .filter(PartUsage.class::isInstance)
                    .map(PartUsage.class::cast)
                    .filter(this.commonQueryService::isComponent)
                    .filter(candidate -> !this.commonQueryService.isComponentActor(candidate))
                    .findFirst()
                    .filter(partUsage::equals)
                    .isPresent();
        }
        return result;
    }
}

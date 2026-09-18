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

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.PATH_SEPARATOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.STRUCTURE_PACKAGE;

import java.util.List;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.EObject;
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

    private static final String SYSTEM_ANALYSIS_PACKAGE_NAME = "'System Analysis'";

    private final CommonQueryService commonQueryService;

    public SAQueryService() {
        this.commonQueryService = new CommonQueryService();
    }

    public List<PartUsage> getSystemOfInterest(EObject eObject) {
        return this.getSystemOfInterestCandidates(eObject).stream()
                .findFirst()
                .stream()
                .toList();
    }

    public List<PartUsage> getSystemActors(EObject eObject) {
        return this.commonQueryService.getSubComponents(eObject).stream()
                .filter(this.commonQueryService::isComponentActor)
                .toList();
    }

    public List<PartUsage> getSystemComponents(EObject eObject) {
        return this.commonQueryService.getSubComponents(eObject).stream()
                .filter(partUsage -> !this.commonQueryService.isComponentActor(partUsage))
                .filter(partUsage -> !this.isSystemOfInterest(partUsage))
                .toList();
    }

    public boolean isSystemComponent(PartUsage partUsage) {
        return this.commonQueryService.isComponent(partUsage)
                && !this.commonQueryService.isComponentActor(partUsage)
                && !this.isSystemOfInterest(partUsage)
                && this.isInSystemAnalysisStructure(partUsage);
    }

    public boolean isSystemAnalysisStructurePackage(Object element) {
        return element instanceof Package packageElt
                && STRUCTURE_PACKAGE.equals(packageElt.getDeclaredName())
                && this.isSystemAnalysisPerspectivePackage(packageElt);
    }

    private boolean isSystemAnalysisPerspectivePackage(Package packageElt) {
        return this.commonQueryService.getArcadiaPerspectivePackage(packageElt)
                .map(Package::getDeclaredName)
                .flatMap(ArcadiaEngineeringPerspective::fromLabel)
                .filter(ArcadiaEngineeringPerspective.SystemAnalysis::equals)
                .isPresent();
    }

    private List<PartUsage> getSystemOfInterestCandidates(EObject eObject) {
        return this.getDirectOwnedPartUsages(eObject).stream()
                .filter(this.commonQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .filter(this::isInSystemAnalysisStructure)
                .filter(this::isSystemOfInterest)
                .toList();
    }

    private List<PartUsage> getDirectOwnedPartUsages(EObject eObject) {
        if (eObject instanceof Element element) {
            return element.getOwnedElement().stream()
                    .filter(PartUsage.class::isInstance)
                    .map(PartUsage.class::cast)
                    .toList();
        }
        return List.of();
    }

    public boolean isSystemOfInterest(PartUsage partUsage) {
        return this.isSystemOfInterestCandidate(partUsage)
                && this.isDirectlyOwnedBySystemAnalysisStructure(partUsage)
                && this.isInSystemAnalysisStructure(partUsage)
                && this.isFirstDirectNonActorComponent(partUsage);
    }

    private boolean isSystemOfInterestCandidate(PartUsage partUsage) {
        return this.commonQueryService.isComponent(partUsage) && !this.commonQueryService.isComponentActor(partUsage);
    }

    private boolean isDirectlyOwnedBySystemAnalysisStructure(PartUsage partUsage) {
        return partUsage.getOwner() instanceof Package packageElement && STRUCTURE_PACKAGE.equals(packageElement.getDeclaredName());
    }

    private boolean isFirstDirectNonActorComponent(PartUsage partUsage) {
        boolean result = false;
        if (partUsage.getOwner() instanceof Package packageElement) {
            result = packageElement.getOwnedElement().stream()
                    .filter(PartUsage.class::isInstance)
                    .map(PartUsage.class::cast)
                    .filter(this.commonQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                    .filter(candidate -> !this.commonQueryService.isComponentActor(candidate))
                    .findFirst()
                    .filter(partUsage::equals)
                    .isPresent();
        }
        return result;
    }

    private boolean isInSystemAnalysisStructure(PartUsage partUsage) {
        String qualifiedName = partUsage.getQualifiedName();
        return qualifiedName != null && (qualifiedName.contains(SYSTEM_ANALYSIS_PACKAGE_NAME + PATH_SEPARATOR + STRUCTURE_PACKAGE + PATH_SEPARATOR)
                || qualifiedName.contains("'" + SYSTEM_ANALYSIS_PACKAGE_NAME + "'" + PATH_SEPARATOR + STRUCTURE_PACKAGE + PATH_SEPARATOR));
    }

}

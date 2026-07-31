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

import org.eclipse.capella.model.services.transverse.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.REQUIREMENTS_PACKAGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.STRUCTURE_PACKAGE;

/**
 * Operational Analysis (OA) related query service. It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class OAQueryService {

    private final TransverseQueryService transverseQueryService;

    public OAQueryService() {
        this.transverseQueryService = new TransverseQueryService();
    }

    public List<InterfaceUsage> getComponentExchanges(EObject eObject) {
        var allPartUsage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getInterfaceUsage());
        return allPartUsage.stream()
                .filter(InterfaceUsage.class::isInstance)
                .map(InterfaceUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE)).toList();
    }


    public org.eclipse.syson.sysml.Package toComponentsPackage(EObject eObject) {
        var allPackage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPackage());
        return allPackage.stream()
                .filter(org.eclipse.syson.sysml.Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> pkg.getQualifiedName().endsWith("'" + ArcadiaEngineeringPerspective.OperationalAnalysis.getLabel() + "'" + TransverseQueryService.PATH_SEPARATOR + STRUCTURE_PACKAGE))
                .findFirst()
                .orElse(null);
    }
    private Predicate<? super Feature> isTypedWith(String qualifiedName) {
        return element -> element.getType().stream().anyMatch(t -> t != null && qualifiedName != null && qualifiedName.equals(t.getQualifiedName()));
    }

    public Package toRequirementsPackage(EObject eObject) {
        var allPackage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPackage());
        return allPackage.stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> pkg.getQualifiedName().endsWith("'" + ArcadiaEngineeringPerspective.OperationalAnalysis.getLabel() + "'" + TransverseQueryService.PATH_SEPARATOR + REQUIREMENTS_PACKAGE))
                .findFirst()
                .orElse(null);
    }



    public List<PartUsage> getSubComponents(EObject eObject) {
        List<Element> allPartUsage = new ArrayList<>();
        if (eObject instanceof Package pkg) {
            Package componentsPackage = this.toComponentsPackage(pkg);
            allPartUsage = componentsPackage.getMember();
        } else if (this.transverseQueryService.isComponent(eObject) && eObject instanceof PartUsage partUsage) {
            allPartUsage = partUsage.getMember();
        }
        return allPartUsage.stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .toList();
    }

    public PartUsage getComponentExchangeSource(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(interfaceUsage.getSource())
                .stream()
                .flatMap(List::stream)
                .map(Element::getOwner)
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .findFirst()
                .orElse(null);
    }

    public PartUsage getComponentExchangeTarget(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(interfaceUsage.getTarget())
                .stream()
                .flatMap(List::stream)
                .map(Element::getOwner)
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .findFirst()
                .orElse(null);
    }
}

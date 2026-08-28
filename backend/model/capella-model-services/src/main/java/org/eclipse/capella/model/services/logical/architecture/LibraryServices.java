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
package org.eclipse.capella.model.services.logical.architecture;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionDefinition;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.InterfaceDefinition;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PortDefinition;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.sysml.Usage;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_REQUIREMENT;


/**
 * Services related to the Arcadia library.
 *
 * @author fbarbin
 */
public class LibraryServices {

    private final UtilService utilService;
    public LibraryServices() {
        this.utilService = new UtilService();
    }

    public void typeWithArcadiaComponent(Usage usage) {
        var componentPartDefinition = this.utilService.getAllReachable(usage, SysmlPackage.eINSTANCE.getPartDefinition()).stream().filter(PartDefinition.class::isInstance).map(PartDefinition.class::cast)
                .filter(partDefinition -> partDefinition.getQualifiedName().equals(ARCADIA_PREFIX + ARCADIA_COMPONENT)).findFirst().orElse(null);
        utilService.setFeatureTyping(usage, componentPartDefinition);
    }

    public void typeWithArcadiaComponentExchange(Usage usage) {
        var componentExchangeInterfaceDefinition = this.utilService.getAllReachable(usage, SysmlPackage.eINSTANCE.getInterfaceDefinition()).stream()
            .filter(InterfaceDefinition.class::isInstance)
            .map(InterfaceDefinition.class::cast)
            .filter(interfaceDefinition -> interfaceDefinition.getQualifiedName().equals(ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE)).findFirst().orElse(null);
        utilService.setFeatureTyping(usage, componentExchangeInterfaceDefinition);
    }

    public void typeWithArcadiaFunction(Usage usage) {
        var functionDefinition = this.utilService.getAllReachable(usage, SysmlPackage.eINSTANCE.getActionDefinition()).stream().filter(ActionDefinition.class::isInstance).map(ActionDefinition.class::cast)
                .filter(actionDefinition -> actionDefinition.getQualifiedName().equals(ARCADIA_PREFIX + ARCADIA_FUNCTION)).findFirst().orElse(null);
        utilService.setFeatureTyping(usage, functionDefinition);
    }

    public void typeWithArcadiaComponentPort(Usage usage) {
        var componentPortPortDefinition = this.utilService.getAllReachable(usage, SysmlPackage.eINSTANCE.getPortDefinition()).stream().filter(PortDefinition.class::isInstance).map(PortDefinition.class::cast)
                .filter(portDefinition -> portDefinition.getQualifiedName().equals(ARCADIA_PREFIX + ARCADIA_COMPONENT_PORT)).findFirst().orElse(null);
        utilService.setFeatureTyping(usage, componentPortPortDefinition);
    }

    public void typeWithArcadiaFunctionalExchange(Usage usage) {
        this.typeWithArcadiaLibrary(usage, ARCADIA_FUNCTIONAL_EXCHANGE, SysmlPackage.eINSTANCE.getFlowDefinition());
    }

    public void typeWithExchangeItem(Usage usage) {
        this.typeWithArcadiaLibrary(usage, ARCADIA_EXCHANGE_ITEM, SysmlPackage.eINSTANCE.getItemDefinition());
    }

    public void typeWithArcadiaFunctionalChain(Usage usage) {
        this.typeWithArcadiaLibrary(usage, ARCADIA_FUNCTIONAL_CHAIN, SysmlPackage.eINSTANCE.getActionDefinition());
    }

    public void typeWithArcadiaRequirement(RequirementUsage requirementUsage) {
        this.typeWithArcadiaLibrary(requirementUsage, ARCADIA_REQUIREMENT, SysmlPackage.eINSTANCE.getRequirementDefinition());
    }

    public void typeWithArcadiaCapability(Usage usage) {
        this.typeWithArcadiaLibrary(usage, ARCADIA_CAPABILITY, SysmlPackage.eINSTANCE.getOccurrenceDefinition());
    }

    public void typeWithArcadiaLibrary(Usage usage, String arcadiaTypeName, EClass librarySysMLElementType) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + arcadiaTypeName, librarySysMLElementType);
    }

    public void typeWithLibrary(Usage usage, String typeQualifiedName, EClass librarySysMLElementType) {
        var elementType = this.utilService.getAllReachable(usage, librarySysMLElementType).stream().filter(librarySysMLElementType::isInstance).filter(Element.class::isInstance)
            .map(Type.class::cast)
            .filter(element -> element.getQualifiedName().equals(typeQualifiedName)).findFirst().orElse(null);
        utilService.setFeatureTyping(usage, elementType);
    }
}

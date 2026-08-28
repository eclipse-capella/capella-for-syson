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
package org.eclipse.capella.model.services.transverse;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.sysml.Usage;


/**
 * Services related to the Arcadia library.
 *
 * @author fbarbin
 */
public class ArcadiaLibraryServices {

    private final UtilService utilService;

    public ArcadiaLibraryServices() {
        this.utilService = new UtilService();
    }

    public void typeWithArcadiaComponent(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_COMPONENT, SysmlPackage.eINSTANCE.getPartDefinition());
    }

    public void typeWithArcadiaComponentExchange(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE, SysmlPackage.eINSTANCE.getInterfaceDefinition());
    }

    public void typeWithArcadiaFunction(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_FUNCTION, SysmlPackage.eINSTANCE.getActionDefinition());
    }

    public void typeWithArcadiaComponentPort(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_COMPONENT_PORT, SysmlPackage.eINSTANCE.getPortDefinition());
    }

    public void typeWithArcadiaFunctionalExchange(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_EXCHANGE, SysmlPackage.eINSTANCE.getFlowDefinition());
    }

    public void typeWithExchangeItem(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM, SysmlPackage.eINSTANCE.getItemDefinition());
    }

    public void typeWithArcadiaFunctionalChain(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN, SysmlPackage.eINSTANCE.getActionDefinition());
    }

    public void typeWithArcadiaCapability(Usage usage) {
        this.typeWithLibrary(usage, ARCADIA_PREFIX + ARCADIA_CAPABILITY, SysmlPackage.eINSTANCE.getOccurrenceDefinition());
    }

    public void typeWithLibrary(Usage usage, String typeQualifiedName, EClass librarySysMLElementType) {
        var elementType = this.utilService.getAllReachable(usage, librarySysMLElementType).stream()
                .filter(librarySysMLElementType::isInstance)
                .filter(Element.class::isInstance)
                .map(Type.class::cast)
                .filter(element -> element.getQualifiedName().equals(typeQualifiedName))
                .findFirst()
                .orElse(null);
        this.utilService.setFeatureTyping(usage, elementType);
    }
}

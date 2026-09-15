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
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_PREFIX;

import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.sysml.Usage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Services related to the Arcadia library.
 *
 * @author fbarbin
 */
public class ArcadiaLibraryServices {

    private final Logger logger = LoggerFactory.getLogger(ArcadiaLibraryServices.class);

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
        // UtilService#findByNameAndType ensures the lookup is performed from the root namespace, so local elements shadowing the Arcadia package aren't taken into account.
        // The solution isn't perfect though, since multiple Arcadia packages can still be defined in the root namespace.
        Optional<Type> optionalLibraryType = Optional.ofNullable(this.utilService.findByNameAndType(usage, typeQualifiedName, Type.class))
                .filter(librarySysMLElementType::isInstance);
        if (optionalLibraryType.isPresent()) {
            this.utilService.setFeatureTyping(usage, optionalLibraryType.get());
        } else {
            this.logger.atWarn()
                    .setMessage("Cannot find type {}")
                    .addArgument(typeQualifiedName)
                    .log();
        }
    }
}

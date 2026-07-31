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

import org.eclipse.capella.model.services.logical.architecture.LibraryServices;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

/**
 * System Analysis (SA) related mutation service.
 * This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class SARepresentationMutationService {

    private static final String WHITE_SPACE = " ";

    private final TransverseMutationService transverseMutationService;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final LibraryServices libraryServices;

    private final MetamodelMutationElementService metamodelMutationElementService;

    public SARepresentationMutationService() {
        this.transverseMutationService = new TransverseMutationService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.transverseQueryService = new TransverseQueryService();
        this.libraryServices = new LibraryServices();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
    }

    public Feature createInputFunctionPort(Element parent) {
        return this.createFunctionPort((ActionUsage) parent, FeatureDirectionKind.IN);
    }

    public Feature createOutputFunctionPort(Element parent) {
        return this.createFunctionPort((ActionUsage) parent, FeatureDirectionKind.OUT);
    }


    public PortUsage createInputComponentPort(Element parent) {
        if (this.transverseQueryService.isComponent(parent)) {
            return this.transverseMutationService.createComponentPort((PartUsage) parent, FeatureDirectionKind.IN);
        }
        return null;
    }

    public PortUsage createOutputComponentPort(Element parent) {
        if (this.transverseQueryService.isComponent(parent)) {
            return this.transverseMutationService.createComponentPort((PartUsage) parent, FeatureDirectionKind.OUT);
        }
        return null;
    }

    public PortUsage createInOutComponentPort(Element parent) {
        if (this.transverseQueryService.isComponent(parent)) {
            return this.transverseMutationService.createComponentPort((PartUsage) parent, FeatureDirectionKind.INOUT);
        }
        return null;
    }

    private Feature createFunctionPort(ActionUsage container, FeatureDirectionKind direction) {
        var itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        itemUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, itemUsage);
        this.elementInitializerSwitch.doSwitch(itemUsage);
        this.libraryServices.typeWithExchangeItem(itemUsage);
        String defaultName = switch (direction) {
            case IN -> "FIP";
            case OUT -> "FOP";
            default -> "FP";
        };
        itemUsage.setDeclaredName(defaultName + WHITE_SPACE + this.transverseQueryService.existingElementsCount(itemUsage));
        return itemUsage;
    }
}

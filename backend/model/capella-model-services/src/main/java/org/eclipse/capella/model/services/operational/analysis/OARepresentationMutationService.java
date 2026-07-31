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

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_HUMAN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.services.logical.architecture.LibraryServices;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationMutationService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

/**
 * Operational Analysis (OA) related mutation service.
 * This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class OARepresentationMutationService {

    private final TransverseMutationService transverseMutationService;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final LibraryServices libraryServices;

    private final OAQueryService oaQueryService;

    private final MetamodelMutationElementService metamodelMutationElementService;

    private final TransverseRepresentationMutationService transverseRepresentationMutationService;

    public  OARepresentationMutationService(TransverseRepresentationMutationService transverseRepresentationMutationService) {
        this.transverseMutationService = new TransverseMutationService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.transverseQueryService = new TransverseQueryService();
        this.libraryServices = new LibraryServices();
        this.oaQueryService = new OAQueryService();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.transverseRepresentationMutationService = Objects.requireNonNull(transverseRepresentationMutationService);
    }

    public PartUsage createEntityComponent(Element parent, boolean isActor) {
        PartUsage partUsage = null;
        Optional<Element> optionalTargetContainer = Optional.of(parent);
        if (!(parent instanceof PartUsage)) {
            optionalTargetContainer = this.transverseQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (optionalTargetContainer.isPresent()) {
            String name = "OE";
            Element targetContainer = optionalTargetContainer.get();
            partUsage = SysmlFactory.eINSTANCE.createPartUsage();
            this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
            this.transverseMutationService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_ACTOR, true);
            if (isActor) {
                name = "OA";
                this.transverseMutationService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_HUMAN, true);
            }
            this.elementInitializerSwitch.doSwitch(partUsage);
            this.libraryServices.typeWithArcadiaComponent(partUsage);
            long existingElementsCount = this.transverseQueryService.existingElementsCount(partUsage);
            partUsage.setDeclaredName(name + " " + existingElementsCount);
        }
        return partUsage;
    }

}

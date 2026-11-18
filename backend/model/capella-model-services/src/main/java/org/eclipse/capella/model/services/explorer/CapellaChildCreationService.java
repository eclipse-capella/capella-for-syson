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
package org.eclipse.capella.model.services.explorer;

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_ACTOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_REQUIREMENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.capella.model.services.logical.architecture.LAMutationService;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationMutationService;
import org.eclipse.capella.model.services.system.analysis.SAMutationService;
import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Defines the allowed explorer child creations and executes their semantic operations.
 *
 * @author tbezierslafosse
 */
public class CapellaChildCreationService {

    private static final String FUNCTION_PACKAGE = "FunctionPackage";

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final SAMutationService saMutationService = new SAMutationService();

    private final LAMutationService laMutationService = new LAMutationService();

    private final OARepresentationMutationService oaMutationService = new OARepresentationMutationService();

    /**
     * Returns the allowed child types in their menu order without changing the model.
     *
     * @param container the selected semantic element
     * @return the allowed child types
     */
    public List<String> getAllowedChildTypes(Element container) {
        List<String> types = new ArrayList<>();
        if (this.commonQueryService.isFunction(container) || this.commonQueryService.isFunctionPackage(container)) {
            types.addAll(List.of(ARCADIA_FUNCTION, ARCADIA_FUNCTIONAL_CHAIN));
            // The perspective's root Functions package cannot own another function package; functions and nested packages can.
            if (this.commonQueryService.getFunctionsPackage(container)
                    .filter(container::equals)
                    .isEmpty()) {
                types.add(FUNCTION_PACKAGE);
            }
        } else if (this.commonQueryService.isRequirementsPackage(container)) {
            types.add(ARCADIA_REQUIREMENT);
        } else if (this.commonQueryService.isCapabilitiesPackage(container)) {
            types.add(ARCADIA_CAPABILITY);
        } else if (this.commonQueryService.isDataPackage(container) || this.commonQueryService.isInterfacesPackage(container)) {
            // Interfaces exposes Exchange Item creation; Data retains the existing explorer creation behavior.
            types.add(ARCADIA_EXCHANGE_ITEM);
        } else if (this.commonQueryService.isStructurePackage(container) || this.commonQueryService.isComponent(container)) {
            types.addAll(this.getAllowedComponentTypes(container));
        }
        return types;
    }

    private List<String> getAllowedComponentTypes(Element container) {
        List<String> types = List.of(ARCADIA_COMPONENT, ARCADIA_ACTOR);
        if (this.commonQueryService.isInPerspective(container, ArcadiaEngineeringPerspective.OperationalAnalysis)) {
            if (this.commonQueryService.isComponentHumanActor(container)) {
                types = List.of();
            }
        } else if (this.commonQueryService.isComponentActor(container)
                || this.commonQueryService.isInPerspective(container, ArcadiaEngineeringPerspective.SystemAnalysis) && container instanceof Package) {
            types = List.of(ARCADIA_ACTOR);
        }
        return types;
    }

    public Optional<Element> createChild(Element container, String type) {
        Optional<Element> result = Optional.empty();
        if (this.getAllowedChildTypes(container).contains(type)) {
            result = Optional.ofNullable(this.createCapellaChild(container, type));
        }
        return result;
    }

    private Element createCapellaChild(Element container, String type) {
        return switch (type) {
            case ARCADIA_FUNCTION -> this.commonCreationService.createFunction(container);
            case ARCADIA_FUNCTIONAL_CHAIN -> this.commonCreationService.createFunctionalChain(container, List.of());
            case FUNCTION_PACKAGE -> this.commonCreationService.createPackage(container);
            case ARCADIA_REQUIREMENT -> this.commonCreationService.createRequirement(container);
            case ARCADIA_CAPABILITY -> this.commonCreationService.createOperationalCapability(container);
            case ARCADIA_EXCHANGE_ITEM -> this.commonCreationService.createNewExchangeItem(container);
            case ARCADIA_COMPONENT -> this.createComponent(container);
            case ARCADIA_ACTOR -> this.createActor(container);
            default -> null;
        };
    }

    private PartUsage createActor(Element container) {
        var perspective = this.commonQueryService.getArcadiaPerspective(container).orElseThrow();
        return switch (perspective) {
            case OperationalAnalysis -> this.oaMutationService.createComponentOA(container, true);
            case SystemAnalysis -> this.saMutationService.createActorSA(container);
            default -> this.commonCreationService.createActor(container);
        };
    }

    private PartUsage createComponent(Element container) {
        var perspective = this.commonQueryService.getArcadiaPerspective(container).orElseThrow();
        return switch (perspective) {
            case OperationalAnalysis -> this.oaMutationService.createComponentOA(container, false);
            case SystemAnalysis -> this.saMutationService.createComponentSA(container);
            case LogicalArchitecture -> this.laMutationService.createComponentLA(container);
            default -> this.commonCreationService.createComponent(container);
        };
    }
}

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
package org.eclipse.capella.application.configuration.explorer.services;

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_ACTOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_MISSION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_REQUIREMENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.FUNCTION_PACKAGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.PACKAGE;

import java.util.List;

import org.eclipse.capella.model.services.logical.architecture.LAMutationService;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationMutationService;
import org.eclipse.capella.model.services.system.analysis.SAMutationService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;

/**
 * Defines the allowed explorer child creations and executes their semantic operations.
 *
 * @author tbezierslafosse
 */
public class CapellaChildCreationService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final SAMutationService saMutationService = new SAMutationService();

    private final LAMutationService laMutationService = new LAMutationService();

    private final OARepresentationMutationService oaMutationService = new OARepresentationMutationService();

    public Element createCapellaChild(Element container, String type) {
        return switch (type) {
            case ARCADIA_FUNCTION -> this.commonCreationService.createFunction(container);
            case ARCADIA_FUNCTIONAL_CHAIN -> this.commonCreationService.createFunctionalChain(container, List.of());
            case FUNCTION_PACKAGE, PACKAGE -> this.commonCreationService.createPackage(container);
            case ARCADIA_REQUIREMENT -> this.commonCreationService.createRequirement(container);
            case ARCADIA_CAPABILITY -> this.commonCreationService.createCapability(container);
            case ARCADIA_MISSION -> this.commonCreationService.createMission(container);
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

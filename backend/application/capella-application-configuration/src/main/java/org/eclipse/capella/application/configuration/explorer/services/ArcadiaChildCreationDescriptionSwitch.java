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

import static org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective.OperationalAnalysis;
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

import org.eclipse.capella.model.transverse.services.AbstractArcadiaSwitch;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;

/**
 * Defines explorer child creation descriptions for Arcadia concepts and their containing packages.
 *
 * @author tbezierslafosse
 */
public class ArcadiaChildCreationDescriptionSwitch extends AbstractArcadiaSwitch<List<ChildCreationDescription>> {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    @Override
    protected List<ChildCreationDescription> caseFunction(Element element) {
        return List.of(this.createFunctionChildCreationDescription(element),
                this.createFunctionPackageChildCreationDescription(element),
                this.createFunctionalChainChildCreationDescription(element));
    }

    @Override
    protected List<ChildCreationDescription> caseComponent(Element element) {
        List<ChildCreationDescription> descriptions = List.of(this.createComponentChildCreationDescription(element), this.createActorChildCreationDescription(element));
        var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
        if (this.commonQueryService.isComponentHumanActor(element)) {
            descriptions = List.of();
        } else if (perspective != OperationalAnalysis && this.commonQueryService.isComponentActor(element)) {
            descriptions = List.of(this.createActorChildCreationDescription(element));
        }
        return descriptions;
    }

    @Override
    protected List<ChildCreationDescription> defaultCase(Element element) {
        List<ChildCreationDescription> descriptions = List.of();
        if (element instanceof Package pkg) {
            descriptions = this.createPackageChildCreationDescriptions(pkg);
        }
        return descriptions;
    }

    private List<ChildCreationDescription> createPackageChildCreationDescriptions(Package element) {
        // Common package roles take precedence, including for packages nested under Structure.
        return switch (element) {
            case Package pkg when this.commonQueryService.isFunctionsPackage(pkg) -> this.createRootFunctionPackageChildCreationDescriptions(pkg);
            case Package pkg when this.commonQueryService.isFunctionSubPackage(pkg) ->
                List.of(this.createFunctionChildCreationDescription(pkg), this.createFunctionPackageChildCreationDescription(pkg), this.createFunctionalChainChildCreationDescription(pkg));
            case Package pkg when this.commonQueryService.isRequirementsPackage(pkg) -> List.of(this.createChildCreationDescription(ARCADIA_REQUIREMENT, "Requirement", "Requirement"));
            case Package pkg when this.commonQueryService.isDataPackage(pkg) || this.commonQueryService.isInterfacesPackage(pkg) ->
                List.of(this.createChildCreationDescription(ARCADIA_EXCHANGE_ITEM, ARCADIA_EXCHANGE_ITEM, ARCADIA_EXCHANGE_ITEM));
            default -> {
                // Packages without a common role use the creation rules of their owning Arcadia perspective.
                var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
                yield switch (perspective) {
                        case OperationalAnalysis -> this.createOperationalAnalysisPackageChildCreationDescriptions(element);
                        case SystemAnalysis -> this.createSystemAnalysisPackageChildCreationDescriptions(element);
                        case null, default -> this.createArchitecturePackageChildCreationDescriptions(element);
                    };
            }
        };
    }

    private List<ChildCreationDescription> createOperationalAnalysisPackageChildCreationDescriptions(Package pkg) {
        List<ChildCreationDescription> descriptions = List.of();
        if (this.commonQueryService.isCapabilitiesPackage(pkg)) {
            descriptions = List.of(this.createCapabilityChildCreationDescription(pkg));
        } else if (this.commonQueryService.isStructurePackage(pkg) || this.commonQueryService.isStructureSubPackage(pkg)) {
            descriptions = List.of(this.createComponentChildCreationDescription(pkg), this.createActorChildCreationDescription(pkg), this.createStructurePackageChildCreationDescription(pkg));
        }
        return descriptions;
    }

    private List<ChildCreationDescription> createSystemAnalysisPackageChildCreationDescriptions(Package pkg) {
        List<ChildCreationDescription> descriptions = List.of();
        if (this.commonQueryService.isCapabilitiesPackage(pkg)) {
            descriptions = List.of(this.createCapabilityChildCreationDescription(pkg), this.createChildCreationDescription(ARCADIA_MISSION, ARCADIA_MISSION, ARCADIA_MISSION));
        } else if (this.commonQueryService.isStructurePackage(pkg)) {
            descriptions = List.of(this.createActorChildCreationDescription(pkg), this.createStructurePackageChildCreationDescription(pkg));
        } else if (this.commonQueryService.isStructureSubPackage(pkg)) {
            descriptions = List.of(this.createStructurePackageChildCreationDescription(pkg));
        }
        return descriptions;
    }

    private List<ChildCreationDescription> createArchitecturePackageChildCreationDescriptions(Package pkg) {
        List<ChildCreationDescription> descriptions = List.of();
        if (this.commonQueryService.isCapabilitiesPackage(pkg)) {
            descriptions = List.of(this.createCapabilityChildCreationDescription(pkg));
        } else if (this.commonQueryService.isStructurePackage(pkg)) {
            descriptions = List.of(this.createComponentChildCreationDescription(pkg), this.createActorChildCreationDescription(pkg), this.createStructurePackageChildCreationDescription(pkg));
        } else if (this.commonQueryService.isStructureSubPackage(pkg)) {
            descriptions = List.of(this.createStructurePackageChildCreationDescription(pkg));
        }
        return descriptions;
    }

    private List<ChildCreationDescription> createRootFunctionPackageChildCreationDescriptions(Package pkg) {
        // The Functions package allows only one root function; functional chains can still be created alongside it.
        if (this.commonQueryService.getRootFunction(pkg).isPresent()) {
            return List.of(this.createFunctionalChainChildCreationDescription(pkg));
        }
        return List.of(this.createFunctionChildCreationDescription(pkg), this.createFunctionalChainChildCreationDescription(pkg));
    }

    private ChildCreationDescription createFunctionChildCreationDescription(Element element) {
        var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
        return switch (perspective) {
            case OperationalAnalysis -> this.createChildCreationDescription(ARCADIA_FUNCTION, "Operational Activity", "OperationalActivity");
            case SystemAnalysis -> this.createChildCreationDescription(ARCADIA_FUNCTION, ARCADIA_FUNCTION, "SystemFunction");
            case LogicalArchitecture -> this.createChildCreationDescription(ARCADIA_FUNCTION, ARCADIA_FUNCTION, "LogicalFunction");
            case PhysicalArchitecture -> this.createChildCreationDescription(ARCADIA_FUNCTION, ARCADIA_FUNCTION, "PhysicalFunction");
            case null, default -> this.createChildCreationDescription(ARCADIA_FUNCTION, ARCADIA_FUNCTION, ARCADIA_FUNCTION);
        };
    }

    private ChildCreationDescription createFunctionPackageChildCreationDescription(Element element) {
        return this.commonQueryService.getArcadiaPerspective(element)
                .filter(OperationalAnalysis::equals)
                .map(any -> this.createChildCreationDescription(FUNCTION_PACKAGE, "Operational Activity Package", "LogicalFunctionPkg"))
                .orElseGet(() -> this.createChildCreationDescription(FUNCTION_PACKAGE, "Function Package", "LogicalFunctionPkg"));
    }

    private ChildCreationDescription createFunctionalChainChildCreationDescription(Element element) {
        return this.commonQueryService.getArcadiaPerspective(element)
                .filter(OperationalAnalysis::equals)
                .map(any -> this.createChildCreationDescription(ARCADIA_FUNCTIONAL_CHAIN, "Operational Process", "OperationalProcess"))
                .orElseGet(() -> this.createChildCreationDescription(ARCADIA_FUNCTIONAL_CHAIN, ARCADIA_FUNCTIONAL_CHAIN, ARCADIA_FUNCTIONAL_CHAIN));
    }

    private ChildCreationDescription createComponentChildCreationDescription(Element element) {
        var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
        return switch (perspective) {
            case OperationalAnalysis -> this.createChildCreationDescription(ARCADIA_COMPONENT, "Operational Entity", "OperationalEntity");
            case SystemAnalysis -> this.createChildCreationDescription(ARCADIA_COMPONENT, ARCADIA_COMPONENT, "SystemComponent");
            case LogicalArchitecture -> this.createChildCreationDescription(ARCADIA_COMPONENT, ARCADIA_COMPONENT, "LogicalComponent");
            case PhysicalArchitecture -> this.createChildCreationDescription(ARCADIA_COMPONENT, ARCADIA_COMPONENT, "PhysicalComponent");
            case null, default -> this.createChildCreationDescription(ARCADIA_COMPONENT, ARCADIA_COMPONENT, ARCADIA_COMPONENT);
        };
    }

    private ChildCreationDescription createActorChildCreationDescription(Element element) {
        var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
        return switch (perspective) {
            case OperationalAnalysis -> this.createChildCreationDescription(ARCADIA_ACTOR, "Human Actor", "ActorHuman");
            case SystemAnalysis -> this.createChildCreationDescription(ARCADIA_ACTOR, ARCADIA_ACTOR, "SystemActor");
            case LogicalArchitecture -> this.createChildCreationDescription(ARCADIA_ACTOR, ARCADIA_ACTOR, "LogicalActor");
            case PhysicalArchitecture -> this.createChildCreationDescription(ARCADIA_ACTOR, ARCADIA_ACTOR, "PhysicalActor");
            case null, default -> this.createChildCreationDescription(ARCADIA_ACTOR, ARCADIA_ACTOR, "Actor");
        };
    }

    private ChildCreationDescription createCapabilityChildCreationDescription(Element element) {
        return this.commonQueryService.getArcadiaPerspective(element)
                .filter(OperationalAnalysis::equals)
                .map(any -> this.createChildCreationDescription(ARCADIA_CAPABILITY, "Operational Capability", "OperationalCapability"))
                .orElseGet(() -> this.createChildCreationDescription(ARCADIA_CAPABILITY, ARCADIA_CAPABILITY, ARCADIA_CAPABILITY));
    }

    private ChildCreationDescription createStructurePackageChildCreationDescription(Element element) {
        var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
        String label = switch (perspective) {
            case OperationalAnalysis -> "Entity Package";
            case SystemAnalysis -> "System Component Package";
            case LogicalArchitecture -> "Logical Component Package";
            case PhysicalArchitecture -> "Physical Component Package";
            case null, default -> "Component Package";
        };
        return this.createChildCreationDescription(PACKAGE, label, "LogicalComponentPkg");
    }

    private ChildCreationDescription createChildCreationDescription(String type, String label, String icon) {
        return new ChildCreationDescription(CapellaEditService.ID_PREFIX + type, label, List.of("/icons/full/obj16/" + icon + ".svg"));
    }
}

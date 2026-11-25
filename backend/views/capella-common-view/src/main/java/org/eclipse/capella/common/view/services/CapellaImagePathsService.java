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
package org.eclipse.capella.common.view.services;

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_MISSION;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.common.view.services.api.ICapellaImagePathsService;
import org.eclipse.capella.model.transverse.services.AbstractArcadiaSwitch;
import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.RequirementUsage;
import org.springframework.stereotype.Service;

/**
 * Computes the image paths of the Capella icons associated to the provided elements.
 *
 * @author fbarbin
 *
 */
@Service
public class CapellaImagePathsService implements ICapellaImagePathsService {

    private static final String REQUIREMENTS = "Requirements";

    private static final String DATA = "Data";

    private static final String INTERFACES = "Interfaces";

    private static final String CAPABILITIES = "Capabilities";

    private static final String STRUCTURE = "Structure";

    private static final String FUNCTIONS = "Functions";

    private static final String PHYSICAL = "Physical";

    private static final String LOGICAL = "Logical";

    private static final String SYSTEM = "System";

    private static final String ICONS_FULL_PATH = "/icons/full/obj16/%s.svg";

    private final ILabelService labelService;

    public CapellaImagePathsService(ILabelService labelService) {
        this.labelService = Objects.requireNonNull(labelService);
    }

    @Override
    public List<String> getImagePaths(Object object) {
        String imageName = null;
        if (object instanceof Element element) {
            imageName = new ArcadiaImageSwitch().doSwitch(element);
        }
        if (imageName != null) {
            return List.of(String.format(ICONS_FULL_PATH, imageName));
        }
        return this.labelService.getImagePaths(object);
    }

    /**
     * Resolves icon names for Arcadia library types using each element's perspective.
     *
     * @author tbezierslafosse
     */
    private static final class ArcadiaImageSwitch extends AbstractArcadiaSwitch<String> {

        private final CommonQueryService commonQueryService;

        private ArcadiaImageSwitch() {
            this.commonQueryService = new CommonQueryService();
        }

        @Override
        protected String caseComponent(Element element) {
            var queryService = this.commonQueryService;
            var perspective = queryService.getArcadiaPerspective(element).orElse(null);
            String imageName = null;
            if (queryService.isComponentHumanActor(element)) {
                imageName = "ActorHuman";
            } else if (ArcadiaEngineeringPerspective.OperationalAnalysis.equals(perspective)) {
                imageName = "OperationalEntity";
            } else {
                String prefix = switch (perspective) {
                    case SystemAnalysis -> SYSTEM;
                    case LogicalArchitecture -> LOGICAL;
                    case PhysicalArchitecture -> PHYSICAL;
                    case null, default -> null;
                };
                if (prefix != null) {
                    String suffix = ARCADIA_COMPONENT;
                    if (queryService.isComponentActor(element)) {
                        suffix = "Actor";
                    }
                    imageName = prefix + suffix;
                }
            }
            return imageName;
        }

        @Override
        protected String caseFunction(Element element) {
            var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
            return switch (perspective) {
                case OperationalAnalysis -> "OperationalActivity";
                case SystemAnalysis -> SYSTEM + ARCADIA_FUNCTION;
                case LogicalArchitecture -> LOGICAL + ARCADIA_FUNCTION;
                case PhysicalArchitecture -> PHYSICAL + ARCADIA_FUNCTION;
                case null, default -> null;
            };
        }

        @Override
        protected String caseCapability(Element element) {
            var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
            if (ArcadiaEngineeringPerspective.OperationalAnalysis.equals(perspective)) {
                return "OperationalCapability";
            }
            return ARCADIA_CAPABILITY;
        }

        @Override
        protected String caseMission(Element element) {
            return ARCADIA_MISSION;
        }

        @Override
        protected String caseComponentPort(Element element) {
            return "FlowPort";
        }

        @Override
        protected String caseComponentExchange(Element element) {
            return ARCADIA_COMPONENT_EXCHANGE;
        }

        @Override
        protected String caseFunctionalExchange(Element element) {
            var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
            if (ArcadiaEngineeringPerspective.OperationalAnalysis.equals(perspective)) {
                return "OperationalInteraction";
            }
            return ARCADIA_FUNCTIONAL_EXCHANGE;
        }

        @Override
        protected String caseFunctionalChain(Element element) {
            var perspective = this.commonQueryService.getArcadiaPerspective(element).orElse(null);
            if (ArcadiaEngineeringPerspective.OperationalAnalysis.equals(perspective)) {
                return "OperationalProcess";
            }
            return ARCADIA_FUNCTIONAL_CHAIN;
        }

        @Override
        protected String caseExchangeItem(Element element) {
            boolean hasParentFunction = Optional.ofNullable(element)
                    .map(Element::getOwner)
                    .filter(this.commonQueryService::isFunction)
                    .isPresent();
            if (hasParentFunction && element instanceof Feature feature) {
                FeatureDirectionKind direction = feature.getDirection();
                return switch (direction) {
                    case IN -> "FunctionInputPort";
                    case OUT -> "FunctionOutputPort";
                    default -> ARCADIA_EXCHANGE_ITEM;
                };
            }
            return ARCADIA_EXCHANGE_ITEM;
        }

        @Override
        protected String caseRequirement(Element element) {
            return "Requirement";
        }

        @Override
        protected String defaultCase(Element element) {
            String imageName = null;
            if (element instanceof Package pkg) {
                String declaredName = pkg.getDeclaredName();
                if (declaredName != null) {
                    imageName = switch (declaredName) {
                        case FUNCTIONS -> "LogicalFunctionPkg";
                        case STRUCTURE -> "LogicalComponentPkg";
                        case CAPABILITIES -> "CapabilitiesPkg";
                        case INTERFACES -> "InterfacesPkg";
                        case DATA -> "DataPkg";
                        case REQUIREMENTS -> "RequirementsPkg";
                        default -> null;
                    };
                    if (imageName == null && ArcadiaEngineeringPerspective.fromLabel(declaredName).isPresent()) {
                        imageName = declaredName.replaceAll("\\s", "");
                    }
                }
            } else if (this.commonQueryService.isDescribes(element)) {
                imageName = "Describes";
            } else if (element instanceof RequirementUsage) {
                imageName = "Requirement";
            }
            return imageName;
        }
    }

}

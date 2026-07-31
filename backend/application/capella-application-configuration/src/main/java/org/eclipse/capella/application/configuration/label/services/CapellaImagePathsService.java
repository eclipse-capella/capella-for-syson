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
package org.eclipse.capella.application.configuration.label.services;

import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.Package;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_REQUIREMENT;

/**
 * A service dedicated to compute the label image path.
 *
 * @author fbarbin
 *
 */
@Service
public class CapellaImagePathsService {

    private static final String REQUIREMENTS = "Requirements";

    private static final String DATA = "Data";

    private static final String INTERFACES = "Interfaces";

    private static final String CAPABILITIES = "Capabilities";

    private static final String STRUCTURE = "Structure";

    private static final String FUNCTIONS = "Functions";

    private static final String PHYSICAL = "Physical";

    private static final String LOGICAL = "Logical";

    private static final String ICONS_FULL_PATH = "/icons/full/obj16/%s.svg";

    private final TransverseQueryService transverseQueryService;

    private final ILabelService labelService;

    private final LAQueryService laQueryService;

    public CapellaImagePathsService(ILabelService labelService) {
        this.transverseQueryService = new TransverseQueryService();
        this.laQueryService = new LAQueryService();
        this.labelService = Objects.requireNonNull(labelService);
    }

    public List<String> getImagePaths(Object object) {
        String imageName = null;
        if (object instanceof org.eclipse.syson.sysml.Package pkg) {
            imageName = this.computePackageImage(pkg);
        } else if (object instanceof Element element) {
            String arcadiaType = this.transverseQueryService.getArcadiaType(element).map(type -> type.replaceFirst(ARCADIA_PREFIX, "")).orElse(null);
            if (arcadiaType != null) {
                imageName = this.computeArcadiaElementImage(element, arcadiaType).orElse(null);
            }
        }
        if (imageName != null) {
            return List.of(imageName);
        }
        return this.labelService.getImagePaths(object);
    }

    /**
     * Provides the image path for the given arcadia element.
     *
     * @param perspective
     *            the {@link ArcadiaEngineeringPerspective} (can be null)
     * @param element
     *            the SysML {@link Element} (can be null)
     * @param arcadiaType
     *            the Arcadia type.
     * @return the optional image path.
     */
    public Optional<String> getImageFromArcadiaType(ArcadiaEngineeringPerspective perspective, Element element, String arcadiaType) {
        String imageName = switch (arcadiaType) {
            case ARCADIA_COMPONENT, ARCADIA_FUNCTION -> this.computeElementNameWithArchitecture(perspective, arcadiaType, element).orElse(null);
            case ARCADIA_COMPONENT_PORT -> "FlowPort";
            case ARCADIA_COMPONENT_EXCHANGE -> arcadiaType;
            case ARCADIA_FUNCTIONAL_EXCHANGE -> arcadiaType;
            case ARCADIA_FUNCTIONAL_CHAIN -> arcadiaType;
            case ARCADIA_EXCHANGE_ITEM -> this.computeExchangeItemIcon(element, arcadiaType);
            case ARCADIA_REQUIREMENT -> "Requirement";
            default -> null;
        };
        if (imageName != null) {
            return Optional.of(String.format(ICONS_FULL_PATH, imageName));
        }
        return Optional.empty();
    }

    private String computeExchangeItemIcon(Element element, String arcadiaType) {
        boolean hasParentFunction = Optional.ofNullable(element)
                .map(Element::getOwner)
                .filter(this.laQueryService::isFunction)
                .isPresent();
        if (hasParentFunction && element instanceof Feature feature) {
            FeatureDirectionKind direction = feature.getDirection();
            return switch (direction) {
                case IN -> "FunctionInputPort";
                case OUT -> "FunctionOutputPort";
                default -> arcadiaType;
            };
        }
        return arcadiaType;
    }

    private Optional<String> computeArcadiaElementImage(Element element, String arcadiaType) {
        ArcadiaEngineeringPerspective perspective = this.transverseQueryService.getArcadiaPerspective(element).orElse(null);
        return this.getImageFromArcadiaType(perspective, element, arcadiaType);

    }

    private String computePackageImage(Package pkg) {
        String value = null;
        String declaredName = pkg.getDeclaredName();
        String packageImageName = switch (declaredName) {
            case FUNCTIONS -> "LogicalFunctionPkg";
            case STRUCTURE -> "LogicalComponentPkg";
            case CAPABILITIES -> "CapabilitiesPkg";
            case INTERFACES -> "InterfacesPkg";
            case DATA -> "DataPkg";
            case REQUIREMENTS -> "RequirementsPkg";
            default -> null;
        };

        if (packageImageName == null && ArcadiaEngineeringPerspective.containsValue(declaredName)) {
            packageImageName = declaredName.replaceAll("\\s", "");
        }
        if (packageImageName != null) {
            value = String.format(ICONS_FULL_PATH, packageImageName);
        }
        return value;
    }

    private Optional<String> computeElementNameWithArchitecture(ArcadiaEngineeringPerspective perspective, String name, Element element) {
        String componentType = null;
        if (ArcadiaEngineeringPerspective.LogicalArchitecture.equals(perspective)) {
            if (this.transverseQueryService.isComponentHumanActor(element)) {
                componentType = LOGICAL + name + "Human";
            } else if (this.transverseQueryService.isComponentActor(element)) {
                componentType = LOGICAL + "Actor";
            } else {
                componentType = LOGICAL + name;
            }
        } else if (ArcadiaEngineeringPerspective.PhysicalArchitecture.equals(perspective)) {
            componentType = PHYSICAL + name;
        }
        return Optional.ofNullable(componentType);
    }
}

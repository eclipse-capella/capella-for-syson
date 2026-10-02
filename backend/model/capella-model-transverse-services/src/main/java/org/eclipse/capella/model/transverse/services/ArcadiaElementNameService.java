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

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;

import java.util.Optional;

import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Usage;

/**
 * Provides perspective-specific Arcadia element names.
 *
 * @author Jerome Gout
 */
public class ArcadiaElementNameService {

    /**
     * Returns the business name of the element for the given perspective.
     *
     * @param element
     *         the element whose name is requested
     * @param perspective
     *         the non-null perspective used to select the business name
     * @return the element name, or an empty optional if the element has no name for the perspective
     */
    public Optional<String> getElementName(Element element, ArcadiaEngineeringPerspective perspective) {
        if (element instanceof Usage usage) {
            return usage.getType().stream()
                    .map(Element::getQualifiedName)
                    .map(type -> this.getElementName(type, perspective))
                    .flatMap(Optional::stream)
                    .findFirst();
        }
        return Optional.empty();
    }

    private Optional<String> getElementName(String type, ArcadiaEngineeringPerspective perspective) {
        if (type == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(switch (type) {
            case ARCADIA_PREFIX + ARCADIA_FUNCTION -> this.getFunctionName(perspective);
            case ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN -> this.getFunctionalChainName(perspective);
            case ARCADIA_PREFIX + ARCADIA_CAPABILITY -> this.getCapabilityName(perspective);
            case ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_EXCHANGE -> this.getFunctionalExchangeName(perspective);
            case ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM, "SysML::ExchangeItem" -> this.getPortName("Function Port", perspective);
            case ARCADIA_PREFIX + ARCADIA_COMPONENT -> this.getComponentName(perspective);
            case ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE -> this.getComponentExchangeName(perspective);
            case ARCADIA_PREFIX + ARCADIA_COMPONENT_PORT -> this.getPortName("Component Port", perspective);
            default -> null;
        });
    }

    private String getFunctionName(ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case OperationalAnalysis -> "Operational Activity";
            case SystemAnalysis -> "System Function";
            case LogicalArchitecture -> "Logical Function";
            case PhysicalArchitecture -> "Physical Function";
            default -> null;
        };
    }

    private String getFunctionalChainName(ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case OperationalAnalysis -> "Operational Process";
            case SystemAnalysis, LogicalArchitecture, PhysicalArchitecture -> "Functional Chain";
            default -> null;
        };
    }

    private String getCapabilityName(ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case OperationalAnalysis -> "Operational Capability";
            case SystemAnalysis -> "Capability";
            case LogicalArchitecture, PhysicalArchitecture, EPBS -> "Capability Realization";
        };
    }

    private String getFunctionalExchangeName(ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case OperationalAnalysis -> "Interaction";
            case SystemAnalysis, LogicalArchitecture, PhysicalArchitecture -> "Functional Exchange";
            default -> null;
        };
    }

    private String getComponentName(ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case OperationalAnalysis -> "Operational Entity";
            case SystemAnalysis -> "System Component";
            case LogicalArchitecture -> "Logical Component";
            case PhysicalArchitecture -> "Physical Component";
            case EPBS -> "Configuration Item";
        };
    }

    private String getComponentExchangeName(ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case OperationalAnalysis -> "Communication Mean";
            case SystemAnalysis, LogicalArchitecture, PhysicalArchitecture -> "Component Exchange";
            default -> null;
        };
    }

    private String getPortName(String name, ArcadiaEngineeringPerspective perspective) {
        return switch (perspective) {
            case SystemAnalysis, LogicalArchitecture, PhysicalArchitecture -> name;
            default -> null;
        };
    }
}

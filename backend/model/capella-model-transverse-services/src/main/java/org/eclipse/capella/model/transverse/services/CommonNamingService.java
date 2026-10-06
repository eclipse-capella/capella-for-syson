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

import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.Namespace;

/**
 * Service for the management of new Arcadia element names.
 *
 * @author adieumegard
 */
public class CommonNamingService {

    public static final String PACKAGE_DEFAULT_DECLAREDNAME_PREFIX = "Package";

    public static final String FUNCTION_PORT_DEFAULT_DECLAREDNAME_PREFIX = "FP";

    public static final String FUNCTION_OUTPUTPORT_DEFAULT_DECLAREDNAME_PREFIX = "FOP";

    public static final String FUNCTION_INPUTPORT_DEFAULT_DECLAREDNAME_PREFIX = "FIP";

    public static final String REQUIREMENT_DEFAULT_DECLAREDNAME_PREFIX = "Requirement";

    public static final String COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX = "CP";

    public static final String COMPONENT_DEFAULT_DECLAREDNAME_PREFIX = "C";

    public static final String ACTOR_DEFAULT_DECLAREDNAME_PREFIX = "A";

    public static final String COMMUNICATION_MEAN_DEFAULT_DECLAREDNAME_PREFIX = "CommunicationMean";

    public static final String OPERATIONAL_ACTOR_DEFAULT_DECLAREDNAME_PREFIX = "OA";

    public static final String OPERATIONAL_ENTITY_DEFAULT_DECLAREDNAME_PREFIX = "OE";

    public static final String OPERATIONAL_CAPABILITY_DEFAULT_DECLAREDNAME_PREFIX = "OC";

    public static final String OPERATIONAL_PROCESS_DEFAULT_DECLAREDNAME_PREFIX = "OperationalProcess";

    public static final String OPERATIONAL_ACTIVITY_DEFAULT_DECLAREDNAME_PREFIX = "OA";

    public static final String FUNCTIONAL_CHAIN_DEFAULT_DECLAREDNAME_PREFIX = "FunctionalChain";

    public static final String COMPONENT_EXCHANGE_DEFAULT_DECLAREDNAME_PREFIX = "CE";

    public static final String FUNCTIONAL_EXCHANGE_DEFAULT_DECLAREDNAME_PREFIX = "FE";

    public static final String FUNCTION_DEFAULT_DECLAREDNAME_PREFIX = "Function";

    public static final String INTERACTION_DEFAULT_DECLAREDNAME_PREFIX = "Interaction";

    private static final String WHITE_SPACE = " ";

    private final CommonQueryService commonQueryService;

    public CommonNamingService() {
        this.commonQueryService = new CommonQueryService();
    }

    /**
     * Set the DeclaredName attribute for the passed element.
     *
     * @param element
     *            The element to be named
     *
     * @param namePrefix
     *            The string to prefix the new name with
     */
    public void setElementDefaultDeclaredName(Element element, String namePrefix) {
        long existingElementsCount = this.existingElementsCount(element);
        element.setDeclaredName(namePrefix + WHITE_SPACE + existingElementsCount);
    }

    /**
     * Computes the count of elements of the same type as {@code element} reachable on its own context. Is limited to
     * directly contained elements not imported ones. Do not use if you want to count number of elements including all
     * dependencies.
     *
     * @param element
     *            The element used as computation context
     * @return
     */
    public long existingElementsCount(Element element) {
        Namespace owningNamespace = element.getOwningNamespace();
        if (owningNamespace != null) {
            return owningNamespace.getOwnedMember().stream()
                    .filter(member -> element.eClass().equals(member.eClass()))
                    .filter(member -> this.commonQueryService.getArcadiaType(element).equals(this.commonQueryService.getArcadiaType(member)))
                    .count();
        }
        return 1;
    }

    /**
     * Provides default name for a port depending on its direction.
     *
     * @param direction
     * @return
     */
    public String getFunctionPortDefaultDeclaredNamePrefix(FeatureDirectionKind direction) {
        String defaultName = switch (direction) {
            case IN -> FUNCTION_INPUTPORT_DEFAULT_DECLAREDNAME_PREFIX;
            case OUT -> FUNCTION_OUTPUTPORT_DEFAULT_DECLAREDNAME_PREFIX;
            default -> FUNCTION_PORT_DEFAULT_DECLAREDNAME_PREFIX;
        };
        return defaultName;
    }

}

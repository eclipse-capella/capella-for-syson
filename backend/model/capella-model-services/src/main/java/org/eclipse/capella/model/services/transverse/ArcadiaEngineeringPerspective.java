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
package org.eclipse.capella.model.services.transverse;

/**
 * An enum listing all Arcadia Architecture Perspectives.
 *
 * @author fbarbin
 */
public enum ArcadiaEngineeringPerspective {
    OperationalAnalysis("Operational Analysis"), SystemAnalysis("System Analysis"), LogicalArchitecture("Logical Architecture"), PhysicalArchitecture("Physical Architecture"), EPBS(
            "EPBS Architecture");

    private final String label;

    ArcadiaEngineeringPerspective(String label) {
        this.label = label;
    }

    public static boolean containsValue(String testValue) {
        for (ArcadiaEngineeringPerspective perspective : ArcadiaEngineeringPerspective.values()) {
            if (perspective.label.equals(testValue)) {
                return true;
            }
        }
        return false;
    }

    public static ArcadiaEngineeringPerspective fromValue(String value) {
        for (ArcadiaEngineeringPerspective perspective : ArcadiaEngineeringPerspective.values()) {
            if (perspective.label.equals(value)) {
                return perspective;
            }
        }
        throw new IllegalArgumentException("No enum constant with value: " + value);
    }

    public String getLabel() {
        return label;
    }
}

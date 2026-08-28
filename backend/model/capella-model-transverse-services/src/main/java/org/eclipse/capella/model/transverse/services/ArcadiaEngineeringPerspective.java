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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * An enum listing all Arcadia Architecture Perspectives.
 *
 * @author fbarbin
 */
public enum ArcadiaEngineeringPerspective {
    OperationalAnalysis("Operational Analysis"),
    SystemAnalysis("System Analysis"),
    LogicalArchitecture("Logical Architecture"),
    PhysicalArchitecture("Physical Architecture"),
    EPBS("EPBS Architecture");

    private final String label;

    ArcadiaEngineeringPerspective(String label) {
        this.label = label;
    }

    public static Optional<ArcadiaEngineeringPerspective> fromLabel(String label) {
        return Arrays.stream(values())
                .filter(arcadiaEngineeringPerspective -> Objects.equals(arcadiaEngineeringPerspective.getLabel(), label))
                .findFirst();
    }

    public String getLabel() {
        return this.label;
    }
}

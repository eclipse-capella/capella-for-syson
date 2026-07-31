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
package org.eclipse.capella.application.configuration.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;

import org.eclipse.capella.tests.AbstractCapellaCodingRulesTests;
import org.junit.jupiter.api.Test;

/**
 * Coding rules tests.
 *
 * @author sbegaudeau
 */
public class CodingRulesTests extends AbstractCapellaCodingRulesTests {

    @Override
    protected String getProjectRootPackage() {
        return ArchitectureConstants.CAPELLA_APPLICATION_CONFIGURATION;
    }

    @Override
    protected JavaClasses getClasses() {
        return ArchitectureConstants.CLASSES;
    }

    @Test
    @Override
    public void noClassesShouldUseApacheCommons() {
        super.noClassesShouldUseApacheCommons();
    }

    @Override
    public void noMethodsShouldBeStatic() {
        super.noMethodsShouldBeStatic();

    }
}

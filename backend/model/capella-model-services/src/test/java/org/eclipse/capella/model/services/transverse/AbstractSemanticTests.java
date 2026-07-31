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

import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.junit.jupiter.api.BeforeEach;

/**
 * Superclass of all the semantic tests used to set up the test environment.
 * <p>
 * Subclasses can use the provided {@code capellaModel} as a base model.
 * This model contains the same element as an empty Capella project.
 * The {@code capellaModel} is reset before each test.
 *
 * @author gdaniel
 */
public abstract class AbstractSemanticTests {

    /**
     * The test fixture used to initialize Capella models.
     * <p>
     * This attribute is static to ensure it is only loaded once, since it requires to load all the SysML/KerML libraries and the Arcadia library.
     */
    private static final SemanticDataTestFixture SEMANTIC_DATA_TEST_FIXTURE = new SemanticDataTestFixture();

    protected CapellaModel capellaModel;

    @BeforeEach
    public void beforeEach() {
        this.capellaModel = SEMANTIC_DATA_TEST_FIXTURE.createCapellaModel();
    }
}

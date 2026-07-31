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

package org.eclipse.capella.application.controllers.libraries;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.sirius.web.tests.graphql.LibrariesQueryRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests of the library controllers.
 *
 * @author gdaniel
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private LibrariesQueryRunner librariesQueryRunner;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @GivenCapellaServer
    @DisplayName("Given a Capella for SysON application, when the libraries are queried, then the Arcadia library is returned")
    public void capellaForSysONContainsArcadiaLibrary(CapturedOutput capturedOutput) {
        Map<String, Object> variables = Map.of("page", 0, "limit", 10);
        var result = this.librariesQueryRunner.run(variables);

        List<String> libraryNames = JsonPath.read(result.data(), "$.data.viewer.libraries.edges[*].node.name");
        assertThat(libraryNames).contains("arcadia");
    }
}

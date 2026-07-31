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
import org.eclipse.sirius.web.tests.graphql.LibrariesQueryRunner;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

/**
 * Integration tests of the library initialization controllers.
 * <p>
 * These tests check the behavior of the {@link org.springframework.boot.CommandLineRunner} that initializes the Arcadia library on an empty server. Note that other integration tests don't rely on
 * this runner, but on {@code arcadia-library.sql}, which populates the database with the Arcadia library.
 * </p>
 *
 * @author gdaniel
 */
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "org.eclipse.capella.arcadia-library-initialization.enabled = true"
})
public class LibraryInitializationControllerIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private LibrariesQueryRunner librariesQueryRunner;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @Sql(scripts = "/scripts/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
    @DisplayName("Given a server without the Arcadia library, when the server is started, then the Arcadia library is loaded")
    public void arcadiaLibraryIsLoadedOnServerWithoutIt(CapturedOutput capturedOutput) {
        // Ensure the json library was actually loaded.
        assertThat(capturedOutput.getOut()).contains("Loaded Arcadia Library resource: arcadia.json");

        Map<String, Object> variables = Map.of("page", 0, "limit", 10);
        var result = this.librariesQueryRunner.run(variables);

        List<String> libraryNames = JsonPath.read(result.data(), "$.data.viewer.libraries.edges[*].node.name");
        assertThat(libraryNames).contains("arcadia");
    }
}

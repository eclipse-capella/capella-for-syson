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

package org.eclipse.capella.application.controllers.diagram;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.CapellaProjectData;
import org.eclipse.capella.GivenCapellaServer;
import org.eclipse.sirius.components.diagrams.tests.graphql.ConnectorPaletteExecutor;
import org.eclipse.sirius.web.tests.services.api.IGivenInitialServerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests of the connector palette controllers.
 *
 * @author gdaniel
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@GivenCapellaServer
public class ConnectorPaletteIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IGivenInitialServerState givenInitialServerState;

    @Autowired
    private ConnectorPaletteExecutor connectorPaletteExecutor;

    @BeforeEach
    public void beforeEach() {
        this.givenInitialServerState.initialize();
    }

    @Test
    @DisplayName("Given a SAB diagram, when the connector palette is requested on the system, then the appropriate content is returned")
    public void getConnectorPaletteOnSABSystem() {
        this.connectorPaletteExecutor.execute(CapellaProjectData.EDITING_CONTEXT_ID, CapellaProjectData.GraphicalIds.SAB_SYSTEM_ANALYSIS_BLANK_DIAGRAM_ID, CapellaProjectData.GraphicalIds.SYSTEM_PAR_NODE_ID, CapellaProjectData.GraphicalIds.SYSTEM_PAR_NODE_ID)
                // SingleCLickOnTwoDiagramElementsTool indicate that we returned a tool directly and not a palette.
                .hasTypeName(e -> assertThat(e).isEqualTo("SingleClickOnTwoDiagramElementsTool"))
                .hasToolLabel(label -> assertThat(label).isEqualTo("New Component Exchange"));
    }
}

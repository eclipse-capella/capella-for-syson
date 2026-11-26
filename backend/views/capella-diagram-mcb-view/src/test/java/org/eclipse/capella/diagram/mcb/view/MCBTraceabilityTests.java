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
package org.eclipse.capella.diagram.mcb.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationReconnectToolServices;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Checks the semantic lifecycle of MCB traceability links.
 *
 * @author tbezierslafosse
 */
public class MCBTraceabilityTests extends AbstractSemanticTests {

    @Test
    public void createDescribesShouldSupportReconnectionAndDeletionWithoutDeletingEndpoints() {
        var creationService = new CommonCreationService();
        var queryService = new CommonQueryService();
        var reconnectService = new TransverseRepresentationReconnectToolServices((element, parent) -> null,
                mock(DiagramMutationElementService.class, CALLS_REAL_METHODS), new IObjectSearchService.NoOp());
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var requirement = creationService.createRequirement(context);
        var newSource = creationService.createRequirement(context);
        var targetRequirement = creationService.createRequirement(context);
        var mission = creationService.createMission(context);
        var capability = creationService.createCapability(context);
        var actor = creationService.createActor(context);

        for (var target : List.of(mission, capability, actor, targetRequirement)) {
            var relation = creationService.createDescribes(requirement, target);
            assertThat(queryService.getDescribesSource(relation)).isSameAs(requirement);
            assertThat(queryService.getDescribesTarget(relation)).isSameAs(target);
            reconnectService.reconnectDescribes(relation, newSource, true);
            reconnectService.reconnectDescribes(relation, capability, false);
            assertThat(queryService.getDescribesSource(relation)).isSameAs(newSource);
            assertThat(queryService.getDescribesTarget(relation)).isSameAs(capability);
            new CommonDeletionService().delete(relation);
            assertThat(relation.eResource()).isNull();
            assertThat(requirement.getOwner()).isNotNull();
            assertThat(target.getOwner()).isNotNull();
        }
    }
}

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
package org.eclipse.capella.model.services.operational.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.syson.sysml.OccurrenceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.Usage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OAReconnectToolServices}.
 *
 * @author tbezierslafosse
 */
public class OAReconnectToolServicesTests {

    private static final SemanticDataTestFixture SEMANTIC_DATA_TEST_FIXTURE = new SemanticDataTestFixture();

    private final OAMutationService oaMutationService = new OAMutationService();

    private final OAReconnectToolServices oaReconnectToolServices = new OAReconnectToolServices();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private CapellaModel capellaModel;

    @BeforeEach
    public void setUp() {
        this.capellaModel = SEMANTIC_DATA_TEST_FIXTURE.createCapellaModel();
    }

    @Test
    public void reconnectCapabilityInvolvementShouldReplaceOnlyTheSelectedParticipant() {
        Package capabilitiesPackage = this.capellaModel.getOperationalAnalysisPerspective().getCapabilitiesPackage().getElement();
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        OccurrenceUsage capability = this.oaMutationService.createOperationalCapability(capabilitiesPackage);
        PartUsage oldParticipant = this.transverseMutationService.createActor(structurePackage);
        PartUsage preservedParticipant = this.transverseMutationService.createActor(structurePackage);
        PartUsage newParticipant = this.transverseMutationService.createActor(structurePackage);
        this.oaMutationService.createCapabilityInvolvement(capability, oldParticipant);
        this.oaMutationService.createCapabilityInvolvement(capability, preservedParticipant);

        Usage result = this.oaReconnectToolServices.reconnectCapabilityInvolvement(capability, oldParticipant, newParticipant);

        assertThat(result).isSameAs(capability);
        assertThat(this.transverseQueryService.getInvolvedComponents(capability)).containsExactly(newParticipant, preservedParticipant);
    }

    @Test
    public void reconnectCapabilityInvolvementSourceShouldMoveTheParticipantToTheNewCapability() {
        Package capabilitiesPackage = this.capellaModel.getOperationalAnalysisPerspective().getCapabilitiesPackage().getElement();
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        OccurrenceUsage oldCapability = this.oaMutationService.createOperationalCapability(capabilitiesPackage);
        OccurrenceUsage newCapability = this.oaMutationService.createOperationalCapability(capabilitiesPackage);
        PartUsage participant = this.transverseMutationService.createActor(structurePackage);
        this.oaMutationService.createCapabilityInvolvement(oldCapability, participant);

        Usage result = this.oaReconnectToolServices.reconnectCapabilityInvolvementSource(oldCapability, newCapability, participant);

        assertThat(result).isSameAs(newCapability);
        assertThat(this.transverseQueryService.getInvolvedComponents(oldCapability)).isEmpty();
        assertThat(this.transverseQueryService.getInvolvedComponents(newCapability)).containsExactly(participant);
    }
}

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
package org.eclipse.capella.model.services.system.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.junit.jupiter.api.Test;

/**
 * Tests System Analysis semantic mutations.
 *
 * @author tbezierslafosse
 */
public class SAMutationServiceTests extends AbstractSemanticTests {

    private final SAMutationService mutationService = new SAMutationService();

    private final CommonCreationService creationService = new CommonCreationService();

    private final SAQueryService queryService = new SAQueryService();

    @Test
    public void createCapabilityExploitationShouldPreserveMultipleTargetsAndRejectDuplicates() {
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var mission = this.creationService.createMission(context);
        var first = this.creationService.createCapability(context);
        var second = this.creationService.createCapability(context);

        this.mutationService.createCapabilityExploitation(mission, first);
        this.mutationService.createCapabilityExploitation(mission, second);
        this.mutationService.createCapabilityExploitation(mission, first);

        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(first, second);
    }

    @Test
    public void deleteCapabilityExploitationShouldPreserveOtherTargetsAndRemoveEmptyReference() {
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var mission = this.creationService.createMission(context);
        var first = this.creationService.createCapability(context);
        var second = this.creationService.createCapability(context);
        this.mutationService.createCapabilityExploitation(mission, first);
        this.mutationService.createCapabilityExploitation(mission, second);

        this.mutationService.deleteCapabilityExploitation(mission, first);
        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(second);
        assertThat(first.getOwner()).isNotNull();
        this.mutationService.deleteCapabilityExploitation(mission, second);
        assertThat(this.queryService.getExploitedCapabilities(mission)).isEmpty();
        assertThat(mission.getNestedReference()).isEmpty();
        assertThat(mission.getOwner()).isNotNull();
    }

    @Test
    public void deleteMCBEndpointShouldPreserveOtherMissionAndCapabilityRelations() {
        var deletionService = new CommonDeletionService();
        var commonQueryService = new CommonQueryService();
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var mission = this.creationService.createMission(context);
        var first = this.creationService.createCapability(context);
        var second = this.creationService.createCapability(context);
        var firstActor = this.creationService.createActor(context);
        var secondActor = this.creationService.createActor(context);
        this.mutationService.createCapabilityExploitation(mission, first);
        this.mutationService.createCapabilityExploitation(mission, second);
        this.creationService.createCapabilityInvolvement(mission, firstActor);
        this.creationService.createCapabilityInvolvement(mission, secondActor);
        this.creationService.createCapabilityInvolvement(second, firstActor);
        this.creationService.createCapabilityInvolvement(second, secondActor);
        deletionService.delete(first);
        deletionService.delete(firstActor);
        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(second);
        assertThat(commonQueryService.getInvolvedComponents(mission)).containsExactly(secondActor);
        assertThat(commonQueryService.getInvolvedComponents(second)).containsExactly(secondActor);
        deletionService.delete(second);
        assertThat(this.queryService.getExploitedCapabilities(mission)).isEmpty();
    }

    @Test
    public void setCapabilityExploitationSourceShouldPreserveOtherRelationsAndRejectDuplicates() {
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var mission = this.creationService.createMission(context);
        var otherMission = this.creationService.createMission(context);
        var first = this.creationService.createCapability(context);
        var second = this.creationService.createCapability(context);
        this.mutationService.createCapabilityExploitation(mission, first);
        this.mutationService.createCapabilityExploitation(mission, second);
        this.mutationService.setCapabilityExploitationTarget(mission, first, second);
        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(first, second);
        this.mutationService.setCapabilityExploitationSource(mission, otherMission, first);
        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(second);
        assertThat(this.queryService.getExploitedCapabilities(otherMission)).containsExactly(first);
        this.mutationService.setCapabilityExploitationTarget(otherMission, first, second);
        assertThat(this.queryService.getExploitedCapabilities(otherMission)).containsExactly(second);
        this.mutationService.setCapabilityExploitationSource(mission, otherMission, second);
        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(second);
        this.mutationService.setCapabilityExploitationSource(mission, first, second);
        this.mutationService.setCapabilityExploitationTarget(mission, second, mission);
        assertThat(this.queryService.getExploitedCapabilities(mission)).containsExactly(second);
    }

    @Test
    public void canCreateCapabilityExploitationShouldCheckEndpointTypesAndDuplicates() {
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var mission = this.creationService.createMission(context);
        var capability = this.creationService.createCapability(context);

        assertThat(this.queryService.canCreateCapabilityExploitation(mission, capability)).isTrue();
        assertThat(this.queryService.canCreateCapabilityExploitation(capability, mission)).isFalse();
        assertThat(this.queryService.canCreateCapabilityExploitation(mission, mission)).isFalse();
        this.mutationService.createCapabilityExploitation(mission, capability);
        assertThat(this.queryService.canCreateCapabilityExploitation(mission, capability)).isFalse();
    }

    @Test
    public void createCapabilityExploitationShouldRejectInvalidEndpoints() {
        var context = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var mission = this.creationService.createMission(context);
        var capability = this.creationService.createCapability(context);
        var relationships = List.copyOf(mission.getOwnedRelationship());
        this.mutationService.createCapabilityExploitation(mission, mission);
        this.mutationService.createCapabilityExploitation(capability, mission);

        assertThat(mission.getOwnedRelationship()).containsExactlyElementsOf(relationships);
        assertThat(this.queryService.getExploitedCapabilities(capability)).isEmpty();
    }
}

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

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.OccurrenceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.Subsetting;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TransverseMutationService}.
 *
 * @author tbezierslafosse
 */
public class TransverseMutationServiceTests {

    private static final SemanticDataTestFixture SEMANTIC_DATA_TEST_FIXTURE = new SemanticDataTestFixture();

    private final OAMutationService oaMutationService = new OAMutationService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private CapellaModel capellaModel;

    @BeforeEach
    public void setUp() {
        this.capellaModel = SEMANTIC_DATA_TEST_FIXTURE.createCapellaModel();
    }

    @Test
    public void createGeneralizationShouldConnectSpecificAndGeneralFeatures() {
        Package capabilitiesPackage = this.capellaModel.getOperationalAnalysisPerspective().getCapabilitiesPackage().getElement();
        OccurrenceUsage specific = this.oaMutationService.createOperationalCapability(capabilitiesPackage);
        OccurrenceUsage general = this.oaMutationService.createOperationalCapability(capabilitiesPackage);

        Subsetting generalization = this.transverseMutationService.createGeneralization(specific, general);

        assertThat(generalization.getSubsettingFeature()).isSameAs(specific);
        assertThat(generalization.getSubsettedFeature()).isSameAs(general);
        assertThat(generalization.getSpecific()).isSameAs(specific);
        assertThat(generalization.getGeneral()).isSameAs(general);
        assertThat(specific.getOwnedRelationship()).contains(generalization);
    }

    @Test
    public void createDescribesShouldLinkRequirementToOperationalCapability() {
        Package capabilitiesPackage = this.capellaModel.getOperationalAnalysisPerspective().getCapabilitiesPackage().getElement();
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        OccurrenceUsage capability = this.oaMutationService.createOperationalCapability(capabilitiesPackage);
        RequirementUsage requirement = this.transverseMutationService.createRequirement(structurePackage);

        AllocationUsage describes = this.transverseMutationService.createDescribes(requirement, capability);

        assertThat(describes).isNotNull();
    }
}

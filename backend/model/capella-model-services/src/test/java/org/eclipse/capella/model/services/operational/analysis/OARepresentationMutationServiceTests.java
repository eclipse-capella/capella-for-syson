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
import static org.mockito.Mockito.mock;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationMutationService;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OARepresentationMutationService}.
 *
 * @author fbarbin
 */
public class OARepresentationMutationServiceTests {

    private final OATestModelFixture fixture = new OATestModelFixture();

    private final OAQueryService oaQueryService = new OAQueryService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final DiagramMutationElementService diagramMutationElementService = mock(DiagramMutationElementService.class);

    private final OAMutationService oaMutationService = new OAMutationService();

    private final OARepresentationMutationService oaRepresentationMutationService = new OARepresentationMutationService(
            new TransverseRepresentationMutationService());

    @Test
    @DisplayName("GIVEN OA structure package, WHEN creating actor and human actor entities, THEN names, typing and actor flags are correct")
    public void testCreateEntityComponent() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaComponentAttributes(root);

        OATestModelFixture.OperationalAnalysisPackages oaPackages = this.fixture.createOperationalAnalysisPackages(root);

        PartUsage entity = this.oaRepresentationMutationService.createEntityComponent(oaPackages.structurePackage(), false);
        PartUsage humanActorEntity = this.oaRepresentationMutationService.createEntityComponent(oaPackages.structurePackage(), true);

        assertThat(oaPackages.structurePackage().getOwnedElement()).contains(entity, humanActorEntity);

        assertThat(entity.getDeclaredName()).startsWith("OE ");
        assertThat(humanActorEntity.getDeclaredName()).startsWith("OA ");

        assertThat(this.transverseQueryService.checkType(entity, TransverseQueryService.ARCADIA_PREFIX + TransverseQueryService.ARCADIA_COMPONENT)).isTrue();
        assertThat(this.transverseQueryService.checkType(humanActorEntity, TransverseQueryService.ARCADIA_PREFIX + TransverseQueryService.ARCADIA_COMPONENT)).isTrue();

        assertThat(this.transverseQueryService.isComponentActor(entity)).isFalse();
        assertThat(this.transverseQueryService.isComponentHumanActor(entity)).isFalse();

        assertThat(this.transverseQueryService.isComponentActor(humanActorEntity)).isTrue();
        assertThat(this.transverseQueryService.isComponentHumanActor(humanActorEntity)).isTrue();
    }

    @Test
    @DisplayName("GIVEN OA requirements package, WHEN creating a requirement, THEN requirement is typed and created in the requirements package")
    public void testCreateRequirement() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaRequirementDefinition(root);

        OATestModelFixture.OperationalAnalysisPackages oaPackages = this.fixture.createOperationalAnalysisPackages(root);

        RequirementUsage requirementUsage = this.transverseMutationService.createRequirement(oaPackages.structurePackage());

        assertThat(requirementUsage).isNotNull();
        assertThat(oaPackages.requirementsPackage().getOwnedElement()).contains(requirementUsage);
        assertThat(requirementUsage.getDeclaredName()).startsWith("Requirement ");
        // Requirements aren't typed by Arcadia anymore, they are SysMLv2 RequirementUsages
        assertThat(this.transverseQueryService.checkType(requirementUsage, TransverseQueryService.ARCADIA_PREFIX + TransverseQueryService.ARCADIA_REQUIREMENT)).isFalse();
        assertThat(requirementUsage).isInstanceOf(RequirementUsage.class);
    }

    @Test
    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    @DisplayName("GIVEN two entities, WHEN creating a communication mean, THEN communication mean is created, named and linked to source and target")
    public void testCreateCommunicationMeanComponentExchange() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaComponentExchangeDefinition(root);

        OATestModelFixture.OperationalAnalysisPackages oaPackages = this.fixture.createOperationalAnalysisPackages(root);
        PartUsage sourceEntity = this.fixture.createArcadiaTypedComponent(oaPackages.structurePackage(), "Source Entity");
        PartUsage targetEntity = this.fixture.createArcadiaTypedComponent(oaPackages.structurePackage(), "Target Entity");

        InterfaceUsage communicationMean = this.oaMutationService.createCommunicationMeanComponentExchangeOA(sourceEntity, targetEntity);

        assertThat(communicationMean).isNotNull();
        assertThat(communicationMean.getDeclaredName()).startsWith("CommunicationMean ");
        assertThat(this.oaQueryService.getComponentExchangeSourceOA(communicationMean)).isSameAs(sourceEntity);
        assertThat(this.oaQueryService.getComponentExchangeTargetOA(communicationMean)).isSameAs(targetEntity);
    }

}

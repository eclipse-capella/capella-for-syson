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
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link OAQueryService}.
 *
 * @author fbarbin
 */
public class OAQueryServiceTests {

    private final OATestModelFixture fixture = new OATestModelFixture();

    private final OAQueryService oaQueryService = new OAQueryService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final DiagramMutationElementService diagramMutationElementService = mock(DiagramMutationElementService.class);

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final MetamodelMutationElementService metamodelMutationElementService = new MetamodelMutationElementService();

    @Test
    @DisplayName("GIVEN OA packages and nested entities, WHEN resolving component/requirements packages and sub-components, THEN only expected typed components are returned")
    public void testToPackagesAndGetSubComponents() {
        Package root = this.fixture.createRootPackage();
        OATestModelFixture.OperationalAnalysisPackages oaPackages = this.fixture.createOperationalAnalysisPackages(root);

        PartUsage componentA = this.fixture.createArcadiaTypedComponent(oaPackages.structurePackage(), "Entity A");
        this.fixture.createPartUsage(oaPackages.structurePackage(), "Untyped Entity");

        PartUsage nestedTypedComponent = this.fixture.createArcadiaTypedComponent(componentA, "Nested Entity");
        this.fixture.createPartUsage(componentA, "Untyped Nested Entity");

        assertThat(this.transverseQueryService.getStructurePackage(nestedTypedComponent)).isPresent().get().isSameAs(oaPackages.structurePackage());
        assertThat(this.transverseQueryService.getRequirementsPackage(nestedTypedComponent)).isPresent().get().isSameAs(oaPackages.requirementsPackage());

        assertThat(this.transverseQueryService.getSubComponents(oaPackages.operationalAnalysisPackage())).containsExactly(componentA);
        assertThat(this.transverseQueryService.getSubComponents(componentA)).containsExactly(nestedTypedComponent);
    }

    @Test
    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    @DisplayName("GIVEN a communication mean between two entities, WHEN querying source and target, THEN source and target entities are resolved")
    public void testGetComponentExchangeSourceAndTarget() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaComponentExchangeDefinition(root);

        OATestModelFixture.OperationalAnalysisPackages oaPackages = this.fixture.createOperationalAnalysisPackages(root);
        PartUsage sourceEntity = this.fixture.createArcadiaTypedComponent(oaPackages.structurePackage(), "Source Entity");
        PartUsage targetEntity = this.fixture.createArcadiaTypedComponent(oaPackages.structurePackage(), "Target Entity");

        InterfaceUsage communicationMean = this.transverseMutationService.createComponentExchange(sourceEntity, targetEntity);
        assertThat(communicationMean).isNotNull();
        assertThat(this.oaQueryService.getComponentExchangeSourceOA(communicationMean)).isSameAs(sourceEntity);
        assertThat(this.oaQueryService.getComponentExchangeTargetOA(communicationMean)).isSameAs(targetEntity);
    }

}

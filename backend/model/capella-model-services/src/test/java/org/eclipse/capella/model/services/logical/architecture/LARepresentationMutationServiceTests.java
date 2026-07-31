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
package org.eclipse.capella.model.services.logical.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LARepresentationMutationService}.
 *
 * @author fbarbin
 */
public class LARepresentationMutationServiceTests {

    private final LATestModelFixture fixture = new LATestModelFixture();

    private final LAQueryService laQueryService = new LAQueryService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final LARepresentationMutationService laRepresentationMutationService = new LARepresentationMutationService();

    private final MetamodelMutationElementService metamodelMutationElementService = new MetamodelMutationElementService();

    @Test
    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    @DisplayName("GIVEN two functions, WHEN creating a functional exchange, THEN a flow is created between OUT and IN function ports and invalid ends are rejected")
    public void testCreateFunctionalExchange() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaTypedExchangeItem(root, "Seed Exchange Item");

        ActionUsage sourceFunction = this.fixture.createArcadiaTypedFunction(root, "Source Function");
        ActionUsage targetFunction = this.fixture.createArcadiaTypedFunction(root, "Target Function");

        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(sourceFunction, targetFunction);

        assertThat(functionalExchange).isNotNull();
        List<Feature> sourcePorts = this.transverseQueryService.getFunctionPorts(sourceFunction);
        List<Feature> targetPorts = this.transverseQueryService.getFunctionPorts(targetFunction);
        assertThat(sourcePorts).hasSize(1);
        assertThat(targetPorts).hasSize(1);
        assertThat(sourcePorts.get(0).getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(targetPorts.get(0).getDirection()).isEqualTo(FeatureDirectionKind.IN);

        assertThat(this.transverseMutationService.createFunctionalExchange(null, targetFunction)).isNull();
        assertThat(this.transverseMutationService.createFunctionalExchange(sourceFunction, null)).isNull();
    }

    @Test
    @DisplayName("GIVEN a component and a parent function, WHEN creating function children, THEN they are created in the expected containers")
    public void testCreateNewFunctionInComponentAndInFunction() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaTypedFunction(root, "Seed Function");

        LogicalArchitecturePackages logicalArchitecturePackages = this.createLogicalArchitecturePackages(root);

        PartUsage component = this.fixture.createArcadiaTypedComponent(logicalArchitecturePackages.structurePackage(), "Component");
        ActionUsage parentFunction = this.fixture.createArcadiaTypedFunction(logicalArchitecturePackages.functionsPackage(), "Parent Function");

        Element functionInComponent = this.transverseMutationService.createFunction(component);
        Element functionInFunction = this.transverseMutationService.createFunction(parentFunction);

        assertThat(functionInComponent).isInstanceOf(ActionUsage.class);
        assertThat(functionInFunction).isInstanceOf(ActionUsage.class);

        ActionUsage createdInComponent = (ActionUsage) functionInComponent;
        ActionUsage createdInFunction = (ActionUsage) functionInFunction;

        assertThat(logicalArchitecturePackages.functionsPackage().getOwnedElement()).contains(createdInComponent);
        assertThat(this.transverseQueryService.getAllocatedFunctions(component)).contains(createdInComponent);

        assertThat(parentFunction.getNestedAction()).contains(createdInFunction);
        assertThat(createdInFunction.getOwningUsage()).isSameAs(parentFunction);
    }

    @Test
    @DisplayName("GIVEN a function, WHEN creating function ports with each direction, THEN direction and default name prefix are correct")
    public void testCreateFunctionPort() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaTypedExchangeItem(root, "Seed Exchange Item");

        ActionUsage function = this.fixture.createArcadiaTypedFunction(root, "Function");

        Feature inPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.IN);
        Feature outPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.OUT);
        Feature inOutPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.INOUT);

        assertThat(inPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(outPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(inOutPort.getDirection()).isEqualTo(FeatureDirectionKind.INOUT);

        assertThat(inPort.getDeclaredName()).startsWith("FIP ");
        assertThat(outPort.getDeclaredName()).startsWith("FOP ");
        assertThat(inOutPort.getDeclaredName()).startsWith("FP ");
    }

    @Test
    @DisplayName("GIVEN selected functional exchanges, WHEN creating a new functional chain, THEN involved exchanges are stored in the given order")
    public void testCreateNewFunctionalChain() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createArcadiaTypedFunctionalChain(root, "Seed Functional Chain");
        this.fixture.createLibraryFlowUsageReference(root, TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN, TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);

        LogicalArchitecturePackages logicalArchitecturePackages = this.createLogicalArchitecturePackages(root);

        ActionUsage sourceFunction = this.fixture.createArcadiaTypedFunction(logicalArchitecturePackages.functionsPackage(), "Source Function");
        ActionUsage targetFunction = this.fixture.createArcadiaTypedFunction(logicalArchitecturePackages.functionsPackage(), "Target Function");

        FlowUsage flowUsage1 = this.fixture.createArcadiaTypedFunctionalExchange(logicalArchitecturePackages.functionsPackage(), "Functional Exchange 1", sourceFunction, targetFunction);
        FlowUsage flowUsage2 = this.fixture.createArcadiaTypedFunctionalExchange(logicalArchitecturePackages.functionsPackage(), "Functional Exchange 2", sourceFunction, targetFunction);

        ActionUsage createdFunctionalChain = this.transverseMutationService.createFunctionalChain(logicalArchitecturePackages.functionsPackage(), List.of(flowUsage1, flowUsage2));

        assertThat(this.transverseQueryService.getFeatureReferenceValue(createdFunctionalChain, TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES))
                .containsExactly(flowUsage1, flowUsage2);
    }

    private LogicalArchitecturePackages createLogicalArchitecturePackages(Package root) {
        Package logicalArchitecturePackage = this.fixture.createPackage(root, "Logical Architecture");
        Package structurePackage = this.fixture.createPackage(logicalArchitecturePackage, TransverseQueryService.STRUCTURE_PACKAGE);
        Package functionsPackage = this.fixture.createPackage(logicalArchitecturePackage, TransverseQueryService.FUNCTIONS_PACKAGE);
        this.fixture.createPackage(logicalArchitecturePackage, TransverseQueryService.REQUIREMENTS_PACKAGE);
        return new LogicalArchitecturePackages(structurePackage, functionsPackage);
    }

    private record LogicalArchitecturePackages(Package structurePackage, Package functionsPackage) {
    }
}

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
import static org.eclipse.capella.model.transverse.services.CommonQueryService.FUNCTIONS_PACKAGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.REQUIREMENTS_PACKAGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.STRUCTURE_PACKAGE;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LAQueryService}.
 *
 * @author fbarbin
 */
public class LAQueryServiceTests {

    private final CommonUpdateService commonUpdateService = new CommonUpdateService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final LATestModelFixture fixture = new LATestModelFixture();

    @Test
    @DisplayName("GIVEN Arcadia-typed and non Arcadia-typed elements, WHEN evaluating predicates, THEN only matching types are detected")
    public void testTypePredicates() {
        Package root = this.fixture.createRootPackage();
        ActionUsage function = this.fixture.createArcadiaTypedFunction(root, "Function");
        ActionUsage function2 = this.fixture.createArcadiaTypedFunction(root, "Function2");
        ActionUsage functionalChain = this.fixture.createArcadiaTypedFunctionalChain(root, "Functional Chain");
        ActionUsage functionalExchange = this.fixture.createArcadiaTypedFunctionalExchange(root, "Functional Exchange", function, function2);
        ItemUsage exchangeItem = this.fixture.createArcadiaTypedExchangeItem(root, "Exchange Item");
        ItemUsage untypedItemUsage = this.fixture.createItemUsage(root, "Untyped Exchange Item");

        assertThat(this.commonQueryService.isFunction(function)).isTrue();
        assertThat(this.commonQueryService.isFunction(functionalChain)).isFalse();

        assertThat(this.commonQueryService.isFunctionalChain(functionalChain)).isTrue();
        assertThat(this.commonQueryService.isFunctionalChain(function)).isFalse();

        assertThat(this.commonQueryService.isFunctionalExchange(functionalExchange)).isTrue();
        assertThat(this.commonQueryService.isFunctionalExchange(function)).isFalse();

        assertThat(this.commonQueryService.isExchangeItem(exchangeItem)).isTrue();
        assertThat(this.commonQueryService.isExchangeItem(untypedItemUsage)).isFalse();
    }

    @Test
    @DisplayName("GIVEN nested function usages, WHEN querying sub-functions and parent function, THEN the function hierarchy is resolved")
    public void testGetSubFunctionsAndGetParentFunction() {
        Package root = this.fixture.createRootPackage();
        ActionUsage parentFunction = this.fixture.createArcadiaTypedFunction(root, "Parent Function");
        ActionUsage subFunction1 = this.fixture.createArcadiaTypedFunction(parentFunction, "Sub Function 1");
        ActionUsage subFunction2 = this.fixture.createArcadiaTypedFunction(parentFunction, "Sub Function 2");
        this.fixture.createArcadiaTypedFunctionalChain(parentFunction, "Nested Chain");

        assertThat(this.commonQueryService.getSubFunctions(parentFunction)).containsExactlyInAnyOrder(subFunction1, subFunction2);
        assertThat(this.commonQueryService.getParentFunction(subFunction1)).contains(parentFunction);
        assertThat(this.commonQueryService.getParentFunction(parentFunction)).isEmpty();
    }

    @Test
    @DisplayName("GIVEN performed-action allocations, WHEN querying allocating component and allocated functions, THEN both views are consistent")
    public void testGetAllocatingComponentAndGetAllocatedFunctions() {
        Package root = this.fixture.createRootPackage();
        PartUsage component1 = this.fixture.createArcadiaTypedComponent(root, "Component 1");
        PartUsage component2 = this.fixture.createArcadiaTypedComponent(root, "Component 2");
        ActionUsage allocatedFunction1 = this.fixture.createArcadiaTypedFunction(root, "Allocated Function 1");
        ActionUsage allocatedFunction2 = this.fixture.createArcadiaTypedFunction(root, "Allocated Function 2");
        ActionUsage unallocatedFunction = this.fixture.createArcadiaTypedFunction(root, "Unallocated Function");

        this.commonUpdateService.setPerformAction(component1, allocatedFunction1);
        this.commonUpdateService.setPerformAction(component2, allocatedFunction2);

        assertThat(this.commonQueryService.getAllocatedFunctions(component1)).containsExactly(allocatedFunction1);
        assertThat(this.commonQueryService.getAllocatedFunctions(component2)).containsExactly(allocatedFunction2);
        assertThat(this.commonQueryService.getAllocatingComponent(allocatedFunction1)).contains(component1);
        assertThat(this.commonQueryService.getAllocatingComponent(unallocatedFunction)).isEmpty();
    }

    @Test
    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    @DisplayName("GIVEN functional exchanges around a function, WHEN querying incoming and outgoing exchanges, THEN source and target direction is respected")
    public void testGetReferencingAndReferencedFunctionalExchange() {
        Package root = this.fixture.createRootPackage();
        ActionUsage function1 = this.fixture.createArcadiaTypedFunction(root, "Function 1");
        ActionUsage function2 = this.fixture.createArcadiaTypedFunction(root, "Function 2");
        ActionUsage function3 = this.fixture.createArcadiaTypedFunction(root, "Function 3");
        FlowUsage outgoingFlow = this.commonCreationService.createFunctionalExchange(function1, function2);
        outgoingFlow.setDeclaredName("Flow 1");
        FlowUsage incomingFlow = this.commonCreationService.createFunctionalExchange(function3, function1);
        incomingFlow.setDeclaredName("Flow 2");

        assertThat(this.commonQueryService.getOutgoingFunctionalExchanges(function1)).containsExactly(outgoingFlow);
        assertThat(this.commonQueryService.getIncomingFunctionalExchanges(function1)).containsExactly(incomingFlow);
    }

    @Test
    @DisplayName("GIVEN functional chains linked to exchanges, WHEN querying implied and involving chains, THEN complete chain sets are returned")
    public void testGetFunctionalChainsImpliedInAndInvolvingFunction() {
        Package root = this.fixture.createRootPackage();
        ActionUsage function1 = this.fixture.createArcadiaTypedFunction(root, "Function 1");
        ActionUsage function2 = this.fixture.createArcadiaTypedFunction(root, "Function 2");
        ActionUsage function3 = this.fixture.createArcadiaTypedFunction(root, "Function 3");

        FlowUsage flow12 = this.fixture.createArcadiaTypedFunctionalExchange(root, "Flow 12", function1, function2);
        FlowUsage flow23 = this.fixture.createArcadiaTypedFunctionalExchange(root, "Flow 23", function2, function3);

        ActionUsage chainA = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain A");
        ActionUsage chainB = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain B");
        ActionUsage chainC = this.fixture.createArcadiaTypedFunctionalChain(root, "Chain C");

        this.fixture.setInvolvedFunctionalExchanges(chainA, flow12);
        this.fixture.setInvolvedFunctionalExchanges(chainB, flow12);
        this.fixture.setInvolvedFunctionalExchanges(chainC, flow23);

        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(flow12)).containsExactlyInAnyOrder(chainA, chainB);
        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(function2)).containsExactlyInAnyOrder(chainA, chainB, chainC);
    }

    @Test
    @DisplayName("GIVEN logical architecture packages, WHEN resolving structure/functions/requirements package from an element, THEN expected packages are returned")
    public void testPackageResolvers() {
        Package root = this.fixture.createRootPackage();
        Package logicalArchitecturePackage = this.fixture.createPackage(root, "Logical Architecture");
        Package structurePackage = this.fixture.createPackage(logicalArchitecturePackage, STRUCTURE_PACKAGE);
        Package functionsPackage = this.fixture.createPackage(logicalArchitecturePackage, FUNCTIONS_PACKAGE);
        Package requirementsPackage = this.fixture.createPackage(logicalArchitecturePackage, REQUIREMENTS_PACKAGE);
        ActionUsage function = this.fixture.createArcadiaTypedFunction(functionsPackage, "Function");

        assertThat(this.commonQueryService.getStructurePackage(function)).isPresent().get().isSameAs(structurePackage);
        assertThat(this.commonQueryService.getFunctionsPackage(function)).isPresent().get().isSameAs(functionsPackage);
        assertThat(this.commonQueryService.getRequirementsPackage(function)).isPresent().get().isSameAs(requirementsPackage);
    }
}

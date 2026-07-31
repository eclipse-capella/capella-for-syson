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
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.FUNCTIONS_PACKAGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.REQUIREMENTS_PACKAGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.STRUCTURE_PACKAGE;

import java.util.List;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.EnumerationUsage;
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

    private final LAQueryService laQueryService = new LAQueryService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

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

        assertThat(this.transverseQueryService.isFunction(function)).isTrue();
        assertThat(this.transverseQueryService.isFunction(functionalChain)).isFalse();

        assertThat(this.transverseQueryService.isFunctionalChain(functionalChain)).isTrue();
        assertThat(this.transverseQueryService.isFunctionalChain(function)).isFalse();

        assertThat(this.transverseQueryService.isFunctionalExchange(functionalExchange)).isTrue();
        assertThat(this.transverseQueryService.isFunctionalExchange(function)).isFalse();

        assertThat(this.transverseQueryService.isExchangeItem(exchangeItem)).isTrue();
        assertThat(this.transverseQueryService.isExchangeItem(untypedItemUsage)).isFalse();
    }

    @Test
    @DisplayName("GIVEN nested function usages, WHEN querying sub-functions and parent function, THEN the function hierarchy is resolved")
    public void testGetSubFunctionsAndGetParentFunction() {
        Package root = this.fixture.createRootPackage();
        ActionUsage parentFunction = this.fixture.createArcadiaTypedFunction(root, "Parent Function");
        ActionUsage subFunction1 = this.fixture.createArcadiaTypedFunction(parentFunction, "Sub Function 1");
        ActionUsage subFunction2 = this.fixture.createArcadiaTypedFunction(parentFunction, "Sub Function 2");
        this.fixture.createArcadiaTypedFunctionalChain(parentFunction, "Nested Chain");

        assertThat(this.transverseQueryService.getSubFunctions(parentFunction)).containsExactlyInAnyOrder(subFunction1, subFunction2);
        assertThat(this.transverseQueryService.getParentFunction(subFunction1)).contains(parentFunction);
        assertThat(this.transverseQueryService.getParentFunction(parentFunction)).isEmpty();
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

        this.transverseMutationService.setPerformAction(component1, allocatedFunction1);
        this.transverseMutationService.setPerformAction(component2, allocatedFunction2);

        assertThat(this.transverseQueryService.getAllocatedFunctions(component1)).containsExactly(allocatedFunction1);
        assertThat(this.transverseQueryService.getAllocatedFunctions(component2)).containsExactly(allocatedFunction2);
        assertThat(this.transverseQueryService.getAllocatingComponent(allocatedFunction1)).contains(component1);
        assertThat(this.transverseQueryService.getAllocatingComponent(unallocatedFunction)).isEmpty();
    }

    @Test
    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    @DisplayName("GIVEN functional exchanges around a function, WHEN querying incoming and outgoing exchanges, THEN source and target direction is respected")
    public void testGetReferencingAndReferencedFunctionalExchange() {
        Package root = this.fixture.createRootPackage();
        ActionUsage function1 = this.fixture.createArcadiaTypedFunction(root, "Function 1");
        ActionUsage function2 = this.fixture.createArcadiaTypedFunction(root, "Function 2");
        ActionUsage function3 = this.fixture.createArcadiaTypedFunction(root, "Function 3");
        FlowUsage outgoingFlow = this.transverseMutationService.createFunctionalExchange(function1, function2);
        outgoingFlow.setDeclaredName("Flow 1");
        FlowUsage incomingFlow = this.transverseMutationService.createFunctionalExchange(function3, function1);
        incomingFlow.setDeclaredName("Flow 2");

        assertThat(this.transverseQueryService.getOutgoingFunctionalExchanges(function1)).containsExactly(outgoingFlow);
        assertThat(this.transverseQueryService.getIncomingFunctionalExchanges(function1)).containsExactly(incomingFlow);
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

        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(flow12)).containsExactlyInAnyOrder(chainA, chainB);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(function2)).containsExactlyInAnyOrder(chainA, chainB, chainC);
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

        assertThat(this.transverseQueryService.getStructurePackage(function)).isPresent().get().isSameAs(structurePackage);
        assertThat(this.transverseQueryService.getFunctionsPackage(function)).isPresent().get().isSameAs(functionsPackage);
        assertThat(this.transverseQueryService.getRequirementsPackage(function)).isPresent().get().isSameAs(requirementsPackage);
    }

    @Test
    @DisplayName("GIVEN status-kind enumeration in editing context, WHEN querying status kind values, THEN enum usages and literals are returned")
    public void testGetStatusKindEnumAndLiterals() {
        Package root = this.fixture.createRootPackage();
        this.fixture.createStatusKindEnumeration(root, List.of("Draft", "Reviewed", "Validated"));
        EditingContext editingContext = this.fixture.createEditingContext(root.eResource(), "LAQueryServiceTests");

        try {
            assertThat(this.transverseQueryService.getStatusKindEnum(editingContext))
                    .extracting(EnumerationUsage::getDeclaredName)
                    .containsExactlyInAnyOrder("Draft", "Reviewed", "Validated");
            assertThat(this.transverseQueryService.getStatusKindEnumLiterals(editingContext))
                    .containsExactlyInAnyOrder("Draft", "Reviewed", "Validated");
        } finally {
            editingContext.dispose();
        }
    }
}

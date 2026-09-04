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

package org.eclipse.capella.model.transverse.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Objects;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests the creation of semantic elements.
 *
 * @author gdaniel
 */
public class ElementCreationTests extends AbstractSemanticTests {

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    @Test
    public void createComponentShouldCreateNonActorComponentInParent() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component = this.transverseMutationService.createComponent(parent);
        assertThat(parent.getOwnedElement()).contains(component);
        assertThat(this.transverseQueryService.isComponent(component)).isTrue();
        assertThat(this.transverseQueryService.isComponentActor(component)).isFalse();
    }

    @Test
    public void createActorShouldCreateActorComponentInParent() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage actor = this.transverseMutationService.createActor(parent);
        assertThat(parent.getOwnedElement()).contains(actor);
        assertThat(this.transverseQueryService.isComponent(actor)).isTrue();
        assertThat(this.transverseQueryService.isComponentActor(actor)).isTrue();
    }

    @Test
    public void createFunctionWhenParentIsNotAFunctionShouldCreateItInTheRootFunctionOfEachArchitecture() {
        ActionUsage function1 = this.transverseMutationService.createFunction(this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement());
        assertThat(function1.getOwner()).isEqualTo(this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement());

        ActionUsage function2 = this.transverseMutationService.createFunction(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement());
        assertThat(function2.getOwner()).isEqualTo(this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement());

        ActionUsage function3 = this.transverseMutationService.createFunction(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        assertThat(function3.getOwner()).isEqualTo(this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement());
    }

    @Test
    public void createFunctionWhenParentIsFunctionShouldCreateTheFunctionInParentFunction() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);

        assertThat(rootFunction.getOwnedElement()).contains(function1);

        ActionUsage function2 = this.transverseMutationService.createFunction(function1);
        assertThat(function1.getOwnedElement()).contains(function2);
    }

    @Test
    public void createFunctionWhenParentIsComponentShouldAllocateTheFunctionToTheComponent() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component = this.transverseMutationService.createComponent(structurePackage);
        ActionUsage function = this.transverseMutationService.createFunction(component);

        assertThat(this.transverseQueryService.getAllocatingComponent(function))
                .isPresent()
                .get()
                .isEqualTo(component);
        assertThat(this.transverseQueryService.getAllocatedFunctions(component)).contains(function);
    }

    @Test
    public void createFunctionWhenParentIsAllocatedFunctionShouldAllocateTheFunctionToItsParentAllocatingComponent() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component = this.transverseMutationService.createComponent(structurePackage);
        ActionUsage function1 = this.transverseMutationService.createFunction(component);
        ActionUsage function2 = this.transverseMutationService.createFunction(function1);

        assertThat(this.transverseQueryService.getAllocatingComponent(function2))
                .isPresent()
                .get()
                .isEqualTo(component);
        assertThat(this.transverseQueryService.getAllocatedFunctions(component)).contains(function1, function2);
    }

    @Test
    public void createFunctionPortShouldSetPortDirectionAndName() {
        ActionUsage function = this.transverseMutationService.createFunction(this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement());

        ItemUsage inPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.IN);
        assertThat(inPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(inPort.getDeclaredName()).startsWith("FIP ");

        ItemUsage outPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.OUT);
        assertThat(outPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(outPort.getDeclaredName()).startsWith("FOP ");

        ItemUsage inOutPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.INOUT);
        assertThat(inOutPort.getDirection()).isEqualTo(FeatureDirectionKind.INOUT);
        assertThat(inOutPort.getDeclaredName()).startsWith("FP ");
    }

    @Test
    public void createComponentExchangeWhenEndpointsAreComponentsShouldCreateAndConnectPorts() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PartUsage component2 = this.transverseMutationService.createComponent(parent);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component2);

        assertThat(this.transverseQueryService.getComponentExchangeSource(componentExchange))
                .matches(this.transverseQueryService::isComponentPort)
                .matches(sourcePort -> Objects.equals(sourcePort.getDirection(), FeatureDirectionKind.OUT))
                .matches(sourcePort -> Objects.equals(sourcePort.getOwner(), component1));

        assertThat(this.transverseQueryService.getComponentExchangeTarget(componentExchange))
                .matches(this.transverseQueryService::isComponentPort)
                .matches(targetPort -> Objects.equals(targetPort.getDirection(), FeatureDirectionKind.IN))
                .matches(targetPort -> Objects.equals(targetPort.getOwner(), component2));

        assertThat(componentExchange.getOwner()).isEqualTo(parent);
    }

    @Test
    public void createComponentExchangeWhenEndpointsArePortsShouldConnectTheProvidedPorts() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PortUsage port1 = this.transverseMutationService.createComponentPort(component1, FeatureDirectionKind.OUT);
        PartUsage component2 = this.transverseMutationService.createComponent(parent);
        PortUsage port2 = this.transverseMutationService.createComponentPort(component2, FeatureDirectionKind.IN);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(port1, port2);

        assertThat(this.transverseQueryService.getComponentExchangeSource(componentExchange)).isEqualTo(port1);
        assertThat(this.transverseQueryService.getComponentExchangeTarget(componentExchange)).isEqualTo(port2);
        assertThat(componentExchange.getOwner()).isEqualTo(parent);
    }

    @Test
    public void createComponentExchangeWhenEndpointsAreTheSameComponentShouldNotCreateComponentExchangeAndPorts() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component1);

        assertThat(componentExchange).isNull();
        assertThat(component1.getNestedPort()).isEmpty();
    }

    @Test
    public void createComponentExchangeWhenEndpointPortsBelongToSameComponentShouldNotCreateComponentExchange() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PortUsage port1 = this.transverseMutationService.createComponentPort(component1, FeatureDirectionKind.OUT);
        PortUsage port2 = this.transverseMutationService.createComponentPort(component1, FeatureDirectionKind.IN);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(port1, port2);

        assertThat(componentExchange).isNull();
        assertThat(component1.getNestedPort()).hasSize(2);
    }

    @Test
    public void createComponentExchangeWhenComponentsBelongToDifferentStructurePackagesShouldNotCreateComponentExchangeAndPorts() {
        Package logicalArchitectureStructurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(logicalArchitectureStructurePackage);
        Package systemAnalysisStructurePackage = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        PartUsage component2 = this.transverseMutationService.createComponent(systemAnalysisStructurePackage);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component2);

        assertThat(componentExchange).isNull();
        assertThat(component1.getNestedPort()).isEmpty();
        assertThat(component2.getNestedPort()).isEmpty();
    }

    @Test
    public void createFunctionalExchangeWhenEndpointsAreFunctionsShouldCreateAndConnectPorts() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(function1, function2);

        assertThat(this.transverseQueryService.getFunctionalExchangeSource(functionalExchange))
                .matches(this.transverseQueryService::isFunctionPort)
                .matches(sourcePort -> Objects.equals(((ItemUsage) sourcePort).getDirection(), FeatureDirectionKind.OUT))
                .matches(sourcePort -> Objects.equals(sourcePort.getOwner(), function1));

        assertThat(this.transverseQueryService.getFunctionalExchangeTarget(functionalExchange))
                .matches(this.transverseQueryService::isFunctionPort)
                .matches(targetPort -> Objects.equals(((ItemUsage) targetPort).getDirection(), FeatureDirectionKind.IN))
                .matches(targetPort -> Objects.equals(targetPort.getOwner(), function2));

        assertThat(functionalExchange.getOwner()).isEqualTo(rootFunction);
    }

    @Test
    public void createFunctionalExchangeWhenEndpointsAreSubFunctionsShouldCreateFunctionalExchangeInCommonAncestor() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function11 = this.transverseMutationService.createFunction(function1);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function21 = this.transverseMutationService.createFunction(function2);
        ActionUsage function211 = this.transverseMutationService.createFunction(function21);
        FlowUsage functionalExchange1 = this.transverseMutationService.createFunctionalExchange(function11, function211);
        assertThat(functionalExchange1.getOwner()).isEqualTo(rootFunction);

        ActionUsage function212 = this.transverseMutationService.createFunction(function21);
        FlowUsage functionalExchange2 = this.transverseMutationService.createFunctionalExchange(function211, function212);
        assertThat(functionalExchange2.getOwner()).isEqualTo(function21);
    }

    @Test
    public void createFunctionalExchangeWhenEndpointsArePortsShouldConnectTheProvidedPorts() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ItemUsage port1 = this.transverseMutationService.createFunctionPort(function1, FeatureDirectionKind.OUT);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        ItemUsage port2 = this.transverseMutationService.createFunctionPort(function2, FeatureDirectionKind.IN);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(port1, port2);

        assertThat(this.transverseQueryService.getFunctionalExchangeSource(functionalExchange)).isEqualTo(port1);
        assertThat(this.transverseQueryService.getFunctionalExchangeTarget(functionalExchange)).isEqualTo(port2);
        assertThat(functionalExchange.getOwner()).isEqualTo(rootFunction);
    }

    @Test
    public void createFunctionalExchangeWhenEndpointsAreTheSameFunctionShouldNotCreateFunctionalExchangeAndPorts() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(function1, function1);

        assertThat(functionalExchange).isNull();
        assertThat(function1.getNestedItem()).isEmpty();
    }

    @Test
    public void createFunctionalExchangeWhenEndpointPortsBelongToSameFunctionShouldNotCreateFunctionalExchange() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ItemUsage port1 = this.transverseMutationService.createFunctionPort(function1, FeatureDirectionKind.OUT);
        ItemUsage port2 = this.transverseMutationService.createFunctionPort(function1, FeatureDirectionKind.IN);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(port1, port2);

        assertThat(functionalExchange).isNull();
        assertThat(function1.getNestedItem()).hasSize(2);
    }

    @Test
    public void createFunctionalExchangeWhenFunctionsBelongToDifferentFunctionsPackagesShouldNotCreateFunctionalExchangeAndPorts() {
        ActionUsage logicalArchitectureRootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(logicalArchitectureRootFunction);
        ActionUsage systemAnalysisRootFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function2 = this.transverseMutationService.createFunction(systemAnalysisRootFunction);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(function1, function2);

        assertThat(functionalExchange).isNull();
        assertThat(function1.getNestedItem()).isEmpty();
        assertThat(function2.getNestedItem()).isEmpty();
    }

    @Test
    public void createFunctionalChainOnFunctionalExchangesShouldCreateAFunctionalChainWithExchangesInTheProvidedOrder() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();

        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange1 = this.transverseMutationService.createFunctionalExchange(function1, function2);

        ActionUsage function3 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange2 = this.transverseMutationService.createFunctionalExchange(function2, function3);

        // functional exchange not involved in the chain.
        FlowUsage functionalExchange3 = this.transverseMutationService.createFunctionalExchange(function3, function2);

        // Functional chains are usually created on the diagram background, so the first argument is the diagram's semantic element: the structure package.
        ActionUsage functionalChain = this.transverseMutationService.createFunctionalChain(structurePackage, List.of(functionalExchange1, functionalExchange2));

        assertThat(this.transverseQueryService.isFunctionalChain(functionalChain)).isTrue();
        assertThat(rootFunction.getOwnedElement()).contains(functionalChain);
        assertThat(this.transverseQueryService.getInvolvedFunctionalExchanges(functionalChain)).containsExactly(functionalExchange1, functionalExchange2);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(functionalExchange1)).containsExactly(functionalChain);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(functionalExchange2)).containsExactly(functionalChain);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(function1)).contains(functionalChain);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(function2)).contains(functionalChain);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(function3)).contains(functionalChain);
        assertThat(this.transverseQueryService.getFunctionalChainsImpliedIn(functionalExchange3)).isEmpty();
    }

}

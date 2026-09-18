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

import org.eclipse.capella.tests.fixtures.FunctionsPackage;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests the deletion of semantic elements.
 *
 * @author gdaniel
 */
public class ElementDeletionTests extends org.eclipse.capella.tests.semantic.AbstractSemanticTests {

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final CommonUpdateService commonUpdateService = new CommonUpdateService();

    private final CommonDeletionService commonDeletionService = new CommonDeletionService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    @Test
    public void deleteComponentExchangeSourcePortShouldDeleteComponentExchange() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.commonCreationService.createComponent(parent);
        PartUsage component2 = this.commonCreationService.createComponent(parent);
        InterfaceUsage componentExchange = this.commonCreationService.createComponentExchange(component1, component2);
        PortUsage sourcePort = this.commonQueryService.getComponentExchangeSource(componentExchange);
        assertThat(this.commonQueryService.isComponentPort(sourcePort)).isTrue();
        assertThat(parent.getOwnedElement()).contains(componentExchange);

        this.commonDeletionService.delete(sourcePort);
        assertThat(parent.getOwnedElement()).doesNotContain(componentExchange);
    }

    @Test
    public void deleteComponentExchangeTargetPortShouldDeleteComponentExchange() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.commonCreationService.createComponent(parent);
        PartUsage component2 = this.commonCreationService.createComponent(parent);
        InterfaceUsage componentExchange = this.commonCreationService.createComponentExchange(component1, component2);
        PortUsage targetPort = this.commonQueryService.getComponentExchangeTarget(componentExchange);
        assertThat(this.commonQueryService.isComponentPort(targetPort)).isTrue();
        assertThat(parent.getOwnedElement()).contains(componentExchange);

        this.commonDeletionService.delete(targetPort);
        assertThat(parent.getOwnedElement()).doesNotContain(componentExchange);
    }

    @Test
    public void deleteComponentExchangeShouldNotDeleteConnectedComponents() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.commonCreationService.createComponent(parent);
        PartUsage component2 = this.commonCreationService.createComponent(parent);
        InterfaceUsage componentExchange = this.commonCreationService.createComponentExchange(component1, component2);

        this.commonDeletionService.delete(componentExchange);
        assertThat(this.commonQueryService.getComponents(parent)).contains(component1, component2);
        assertThat(parent.getOwnedElement())
                .contains(component1, component2)
                .doesNotContain(componentExchange);
    }

    @Test
    public void deleteCapabilityInvolvementShouldRemoveTheLastInvolvedComponentReference() {
        var perspective = this.capellaModel.getOperationalAnalysisPerspective();
        var capability = this.commonCreationService.createOperationalCapability(perspective.getElement());
        var component = this.commonCreationService.createComponent(perspective.getStructurePackage().getElement());
        this.commonCreationService.createCapabilityInvolvement(capability, component);

        var result = this.commonDeletionService.deleteCapabilityInvolvement(capability, component);

        assertThat(result).isSameAs(capability);
        assertThat(this.commonQueryService.getInvolvedComponents(capability)).isEmpty();
    }

    @Test
    public void deleteComponentExchangeBetweenSubComponentsShouldRemoveTheComponentExchangeFromTheParentComponent() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage parentComponent = this.commonCreationService.createComponent(parent);
        PartUsage component1 = this.commonCreationService.createComponent(parentComponent);
        PartUsage component2 = this.commonCreationService.createComponent(parentComponent);
        InterfaceUsage componentExchange = this.commonCreationService.createComponentExchange(component1, component2);
        assertThat(parentComponent.getOwnedElement()).contains(componentExchange);

        this.commonDeletionService.delete(componentExchange);
        assertThat(parentComponent.getOwnedElement()).doesNotContain(componentExchange);
    }

    @Test
    public void deleteFunctionalExchangeSourcePortShouldDeleteFunctionalExchange() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.commonCreationService.createFunction(rootFunction);
        ActionUsage function2 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.commonCreationService.createFunctionalExchange(function1, function2);
        Element sourcePort = this.commonQueryService.getFunctionalExchangeSource(functionalExchange);
        assertThat(this.commonQueryService.isFunctionPort(sourcePort)).isTrue();
        assertThat(rootFunction.getOwnedElement()).contains(functionalExchange);

        this.commonDeletionService.delete(sourcePort);
        assertThat(rootFunction.getOwnedElement()).doesNotContain(functionalExchange);
    }

    @Test
    public void deleteFunctionalExchangeTargetPortShouldDeleteFunctionalExchange() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.commonCreationService.createFunction(rootFunction);
        ActionUsage function2 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.commonCreationService.createFunctionalExchange(function1, function2);
        Element targetPort = this.commonQueryService.getFunctionalExchangeTarget(functionalExchange);
        assertThat(this.commonQueryService.isFunctionPort(targetPort)).isTrue();
        assertThat(rootFunction.getOwnedElement()).contains(functionalExchange);

        this.commonDeletionService.delete(targetPort);
        assertThat(rootFunction.getOwnedElement()).doesNotContain(functionalExchange);
    }

    @Test
    public void deleteFunctionalExchangeShouldNotDeleteConnectedFunctions() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage function1 = this.commonCreationService.createFunction(rootFunction);
        ActionUsage function2 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.commonCreationService.createFunctionalExchange(function1, function2);

        this.commonDeletionService.delete(functionalExchange);
        assertThat(this.commonQueryService.getFunctions(rootFunction)).contains(function1, function2);
        assertThat(rootFunction.getOwnedElement()).contains(function1, function2);
        assertThat(rootFunction.getOwnedElement()).doesNotContain(functionalExchange);
    }

    @Test
    public void deleteFunctionalExchangeShouldRemoveItFromTheFunctionalChainsInvolvingIt() {
        FunctionsPackage functionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage();
        ActionUsage rootFunction = functionsPackage.getRootFunction().getElement();

        ActionUsage function1 = this.commonCreationService.createFunction(rootFunction);
        ActionUsage function2 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange1 = this.commonCreationService.createFunctionalExchange(function1, function2);

        ActionUsage function3 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange2 = this.commonCreationService.createFunctionalExchange(function2, function3);

        // functional exchange not involved in the chain.
        FlowUsage functionalExchange3 = this.commonCreationService.createFunctionalExchange(function3, function2);

        ActionUsage functionalChain = this.commonCreationService.createFunctionalChain(functionsPackage.getElement(), List.of(functionalExchange1, functionalExchange2));
        assertThat(this.commonQueryService.getInvolvedFunctionalExchanges(functionalChain)).containsExactly(functionalExchange1, functionalExchange2);

        // Deleting an unrelated functional exchange doesn't change the functional chain.
        this.commonDeletionService.delete(functionalExchange3);
        assertThat(this.commonQueryService.getInvolvedFunctionalExchanges(functionalChain)).containsExactly(functionalExchange1, functionalExchange2);

        this.commonDeletionService.delete(functionalExchange1);
        assertThat(this.commonQueryService.getInvolvedFunctionalExchanges(functionalChain)).containsExactly(functionalExchange2);

        this.commonDeletionService.delete(functionalExchange2);
        assertThat(this.commonQueryService.getInvolvedFunctionalExchanges(functionalChain)).isEmpty();
    }

    @Test
    public void deleteFunctionalChainShouldNotDeleteTheInvolvedFunctionalExchangesAndFunctions() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();

        ActionUsage function1 = this.commonCreationService.createFunction(rootFunction);
        ActionUsage function2 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange1 = this.commonCreationService.createFunctionalExchange(function1, function2);

        ActionUsage functionalChain = this.commonCreationService.createFunctionalChain(structurePackage, List.of(functionalExchange1));

        this.commonDeletionService.delete(functionalChain);

        assertThat(rootFunction.getOwnedElement()).contains(function1, function2, functionalExchange1);
    }

    @Test
    public void deleteFunctionalChainShouldRemoveTheReferenceToTheFunctionalChainFromTheInvolvedFunctionalExchangesAndFunctions() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();

        ActionUsage function1 = this.commonCreationService.createFunction(rootFunction);
        ActionUsage function2 = this.commonCreationService.createFunction(rootFunction);
        FlowUsage functionalExchange1 = this.commonCreationService.createFunctionalExchange(function1, function2);

        ActionUsage functionalChain = this.commonCreationService.createFunctionalChain(structurePackage, List.of(functionalExchange1));

        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(functionalExchange1)).containsExactly(functionalChain);
        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(function1)).containsExactly(functionalChain);
        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(function2)).containsExactly(functionalChain);

        this.commonDeletionService.delete(functionalChain);

        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(functionalExchange1)).isEmpty();
        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(function1)).isEmpty();
        assertThat(this.commonQueryService.getFunctionalChainsImpliedIn(function2)).isEmpty();
    }
}

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
public class ElementDeletionTests extends AbstractSemanticTests {

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    @Test
    public void deleteComponentExchangeSourcePortShouldDeleteComponentExchange() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PartUsage component2 = this.transverseMutationService.createComponent(parent);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component2);
        PortUsage sourcePort = this.transverseQueryService.getComponentExchangeSource(componentExchange);
        assertThat(this.transverseQueryService.isComponentPort(sourcePort)).isTrue();
        assertThat(parent.getOwnedElement()).contains(componentExchange);

        this.transverseMutationService.delete(sourcePort);
        assertThat(parent.getOwnedElement()).doesNotContain(componentExchange);
    }

    @Test
    public void deleteComponentExchangeTargetPortShouldDeleteComponentExchange() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PartUsage component2 = this.transverseMutationService.createComponent(parent);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component2);
        PortUsage targetPort = this.transverseQueryService.getComponentExchangeTarget(componentExchange);
        assertThat(this.transverseQueryService.isComponentPort(targetPort)).isTrue();
        assertThat(parent.getOwnedElement()).contains(componentExchange);

        this.transverseMutationService.delete(targetPort);
        assertThat(parent.getOwnedElement()).doesNotContain(componentExchange);
    }

    @Test
    public void deleteComponentExchangeShouldNotDeleteConnectedComponents() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PartUsage component2 = this.transverseMutationService.createComponent(parent);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component2);

        this.transverseMutationService.delete(componentExchange);
        assertThat(this.transverseQueryService.getComponents(parent)).contains(component1, component2);
        assertThat(parent.getOwnedElement())
                .contains(component1, component2)
                .doesNotContain(componentExchange);
    }

    @Test
    public void deleteFunctionalExchangeSourcePortShouldDeleteFunctionalExchange() {
        FunctionsPackage functionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage();
        ActionUsage rootFunction = functionsPackage.getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(function1, function2);
        Element sourcePort = this.transverseQueryService.getFunctionalExchangeSource(functionalExchange);
        assertThat(this.transverseQueryService.isFunctionPort(sourcePort)).isTrue();
        assertThat(functionsPackage.getElement().getOwnedElement()).contains(functionalExchange);

        this.transverseMutationService.delete(sourcePort);
        assertThat(functionsPackage.getElement().getOwnedElement()).doesNotContain(functionalExchange);
    }

    @Test
    public void deleteFunctionalExchangeTargetPortShouldDeleteFunctionalExchange() {
        FunctionsPackage functionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage();
        ActionUsage rootFunction = functionsPackage.getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(function1, function2);
        Element targetPort = this.transverseQueryService.getFunctionalExchangeTarget(functionalExchange);
        assertThat(this.transverseQueryService.isFunctionPort(targetPort)).isTrue();
        assertThat(functionsPackage.getElement().getOwnedElement()).contains(functionalExchange);

        this.transverseMutationService.delete(targetPort);
        assertThat(functionsPackage.getElement().getOwnedElement()).doesNotContain(functionalExchange);
    }

    @Test
    public void deleteFunctionalExchangeShouldNotDeleteConnectedFunctions() {
        FunctionsPackage functionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage();
        ActionUsage rootFunction = functionsPackage.getRootFunction().getElement();
        ActionUsage function1 = this.transverseMutationService.createFunction(rootFunction);
        ActionUsage function2 = this.transverseMutationService.createFunction(rootFunction);
        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(function1, function2);

        this.transverseMutationService.delete(functionalExchange);
        assertThat(this.transverseQueryService.getFunctions(functionsPackage.getElement())).contains(function1, function2);
        assertThat(rootFunction.getOwnedElement()).contains(function1, function2);
        assertThat(functionsPackage.getElement().getOwnedElement()).doesNotContain(functionalExchange);
    }
}

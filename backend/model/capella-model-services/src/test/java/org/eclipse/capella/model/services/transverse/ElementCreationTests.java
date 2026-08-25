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

import java.util.Objects;

import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.InterfaceUsage;
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
    public void createFunctionShouldCreateItInTheRootFunctionOfEachArchitecture() {
        this.assertFunctionIsCreatedInRootFunction(
                this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement(),
                this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getElement());
        this.assertFunctionIsCreatedInRootFunction(
                this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement(),
                this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getElement());
        this.assertFunctionIsCreatedInRootFunction(
                this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement(),
                this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getElement());
    }

    @Test
    public void createComponentExchangeWhenEndpointsAreComponentsShouldCreateAndConnectPorts() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.transverseMutationService.createComponent(parent);
        PartUsage component2 = this.transverseMutationService.createComponent(parent);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(component1, component2);

        assertThat(this.transverseQueryService.getComponentExchangeSource(componentExchange))
                .matches(this.transverseQueryService::isComponentPort)
                .matches(sourcePort -> Objects.equals(sourcePort.getOwner(), component1));

        assertThat(this.transverseQueryService.getComponentExchangeTarget(componentExchange))
                .matches(this.transverseQueryService::isComponentPort)
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

    private void assertFunctionIsCreatedInRootFunction(Package structurePackage, Package functionsPackage) {
        ActionUsage rootFunction = functionsPackage.getOwnedElement().stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.transverseQueryService::isFunction)
                .findFirst()
                .orElseThrow();

        ActionUsage function = this.transverseMutationService.createFunction(structurePackage);

        assertThat(rootFunction.getNestedAction()).contains(function);
    }

}

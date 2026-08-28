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

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PayloadFeature;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.ReferenceSubsetting;
import org.eclipse.syson.sysml.SysmlPackage;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LAMutationService}.
 *
 * @author fbarbin
 */
public class LAMutationServiceTests extends AbstractSemanticTests {

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final LAMutationService laMutationService = new LAMutationService();

    private final LATestModelFixture fixture = new LATestModelFixture();

    @Test
    public void createComponentFromLABRootShouldAddComponentToSystem() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage system = this.getSystem(structurePackage);

        PartUsage createdComponent = this.laMutationService.createComponentLA(structurePackage);

        assertThat(system.getOwnedElement()).contains(createdComponent);
        assertThat(structurePackage.getOwnedElement()).doesNotContain(createdComponent);
    }

    @Test
    public void createComponentInsideExistingComponentShouldAddItToComponent() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage system = this.getSystem(structurePackage);
        PartUsage partUsage = this.laMutationService.createComponentLA(system);

        PartUsage createdComponent = this.laMutationService.createComponentLA(partUsage);

        assertThat(partUsage.getOwnedElement()).contains(createdComponent);
        assertThat(structurePackage.getOwnedElement()).doesNotContain(createdComponent);
    }

    @Test
    public void setPerformActionShouldAllocateFunction() {
        Package root = this.fixture.createRootPackage();
        PartUsage component = this.fixture.createArcadiaTypedComponent(root, "Component");
        ActionUsage function = this.fixture.createArcadiaTypedFunction(root, "Function 1");

        this.transverseMutationService.setPerformAction(component, function);

        List<ActionUsage> allocatedFunctions = this.getAllocatedFunctions(component);
        assertThat(allocatedFunctions).containsExactly(function);
    }


    @Test
    public void deletePerformedActionUsageShouldRemoveOnlySelectedAllocation() {
        Package root = this.fixture.createRootPackage();
        PartUsage component = this.fixture.createArcadiaTypedComponent(root, "Component");
        ActionUsage function1 = this.fixture.createArcadiaTypedFunction(root, "Function 1");
        ActionUsage function2 = this.fixture.createArcadiaTypedFunction(root, "Function 2");

        this.transverseMutationService.setPerformAction(component, function1);
        this.transverseMutationService.setPerformAction(component, function2);
        this.transverseMutationService.deletePerformedActionUsage(component, function1);

        List<ActionUsage> allocatedFunctions = this.getAllocatedFunctions(component);
        assertThat(allocatedFunctions).containsExactly(function2);
    }

    @Test
    public void deleteFunctionShouldRemoveItsAllocationAndRetainOtherFunctions() {
        Package root = this.fixture.createRootPackage();
        this.fixture.attachCrossReferenceAdapter(root);

        PartUsage component = this.fixture.createArcadiaTypedComponent(root, "Component");
        ActionUsage functionToDelete = this.fixture.createArcadiaTypedFunction(root, "Function To Delete");
        ActionUsage functionToKeep = this.fixture.createArcadiaTypedFunction(root, "Function To Keep");
        this.transverseMutationService.setPerformAction(component, functionToDelete);
        this.transverseMutationService.setPerformAction(component, functionToKeep);

        this.transverseMutationService.delete(functionToDelete);

        assertThat(root.getOwnedElement()).doesNotContain(functionToDelete);
        assertThat(root.getOwnedElement()).contains(functionToKeep);
        assertThat(this.getAllocatedFunctions(component)).containsExactly(functionToKeep);
    }

    @Test
    public void setFunctionalExchangePayloadShouldReplaceExistingPayload() {
        Package root = this.fixture.createRootPackage();
        FlowUsage functionalExchange = this.fixture.createFlowUsage(root, "Functional Exchange");
        ItemUsage exchangeItem1 = this.fixture.createArcadiaTypedExchangeItem(root, "Exchange Item 1");
        ItemUsage exchangeItem2 = this.fixture.createArcadiaTypedExchangeItem(root, "Exchange Item 2");
        ItemUsage exchangeItem3 = this.fixture.createArcadiaTypedExchangeItem(root, "Exchange Item 3");

        this.transverseMutationService.setFunctionalExchangePayload(functionalExchange, exchangeItem1);
        PayloadFeature firstPayloadFeature = functionalExchange.getPayloadFeature();
        assertThat(firstPayloadFeature).isNotNull();
        assertThat(this.fixture.getPayloadFeatureTypedItems(functionalExchange)).containsExactly(exchangeItem1);

        this.transverseMutationService.setFunctionalExchangePayload(functionalExchange, List.of(exchangeItem2, exchangeItem3));

        assertThat(functionalExchange.getPayloadFeature()).isNotNull();
        assertThat(functionalExchange.getPayloadFeature()).isNotSameAs(firstPayloadFeature);
        assertThat(firstPayloadFeature.eResource()).isNull();
        assertThat(this.fixture.getPayloadFeatureTypedItems(functionalExchange)).containsExactlyInAnyOrder(exchangeItem2, exchangeItem3);
    }

    @Test
    public void setStatusKindThenUnSetUsageStatusKindShouldCreateThenRemoveStatusMetadata() {
        Package root = this.fixture.createRootPackage("ModelingMetadata");
        this.fixture.createStatusKindEnumeration(root, List.of("Draft", "Reviewed"));
        this.fixture.createModelingMetadataLibrary(root);
        ActionUsage function = this.fixture.createArcadiaTypedFunction(root, "Function 1");

        LAQueryService laQueryService = new LAQueryService();
        TransverseQueryService transverseQueryService = new TransverseQueryService();
        EditingContext editingContext = this.fixture.createEditingContext(root.eResource(), "LAMutationServiceTests");

        try {
            this.transverseMutationService.setStatusKind(function, "Draft", editingContext);

            assertThat(transverseQueryService.getStatus(function))
                    .extracting(Element::getDeclaredName)
                    .isEqualTo("Draft");
            assertThat(function.getOwnedElement().stream()
                    .filter(MetadataUsage.class::isInstance)
                    .map(MetadataUsage.class::cast)
                    .filter(transverseQueryService::isStatusInfo)
                    .count()).isEqualTo(1);

            this.transverseMutationService.unSetUsageStatusKind(function);

            assertThat(transverseQueryService.getStatus(function)).isNull();
            assertThat(function.getOwnedElement().stream()
                    .filter(MetadataUsage.class::isInstance)
                    .map(MetadataUsage.class::cast)
                    .filter(transverseQueryService::isStatusInfo))
                    .isEmpty();
        } finally {
            editingContext.dispose();
        }
    }

    @Test
    public void setArcadiaReferenceFeatureShouldReplaceReferenceValues() {
        Package root = this.fixture.createRootPackage();
        ActionUsage functionalChain = this.fixture.createArcadiaTypedFunctionalChain(root, "Functional Chain");

        String referencePrefix = "Arcadia::FunctionalChain";
        String referenceName = "involvedFunctionalExchanges";
        this.fixture.createLibraryFlowUsageReference(root, "FunctionalChain", referenceName);

        FlowUsage functionalExchange1 = this.fixture.createFlowUsage(root, "Functional Exchange 1");
        FlowUsage functionalExchange2 = this.fixture.createFlowUsage(root, "Functional Exchange 2");
        FlowUsage functionalExchange3 = this.fixture.createFlowUsage(root, "Functional Exchange 3");

        TransverseQueryService transverseQueryService = new TransverseQueryService();

        this.transverseMutationService.setArcadiaReferenceFeature(functionalChain, referencePrefix, referenceName, functionalExchange1, SysmlPackage.eINSTANCE.getFlowUsage().getName());
        assertThat(transverseQueryService.getFeatureReferenceValue(functionalChain, referenceName)).containsExactly(functionalExchange1);

        this.transverseMutationService.setArcadiaReferenceFeature(functionalChain, referencePrefix, referenceName, List.of(functionalExchange2, functionalExchange3),
                SysmlPackage.eINSTANCE.getFlowUsage().getName());
        assertThat(transverseQueryService.getFeatureReferenceValue(functionalChain, referenceName)).containsExactlyInAnyOrder(functionalExchange2, functionalExchange3);
    }

    private List<ActionUsage> getAllocatedFunctions(PartUsage component) {
        return component.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .map(performActionUsage -> performActionUsage.getOwnedRelationship().stream()
                        .filter(ReferenceSubsetting.class::isInstance)
                        .map(ReferenceSubsetting.class::cast)
                        .findFirst()
                        .map(ReferenceSubsetting::getReferencedFeature)
                        .orElse(null))
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .toList();
    }

    private PartUsage getSystem(Package structurePackage) {
        return structurePackage.getOwnedElement().stream()
                .filter(transverseQueryService::isComponent)
                .map(PartUsage.class::cast)
                .findFirst()
                .orElseThrow();
    }
}

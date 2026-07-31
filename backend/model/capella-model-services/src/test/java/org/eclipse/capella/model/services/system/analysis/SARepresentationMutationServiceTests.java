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
package org.eclipse.capella.model.services.system.analysis;

import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionDefinition;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowDefinition;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceDefinition;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.PortDefinition;
import org.eclipse.syson.sysml.RequirementDefinition;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the System Analysis representation mutation service.
 *
 * @author mbats
 */
public class SARepresentationMutationServiceTests {

    private final SARepresentationMutationService mutationService = new SARepresentationMutationService();

    private final SAMutationService semanticMutationService = new SAMutationService();

    @Test
    public void createSystemActorShouldNameFirstActorAOne() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var actor = this.mutationService.createSystemActor(structurePackage);

        assertEquals("A 1", actor.getDeclaredName());
    }

    @Test
    public void creationShouldRejectParentsOutsideSystemAnalysis() {
        var packageOutsideSystemAnalysis = SysmlFactory.eINSTANCE.createPackage();
        var componentOutsideSystemAnalysis = SysmlFactory.eINSTANCE.createPartUsage();

        assertNull(this.mutationService.createSystemActor(packageOutsideSystemAnalysis));
        assertNull(this.mutationService.createRequirement(packageOutsideSystemAnalysis));
        assertNull(this.mutationService.createNewFunction(componentOutsideSystemAnalysis));
    }

    @Test
    public void createSystemActorShouldCreateNestedActorUnderSelectedActor() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var actor = this.mutationService.createSystemActor(structurePackage);
        var nestedActor = this.mutationService.createSystemActor(actor);

        assertEquals("A 1", nestedActor.getDeclaredName());
        assertTrue(actor.getOwnedElement().contains(nestedActor));
    }

    @Test
    public void createSystemActorShouldNotCountNonActorComponentsForDefaultName() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .ifPresent(system -> SAQueryServiceTests.addOwnedMember(structurePackage, this.createComponent("component", (PartDefinition) system.getType().get(0))));

        var actor = this.mutationService.createSystemActor(structurePackage);

        assertEquals("A 1", actor.getDeclaredName());
    }

    @Test
    public void createSystemActorShouldRedirectCreationFromSystemOfInterestToStructurePackage() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();

        var actor = this.mutationService.createSystemActor(system);

        assertEquals("A 1", actor.getDeclaredName());
        assertTrue(structurePackage.getOwnedElement().contains(actor));
        assertFalse(system.getOwnedElement().contains(actor));
    }

    @Test
    public void createSystemComponentShouldCreateInternalComponentUnderSelectedSystemOrComponent() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);

        var component = this.mutationService.createSystemComponent(system);
        var nestedComponent = this.mutationService.createSystemComponent(component);

        assertNotNull(component);
        assertEquals("C 1", component.getDeclaredName());
        assertTrue(system.getOwnedElement().contains(component));
        assertTrue(new SAQueryService().getSystemComponents(system).contains(component));
        assertEquals("C 1", nestedComponent.getDeclaredName());
        assertTrue(component.getOwnedElement().contains(nestedComponent));
        assertTrue(new SAQueryService().getSystemComponents(component).contains(nestedComponent));
    }

    @Test
    public void createSystemComponentShouldRejectStructurePackageAndActors() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var actor = this.mutationService.createSystemActor(structurePackage);

        assertNull(this.mutationService.createSystemComponent(structurePackage));
        assertNull(this.mutationService.createSystemComponent(actor));
    }

    @Test
    public void createComponentPortsShouldCreateDirectedComponentPortsOnSelectedComponent() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();

        var inputPort = this.mutationService.createInputComponentPort(system);
        var outputPort = this.mutationService.createOutputComponentPort(system);
        var inOutPort = this.mutationService.createInOutComponentPort(system);

        assertEquals(FeatureDirectionKind.IN, inputPort.getDirection());
        assertEquals(FeatureDirectionKind.OUT, outputPort.getDirection());
        assertEquals(FeatureDirectionKind.INOUT, inOutPort.getDirection());
        assertTrue(system.getOwnedElement().contains(inputPort));
        assertTrue(system.getOwnedElement().contains(outputPort));
        assertTrue(system.getOwnedElement().contains(inOutPort));
    }

    @Test
    public void createComponentExchangeShouldAcceptAComponentActor() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var actor = this.mutationService.createSystemActor(structurePackage);
        var componentExchange = this.mutationService.createComponentExchange(system, actor);

        assertNotNull(componentExchange);
        assertEquals(structurePackage, componentExchange.getOwner());
        assertEquals("Arcadia::ComponentExchange", componentExchange.getType().get(0).getQualifiedName());
        assertEquals(system, new SAQueryService().getComponentExchangeSource(componentExchange));
        assertEquals(actor, new SAQueryService().getComponentExchangeTarget(componentExchange));
    }

    @Test
    public void createComponentExchangeShouldRejectComponentsFromDifferentSystemAnalysisStructures() {
        var sourceStructurePackage = this.createSystemAnalysisStructurePackage();
        var targetStructurePackage = this.createSystemAnalysisStructurePackage();
        var sourceSystem = this.getSystem(sourceStructurePackage);
        var targetSystem = this.getSystem(targetStructurePackage);

        assertNull(this.mutationService.createComponentExchange(sourceSystem, targetSystem));
        assertTrue(sourceSystem.getOwnedElement().isEmpty());
        assertTrue(targetSystem.getOwnedElement().isEmpty());
    }

    @Test
    public void createNewFunctionShouldCreateFunctionInFunctionsPackageAndAllocateItToSelectedComponent() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();
        var functionsPackage = structurePackage.getOwner().getOwnedElement().stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> "Functions".equals(pkg.getDeclaredName()))
                .findFirst()
                .orElseThrow();

        var function = (ActionUsage) this.mutationService.createNewFunction(system);

        assertEquals("Function 0", function.getDeclaredName());
        assertTrue(functionsPackage.getOwnedElement().contains(function));
        assertTrue(system.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .anyMatch(performActionUsage -> function.equals(performActionUsage.getPerformedAction())));
    }

    @Test
    public void createNewFunctionShouldCreateSubFunctionUnderSelectedFunction() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();
        var function = (ActionUsage) this.mutationService.createNewFunction(system);

        var subFunction = (ActionUsage) this.mutationService.createNewFunction(function);

        assertEquals("Function 0", subFunction.getDeclaredName());
        assertTrue(function.getOwnedElement().contains(subFunction));
        assertTrue(system.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .anyMatch(performActionUsage -> subFunction.equals(performActionUsage.getPerformedAction())));
    }

    @Test
    public void createFunctionPortsShouldCreateDirectedFunctionPortsOnSelectedFunction() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();
        var function = (ActionUsage) this.mutationService.createNewFunction(system);

        var inputPort = this.mutationService.createInputFunctionPort(function);
        var outputPort = this.mutationService.createOutputFunctionPort(function);

        assertEquals(FeatureDirectionKind.IN, inputPort.getDirection());
        assertEquals(FeatureDirectionKind.OUT, outputPort.getDirection());
        assertTrue(function.getOwnedElement().contains(inputPort));
        assertTrue(function.getOwnedElement().contains(outputPort));
        assertEquals("FIP 0", inputPort.getDeclaredName());
        assertEquals("FOP 0", outputPort.getDeclaredName());
    }

    @Test
    public void createFunctionalExchangeShouldConnectOutPortToInPort() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var targetFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var sourcePort = this.mutationService.createOutputFunctionPort(sourceFunction);
        var targetPort = this.mutationService.createInputFunctionPort(targetFunction);

        FlowUsage functionalExchange = this.mutationService.createFunctionalExchange(sourcePort, targetPort);

        assertNotNull(functionalExchange);
        assertEquals("FE 0", functionalExchange.getDeclaredName());
        assertEquals(sourcePort, new SAQueryService().getFunctionalExchangeSource(functionalExchange));
        assertEquals(targetPort, new SAQueryService().getFunctionalExchangeTarget(functionalExchange));
        assertEquals(functionalExchange, new SAQueryService().getFunctionalExchanges(structurePackage.getOwner()).get(0));
        assertEquals(sourcePort, new SAQueryService().getFunctionalExchangeSource(functionalExchange));
        assertEquals(targetPort, new SAQueryService().getFunctionalExchangeTarget(functionalExchange));
    }

    @Test
    public void createFunctionalExchangeShouldRejectInvalidDirections() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var targetFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var sourcePort = this.mutationService.createInputFunctionPort(sourceFunction);
        var targetPort = this.mutationService.createOutputFunctionPort(targetFunction);

        assertNull(this.mutationService.createFunctionalExchange(sourcePort, targetPort));
    }

    @Test
    public void createFunctionalExchangeShouldCreateMissingFunctionPortsWhenStartedFromFunctions() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var targetFunction = (ActionUsage) this.mutationService.createNewFunction(system);

        FlowUsage functionalExchange = this.mutationService.createFunctionalExchange(sourceFunction, targetFunction);

        assertNotNull(functionalExchange);
        var sourcePort = new SAQueryService().getFunctionalExchangeSource(functionalExchange);
        var targetPort = new SAQueryService().getFunctionalExchangeTarget(functionalExchange);
        assertEquals(FeatureDirectionKind.OUT, sourcePort.getDirection());
        assertEquals(FeatureDirectionKind.IN, targetPort.getDirection());
        assertTrue(sourceFunction.getOwnedElement().contains(sourcePort));
        assertTrue(targetFunction.getOwnedElement().contains(targetPort));
    }

    private Package createSystemAnalysisStructurePackage() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var arcadia = this.createPackage("Arcadia");
        var componentType = this.createArcadiaComponentType();
        SAQueryServiceTests.addOwnedMember(arcadia, componentType);
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaFunctionType());
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaFunctionalChainType());
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaExchangeItemType());
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaFunctionalExchangeType());
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaComponentExchangeType());
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaRequirementType());
        SAQueryServiceTests.addOwnedMember(componentType, this.createAttribute("isActor"));
        SAQueryServiceTests.addOwnedMember(componentType, this.createAttribute("isHuman"));
        SAQueryServiceTests.addOwnedMember(arcadia, this.createArcadiaComponentPortType());
        SAQueryServiceTests.addOwnedMember(root, arcadia);

        var systemAnalysis = this.createPackage("System Analysis");
        var structurePackage = this.createPackage("Structure");
        var functionsPackage = this.createPackage("Functions");
        var requirementsPackage = this.createPackage("Requirements");
        SAQueryServiceTests.addOwnedMember(systemAnalysis, structurePackage);
        SAQueryServiceTests.addOwnedMember(systemAnalysis, functionsPackage);
        SAQueryServiceTests.addOwnedMember(systemAnalysis, requirementsPackage);
        SAQueryServiceTests.addOwnedMember(root, systemAnalysis);

        var system = this.createComponent("system", componentType);
        SAQueryServiceTests.addOwnedMember(structurePackage, system);
        return structurePackage;
    }

    @Test
    public void createFunctionalChainShouldStoreSelectedFunctionalExchangesInFunctionsPackage() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var targetFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var functionalExchange = this.mutationService.createFunctionalExchange(sourceFunction, targetFunction);

        var functionalChain = this.mutationService.createNewFunctionalChain(structurePackage, java.util.List.of(functionalExchange));

        assertNotNull(functionalChain);
        assertEquals("FunctionalChain 0", functionalChain.getDeclaredName());
        assertEquals("Arcadia::FunctionalChain", functionalChain.getType().get(0).getQualifiedName());
        assertTrue(new SAQueryService().getFunctionalChains(structurePackage.getOwner()).contains(functionalChain));
        assertEquals(java.util.List.of(functionalExchange), new SAQueryService().getInvolvedFunctionalExchanges(functionalChain));
        assertTrue(new SAQueryService().getInvolvedFunctions(functionalChain).contains(sourceFunction));
        assertTrue(new SAQueryService().getInvolvedFunctions(functionalChain).contains(targetFunction));
    }

    @Test
    public void createFunctionalChainShouldRejectEmptySelection() {
        var structurePackage = this.createSystemAnalysisStructurePackage();

        assertNull(this.mutationService.createNewFunctionalChain(structurePackage, java.util.List.of()));
    }

    @Test
    public void deleteSystemComponentShouldDeleteAllocatedFunctionsFunctionalExchangesAndFunctionalChains() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var deletedComponent = this.mutationService.createSystemComponent(system);
        var retainedComponent = this.mutationService.createSystemComponent(system);
        var deletedFunction = (ActionUsage) this.mutationService.createNewFunction(deletedComponent);
        var retainedFunction = (ActionUsage) this.mutationService.createNewFunction(retainedComponent);
        var functionalExchange = this.mutationService.createFunctionalExchange(deletedFunction, retainedFunction);
        var functionalChain = this.mutationService.createNewFunctionalChain(structurePackage, java.util.List.of(functionalExchange));
        var systemAnalysisPackage = structurePackage.getOwner();

        this.semanticMutationService.deleteSystemComponent(deletedComponent);

        assertFalse(system.getOwnedElement().contains(deletedComponent));
        assertTrue(system.getOwnedElement().contains(retainedComponent));
        assertTrue(new SAQueryService().getFunctionalExchanges(systemAnalysisPackage).isEmpty());
        assertFalse(new SAQueryService().getFunctionalChains(systemAnalysisPackage).contains(functionalChain));
        assertFalse(this.getFunctionsPackage(structurePackage).getOwnedElement().contains(deletedFunction));
        assertTrue(this.getFunctionsPackage(structurePackage).getOwnedElement().contains(retainedFunction));
    }

    @Test
    public void deleteFunctionalExchangeShouldRepairFunctionalChains() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var middleFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var targetFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var deletedFunctionalExchange = this.mutationService.createFunctionalExchange(sourceFunction, middleFunction);
        var retainedFunctionalExchange = this.mutationService.createFunctionalExchange(middleFunction, targetFunction);
        var functionalChain = (ActionUsage) this.mutationService.createNewFunctionalChain(structurePackage, java.util.List.of(deletedFunctionalExchange, retainedFunctionalExchange));

        this.semanticMutationService.deleteFunctionalExchange(deletedFunctionalExchange);

        assertFalse(new SAQueryService().getFunctionalExchanges(structurePackage.getOwner()).contains(deletedFunctionalExchange));
        assertEquals(java.util.List.of(retainedFunctionalExchange), new SAQueryService().getInvolvedFunctionalExchanges(functionalChain));
    }

    @Test
    public void deleteFunctionPortShouldDeleteFunctionalExchangesAndFunctionalChains() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var targetFunction = (ActionUsage) this.mutationService.createNewFunction(system);
        var functionalExchange = this.mutationService.createFunctionalExchange(sourceFunction, targetFunction);
        var functionalChain = this.mutationService.createNewFunctionalChain(structurePackage, java.util.List.of(functionalExchange));
        var sourcePort = new SAQueryService().getFunctionalExchangeSource(functionalExchange);

        this.semanticMutationService.deleteFunctionPort(sourcePort);

        assertFalse(sourceFunction.getOwnedElement().contains(sourcePort));
        assertTrue(new SAQueryService().getFunctionalExchanges(structurePackage.getOwner()).isEmpty());
        assertFalse(new SAQueryService().getFunctionalChains(structurePackage.getOwner()).contains(functionalChain));
    }

    @Test
    public void deleteFunctionShouldRemoveExternalAllocationsAndDependentElements() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceComponent = this.mutationService.createSystemComponent(system);
        var targetComponent = this.mutationService.createSystemComponent(system);
        var deletedFunction = (ActionUsage) this.mutationService.createNewFunction(sourceComponent);
        var retainedFunction = (ActionUsage) this.mutationService.createNewFunction(targetComponent);
        var functionalExchange = this.mutationService.createFunctionalExchange(deletedFunction, retainedFunction);
        var functionalChain = this.mutationService.createNewFunctionalChain(structurePackage, java.util.List.of(functionalExchange));

        this.semanticMutationService.deleteFunction(deletedFunction);

        var queryService = new SAQueryService();
        assertFalse(this.getFunctionsPackage(structurePackage).getOwnedElement().contains(deletedFunction));
        assertTrue(queryService.getAllocatedFunctions(sourceComponent).isEmpty());
        assertEquals(java.util.List.of(retainedFunction), queryService.getAllocatedFunctions(targetComponent));
        assertTrue(queryService.getFunctionalExchanges(structurePackage.getOwner()).isEmpty());
        assertFalse(queryService.getFunctionalChains(structurePackage.getOwner()).contains(functionalChain));
    }

    @Test
    public void moveFunctionToComponentShouldReplaceItsAllocation() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceComponent = this.mutationService.createSystemComponent(system);
        var targetComponent = this.mutationService.createSystemComponent(system);
        var function = (ActionUsage) this.mutationService.createNewFunction(sourceComponent);

        this.semanticMutationService.moveFunctionToComponent(function, targetComponent);

        var queryService = new SAQueryService();
        assertTrue(queryService.getAllocatedFunctions(sourceComponent).isEmpty());
        assertEquals(java.util.List.of(function), queryService.getAllocatedFunctions(targetComponent));
    }

    @Test
    public void deleteComponentPortShouldDeleteItsComponentExchange() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceComponent = this.mutationService.createSystemComponent(system);
        var targetComponent = this.mutationService.createSystemComponent(system);
        var componentExchange = this.mutationService.createComponentExchange(sourceComponent, targetComponent);
        var sourcePort = new SAQueryService().getComponentExchangeSourcePort(componentExchange);

        this.semanticMutationService.deleteComponentPort(sourcePort);

        assertFalse(sourceComponent.getOwnedElement().contains(sourcePort));
        assertTrue(new SAQueryService().getComponentExchanges(structurePackage.getOwner()).isEmpty());
    }

    @Test
    public void deleteComponentExchangeShouldRetainItsComponents() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceComponent = this.mutationService.createSystemComponent(system);
        var targetComponent = this.mutationService.createSystemComponent(system);
        var componentExchange = this.mutationService.createComponentExchange(sourceComponent, targetComponent);

        this.semanticMutationService.deleteComponentExchange(componentExchange);

        assertTrue(system.getOwnedElement().contains(sourceComponent));
        assertTrue(system.getOwnedElement().contains(targetComponent));
        assertTrue(new SAQueryService().getComponentExchanges(structurePackage.getOwner()).isEmpty());
    }

    @Test
    public void deleteRequirementShouldDeleteDescribesReferencingIt() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var requirement = this.mutationService.createRequirement(structurePackage);
        var describes = this.mutationService.createDescribes(requirement, system);

        this.semanticMutationService.deleteRequirement(requirement);

        assertFalse(this.getRequirementsPackage(structurePackage).getOwnedElement().contains(requirement));
        assertFalse(new SAQueryService().getDescribes(structurePackage.getOwner()).contains(describes));
    }

    @Test
    public void deleteSystemActorShouldDeleteTheActorOnly() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var actor = this.mutationService.createSystemActor(structurePackage);

        this.semanticMutationService.deleteSystemActor(actor);

        assertFalse(structurePackage.getOwnedElement().contains(actor));
        assertTrue(structurePackage.getOwnedElement().contains(system));
    }

    private PartDefinition createArcadiaComponentType() {
        var componentType = SysmlFactory.eINSTANCE.createPartDefinition();
        componentType.setDeclaredName("Component");
        return componentType;
    }

    private ActionDefinition createArcadiaFunctionType() {
        var functionType = SysmlFactory.eINSTANCE.createActionDefinition();
        functionType.setDeclaredName("Function");
        return functionType;
    }

    private ActionDefinition createArcadiaFunctionalChainType() {
        var functionalChainType = SysmlFactory.eINSTANCE.createActionDefinition();
        functionalChainType.setDeclaredName("FunctionalChain");
        SAQueryServiceTests.addOwnedMember(functionalChainType, this.createReference("involvedFunctionalExchanges"));
        return functionalChainType;
    }

    private org.eclipse.syson.sysml.ReferenceUsage createReference(String declaredName) {
        var referenceUsage = SysmlFactory.eINSTANCE.createReferenceUsage();
        referenceUsage.setDeclaredName(declaredName);
        return referenceUsage;
    }

    private AttributeUsage createAttribute(String declaredName) {
        AttributeUsage attributeUsage = SysmlFactory.eINSTANCE.createAttributeUsage();
        attributeUsage.setDeclaredName(declaredName);
        return attributeUsage;
    }

    private PortDefinition createArcadiaComponentPortType() {
        var componentPortType = SysmlFactory.eINSTANCE.createPortDefinition();
        componentPortType.setDeclaredName("ComponentPort");
        return componentPortType;
    }

    private org.eclipse.syson.sysml.ItemDefinition createArcadiaExchangeItemType() {
        var exchangeItemType = SysmlFactory.eINSTANCE.createItemDefinition();
        exchangeItemType.setDeclaredName("ExchangeItem");
        return exchangeItemType;
    }

    private FlowDefinition createArcadiaFunctionalExchangeType() {
        var functionalExchangeType = SysmlFactory.eINSTANCE.createFlowDefinition();
        functionalExchangeType.setDeclaredName("FunctionalExchange");
        return functionalExchangeType;
    }

    private InterfaceDefinition createArcadiaComponentExchangeType() {
        var componentExchangeType = SysmlFactory.eINSTANCE.createInterfaceDefinition();
        componentExchangeType.setDeclaredName("ComponentExchange");
        return componentExchangeType;
    }

    private RequirementDefinition createArcadiaRequirementType() {
        var requirementType = SysmlFactory.eINSTANCE.createRequirementDefinition();
        requirementType.setDeclaredName("Requirement");
        return requirementType;
    }

    private PartUsage getSystem(Package structurePackage) {
        return structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();
    }

    private Package getFunctionsPackage(Package structurePackage) {
        return ((Element) structurePackage.getOwner()).getOwnedElement().stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> "Functions".equals(pkg.getDeclaredName()))
                .findFirst()
                .orElseThrow();
    }

    private Package getRequirementsPackage(Package structurePackage) {
        return ((Element) structurePackage.getOwner()).getOwnedElement().stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> "Requirements".equals(pkg.getDeclaredName()))
                .findFirst()
                .orElseThrow();
    }

    private PartUsage createComponent(String declaredName, PartDefinition componentType) {
        PartUsage partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        partUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(partUsage, componentType);
        return partUsage;
    }

    private Package createPackage(String declaredName) {
        Package packageElement = SysmlFactory.eINSTANCE.createPackage();
        packageElement.setDeclaredName(declaredName);
        return packageElement;
    }
}

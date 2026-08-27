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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
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
import org.eclipse.syson.util.SysONEContentAdapter;
import org.junit.jupiter.api.Test;

/**
 * Tests for the System Analysis representation mutation service.
 *
 * @author mbats
 */
public class SARepresentationMutationServiceTests {

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final SARepresentationMutationService mutationService = new SARepresentationMutationService();

    private final SAMutationService semanticMutationService = new SAMutationService();

    @Test
    public void createSystemActorShouldNameFirstActorAOne() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var actor = this.transverseMutationService.createActor(structurePackage);

        assertEquals("A 2", actor.getDeclaredName());
    }

    @Test
    public void creationShouldRejectParentsOutsideSystemAnalysis() {
        var packageOutsideSystemAnalysis = SysmlFactory.eINSTANCE.createPackage();
        var componentOutsideSystemAnalysis = SysmlFactory.eINSTANCE.createPartUsage();

        assertNull(this.transverseMutationService.createActor(packageOutsideSystemAnalysis));
        assertNull(this.transverseMutationService.createRequirement(packageOutsideSystemAnalysis));
        assertNull(this.transverseMutationService.createFunction(componentOutsideSystemAnalysis));
    }

    @Test
    public void createSystemActorShouldCreateNestedActorUnderSelectedActor() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var actor = this.transverseMutationService.createActor(structurePackage);
        var nestedActor = this.transverseMutationService.createActor(actor);

        assertEquals("A 3", nestedActor.getDeclaredName());
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

        var actor = this.transverseMutationService.createActor(structurePackage);

        assertEquals("A 3", actor.getDeclaredName());
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

        var actor = this.semanticMutationService.createActorSA(system);

        assertEquals("A 2", actor.getDeclaredName());
        assertTrue(structurePackage.getOwnedElement().contains(actor));
        assertFalse(system.getOwnedElement().contains(actor));
    }

    @Test
    public void createSystemComponentShouldCreateInternalComponentUnderSelectedSystemOrComponent() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);

        var component = this.transverseMutationService.createComponent(system);
        var nestedComponent = this.transverseMutationService.createComponent(component);

        assertNotNull(component);
        assertEquals("C 2", component.getDeclaredName());
        assertTrue(system.getOwnedElement().contains(component));
        assertTrue(new SAQueryService().getSystemComponents(system).contains(component));
        assertEquals("C 3", nestedComponent.getDeclaredName());
        assertTrue(component.getOwnedElement().contains(nestedComponent));
        assertTrue(new SAQueryService().getSystemComponents(component).contains(nestedComponent));
    }

    @Test
    public void createSystemComponentShouldRejectStructurePackageAndActors() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var actor = this.transverseMutationService.createActor(structurePackage);

        assertNull(this.semanticMutationService.createComponentSA(structurePackage));
        assertNull(this.semanticMutationService.createComponentSA(actor));
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
        var actor = this.transverseMutationService.createActor(structurePackage);
        var componentExchange = this.transverseMutationService.createComponentExchange(system, actor);

        assertNotNull(componentExchange);
        assertEquals(structurePackage, componentExchange.getOwner());
        assertEquals("Arcadia::ComponentExchange", componentExchange.getType().get(0).getQualifiedName());
        assertEquals(system, this.transverseQueryService.getComponentExchangeSource(componentExchange).getOwner());
        assertEquals(actor, this.transverseQueryService.getComponentExchangeTarget(componentExchange).getOwner());
    }

    @Test
    public void createComponentExchangeShouldRejectComponentsFromDifferentSystemAnalysisStructures() {
        var sourceStructurePackage = this.createSystemAnalysisStructurePackage();
        var targetStructurePackage = this.createSystemAnalysisStructurePackage();
        var sourceSystem = this.getSystem(sourceStructurePackage);
        var targetSystem = this.getSystem(targetStructurePackage);

        assertNull(this.transverseMutationService.createComponentExchange(sourceSystem, targetSystem));
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

        var function = this.transverseMutationService.createFunction(system);

        assertEquals("Function 2", function.getDeclaredName());
        assertTrue(this.getRootFunction(functionsPackage).getNestedAction().contains(function));
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
        var function = this.transverseMutationService.createFunction(system);

        var subFunction = this.transverseMutationService.createFunction(function);

        assertEquals("Function 3", subFunction.getDeclaredName());
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
        var function = this.transverseMutationService.createFunction(system);

        var inputPort = this.mutationService.createInputFunctionPort(function);
        var outputPort = this.mutationService.createOutputFunctionPort(function);

        assertEquals(FeatureDirectionKind.IN, inputPort.getDirection());
        assertEquals(FeatureDirectionKind.OUT, outputPort.getDirection());
        assertTrue(function.getOwnedElement().contains(inputPort));
        assertTrue(function.getOwnedElement().contains(outputPort));
        assertEquals("FIP 1", inputPort.getDeclaredName());
        assertEquals("FOP 2", outputPort.getDeclaredName());
    }

    private Package createSystemAnalysisStructurePackage() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.eAdapters().add(new ECrossReferenceAdapter());
        Resource resource = new ResourceImpl(URI.createURI("model.sysml"));
        resourceSet.getResources().add(resource);

        var root = SysmlFactory.eINSTANCE.createPackage();
        root.eAdapters().add(new SysONEContentAdapter());
        resource.getContents().add(root);

        var arcadia = this.createPackage("Arcadia");
        var componentType = this.createArcadiaComponentType();
        SAQueryServiceTests.addOwnedMember(arcadia, componentType);
        var functionType = this.createArcadiaFunctionType();
        SAQueryServiceTests.addOwnedMember(arcadia, functionType);
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

        SAQueryServiceTests.addOwnedMember(functionsPackage, this.createFunction("Root Function", functionType));

        var system = this.createComponent("system", componentType);
        SAQueryServiceTests.addOwnedMember(structurePackage, system);
        return structurePackage;
    }

    @Test
    public void deleteSystemComponentShouldDeleteSubComponentsButNotAllocatedFunctionsFunctionalExchangesAndFunctionalChains() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var deletedComponent = this.transverseMutationService.createComponent(system);
        var retainedComponent = this.transverseMutationService.createComponent(system);
        var deletedFunction = this.transverseMutationService.createFunction(deletedComponent);
        var retainedFunction = this.transverseMutationService.createFunction(retainedComponent);

        var functionalExchange = this.transverseMutationService.createFunctionalExchange(deletedFunction, retainedFunction);
        var functionalChain = this.transverseMutationService.createFunctionalChain(structurePackage, java.util.List.of(functionalExchange));
        var systemAnalysisPackage = structurePackage.getOwner();

        this.transverseMutationService.delete(deletedComponent);

        assertFalse(system.getOwnedElement().contains(deletedComponent));
        assertTrue(system.getOwnedElement().contains(retainedComponent));
        assertFalse(this.transverseQueryService.getFunctionalExchanges(systemAnalysisPackage).isEmpty());
        assertTrue(this.transverseQueryService.getFunctionalChains(systemAnalysisPackage).contains(functionalChain));
        assertTrue(this.getRootFunction(this.getFunctionsPackage(structurePackage)).getNestedAction().contains(deletedFunction));
        assertTrue(this.getRootFunction(this.getFunctionsPackage(structurePackage)).getNestedAction().contains(retainedFunction));
    }



    @Test
    public void deleteFunctionShouldRemoveExternalAllocationsAndDependentElements() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceComponent = this.transverseMutationService.createComponent(system);
        var targetComponent = this.transverseMutationService.createComponent(system);
        var deletedFunction = this.transverseMutationService.createFunction(sourceComponent);
        var retainedFunction = this.transverseMutationService.createFunction(targetComponent);

        var functionalExchange = this.transverseMutationService.createFunctionalExchange(deletedFunction, retainedFunction);
        var functionalChain = this.transverseMutationService.createFunctionalChain(structurePackage, java.util.List.of(functionalExchange));

        this.transverseMutationService.delete(deletedFunction);

        var queryService = new TransverseQueryService();
        assertFalse(this.getRootFunction(this.getFunctionsPackage(structurePackage)).getNestedAction().contains(deletedFunction));
        assertTrue(queryService.getAllocatedFunctions(sourceComponent).isEmpty());
        assertEquals(java.util.List.of(retainedFunction), queryService.getAllocatedFunctions(targetComponent));
        assertTrue(new TransverseQueryService().getFunctionalExchanges(structurePackage.getOwner()).isEmpty());
    }

    @Test
    public void moveFunctionToComponentShouldReplaceItsAllocation() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceComponent = this.transverseMutationService.createComponent(system);
        var targetComponent = this.transverseMutationService.createComponent(system);
        var function = this.transverseMutationService.createFunction(sourceComponent);

        this.semanticMutationService.moveFunctionToComponent(function, targetComponent);

        var queryService = new TransverseQueryService();
        assertTrue(queryService.getAllocatedFunctions(sourceComponent).isEmpty());
        assertEquals(java.util.List.of(function), queryService.getAllocatedFunctions(targetComponent));
    }

    @Test
    public void deleteRequirementShouldDeleteDescribesReferencingIt() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var requirement = this.transverseMutationService.createRequirement(structurePackage);
        var describes = this.transverseMutationService.createDescribes(requirement, system);

        this.transverseMutationService.delete(requirement);

        assertFalse(this.getRequirementsPackage(structurePackage).getOwnedElement().contains(requirement));
        assertFalse(this.transverseQueryService.getDescribes(structurePackage.getOwner()).contains(describes));
    }

    @Test
    public void deleteSystemActorShouldDeleteTheActorOnly() {
        var structurePackage = this.createSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var actor = this.transverseMutationService.createActor(structurePackage);

        this.transverseMutationService.delete(actor);

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
        SAQueryServiceTests.addOwnedMember(functionalChainType, this.createFunctionalExchangeReference("involvedFunctionalExchanges"));
        return functionalChainType;
    }

    private FlowUsage createFunctionalExchangeReference(String declaredName) {
        var flowUsage = SysmlFactory.eINSTANCE.createFlowUsage();
        flowUsage.setDeclaredName(declaredName);
        return flowUsage;
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

    private ActionUsage getRootFunction(Package functionsPackage) {
        return functionsPackage.getOwnedElement().stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.transverseQueryService::isFunction)
                .findFirst()
                .orElseThrow();
    }

    private PartUsage createComponent(String declaredName, PartDefinition componentType) {
        PartUsage partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        partUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(partUsage, componentType);
        return partUsage;
    }

    private ActionUsage createFunction(String declaredName, ActionDefinition functionType) {
        ActionUsage actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
        actionUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(actionUsage, functionType);
        return actionUsage;
    }

    private Package createPackage(String declaredName) {
        Package packageElement = SysmlFactory.eINSTANCE.createPackage();
        packageElement.setDeclaredName(declaredName);
        return packageElement;
    }
}

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemDefinition;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortDefinition;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.eclipse.syson.util.SysONEContentAdapter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for the System Analysis query service.
 *
 * @author mbats
 */
public class SAQueryServiceTests {

    private final SAQueryService saQueryService = new SAQueryService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final MetamodelMutationElementService metamodelMutationElementService = new MetamodelMutationElementService();

    @Test
    public void isSystemAnalysisStructurePackageShouldAcceptOnlySystemAnalysisStructure() {
        var componentType = this.createArcadiaComponentType();
        var systemAnalysis = this.createPackage("System Analysis");
        var systemAnalysisStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, systemAnalysisStructure);
        this.addOwnedMember(systemAnalysisStructure, this.createComponent("system", componentType));

        var logicalArchitecture = this.createPackage("Logical Architecture");
        var logicalArchitectureStructure = this.createPackage("Structure");
        this.addOwnedMember(logicalArchitecture, logicalArchitectureStructure);
        this.addOwnedMember(logicalArchitectureStructure, this.createComponent("system", componentType));

        assertTrue(this.saQueryService.isSystemAnalysisStructurePackage(systemAnalysisStructure));
        assertFalse(this.saQueryService.isSystemAnalysisStructurePackage(logicalArchitectureStructure));
        assertFalse(this.saQueryService.isSystemAnalysisStructurePackage(systemAnalysis));
    }

    @Test
    public void isSystemAnalysisStructurePackageShouldNotDependOnSystemOfInterestCount() {
        var componentType = this.createArcadiaComponentType();
        var systemAnalysis = this.createPackage("System Analysis");

        var emptyStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, emptyStructure);
        assertTrue(this.saQueryService.isSystemAnalysisStructurePackage(emptyStructure));

        var duplicateStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, duplicateStructure);
        this.addOwnedMember(duplicateStructure, this.createComponent("system", componentType));
        this.addOwnedMember(duplicateStructure, this.createComponent("system", componentType));
        assertTrue(this.saQueryService.isSystemAnalysisStructurePackage(duplicateStructure));
    }

    @Test
    public void getSystemOfInterestShouldReturnOnlyOneSystemFromSystemAnalysisStructure() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var arcadia = this.createPackage("Arcadia");
        var componentType = SysmlFactory.eINSTANCE.createPartDefinition();
        componentType.setDeclaredName("Component");
        this.addOwnedMember(arcadia, componentType);
        this.addOwnedMember(root, arcadia);

        var systemAnalysis = this.createPackage("System Analysis");
        var systemAnalysisStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, systemAnalysisStructure);
        this.addOwnedMember(root, systemAnalysis);
        var firstSystem = this.createComponent("system", componentType);
        var duplicateSystem = this.createComponent("system", componentType);
        this.addOwnedMember(systemAnalysisStructure, firstSystem);
        this.addOwnedMember(systemAnalysisStructure, duplicateSystem);
        this.addOwnedMember(firstSystem, this.createComponent("system", componentType));

        var logicalArchitecture = this.createPackage("Logical Architecture");
        var logicalArchitectureStructure = this.createPackage("Structure");
        this.addOwnedMember(logicalArchitecture, logicalArchitectureStructure);
        this.addOwnedMember(root, logicalArchitecture);
        this.addOwnedMember(logicalArchitectureStructure, this.createComponent("system", componentType));

        var systems = this.saQueryService.getSystemOfInterest(systemAnalysisStructure);

        assertEquals("Arcadia::Component", componentType.getQualifiedName());
        assertEquals("'System Analysis'::Structure::system", firstSystem.getQualifiedName());
        assertTrue(new TransverseQueryService().isComponent(firstSystem));
        assertEquals(1, systems.size());
        assertTrue(systems.contains(firstSystem));
        assertTrue(this.saQueryService.getSystemOfInterest(logicalArchitectureStructure).isEmpty());
        assertTrue(this.saQueryService.isSystemAnalysisStructurePackage(systemAnalysisStructure));
    }

    @Test
    public void getSystemOfInterestShouldSurviveRename() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var arcadia = this.createPackage("Arcadia");
        var componentType = SysmlFactory.eINSTANCE.createPartDefinition();
        componentType.setDeclaredName("Component");
        this.addOwnedMember(arcadia, componentType);
        this.addOwnedMember(root, arcadia);

        var systemAnalysis = this.createPackage("System Analysis");
        var systemAnalysisStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, systemAnalysisStructure);
        this.addOwnedMember(root, systemAnalysis);
        var system = this.createComponent("Renamed System", componentType);
        this.addOwnedMember(systemAnalysisStructure, system);

        var systems = this.saQueryService.getSystemOfInterest(systemAnalysisStructure);

        assertEquals(1, systems.size());
        assertTrue(systems.contains(system));
    }

    @Test
    public void isSystemAnalysisStructurePackageShouldIgnoreNestedSystemNamedComponents() {
        var componentType = this.createArcadiaComponentType();
        var systemAnalysis = this.createPackage("System Analysis");
        var systemAnalysisStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, systemAnalysisStructure);

        var system = this.createComponent("system", componentType);
        this.addOwnedMember(systemAnalysisStructure, system);
        this.addOwnedMember(system, this.createComponent("system", componentType));

        assertTrue(this.saQueryService.isSystemAnalysisStructurePackage(systemAnalysisStructure));
        assertEquals(List.of(system), this.saQueryService.getSystemOfInterest(systemAnalysisStructure));
    }

    @Test
    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    public void getComponentExchangesShouldReturnOnlySystemAnalysisStructureExchangesAndResolveOwners() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var arcadia = this.createPackage("Arcadia");
        var componentType = this.createArcadiaComponentType();
        var componentPortType = this.createArcadiaComponentPortType();
        var componentExchangeType = this.createComponentExchangeType();
        this.addOwnedMember(arcadia, componentType);
        this.addOwnedMember(arcadia, componentPortType);
        this.addOwnedMember(arcadia, componentExchangeType);
        this.addOwnedMember(root, arcadia);

        var systemAnalysis = this.createPackage("System Analysis");
        var systemAnalysisStructure = this.createPackage("Structure");
        this.addOwnedMember(systemAnalysis, systemAnalysisStructure);
        this.addOwnedMember(root, systemAnalysis);
        var system = this.createComponent("system", componentType);
        var actor = this.createComponent("A 1", componentType);
        this.addOwnedMember(systemAnalysisStructure, system);
        this.addOwnedMember(systemAnalysisStructure, actor);
        var systemPort = this.createComponentPort("CP 1", componentPortType);
        var actorPort = this.createComponentPort("CP 2", componentPortType);
        this.addOwnedMember(system, systemPort);
        this.addOwnedMember(actor, actorPort);
        var systemAnalysisExchange = this.createComponentExchange("CE 1", componentExchangeType, systemPort, actorPort);
        this.addOwnedMember(systemAnalysisStructure, systemAnalysisExchange);
        assertTrue(new TransverseQueryService().isComponentExchange(systemAnalysisExchange));
        assertEquals(system, this.transverseQueryService.getComponentExchangeSource(systemAnalysisExchange).getOwner());
        assertEquals(actor, this.transverseQueryService.getComponentExchangeTarget(systemAnalysisExchange).getOwner());

        var logicalArchitecture = this.createPackage("Logical Architecture");
        var logicalArchitectureStructure = this.createPackage("Structure");
        this.addOwnedMember(logicalArchitecture, logicalArchitectureStructure);
        this.addOwnedMember(root, logicalArchitecture);
        var logicalSource = this.createComponent("LC 1", componentType);
        var logicalTarget = this.createComponent("LC 2", componentType);
        this.addOwnedMember(logicalArchitectureStructure, logicalSource);
        this.addOwnedMember(logicalArchitectureStructure, logicalTarget);
        var logicalSourcePort = this.createComponentPort("CP 3", componentPortType);
        var logicalTargetPort = this.createComponentPort("CP 4", componentPortType);
        this.addOwnedMember(logicalSource, logicalSourcePort);
        this.addOwnedMember(logicalTarget, logicalTargetPort);
        this.addOwnedMember(logicalArchitectureStructure, this.createComponentExchange("CE 2", componentExchangeType, logicalSourcePort, logicalTargetPort));

        assertEquals(List.of(systemAnalysisExchange), this.transverseQueryService.getComponentExchanges(root));
    }

    @Test
    public void getFunctionPortsShouldReturnOnlyExchangeItemParameters() {
        var arcadia = this.createPackage("Arcadia");
        var exchangeItemType = this.createArcadiaExchangeItemType();
        this.addOwnedMember(arcadia, exchangeItemType);
        var function = SysmlFactory.eINSTANCE.createActionUsage();
        this.addOwnedMember(arcadia, function);
        var functionPort = SysmlFactory.eINSTANCE.createItemUsage();
        functionPort.setDeclaredName("FIP 0");
        functionPort.setDirection(FeatureDirectionKind.IN);
        new UtilService().setFeatureTyping(functionPort, exchangeItemType);
        this.metamodelMutationElementService.addChildInParent(function, functionPort);
        var unrelatedParameter = SysmlFactory.eINSTANCE.createItemUsage();
        unrelatedParameter.setDeclaredName("unrelated");
        this.addOwnedMember(function, unrelatedParameter);

        assertEquals(List.of(functionPort), this.transverseQueryService.getFunctionPorts(function));
    }

    @Test
    public void getDescribesShouldReturnOnlyAllocationsFromRequirementsAndResolveEndpoints() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        root.eAdapters().add(new SysONEContentAdapter());
        var requirement = SysmlFactory.eINSTANCE.createRequirementUsage();
        var target = SysmlFactory.eINSTANCE.createPartUsage();
        var describes = this.createAllocation(requirement, target);
        var unrelatedAllocation = this.createAllocation(target, requirement);
        this.addOwnedMember(root, requirement);
        this.addOwnedMember(root, target);
        this.addOwnedMember(root, describes);
        this.addOwnedMember(root, unrelatedAllocation);

        assertEquals(List.of(describes), this.transverseQueryService.getDescribes(root));
        assertEquals(requirement, this.transverseQueryService.getDescribesSource(describes));
        assertEquals(target, this.transverseQueryService.getDescribesTarget(describes));
    }

    private PartDefinition createArcadiaComponentType() {
        var arcadia = this.createPackage("Arcadia");
        var componentType = SysmlFactory.eINSTANCE.createPartDefinition();
        componentType.setDeclaredName("Component");
        this.addOwnedMember(arcadia, componentType);
        return componentType;
    }

    private PortDefinition createArcadiaComponentPortType() {
        var componentPortType = SysmlFactory.eINSTANCE.createPortDefinition();
        componentPortType.setDeclaredName("ComponentPort");
        return componentPortType;
    }

    private ItemDefinition createArcadiaExchangeItemType() {
        var exchangeItemType = SysmlFactory.eINSTANCE.createItemDefinition();
        exchangeItemType.setDeclaredName("ExchangeItem");
        return exchangeItemType;
    }

    private PartDefinition createComponentExchangeType() {
        var componentExchangeType = SysmlFactory.eINSTANCE.createPartDefinition();
        componentExchangeType.setDeclaredName("ComponentExchange");
        return componentExchangeType;
    }

    private Package createPackage(String declaredName) {
        Package packageElement = SysmlFactory.eINSTANCE.createPackage();
        packageElement.setDeclaredName(declaredName);
        return packageElement;
    }

    private PartUsage createComponent(String declaredName, PartDefinition componentType) {
        PartUsage partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        partUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(partUsage, componentType);
        return partUsage;
    }

    private PortUsage createComponentPort(String declaredName, PortDefinition componentPortType) {
        PortUsage portUsage = SysmlFactory.eINSTANCE.createPortUsage();
        portUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(portUsage, componentPortType);
        return portUsage;
    }

    private InterfaceUsage createComponentExchange(String declaredName, PartDefinition componentExchangeType, PortUsage source, PortUsage target) {
        InterfaceUsage interfaceUsage = SysmlFactory.eINSTANCE.createInterfaceUsage();
        interfaceUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(interfaceUsage, componentExchangeType);
        interfaceUsage.getOwnedRelationship().add(this.createConnectionEnd(source));
        interfaceUsage.getOwnedRelationship().add(this.createConnectionEnd(target));
        return interfaceUsage;
    }

    private AllocationUsage createAllocation(Feature source, Feature target) {
        AllocationUsage allocationUsage = SysmlFactory.eINSTANCE.createAllocationUsage();
        allocationUsage.getOwnedRelationship().add(this.createConnectionEnd(source));
        allocationUsage.getOwnedRelationship().add(this.createConnectionEnd(target));
        return allocationUsage;
    }

    private org.eclipse.syson.sysml.ReferenceSubsetting createReferenceSubsetting(Feature referencedFeature) {
        var subsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        subsetting.setReferencedFeature(referencedFeature);
        return subsetting;
    }

    private org.eclipse.syson.sysml.EndFeatureMembership createConnectionEnd(Feature referencedPort) {
        var endFeatureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
        Feature endFeature = SysmlFactory.eINSTANCE.createFeature();
        endFeature.setIsEnd(true);
        endFeatureMembership.getOwnedRelatedElement().add(endFeature);
        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(referencedPort);
        endFeature.getOwnedRelationship().add(referenceSubsetting);
        return endFeatureMembership;
    }


    static void addOwnedMember(Element parent, Element child) {
        Membership membership = SysmlFactory.eINSTANCE.createOwningMembership();
        membership.getOwnedRelatedElement().add(child);
        parent.getOwnedRelationship().add(membership);
    }
}

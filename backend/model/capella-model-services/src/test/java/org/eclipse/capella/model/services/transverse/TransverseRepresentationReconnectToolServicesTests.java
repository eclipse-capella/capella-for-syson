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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Connector;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EndFeatureMembership;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemDefinition;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortDefinition;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.eclipse.syson.sysml.metamodel.services.MetamodelQueryElementService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for transverse reconnection services.
 *
 * @author mbats
 */
public class TransverseRepresentationReconnectToolServicesTests {

    private final DiagramMutationElementService diagramMutationElementService = mock(DiagramMutationElementService.class);

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final TransverseRepresentationReconnectToolServices reconnectToolServices = new TransverseRepresentationReconnectToolServices((element, newParent) -> null,
            this.diagramMutationElementService);

    private final MetamodelMutationElementService metamodelMutationElementService = new MetamodelMutationElementService();

    @Test
    public void reconnectComponentExchangeShouldUpdateSourceAndTargetPorts() {
        var componentPortType = this.createArcadiaComponentPortType();
        var componentType = this.createArcadiaComponentType();
        var structurePackage = this.createStructurePackage();
        var sourceComponent = this.createComponent("Source", componentType, structurePackage);
        var targetComponent = this.createComponent("Target", componentType, structurePackage);
        var sourcePort = this.createComponentPort(sourceComponent, "CP 1", componentPortType, FeatureDirectionKind.OUT);
        var targetPort = this.createComponentPort(targetComponent, "CP 2", componentPortType, FeatureDirectionKind.IN);
        var newSourcePort = this.createComponentPort(sourceComponent, "CP 3", componentPortType, FeatureDirectionKind.INOUT);
        var newTargetPort = this.createComponentPort(targetComponent, "CP 4", componentPortType, FeatureDirectionKind.INOUT);
        var invalidTargetPort = this.createComponentPort(sourceComponent, "CP 5", componentPortType, FeatureDirectionKind.IN);
        InterfaceUsage componentExchange = this.transverseMutationService.createComponentExchange(sourcePort, targetPort);

        this.reconnectToolServices.reconnectComponentExchange(componentExchange, newSourcePort, sourcePort);
        this.reconnectToolServices.reconnectComponentExchange(componentExchange, newTargetPort, targetPort);
        this.reconnectToolServices.reconnectComponentExchange(componentExchange, invalidTargetPort, newTargetPort);

        assertEquals(newSourcePort, this.transverseQueryService.getComponentExchangeSource(componentExchange));
        assertEquals(newTargetPort, this.transverseQueryService.getComponentExchangeTarget(componentExchange));
    }

    @Disabled("This test will be re-enabled once we use the actual arcadia library for the unit tests")
    @Test
    public void reconnectFunctionalExchangeShouldUpdateOnlyValidDirectionalPorts() {
        var sourceFunction = SysmlFactory.eINSTANCE.createActionUsage();
        var targetFunction = SysmlFactory.eINSTANCE.createActionUsage();
        var sourcePort = this.createFunctionPort(sourceFunction, FeatureDirectionKind.OUT);
        var targetPort = this.createFunctionPort(targetFunction, FeatureDirectionKind.IN);
        var newSourcePort = this.createFunctionPort(sourceFunction, FeatureDirectionKind.INOUT);
        var newTargetPort = this.createFunctionPort(targetFunction, FeatureDirectionKind.INOUT);
        var invalidSourcePort = this.createFunctionPort(targetFunction, FeatureDirectionKind.OUT);
        var invalidTargetPort = this.createFunctionPort(sourceFunction, FeatureDirectionKind.IN);
        FlowUsage functionalExchange = new org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService().createFlowUsage(sourcePort, targetPort, null, null, sourceFunction);

        when(this.diagramMutationElementService.reconnectSource(
                any(Connector.class),
                any(Feature.class),
                any(Node.class),
                any(Node.class),
                any(IEditingContext.class),
                any(Diagram.class)
        )).thenAnswer(invocation -> {
            Connector connector = invocation.getArgument(0);
            Feature newSource = invocation.getArgument(1);
            Feature oldTarget = new MetamodelQueryElementService().getConnectorTarget(invocation.getArgument(0)).stream().findFirst().get();
            List<EndFeatureMembership> endFeatureMemberships = connector.getOwnedFeatureMembership().stream()
                    .filter(EndFeatureMembership.class::isInstance)
                    .map(EndFeatureMembership.class::cast)
                    .toList();
            connector.getOwnedRelationship().removeAll(endFeatureMemberships);
            this.metamodelMutationElementService.setConnectorEnds(connector, newSource, oldTarget, newSource.getOwner(), oldTarget.getOwner(), connector.getOwner());
            return connector;
        });

        when(this.diagramMutationElementService.reconnectTarget(
                any(Connector.class),
                any(Feature.class),
                any(Node.class),
                any(Node.class),
                any(IEditingContext.class),
                any(Diagram.class)
        )).thenAnswer(invocation -> {
            Connector connector = invocation.getArgument(0);
            Feature newTarget = invocation.getArgument(1);
            Feature source = new MetamodelQueryElementService().getConnectorSource(connector);
            List<EndFeatureMembership> endFeatureMemberships = connector.getOwnedFeatureMembership().stream()
                    .filter(EndFeatureMembership.class::isInstance)
                    .map(EndFeatureMembership.class::cast)
                    .toList();
            connector.getOwnedRelationship().removeAll(endFeatureMemberships);
            this.metamodelMutationElementService.setConnectorEnds(connector, source, newTarget, source.getOwner(), newTarget.getOwner(), connector.getOwner());
            return connector;
        });

        this.reconnectToolServices.reconnectFunctionalExchangeSource(functionalExchange, newSourcePort, sourcePort, mock(Node.class), mock(Node.class), new IEditingContext.NoOp(),
                mock(Diagram.class));
        this.reconnectToolServices.reconnectFunctionalExchangeTarget(functionalExchange, newTargetPort, targetPort, mock(Node.class), mock(Node.class), new IEditingContext.NoOp(),
                mock(Diagram.class));
        this.reconnectToolServices.reconnectFunctionalExchangeSource(functionalExchange, invalidSourcePort, sourcePort, mock(Node.class), mock(Node.class), new IEditingContext.NoOp(),
                mock(Diagram.class));
        this.reconnectToolServices.reconnectFunctionalExchangeTarget(functionalExchange, invalidTargetPort, targetPort, mock(Node.class), mock(Node.class), new IEditingContext.NoOp(),
                mock(Diagram.class));

        assertEquals(newSourcePort, new TransverseQueryService().getFunctionalExchangeSource(functionalExchange));
        assertEquals(newTargetPort, new TransverseQueryService().getFunctionalExchangeTarget(functionalExchange));
    }

    private PortDefinition createArcadiaComponentPortType() {
        var arcadia = SysmlFactory.eINSTANCE.createPackage();
        arcadia.setDeclaredName("Arcadia");
        var componentPortType = SysmlFactory.eINSTANCE.createPortDefinition();
        componentPortType.setDeclaredName("ComponentPort");
        arcadia.getOwnedRelationship().add(SysmlFactory.eINSTANCE.createOwningMembership());
        arcadia.getOwnedRelationship().get(0).getOwnedRelatedElement().add(componentPortType);
        return componentPortType;
    }

    private PartDefinition createArcadiaComponentType() {
        var arcadia = SysmlFactory.eINSTANCE.createPackage();
        arcadia.setDeclaredName("Arcadia");
        var componentType = SysmlFactory.eINSTANCE.createPartDefinition();
        componentType.setDeclaredName("Component");
        arcadia.getOwnedRelationship().add(SysmlFactory.eINSTANCE.createOwningMembership());
        arcadia.getOwnedRelationship().get(0).getOwnedRelatedElement().add(componentType);
        return componentType;
    }

    private Package createStructurePackage() {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.eAdapters().add(new ECrossReferenceAdapter());

        Resource resource = new ResourceImpl(URI.createURI("model.sysml"));
        resourceSet.getResources().add(resource);

        var systemAnalysis = SysmlFactory.eINSTANCE.createPackage();
        systemAnalysis.setDeclaredName("System Analysis");
        resource.getContents().add(systemAnalysis);
        var structurePackage = SysmlFactory.eINSTANCE.createPackage();
        structurePackage.setDeclaredName("Structure");
        this.metamodelMutationElementService.addChildInParent(systemAnalysis, structurePackage);
        return structurePackage;
    }

    private PartUsage createComponent(String declaredName, PartDefinition componentType, Element parent) {
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        component.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(component, componentType);
        this.metamodelMutationElementService.addChildInParent(parent, component);
        return component;
    }

    private PortUsage createComponentPort(PartUsage component, String declaredName, PortDefinition componentPortType, FeatureDirectionKind direction) {
        PortUsage portUsage = SysmlFactory.eINSTANCE.createPortUsage();
        portUsage.setDeclaredName(declaredName);
        portUsage.setDirection(direction);
        new UtilService().setFeatureTyping(portUsage, componentPortType);
        this.metamodelMutationElementService.addChildInParent(component, portUsage);
        return portUsage;
    }

    private Feature createFunctionPort(ActionUsage function, FeatureDirectionKind direction) {
        var arcadia = SysmlFactory.eINSTANCE.createPackage();
        arcadia.setDeclaredName("Arcadia");
        ItemDefinition exchangeItemType = SysmlFactory.eINSTANCE.createItemDefinition();
        exchangeItemType.setDeclaredName("ExchangeItem");
        arcadia.getOwnedRelationship().add(SysmlFactory.eINSTANCE.createOwningMembership());
        arcadia.getOwnedRelationship().get(0).getOwnedRelatedElement().add(exchangeItemType);
        var port = SysmlFactory.eINSTANCE.createItemUsage();
        port.setDirection(direction);
        new UtilService().setFeatureTyping(port, exchangeItemType);
        var membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        membership.getOwnedRelatedElement().add(port);
        function.getOwnedRelationship().add(membership);
        return port;
    }

    private org.eclipse.syson.sysml.EndFeatureMembership createConnectionEnd(PortUsage referencedPort) {
        var endFeatureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
        Feature endFeature = SysmlFactory.eINSTANCE.createFeature();
        endFeature.setIsEnd(true);
        endFeatureMembership.getOwnedRelatedElement().add(endFeature);
        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(referencedPort);
        endFeature.getOwnedRelationship().add(referenceSubsetting);
        return endFeatureMembership;
    }
}

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

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.sirius.components.web.services.FeedbackMessageService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemDefinition;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortDefinition;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests for transverse reconnection services.
 *
 * @author mbats
 */
public class TransverseRepresentationReconnectToolServicesTests {

    private final TransverseRepresentationReconnectToolServices reconnectToolServices = new TransverseRepresentationReconnectToolServices((element, newParent) -> null, new FeedbackMessageService());

    @Test
    public void reconnectComponentExchangeShouldUpdateSourceAndTargetPorts() {
        var componentPortType = this.createArcadiaComponentPortType();
        var componentType = this.createArcadiaComponentType();
        var sourceComponent = this.createComponent("Source", componentType);
        var targetComponent = this.createComponent("Target", componentType);
        var sourcePort = this.createComponentPort(sourceComponent, "CP 1", componentPortType);
        var targetPort = this.createComponentPort(targetComponent, "CP 2", componentPortType);
        var newSourcePort = this.createComponentPort(sourceComponent, "CP 3", componentPortType);
        var newTargetPort = this.createComponentPort(targetComponent, "CP 4", componentPortType);
        var invalidTargetPort = this.createComponentPort(sourceComponent, "CP 5", componentPortType);
        InterfaceUsage componentExchange = SysmlFactory.eINSTANCE.createInterfaceUsage();
        componentExchange.getOwnedRelationship().add(this.createConnectionEnd(sourcePort));
        componentExchange.getOwnedRelationship().add(this.createConnectionEnd(targetPort));

        this.reconnectToolServices.reconnectComponentExchangeEnd(componentExchange, newSourcePort, true);
        this.reconnectToolServices.reconnectComponentExchangeEnd(componentExchange, newTargetPort, false);
        this.reconnectToolServices.reconnectComponentExchangeEnd(componentExchange, invalidTargetPort, false);

        assertEquals(newSourcePort, componentExchange.getSourceFeature());
        assertEquals(newTargetPort, componentExchange.getTargetFeature().get(0));
    }

    @Test
    public void reconnectFunctionalExchangeShouldUpdateOnlyValidOutToInPorts() {
        var sourceFunction = SysmlFactory.eINSTANCE.createActionUsage();
        var targetFunction = SysmlFactory.eINSTANCE.createActionUsage();
        var sourcePort = this.createFunctionPort(sourceFunction, FeatureDirectionKind.OUT);
        var targetPort = this.createFunctionPort(targetFunction, FeatureDirectionKind.IN);
        var newSourcePort = this.createFunctionPort(sourceFunction, FeatureDirectionKind.OUT);
        var newTargetPort = this.createFunctionPort(targetFunction, FeatureDirectionKind.IN);
        var invalidSourcePort = this.createFunctionPort(targetFunction, FeatureDirectionKind.OUT);
        var invalidTargetPort = this.createFunctionPort(sourceFunction, FeatureDirectionKind.IN);
        FlowUsage functionalExchange = new org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService().createFlowUsage(sourcePort, targetPort, null, null, sourceFunction);

        this.reconnectToolServices.reconnectFunctionalExchangeEnd(functionalExchange, newSourcePort, true);
        this.reconnectToolServices.reconnectFunctionalExchangeEnd(functionalExchange, newTargetPort, false);
        this.reconnectToolServices.reconnectFunctionalExchangeEnd(functionalExchange, invalidSourcePort, true);
        this.reconnectToolServices.reconnectFunctionalExchangeEnd(functionalExchange, invalidTargetPort, false);

        assertEquals(newSourcePort, new SAQueryService().getFunctionalExchangeSource(functionalExchange));
        assertEquals(newTargetPort, new SAQueryService().getFunctionalExchangeTarget(functionalExchange));
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

    private PartUsage createComponent(String declaredName, PartDefinition componentType) {
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        component.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(component, componentType);
        return component;
    }

    private PortUsage createComponentPort(PartUsage component, String declaredName, PortDefinition componentPortType) {
        PortUsage portUsage = SysmlFactory.eINSTANCE.createPortUsage();
        portUsage.setDeclaredName(declaredName);
        new UtilService().setFeatureTyping(portUsage, componentPortType);
        Membership membership = SysmlFactory.eINSTANCE.createOwningMembership();
        membership.getOwnedRelatedElement().add(portUsage);
        component.getOwnedRelationship().add(membership);
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

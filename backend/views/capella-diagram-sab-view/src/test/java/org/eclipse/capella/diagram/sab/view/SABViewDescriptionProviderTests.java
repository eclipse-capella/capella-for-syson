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
package org.eclipse.capella.diagram.sab.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.capella.diagram.common.view.ColorProvider;
import org.eclipse.capella.diagram.sab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.edges.describes.DescribesEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.actor.SystemActorNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.component.SystemComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.componentport.ComponentPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.functionalchain.FunctionalChainNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.system.SystemOfInterestNodeDescriptionProvider;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.DeleteView;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.Tool;
import org.eclipse.syson.util.ServiceMethod;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SAB view description provider.
 *
 * @author mbats
 * @author tbezierslafosse
 */
public class SABViewDescriptionProviderTests {

    @Test
    public void createShouldBuildSABDiagramDescriptionScopedToSystemAnalysisStructure() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new SABViewDescriptionProvider().getColorProvider(view);
        var representationDescription = new SABViewDiagramDescriptionProvider().create(colorProvider);

        var diagramDescription = assertInstanceOf(DiagramDescription.class, representationDescription);
        assertEquals(SABViewConstants.DESCRIPTION_NAME, diagramDescription.getName());
        assertEquals(SABViewConstants.DESCRIPTION_NAME, diagramDescription.getTitleExpression());
        assertEquals(ServiceMethod.of0(SAQueryService::isSystemAnalysisStructurePackage).aqlSelf(), diagramDescription.getPreconditionExpression());
        assertNotNull(diagramDescription.getPalette());
    }

    @Test
    public void createShouldRegisterExpectedIncrementOneDescriptionsAndSynchronizationPolicies() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        assertNode(diagramDescription, SystemOfInterestNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.SYNCHRONIZED);
        assertNode(diagramDescription, SystemComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.UNSYNCHRONIZED);
        assertNode(diagramDescription, SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.UNSYNCHRONIZED);
        assertNode(diagramDescription, FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.UNSYNCHRONIZED);
        assertNode(diagramDescription, ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.SYNCHRONIZED);
        assertNode(diagramDescription, FunctionalChainNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.UNSYNCHRONIZED);
        assertNode(diagramDescription, RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME, SynchronizationPolicy.UNSYNCHRONIZED);
        assertEdge(diagramDescription, ComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME, SynchronizationPolicy.SYNCHRONIZED);
        assertEdge(diagramDescription, FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME, SynchronizationPolicy.SYNCHRONIZED);
        assertEdge(diagramDescription, DescribesEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME, SynchronizationPolicy.SYNCHRONIZED);
    }

    @Test
    public void createShouldConfigureFunctionalChainNodeAndDiagramTool() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var functionalChainDescription = this.getNodeDescription(diagramDescription, FunctionalChainNodeDescriptionProvider.NODE_DESCRIPTION_NAME);

        assertEquals(ServiceMethod.of0(TransverseQueryService::getFunctionalChains).aqlSelf(), functionalChainDescription.getSemanticCandidatesExpression());
        assertEquals(SynchronizationPolicy.UNSYNCHRONIZED, functionalChainDescription.getSynchronizationPolicy());
        assertEquals("30", functionalChainDescription.getDefaultWidthExpression());
        assertEquals("30", functionalChainDescription.getDefaultHeightExpression());
        assertEquals(3, functionalChainDescription.getConditionalStyles().size());
        assertNotNull(functionalChainDescription.getPalette().getDeleteTool());
        assertNotNull(functionalChainDescription.getPalette().getLabelEditTool());
        assertDeleteFromDiagramQuickAccessTool(functionalChainDescription);
        assertTrue(diagramDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Functional Chain".equals(nodeTool.getName())));
    }

    @Test
    public void createShouldConfigureSystemAndActorStructurePalettesAndContainment() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var systemDescription = diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> SystemOfInterestNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(nodeDescription.getName()))
                .findFirst()
                .orElseThrow();
        assertNotNull(systemDescription.getPalette().getLabelEditTool());
        assertNotNull(systemDescription.getPalette().getDropNodeTool());
        assertTrue(systemDescription.getReusedChildNodeDescriptions().stream()
                .anyMatch(childDescription -> SystemComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(childDescription.getName())));
        assertTrue(systemDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Component".equals(nodeTool.getName())));

        var actorDescription = diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(nodeDescription.getName()))
                .findFirst()
                .orElseThrow();
        assertTrue(actorDescription.getReusedChildNodeDescriptions().stream()
                .anyMatch(childDescription -> SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(childDescription.getName())));
        assertDeleteFromDiagramQuickAccessTool(actorDescription);
        assertTrue(actorDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Actor".equals(nodeTool.getName())));
    }

    @Test
    public void createShouldConfigureSystemComponentPaletteAndContainment() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var componentDescription = this.getNodeDescription(diagramDescription, SystemComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME);

        assertEquals(ServiceMethod.of0(SAQueryService::getSystemComponents).aqlSelf(), componentDescription.getSemanticCandidatesExpression());
        assertEquals(SynchronizationPolicy.UNSYNCHRONIZED, componentDescription.getSynchronizationPolicy());
        assertTrue(componentDescription.getReusedChildNodeDescriptions().stream()
                .anyMatch(childDescription -> SystemComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(childDescription.getName())));
        assertTrue(componentDescription.getReusedChildNodeDescriptions().stream()
                .anyMatch(childDescription -> FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(childDescription.getName())));
        assertTrue(componentDescription.getReusedBorderNodeDescriptions().stream()
                .anyMatch(borderDescription -> ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(borderDescription.getName())));
        assertNotNull(componentDescription.getPalette().getDeleteTool());
        assertNotNull(componentDescription.getPalette().getDropNodeTool());
        assertDeleteFromDiagramQuickAccessTool(componentDescription);
        assertTrue(componentDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Component".equals(nodeTool.getName())));
        assertTrue(componentDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Function".equals(nodeTool.getName())));
        assertTrue(componentDescription.getPalette().getEdgeTools().stream()
                .anyMatch(edgeTool -> "New Component Exchange".equals(edgeTool.getName())));
    }

    @Test
    public void createShouldConfigureComponentPortPaletteForSynchronizedLifecycle() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var componentPortDescription = diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(nodeDescription.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(SynchronizationPolicy.SYNCHRONIZED, componentPortDescription.getSynchronizationPolicy());
        assertTrue(componentPortDescription.getPalette().getQuickAccessTools().isEmpty());
        assertNotNull(componentPortDescription.getPalette().getDeleteTool());
        assertFalse(componentPortDescription.getPalette().getToolSections().isEmpty());
        assertTrue(componentPortDescription.getPalette().getEdgeTools().stream()
                .anyMatch(edgeTool -> "New Component Exchange".equals(edgeTool.getName())));
    }

    @Test
    public void createShouldExposeOnlyDirectedComponentPortToolsOnComponentPalettes() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        assertComponentPortTools(this.getNodeDescription(diagramDescription, SystemOfInterestNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
        assertComponentPortTools(this.getNodeDescription(diagramDescription, SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
    }

    @Test
    public void createShouldConfigureComponentExchangeAsSynchronizedSABEdge() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var componentExchangeDescription = diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> ComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME.equals(edgeDescription.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(SynchronizationPolicy.SYNCHRONIZED, componentExchangeDescription.getSynchronizationPolicy());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getComponentExchanges).aqlSelf(), componentExchangeDescription.getSemanticCandidatesExpression());
        assertEquals(List.of(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME), componentExchangeDescription.getSourceDescriptions().stream().map(DiagramElementDescription::getName).toList());
        assertEquals(List.of(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME), componentExchangeDescription.getTargetDescriptions().stream().map(DiagramElementDescription::getName).toList());
        assertNotNull(componentExchangeDescription.getPalette().getDeleteTool());
        assertFalse(componentExchangeDescription.getPalette().getToolSections().isEmpty());
        assertEquals(List.of("componentExchangeSourceReconnectionTool", "componentExchangeTargetReconnectionTool"), componentExchangeDescription.getPalette().getEdgeReconnectionTools().stream()
                .map(Tool::getName)
                .toList());
    }

    @Test
    public void createShouldConfigureFunctionPortAsSynchronizedBorderNode() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var functionDescription = this.getNodeDescription(diagramDescription, FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME);
        assertDeleteFromDiagramQuickAccessTool(functionDescription);
        var functionPortDescription = functionDescription.getReusedBorderNodeDescriptions().stream()
                .filter(nodeDescription -> FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME.equals(nodeDescription.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(SynchronizationPolicy.SYNCHRONIZED, functionPortDescription.getSynchronizationPolicy());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getFunctionPorts).aqlSelf(), functionPortDescription.getSemanticCandidatesExpression());
        assertNotNull(functionPortDescription.getPalette().getDeleteTool());
        assertDeleteFromDiagramQuickAccessTool(functionPortDescription);
        assertTrue(functionDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Input Function Port".equals(nodeTool.getName())));
        assertTrue(functionDescription.getPalette().getNodeTools().stream()
                .anyMatch(nodeTool -> "New Output Function Port".equals(nodeTool.getName())));
        assertTrue(functionPortDescription.getPalette().getEdgeTools().stream()
                .anyMatch(edgeTool -> "New Functional Exchange".equals(edgeTool.getName())));
    }

    @Test
    public void createShouldConfigureFunctionalExchangeAsSynchronizedSABEdge() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var functionalExchangeDescription = diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME.equals(edgeDescription.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals(SynchronizationPolicy.SYNCHRONIZED, functionalExchangeDescription.getSynchronizationPolicy());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getFunctionalExchanges).aqlSelf(), functionalExchangeDescription.getSemanticCandidatesExpression());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getFunctionalExchangeSource).aqlSelf(), functionalExchangeDescription.getSourceExpression());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getFunctionalExchangeTarget).aqlSelf(), functionalExchangeDescription.getTargetExpression());
        assertEquals(4, functionalExchangeDescription.getConditionalStyles().size());
        assertEquals(List.of(FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME), functionalExchangeDescription.getSourceDescriptions().stream().map(DiagramElementDescription::getName).toList());
        assertEquals(List.of(FunctionPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME), functionalExchangeDescription.getTargetDescriptions().stream().map(DiagramElementDescription::getName).toList());
        assertNotNull(functionalExchangeDescription.getPalette().getDeleteTool());
        assertEquals(List.of("functionalExchangeSourceReconnectionTool", "functionalExchangeTargetReconnectionTool"), functionalExchangeDescription.getPalette().getEdgeReconnectionTools().stream()
                .map(Tool::getName)
                .toList());
    }

    @Test
    public void createShouldConfigureRequirementAndDescribesInteractions() {
        var view = ViewFactory.eINSTANCE.createView();
        var colorProvider = new ColorProvider(view);
        var diagramDescription = (DiagramDescription) new SABViewDiagramDescriptionProvider().create(colorProvider);

        var requirementDescription = this.getNodeDescription(diagramDescription, RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME);
        assertEquals(ServiceMethod.of0(TransverseQueryService::getRequirements).aqlSelf(), requirementDescription.getSemanticCandidatesExpression());
        assertEquals(SynchronizationPolicy.UNSYNCHRONIZED, requirementDescription.getSynchronizationPolicy());
        assertNotNull(requirementDescription.getPalette().getDeleteTool());
        assertDeleteFromDiagramQuickAccessTool(requirementDescription);
        assertTrue(requirementDescription.getPalette().getEdgeTools().stream()
                .anyMatch(edgeTool -> "New Describes".equals(edgeTool.getName())));

        var describesDescription = diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> DescribesEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME.equals(edgeDescription.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals(ServiceMethod.of0(TransverseQueryService::getDescribes).aqlSelf(), describesDescription.getSemanticCandidatesExpression());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getDescribesSource).aqlSelf(), describesDescription.getSourceExpression());
        assertEquals(ServiceMethod.of0(TransverseQueryService::getDescribesTarget).aqlSelf(), describesDescription.getTargetExpression());
        assertEquals(List.of(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME), describesDescription.getSourceDescriptions().stream()
                .map(DiagramElementDescription::getName)
                .toList());
        assertTrue(describesDescription.getTargetDescriptions().stream()
                .anyMatch(description -> FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME.equals(description.getName())));
        assertFalse(describesDescription.getPalette().getToolSections().isEmpty());
        assertEquals(List.of("describesSourceReconnectionTool", "describesTargetReconnectionTool"), describesDescription.getPalette().getEdgeReconnectionTools().stream()
                .map(Tool::getName)
                .toList());
    }

    private static void assertNode(DiagramDescription diagramDescription, String name, SynchronizationPolicy synchronizationPolicy) {
        var matchingNodes = diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> name.equals(nodeDescription.getName()))
                .toList();
        assertEquals(List.of(synchronizationPolicy), matchingNodes.stream().map(DiagramElementDescription::getSynchronizationPolicy).toList(), name);
    }

    private static void assertEdge(DiagramDescription diagramDescription, String name, SynchronizationPolicy synchronizationPolicy) {
        var matchingEdges = diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> name.equals(edgeDescription.getName()))
                .toList();
        assertEquals(List.of(synchronizationPolicy), matchingEdges.stream().map(DiagramElementDescription::getSynchronizationPolicy).toList(), name);
    }

    private NodeDescription getNodeDescription(DiagramDescription diagramDescription, String name) {
        return diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> name.equals(nodeDescription.getName()))
                .findFirst()
                .orElseThrow();
    }

    private static void assertComponentPortTools(NodeDescription nodeDescription) {
        var toolNames = nodeDescription.getPalette().getNodeTools().stream()
                .map(Tool::getName)
                .toList();
        assertTrue(toolNames.contains("New Input Port"));
        assertTrue(toolNames.contains("New Output Port"));
        assertTrue(toolNames.contains("New InOut Port"));
        assertFalse(toolNames.contains("New Component Port"));
        assertFalse(toolNames.contains("New Unset Port"));
    }

    private static void assertDeleteFromDiagramQuickAccessTool(NodeDescription nodeDescription) {
        var quickAccessTools = nodeDescription.getPalette().getQuickAccessTools();
        assertEquals(List.of("Delete from Diagram"), quickAccessTools.stream().map(Tool::getName).toList());
        var deleteView = assertInstanceOf(DeleteView.class, quickAccessTools.get(0).getBody().get(0));
        assertEquals("aql:selectedNode", deleteView.getViewExpression());
    }
}

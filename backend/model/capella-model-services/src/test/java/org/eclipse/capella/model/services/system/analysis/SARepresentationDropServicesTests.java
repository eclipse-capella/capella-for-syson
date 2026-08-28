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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.layoutdata.DiagramLayoutData;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests for SAB drop services.
 *
 * @author mbats
 * @author tbezierslafosse
 */
public class SARepresentationDropServicesTests extends AbstractSemanticTests {

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final SARepresentationMutationService mutationService = new SARepresentationMutationService();

    @Test
    public void createFunctionalExchangeWhenDroppedShouldRevealDependenciesInSpecificationOrder() {
        var structurePackage = this.getSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = this.transverseMutationService.createFunction(system);
        var targetFunction = this.transverseMutationService.createFunction(system);

        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(sourceFunction, targetFunction);
        var sourcePort = new TransverseQueryService().getFunctionalExchangeSource(functionalExchange);
        var targetPort = new TransverseQueryService().getFunctionalExchangeTarget(functionalExchange);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext(structurePackage);

        new SARepresentationDropServices(null, diagramServices.elementService, diagramServices.exposeService)
                .dropIntoDiagramFromExplorer(functionalExchange, null, null, diagramContext, Map.of());

        assertEquals(List.of(system, sourceFunction, targetFunction, sourcePort, targetPort, functionalExchange), diagramServices.createdElements);
        assertEquals(List.of(NodeContainmentKind.CHILD_NODE, NodeContainmentKind.CHILD_NODE, NodeContainmentKind.CHILD_NODE, NodeContainmentKind.BORDER_NODE,
                NodeContainmentKind.BORDER_NODE, NodeContainmentKind.CHILD_NODE), diagramServices.containmentKinds);
    }

    @Test
    public void createFunctionalExchangeWhenDroppedWithAnUnallocatedEndpointShouldNotCreatePartialViews() {
        var structurePackage = this.getSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = this.transverseMutationService.createFunction(system);
        var targetFunction = this.transverseMutationService.createFunction(system);
        this.transverseMutationService.deletePerformedActionUsage(system, targetFunction);

        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(sourceFunction, targetFunction);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext(structurePackage);

        new SARepresentationDropServices(null, diagramServices.elementService, diagramServices.exposeService)
                .dropIntoDiagramFromExplorer(functionalExchange, null, null, diagramContext, Map.of());

        assertEquals(List.of(), diagramServices.createdElements);
        assertEquals(List.of(), diagramContext.viewCreationRequests());
    }

    @Test
    public void createFunctionalExchangeWhenDroppedTwiceShouldReuseAlreadyRequestedDependencies() {
        var structurePackage = this.getSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var sourceFunction = this.transverseMutationService.createFunction(system);
        var targetFunction = this.transverseMutationService.createFunction(system);

        FlowUsage functionalExchange = this.transverseMutationService.createFunctionalExchange(sourceFunction, targetFunction);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext(structurePackage);
        new SARepresentationDropServices(null, diagramServices.elementService, diagramServices.exposeService)
                .dropIntoDiagramFromExplorer(functionalExchange, null, null, diagramContext, Map.of());
        diagramServices.createdElements.clear();
        diagramServices.containmentKinds.clear();

        new SARepresentationDropServices(null, diagramServices.elementService, diagramServices.exposeService)
                .dropIntoDiagramFromExplorer(functionalExchange, null, null, diagramContext, Map.of());

        assertEquals(List.of(functionalExchange), diagramServices.createdElements);
    }

    @Test
    public void createRequirementWhenDroppedShouldCreateOnlyRequirementView() {
        var structurePackage = this.getSystemAnalysisStructurePackage();
        RequirementUsage requirement = this.transverseMutationService.createRequirement(structurePackage);
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext(structurePackage);

        new SARepresentationDropServices(null, diagramServices.elementService, diagramServices.exposeService)
                .dropIntoDiagramFromExplorer(requirement, null, null, diagramContext, Map.of());

        assertEquals(List.of(requirement), diagramServices.createdElements);
        assertEquals(List.of(NodeContainmentKind.CHILD_NODE), diagramServices.containmentKinds);
    }

    @Test
    public void createComponentWhenDroppedWithoutSemanticParentViewShouldRevealItInTheDropTarget() {
        var structurePackage = this.getSystemAnalysisStructurePackage();
        var system = this.getSystem(structurePackage);
        var component = this.transverseMutationService.createComponent(system);
        component.setDeclaredName("C 1");
        var diagramServices = new RecordingDiagramServices();
        var diagramContext = this.createDiagramContext(structurePackage);

        new SARepresentationDropServices(null, diagramServices.elementService, diagramServices.exposeService)
                .dropIntoDiagramFromExplorer(component, "system node", null, diagramContext, Map.of());

        assertEquals(List.of(component), diagramServices.createdElements);
    }

    private DiagramContext createDiagramContext(Package structurePackage) {
        var diagramLayoutData = new DiagramLayoutData(Map.of(), Map.of(), Map.of(), false);
        return new DiagramContext(Diagram.newDiagram("diagram").targetObjectId(structurePackage.getElementId()).descriptionId("diagramDescription")
                .nodes(List.of())
                .edges(List.of())
                .style(DiagramStyle.newDiagramStyle().build())
                .layoutData(diagramLayoutData).build());
    }

    private Package getSystemAnalysisStructurePackage() {
        return this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
    }

    private PartUsage getSystem(Package structurePackage) {
        return structurePackage.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(partUsage -> "system".equals(partUsage.getDeclaredName()))
                .findFirst()
                .orElseThrow();
    }

    private static final class RecordingDiagramServices {

        private final List<Element> createdElements = new ArrayList<>();

        private final List<NodeContainmentKind> containmentKinds = new ArrayList<>();

        private final DiagramMutationElementService elementService = mock(DiagramMutationElementService.class);

        private final DiagramMutationExposeService exposeService = mock(DiagramMutationExposeService.class);

        private RecordingDiagramServices() {
            doAnswer(invocation -> this.recordView(invocation.getArgument(0), invocation.getArgument(2), NodeContainmentKind.CHILD_NODE))
                    .when(this.elementService).createView(any(Element.class), any(), any(DiagramContext.class), any(), any());
            doAnswer(invocation -> this.recordView(invocation.getArgument(0), invocation.getArgument(4), invocation.getArgument(5)))
                    .when(this.elementService).createView(any(Element.class), any(String.class), any(String.class), any(), any(DiagramContext.class), any(NodeContainmentKind.class));
            when(this.elementService.getBorderNodeDescriptionIdForRendering(any(Element.class), any(), any(DiagramContext.class), any(), any()))
                    .thenReturn(Optional.of("borderDescription"));
            doAnswer(invocation -> this.recordExposure(invocation.getArgument(0))).when(this.exposeService)
                    .expose(any(Element.class), any(), any(DiagramContext.class), any(), any());
        }

        private ViewCreationRequest recordView(Element element, DiagramContext diagramContext, NodeContainmentKind containmentKind) {
            this.createdElements.add(element);
            this.containmentKinds.add(containmentKind);
            ViewCreationRequest request = ViewCreationRequest.newViewCreationRequest()
                    .parentElementId("diagram")
                    .descriptionId("description")
                    .targetObjectId(element.getElementId())
                    .containmentKind(containmentKind)
                    .build();
            diagramContext.viewCreationRequests().add(request);
            return request;
        }

        private Element recordExposure(Element element) {
            this.createdElements.add(element);
            this.containmentKinds.add(NodeContainmentKind.CHILD_NODE);
            return element;
        }
    }
}

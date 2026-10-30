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
package org.eclipse.capella.diagram.common.view.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewModifier;
import org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.interpreter.Result;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.emf.api.IViewAQLInterpreterFactory;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks direct connectivity in both directions, without expanding unrelated relations.
 *
 * @author jgout
 */
public class ConnectedElementsServiceTests {

    private final IViewDiagramDescriptionSearchService descriptionSearchService = mock(IViewDiagramDescriptionSearchService.class);

    private final IViewAQLInterpreterFactory interpreterFactory = mock(IViewAQLInterpreterFactory.class);

    private final IObjectSearchService objectSearchService = mock(IObjectSearchService.class);

    private final IIdentityService identityService = mock(IIdentityService.class);

    private final RelatedElementsService renderer = mock(RelatedElementsService.class);

    private final IEditingContext editingContext = mock(IEditingContext.class);

    private final Node selectedNode = mock(Node.class);

    private final AQLInterpreter interpreter = mock(AQLInterpreter.class);

    private final ConnectedElementsService service = new ConnectedElementsService(this.descriptionSearchService, this.interpreterFactory,
            this.objectSearchService, this.identityService, this.renderer);

    private DiagramContext context(List<Edge> edges) {
        var description = DiagramFactory.eINSTANCE.createDiagramDescription();
        var edge = DiagramFactory.eINSTANCE.createEdgeDescription();
        edge.setIsDomainBasedEdge(true);
        edge.setSemanticCandidatesExpression("relations");
        edge.setSourceExpression("sources");
        edge.setTargetExpression("targets");
        description.getEdgeDescriptions().add(edge);
        var view = ViewFactory.eINSTANCE.createView();
        view.getDescriptions().add(description);
        when(this.descriptionSearchService.findById(this.editingContext, "description")).thenReturn(Optional.of(description));
        when(this.interpreterFactory.createInterpreter(this.editingContext, view)).thenReturn(this.interpreter);
        when(this.identityService.getId(any(Element.class))).thenAnswer(invocation -> ((Element) invocation.getArgument(0)).getElementId());
        return new DiagramContext(Diagram.newDiagram("diagram").descriptionId("description").targetObjectId("root")
                .nodes(List.of()).edges(edges).style(DiagramStyle.newDiagramStyle().build()).build());
    }

    private void relations(Map<Element, List<Element>> sources, Map<Element, List<Element>> targets) {
        when(this.interpreter.evaluateExpression(anyMap(), anyString())).thenAnswer(invocation -> {
            Map<String, Object> variables = invocation.getArgument(0);
            String expression = invocation.getArgument(1);
            List<?> elements = switch (expression) {
                case "relations" -> List.copyOf(sources.keySet());
                case "sources" -> sources.get(variables.get("self"));
                case "targets" -> targets.get(variables.get("self"));
                default -> List.of();
            };
            var result = mock(Result.class);
            when(result.asObjects()).thenReturn(Optional.of(List.copyOf(elements)));
            return result;
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void showImmediateNeighboursInBothDirectionsAndRevealHiddenRelations(boolean reverse) {
        var selected = SysmlFactory.eINSTANCE.createActionUsage();
        var neighbour = SysmlFactory.eINSTANCE.createActionUsage();
        var distant = SysmlFactory.eINSTANCE.createActionUsage();
        var connected = SysmlFactory.eINSTANCE.createFlowUsage();
        var unrelated = SysmlFactory.eINSTANCE.createFlowUsage();
        var hidden = mock(Edge.class);
        when(hidden.getState()).thenReturn(ViewModifier.Hidden);
        when(hidden.getId()).thenReturn("hidden-edge");
        when(hidden.getTargetObjectId()).thenReturn(connected.getElementId());
        var context = this.context(List.of(hidden));
        Element source = selected;
        Element target = neighbour;
        if (reverse) {
            source = neighbour;
            target = selected;
        }
        this.relations(Map.of(connected, List.of(source), unrelated, List.of(neighbour)), Map.of(connected, List.of(target), unrelated, List.of(distant)));

        this.service.showConnectedElements(selected, this.editingContext, context, this.selectedNode, Map.of());

        verify(this.renderer).showElementsWithOwners(Set.of(selected, neighbour), this.editingContext, context, Map.of());
        verifyNoMoreInteractions(this.renderer);
        assertThat(context.diagramEvents()).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new HideDiagramElementEvent(java.util.Set.of("hidden-edge"), false));
    }

    @Test
    public void sharedAndMultipleTargetsAreRenderedOnce() {
        var capability = SysmlFactory.eINSTANCE.createOccurrenceUsage();
        var participant = SysmlFactory.eINSTANCE.createPartUsage();
        var otherParticipant = SysmlFactory.eINSTANCE.createPartUsage();
        var relation = SysmlFactory.eINSTANCE.createOccurrenceUsage();
        var duplicate = SysmlFactory.eINSTANCE.createOccurrenceUsage();
        var context = this.context(List.of());
        this.relations(Map.of(relation, List.of(capability), duplicate, List.of(capability)),
                Map.of(relation, List.of(participant, otherParticipant), duplicate, List.of(participant)));

        this.service.showConnectedElements(capability, this.editingContext, context, this.selectedNode, Map.of());

        verify(this.renderer).showElementsWithOwners(Set.of(capability, participant, otherParticipant), this.editingContext, context, Map.of());
        verifyNoMoreInteractions(this.renderer);
    }

    @Test
    public void componentMatchesRelationsOfItsPorts() {
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        var port = SysmlFactory.eINSTANCE.createPortUsage();
        var membership = SysmlFactory.eINSTANCE.createOwningMembership();
        component.getOwnedRelationship().add(membership);
        membership.getOwnedRelatedElement().add(port);
        var arcadia = SysmlFactory.eINSTANCE.createPackage();
        arcadia.setDeclaredName("Arcadia");
        var definition = SysmlFactory.eINSTANCE.createPortDefinition();
        definition.setDeclaredName("ComponentPort");
        var definitionMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        arcadia.getOwnedRelationship().add(definitionMembership);
        definitionMembership.getOwnedRelatedElement().add(definition);
        var typing = SysmlFactory.eINSTANCE.createFeatureTyping();
        typing.setTypedFeature(port);
        port.getOwnedRelationship().add(typing);
        typing.setType(definition);
        var neighbour = SysmlFactory.eINSTANCE.createPortUsage();
        var relation = SysmlFactory.eINSTANCE.createInterfaceUsage();
        var context = this.context(List.of());
        this.relations(Map.of(relation, List.of(port)), Map.of(relation, List.of(neighbour)));

        this.service.showConnectedElements(component, this.editingContext, context, this.selectedNode, Map.of());

        verify(this.renderer).showElementsWithOwners(Set.of(port, neighbour), this.editingContext, context, Map.of());
        verifyNoMoreInteractions(this.renderer);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void edgeNeighboursRevealTheirEndpointsAndHiddenEdgesWithoutExpandingOtherConnections(boolean reverse) {
        var requirement = SysmlFactory.eINSTANCE.createRequirementUsage();
        var exchange = SysmlFactory.eINSTANCE.createInterfaceUsage();
        var describes = SysmlFactory.eINSTANCE.createAllocationUsage();
        var source = SysmlFactory.eINSTANCE.createPortUsage();
        var target = SysmlFactory.eINSTANCE.createPortUsage();
        var distant = SysmlFactory.eINSTANCE.createPortUsage();
        var unrelated = SysmlFactory.eINSTANCE.createInterfaceUsage();
        var context = this.context(List.of(this.hiddenEdge(describes), this.hiddenEdge(exchange), this.hiddenEdge(unrelated)));
        this.useRelationsAsEdgeEndpoints();
        Element describesSource = requirement;
        Element describesTarget = exchange;
        if (reverse) {
            describesSource = exchange;
            describesTarget = requirement;
        }
        this.relations(Map.of(describes, List.of(describesSource), exchange, List.of(source), unrelated, List.of(target)),
                Map.of(describes, List.of(describesTarget), exchange, List.of(target), unrelated, List.of(distant)));

        this.service.showConnectedElements(requirement, this.editingContext, context, this.selectedNode, Map.of());

        verify(this.renderer).showElementsWithOwners(Set.of(requirement, source, target), this.editingContext, context, Map.of());
        verifyNoMoreInteractions(this.renderer);
        assertThat(context.diagramEvents()).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new HideDiagramElementEvent(Set.of(describes.getElementId(), exchange.getElementId()), false));
    }

    @Test
    public void nestedEdgeDependenciesStopAtCyclesAndDeduplicateSharedEndpoints() {
        var requirement = SysmlFactory.eINSTANCE.createRequirementUsage();
        var describes = SysmlFactory.eINSTANCE.createAllocationUsage();
        var first = SysmlFactory.eINSTANCE.createInterfaceUsage();
        var second = SysmlFactory.eINSTANCE.createInterfaceUsage();
        var port = SysmlFactory.eINSTANCE.createPortUsage();
        var context = this.context(List.of(this.hiddenEdge(first), this.hiddenEdge(second)));
        this.useRelationsAsEdgeEndpoints();
        this.relations(Map.of(describes, List.of(requirement), first, List.of(second), second, List.of(first)),
                Map.of(describes, List.of(first), first, List.of(port), second, List.of(port)));

        this.service.showConnectedElements(requirement, this.editingContext, context, this.selectedNode, Map.of());

        verify(this.renderer).showElementsWithOwners(Set.of(requirement, port), this.editingContext, context, Map.of());
        verifyNoMoreInteractions(this.renderer);
        assertThat(context.diagramEvents()).usingRecursiveFieldByFieldElementComparator()
                .containsExactly(new HideDiagramElementEvent(Set.of(first.getElementId(), second.getElementId()), false));
    }

    @Test
    public void nodesThatAlsoProvideRelationSemanticsAreNotExpandedAsEdgeDependencies() {
        var requirement = SysmlFactory.eINSTANCE.createRequirementUsage();
        var describes = SysmlFactory.eINSTANCE.createAllocationUsage();
        var activity = SysmlFactory.eINSTANCE.createActionUsage();
        var parent = SysmlFactory.eINSTANCE.createActionUsage();
        var context = this.context(List.of());
        this.relations(Map.of(describes, List.of(requirement), activity, List.of(activity)),
                Map.of(describes, List.of(activity), activity, List.of(parent)));

        this.service.showConnectedElements(requirement, this.editingContext, context, this.selectedNode, Map.of());

        verify(this.renderer).showElementsWithOwners(Set.of(requirement, activity), this.editingContext, context, Map.of());
        verifyNoMoreInteractions(this.renderer);
    }

    private void useRelationsAsEdgeEndpoints() {
        var description = this.descriptionSearchService.findById(this.editingContext, "description").orElseThrow();
        var edge = description.getEdgeDescriptions().getFirst();
        edge.getSourceDescriptions().add(edge);
        edge.getTargetDescriptions().add(edge);
    }

    private Edge hiddenEdge(Element relation) {
        var edge = mock(Edge.class);
        when(edge.getId()).thenReturn(relation.getElementId());
        when(edge.getTargetObjectId()).thenReturn(relation.getElementId());
        when(edge.getState()).thenReturn(ViewModifier.Hidden);
        return edge;
    }
}

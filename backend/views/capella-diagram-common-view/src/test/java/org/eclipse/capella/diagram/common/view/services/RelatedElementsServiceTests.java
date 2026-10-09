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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks pending view deduplication and traversal of unsupported containers.
 *
 * @author jgout
 */
public class RelatedElementsServiceTests {

    private final DiagramMutationElementService elementService = mock(DiagramMutationElementService.class);

    private final IIdentityService identityService = mock(IIdentityService.class);

    private final RelatedElementsService service = new RelatedElementsService(mock(CommonQueryService.class), this.elementService, this.identityService);

    private DiagramContext context() {
        when(this.identityService.getId(any(Element.class))).thenAnswer(invocation -> ((Element) invocation.getArgument(0)).getElementId());
        return new DiagramContext(Diagram.newDiagram("diagram").descriptionId("description").targetObjectId("root")
                .nodes(List.of()).edges(List.of()).style(DiagramStyle.newDiagramStyle().build()).build());
    }

    private void createViews(DiagramContext context) {
        doAnswer(invocation -> {
            Element element = invocation.getArgument(0);
            Object parent = invocation.getArgument(3);
            String parentId = context.diagram().getId();
            if (parent instanceof ViewCreationRequest request) {
                parentId = request.getTargetObjectId();
            }
            var request = ViewCreationRequest.newViewCreationRequest().targetObjectId(element.getElementId())
                    .descriptionId("node").parentElementId(parentId).containmentKind(NodeContainmentKind.CHILD_NODE).build();
            context.viewCreationRequests().add(request);
            return request;
        }).when(this.elementService).createView(any(Element.class), isNull(), same(context), any(), any());
    }

    private void addMember(Namespace parent, Element child) {
        var membership = SysmlFactory.eINSTANCE.createOwningMembership();
        parent.getOwnedRelationship().add(membership);
        membership.getOwnedRelatedElement().add(child);
    }

    @Test
    public void recursivePopulationDoesNotDuplicatePendingViews() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        var nested = SysmlFactory.eINSTANCE.createPartUsage();
        this.addMember(root, component);
        this.addMember(component, nested);
        var context = this.context();
        this.createViews(context);

        this.service.showContainedElements(root, true, null, context, null, Map.of());
        this.service.showContainedElements(root, true, null, context, null, Map.of());

        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getTargetObjectId)
                .containsExactly(component.getElementId(), nested.getElementId());
        assertThat(context.viewCreationRequests().getLast().getParentElementId()).isEqualTo(component.getElementId());
    }

    @Test
    public void unsupportedContainerIsTraversedDuringRecursivePopulation() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var container = SysmlFactory.eINSTANCE.createPackage();
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        this.addMember(root, container);
        this.addMember(container, component);
        var context = this.context();
        this.createViews(context);
        when(this.elementService.createView(same(container), isNull(), same(context), isNull(), any())).thenReturn(null);

        this.service.showContainedElements(root, true, null, context, null, Map.of());

        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getTargetObjectId).containsExactly(component.getElementId());
        assertThat(context.viewCreationRequests().getFirst().getParentElementId()).isEqualTo("diagram");
    }

    @Test
    @DisplayName("GIVEN nested nodes representing the same element, WHEN the inner node is populated, THEN children are created under the outer node")
    public void populationUsesTheOuterNodeForTheSameElement() {
        var root = SysmlFactory.eINSTANCE.createPackage();
        var child = SysmlFactory.eINSTANCE.createPackage();
        this.addMember(root, child);
        var innerNode = mock(Node.class);
        when(innerNode.getId()).thenReturn("inner");
        when(innerNode.getTargetObjectId()).thenReturn(root.getElementId());
        var outerNode = mock(Node.class);
        when(outerNode.getId()).thenReturn("outer");
        when(outerNode.getTargetObjectId()).thenReturn(root.getElementId());
        when(outerNode.getChildNodes()).thenReturn(List.of(innerNode));
        var context = new DiagramContext(Diagram.newDiagram(this.context().diagram()).nodes(List.of(outerNode)).build());
        this.createViews(context);

        this.service.showContainedElements(root, false, null, context, innerNode, Map.of());

        verify(this.elementService).createView(same(child), isNull(), same(context), same(outerNode), any());
        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getTargetObjectId).containsExactly(child.getElementId());
    }

    @Test
    @DisplayName("GIVEN a cyclic element graph, WHEN population is recursive, THEN each element is visited only once")
    public void recursivePopulationStopsAtAlreadyVisitedElements() {
        var root = mock(Namespace.class);
        when(root.getElementId()).thenReturn("root");
        when(root.getOwnedMember()).thenReturn(new BasicEList<>(List.of(root)));
        var context = this.context();
        this.createViews(context);

        this.service.showContainedElements(root, true, null, context, null, Map.of());

        verify(root).getOwnedMember();
        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getTargetObjectId).containsExactly("root");
    }

    @Test
    @DisplayName("GIVEN an element that is not a namespace, WHEN population is recursive, THEN no child views are created")
    public void populationIgnoresElementsWithoutMembers() {
        var element = SysmlFactory.eINSTANCE.createOwningMembership();
        var context = this.context();

        this.service.showContainedElements(element, true, null, context, null, Map.of());

        assertThat(context.viewCreationRequests()).isEmpty();
        verifyNoInteractions(this.elementService);
    }
}

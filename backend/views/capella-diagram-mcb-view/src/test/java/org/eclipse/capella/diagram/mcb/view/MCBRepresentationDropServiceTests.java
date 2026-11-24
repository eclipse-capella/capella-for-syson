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
package org.eclipse.capella.diagram.mcb.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.diagram.mcb.view.services.MCBRepresentationDropService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonMoveService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.layoutdata.DiagramLayoutData;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.junit.jupiter.api.Test;

/**
 * Checks Mission, Capability, Requirement, and Actor explorer drops in MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class MCBRepresentationDropServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final CommonCreationService creationService = new CommonCreationService();

    private final DiagramMutationElementService elementService = mock(DiagramMutationElementService.class);

    private final MCBRepresentationDropService dropService = new MCBRepresentationDropService(this.elementService, new CommonMoveService()::moveSemanticElement);

    @Test
    public void dropIntoDiagramFromExplorerWhenSupportedElementShouldCreateOnlyARootView() {
        var perspective = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var diagramContext = this.createDiagramContext();

        for (var element : this.createDroppableElements(perspective)) {
            var owner = element.getOwner();
            var relationships = List.copyOf(element.getOwnedRelationship());

            assertThat(this.dropService.dropIntoDiagramFromExplorer(element, null, diagramContext, Map.of())).isSameAs(element);

            verify(this.elementService).createView(element, null, diagramContext, diagramContext.diagram(), Map.of());
            assertThat(element.getOwner()).isSameAs(owner);
            assertThat(element.getOwnedRelationship()).containsExactlyElementsOf(relationships);
        }
        verifyNoMoreInteractions(this.elementService);
    }

    @Test
    public void dropIntoDiagramFromExplorerWhenElementIsInAnotherPerspectiveShouldNotCreateViews() {
        var diagramContext = this.createDiagramContext();
        var perspectives = List.of(this.capellaModel.getOperationalAnalysisPerspective().getElement(),
                this.capellaModel.getLogicalArchitecturePerspective().getElement(), this.capellaModel.getPhysicalArchitecturePerspective().getElement());

        for (var perspective : perspectives) {
            for (var element : this.createDroppableElements(perspective)) {
                var owner = element.getOwner();
                this.dropService.dropIntoDiagramFromExplorer(element, null, diagramContext, Map.of());
                assertThat(element.getOwner()).isSameAs(owner);
            }
        }

        verifyNoInteractions(this.elementService);
        assertThat(diagramContext.viewCreationRequests()).isEmpty();
    }

    @Test
    public void dropIntoActorFromDiagramWhenInvalidTargetShouldNotModifyTheModel() {
        var context = this.createDiagramContext();
        var actor = this.creationService.createActor(this.capellaModel.getSystemAnalysisPerspective().getElement());
        var child = this.creationService.createActor(actor);
        var owner = actor.getOwner();
        var sourceNode = this.createNode(actor, List.of());

        this.dropService.dropIntoActorFromDiagram(actor, sourceNode, actor, null, null, context, Map.of());
        this.dropService.dropIntoActorFromDiagram(actor, sourceNode, child, null, null, context, Map.of());
        var human = this.creationService.createActor(owner);
        new CommonUpdateService().setBooleanAttribute(human, "Arcadia::Component", "isHuman", true);
        this.dropService.dropIntoActorFromDiagram(actor, sourceNode, human, null, null, context, Map.of());

        var capability = this.creationService.createCapability(owner);
        this.dropService.dropIntoActorFromDiagram(actor, sourceNode, capability, null, null, context, Map.of());

        assertThat(actor.getOwner()).isSameAs(owner);
        verifyNoInteractions(this.elementService);
    }

    @Test
    public void dropIntoActorFromDiagramShouldPreserveOnlyDisplayedDescendants() {
        var context = this.createDiagramContext();
        var perspective = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var actor = this.creationService.createActor(perspective);
        var child = this.creationService.createActor(actor);
        var grandchild = this.creationService.createActor(child);
        this.creationService.createActor(actor);
        var target = this.creationService.createActor(perspective);
        var childNode = this.createNode(child, List.of(this.createNode(grandchild, List.of())));
        var sourceNode = this.createNode(actor, List.of(childNode));
        var targetNode = this.createNode(target, List.of());
        context = new DiagramContext(Diagram.newDiagram(context.diagram()).nodes(List.of(sourceNode, targetNode)).build());
        this.recordViews();

        this.dropService.dropIntoActorFromDiagram(actor, sourceNode, target, targetNode, null, context, Map.of());

        assertThat(actor.getOwner()).isSameAs(target);
        assertThat(child.getOwner()).isSameAs(actor);
        assertThat(grandchild.getOwner()).isSameAs(child);
        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getTargetObjectId)
                .containsExactly(actor.getElementId(), child.getElementId(), grandchild.getElementId());
        assertThat(context.viewDeletionRequests()).hasSize(1);
    }

    @Test
    public void dropIntoDiagramFromExplorerShouldUseClosestDisplayedAncestor() {
        var context = this.createDiagramContext();
        var root = this.creationService.createActor(this.capellaModel.getSystemAnalysisPerspective().getElement());
        var ancestor = this.creationService.createActor(root);
        var parent = this.creationService.createActor(ancestor);
        var actor = this.creationService.createActor(parent);
        var rootNode = this.createNode(root, List.of());
        var ancestorNode = this.createNode(ancestor, List.of());
        context = new DiagramContext(Diagram.newDiagram(context.diagram()).nodes(List.of(rootNode, ancestorNode)).build());
        this.recordViews();

        var directChild = this.creationService.createActor(ancestor);
        this.dropService.dropIntoDiagramFromExplorer(actor, null, context, Map.of());
        this.dropService.dropIntoDiagramFromExplorer(directChild, null, context, Map.of());

        assertThat(actor.getOwner()).isSameAs(parent);
        assertThat(context.viewCreationRequests()).hasSize(2);
        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getParentElementId)
                .containsOnly(ancestorNode.getId());
        assertThat(context.viewCreationRequests()).extracting(ViewCreationRequest::getTargetObjectId)
                .containsExactly(actor.getElementId(), directChild.getElementId());
    }

    @Test
    public void dropIntoDiagramFromDiagramWhenActorIsNestedShouldMoveItToStructureAndShowItAtRoot() {
        var context = this.createDiagramContext();
        var structure = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        var parent = this.creationService.createActor(structure);
        var actor = this.creationService.createActor(parent);
        var child = this.creationService.createActor(actor);
        var sourceNode = this.createNode(actor, List.of());
        this.recordViews();

        this.dropService.dropIntoDiagramFromDiagram(actor, sourceNode, null, context, Map.of());

        assertThat(actor.getOwner()).isSameAs(structure);
        assertThat(child.getOwner()).isSameAs(actor);
        assertThat(context.viewCreationRequests().getFirst().getParentElementId()).isEqualTo(context.diagram().getId());
        assertThat(context.viewDeletionRequests()).extracting(ViewDeletionRequest::getElementId)
                .containsExactly(sourceNode.getId());
    }

    private Node createNode(Element element, List<Node> children) {
        var node = mock(Node.class);
        when(node.getId()).thenReturn("view-" + element.getElementId());
        when(node.getTargetObjectId()).thenReturn(element.getElementId());
        when(node.getDescriptionId()).thenReturn("actor");
        when(node.getChildNodes()).thenReturn(children);
        return node;
    }

    private void recordViews() {
        doAnswer(invocation -> {
            Element element = invocation.getArgument(0);
            DiagramContext context = invocation.getArgument(2);
            Object parent = invocation.getArgument(3);
            String parentId = context.diagram().getId();
            if (parent instanceof Node node) {
                parentId = node.getId();
            }
            var request = ViewCreationRequest.newViewCreationRequest().parentElementId(parentId).descriptionId("actor")
                    .targetObjectId(element.getElementId()).containmentKind(NodeContainmentKind.CHILD_NODE).build();
            context.viewCreationRequests().add(request);
            return request;
        }).when(this.elementService).createView(any(), any(), any(), any(), any());
    }

    private List<Element> createDroppableElements(Package perspective) {
        var actor = this.creationService.createActor(perspective);
        return List.of(this.creationService.createMission(perspective), this.creationService.createCapability(perspective),
                this.creationService.createRequirement(perspective), actor, this.creationService.createActor(actor));
    }

    private DiagramContext createDiagramContext() {
        var capabilities = this.capellaModel.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        return new DiagramContext(Diagram.newDiagram("diagram").targetObjectId(capabilities.getElementId()).descriptionId("mcb")
                .nodes(List.of()).edges(List.of()).layoutData(new DiagramLayoutData(Map.of(), Map.of(), Map.of(), false)).style(DiagramStyle.newDiagramStyle().build()).build());
    }
}

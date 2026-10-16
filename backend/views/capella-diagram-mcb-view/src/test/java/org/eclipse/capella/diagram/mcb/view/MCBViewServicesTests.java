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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.capella.diagram.mcb.view.nodes.actor.SystemActorNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.model.transverse.services.CommonViewCreationRequestSubtreeService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises MCB reveal and move services against real semantic models.
 *
 * @author tbezierslafosse
 */
public class MCBViewServicesTests {

    private static final SemanticDataTestFixture FIXTURE = new SemanticDataTestFixture();

    private final CommonCreationService creation = new CommonCreationService();

    private final MCBViewQueryService queries = new MCBViewQueryService();

    private final IEditingContext editingContext = mock(IEditingContext.class);

    private final IObjectSearchService objectSearchService = mock(IObjectSearchService.class);

    private final IIdentityService identityService = mock(IIdentityService.class);

    private CapellaModel model;

    private MCBViewDropService dropService;

    @BeforeEach
    public void setUp() {
        this.model = FIXTURE.createCapellaModel();
        this.dropService = new MCBViewDropService(this.objectSearchService, this.identityService);
        when(this.identityService.getId(any())).thenAnswer(invocation -> ((Element) invocation.getArgument(0)).getElementId());
        when(this.objectSearchService.getObject(this.editingContext, "context"))
                .thenReturn(Optional.of(this.model.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement()));
    }

    @Test
    public void dropFromExplorerShouldRevealAncestorsOnceWithoutChangingSemantics() {
        var context = this.model.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        var parent = this.creation.createActor(context);
        var child = this.creation.createHumanActor(parent);
        var before = EcoreUtil.copy(this.model.getSystemAnalysisPerspective().getElement());
        var diagramContext = this.diagramContext(List.of());
        var descriptions = this.descriptions(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME);
        this.dropService.dropFromExplorer(child, null, this.editingContext, diagramContext, descriptions);
        this.dropService.dropFromExplorer(child, null, this.editingContext, diagramContext, descriptions);
        assertThat(diagramContext.viewCreationRequests()).hasSize(2);
        var parentRequest = diagramContext.viewCreationRequests().getFirst();
        var childRequest = diagramContext.viewCreationRequests().getLast();
        assertThat(parentRequest.getTargetObjectId()).isEqualTo(parent.getElementId());
        assertThat(childRequest.getParentElementId()).isEqualTo(new CommonViewCreationRequestSubtreeService().getNodeIdFromViewCreationRequest(parentRequest));
        assertThat(EcoreUtil.equals(before, this.model.getSystemAnalysisPerspective().getElement())).isTrue();
    }

    @Test
    public void dropFromExplorerShouldReuseVisibleParentsAndRejectInvalidChainsAtomically() {
        var context = this.model.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        var parent = this.creation.createActor(context);
        var child = this.creation.createHumanActor(parent);
        var diagramContext = this.diagramContext(List.of(this.node(parent, List.of())));
        this.dropService.dropFromExplorer(child, null, this.editingContext, diagramContext, this.descriptions(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
        assertThat(diagramContext.viewCreationRequests()).hasSize(1);
        assertThat(diagramContext.viewCreationRequests().getFirst().getParentElementId()).isEqualTo("node-" + parent.getElementId());
        var invalidContext = this.diagramContext(List.of());
        this.dropService.dropFromExplorer(child, null, this.editingContext, invalidContext, Map.of());
        assertThat(invalidContext.viewCreationRequests()).isEmpty();
        new CommonUpdateService().setBooleanAttribute(parent, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_COMPONENT, CommonQueryService.ARCADIA_IS_HUMAN, true);
        this.dropService.dropFromExplorer(child, null, this.editingContext, invalidContext, this.descriptions(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
        assertThat(invalidContext.viewCreationRequests()).isEmpty();
    }

    @Test
    public void dropFromExplorerShouldAcceptTheFourNodeTypesAndRejectOtherPerspectives() {
        var context = this.model.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        var elements = List.of(this.creation.createHumanActor(context), this.creation.createMission(context), this.creation.createCapability(context), this.creation.createRequirement(context));
        var diagramContext = this.diagramContext(List.of());
        var descriptions = this.descriptions(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME, "MissionNodeDescription", "CapabilityNodeDescription", "RequirementNodeDescription");
        elements.forEach(element -> this.dropService.dropFromExplorer(element, null, this.editingContext, diagramContext, descriptions));
        assertThat(diagramContext.viewCreationRequests()).hasSize(4);
        var foreignActor = this.creation.createActor(this.model.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        this.dropService.dropFromExplorer(foreignActor, null, this.editingContext, diagramContext, descriptions);
        assertThat(diagramContext.viewCreationRequests()).hasSize(4);
        assertThat(this.queries.getCapabilityCandidates(context)).hasSize(1);
        assertThat(this.queries.getMissionCandidates(context)).hasSize(1);
        assertThat(this.queries.getRequirementCandidates(context)).hasSize(1);
    }

    @Test
    public void dropIntoActorShouldPreserveVisibleDescendantsAndAllowReturnToBackground() {
        var context = this.model.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        var parent = this.creation.createActor(context);
        var moved = this.creation.createActor(context);
        var child = this.creation.createHumanActor(moved);
        var movedNode = this.node(moved, List.of(this.node(child, List.of())));
        var targetNode = this.node(parent, List.of());
        var diagramContext = this.diagramContext(List.of(targetNode, movedNode));
        var descriptions = this.descriptions(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME);
        this.dropService.dropIntoActor(moved, movedNode, parent, targetNode, this.editingContext, diagramContext, descriptions);
        assertThat(moved.getOwner()).isSameAs(parent);
        assertThat(diagramContext.viewCreationRequests()).hasSize(2);
        assertThat(diagramContext.viewDeletionRequests()).hasSize(1);
        var background = this.diagramContext(List.of(movedNode));
        this.dropService.dropOnBackground(moved, movedNode, this.editingContext, background, descriptions);
        assertThat(moved.getOwner()).isSameAs(this.model.getSystemAnalysisPerspective().getStructurePackage().getElement());
        assertThat(background.viewCreationRequests()).hasSize(2);
    }

    @Test
    public void dropIntoActorShouldRejectHumanParentsWithoutChangingViewsOrModel() {
        var context = this.model.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        var human = this.creation.createHumanActor(context);
        var child = this.creation.createHumanActor(context);
        var diagramContext = this.diagramContext(List.of());
        var owner = child.getOwner();
        this.dropService.dropIntoActor(child, this.node(child, List.of()), human, this.node(human, List.of()), this.editingContext, diagramContext, this.descriptions(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
        assertThat(child.getOwner()).isSameAs(owner);
        assertThat(diagramContext.viewCreationRequests()).isEmpty();
        assertThat(diagramContext.viewDeletionRequests()).isEmpty();
    }

    private DiagramContext diagramContext(List<Node> nodes) {
        var diagram = Diagram.newDiagram("diagram").descriptionId("description").targetObjectId("context").nodes(nodes).edges(List.of())
                .style(DiagramStyle.newDiagramStyle().build()).build();
        return new DiagramContext(diagram, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> descriptions(String... names) {
        Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> result = new HashMap<>();
        for (String name : names) {
            var view = DiagramFactory.eINSTANCE.createNodeDescription();
            view.setName(name);
            var runtime = mock(NodeDescription.class);
            when(runtime.getId()).thenReturn(name);
            result.put(view, runtime);
        }
        return result;
    }

    private Node node(PartUsage actor, List<Node> children) {
        var node = mock(Node.class);
        when(node.getId()).thenReturn("node-" + actor.getElementId());
        when(node.getTargetObjectId()).thenReturn(actor.getElementId());
        when(node.getDescriptionId()).thenReturn(SystemActorNodeDescriptionProvider.NODE_DESCRIPTION_NAME);
        when(node.getChildNodes()).thenReturn(children);
        when(node.getBorderNodes()).thenReturn(List.of());
        return node;
    }
}

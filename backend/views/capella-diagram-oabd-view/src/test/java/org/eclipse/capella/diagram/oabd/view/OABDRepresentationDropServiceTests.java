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
package org.eclipse.capella.diagram.oabd.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.capella.diagram.oabd.view.services.OABDRepresentationDropService;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.diagrams.layoutdata.DiagramLayoutData;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.junit.jupiter.api.Test;

/**
 * Tests for OABD drop services.
 *
 * @author tbezierslafosse
 */
public class OABDRepresentationDropServiceTests extends AbstractSemanticTests {

    private final TransverseMutationService mutationService = new TransverseMutationService();

    @Test
    public void createOperationalActivityWhenDroppedShouldCreateItsViewInTheDropTarget() {
        var root = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var activity = this.mutationService.createOperationalActivity(root);
        var diagramServices = new RecordingDiagramServices();
        var objectSearchService = mock(IObjectSearchService.class);
        var editingContext = mock(IEditingContext.class);
        var diagramContext = this.createDiagramContext(root);
        when(objectSearchService.getObject(editingContext, root.getElementId())).thenReturn(Optional.of(root));

        new OABDRepresentationDropService(objectSearchService, diagramServices.elementService)
                .dropIntoDiagramFromExplorer(activity, "diagram node", editingContext, diagramContext, Map.of());

        assertEquals(List.of(activity), diagramServices.createdElements);
        assertEquals(List.of("diagram node"), diagramServices.parentViews);
    }

    private DiagramContext createDiagramContext(Element context) {
        return new DiagramContext(Diagram.newDiagram("diagram").targetObjectId(context.getElementId()).descriptionId("oabd")
                .nodes(List.of()).edges(List.of()).style(DiagramStyle.newDiagramStyle().build())
                .layoutData(new DiagramLayoutData(Map.of(), Map.of(), Map.of(), false)).build());
    }

    /**
     * Records requests at the graphical service boundary.
     *
     * @author tbezierslafosse
     */
    private static final class RecordingDiagramServices {
        private final List<Element> createdElements = new ArrayList<>();

        private final List<Object> parentViews = new ArrayList<>();

        private final DiagramMutationElementService elementService = mock(DiagramMutationElementService.class);

        private RecordingDiagramServices() {
            doAnswer(invocation -> {
                Element element = invocation.getArgument(0);
                this.createdElements.add(element);
                this.parentViews.add(invocation.getArgument(3));
                var request = ViewCreationRequest.newViewCreationRequest().parentElementId("diagram").descriptionId("node")
                        .targetObjectId(element.getElementId()).containmentKind(NodeContainmentKind.CHILD_NODE).build();
                ((DiagramContext) invocation.getArgument(2)).viewCreationRequests().add(request);
                return request;
            }).when(this.elementService).createView(any(Element.class), any(), any(DiagramContext.class), any(), any());
        }
    }
}

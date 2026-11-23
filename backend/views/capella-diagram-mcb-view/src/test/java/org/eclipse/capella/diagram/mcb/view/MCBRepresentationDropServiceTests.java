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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.diagram.mcb.view.services.MCBRepresentationDropService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.layoutdata.DiagramLayoutData;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.Test;

/**
 * Checks Mission, Capability, Requirement, and Actor explorer drops in MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class MCBRepresentationDropServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final CommonCreationService creationService = new CommonCreationService();

    private final DiagramMutationElementService elementService = mock(DiagramMutationElementService.class);

    private final MCBRepresentationDropService dropService = new MCBRepresentationDropService(this.elementService);

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
    public void dropIntoDiagramFromExplorerWhenUnsupportedElementShouldNotCreateViews() {
        var perspective = this.capellaModel.getSystemAnalysisPerspective().getElement();
        var diagramContext = this.createDiagramContext();
        var elements = List.of(this.creationService.createComponent(perspective), SysmlFactory.eINSTANCE.createOccurrenceUsage());

        for (var element : elements) {
            assertThat(this.dropService.dropIntoDiagramFromExplorer(element, null, diagramContext, Map.of())).isSameAs(element);
        }

        verifyNoInteractions(this.elementService);
        assertThat(diagramContext.viewCreationRequests()).isEmpty();
    }

    private List<Element> createDroppableElements(Package perspective) {
        return List.of(this.creationService.createMission(perspective), this.creationService.createCapability(perspective),
                this.creationService.createRequirement(perspective), this.creationService.createActor(perspective));
    }

    private DiagramContext createDiagramContext() {
        var capabilities = this.capellaModel.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        return new DiagramContext(Diagram.newDiagram("diagram").targetObjectId(capabilities.getElementId()).descriptionId("mcb")
                .nodes(List.of()).edges(List.of()).layoutData(new DiagramLayoutData(Map.of(), Map.of(), Map.of(), false)).style(DiagramStyle.newDiagramStyle().build()).build());
    }
}

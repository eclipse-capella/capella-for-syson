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

import java.util.List;

import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.sirius.components.view.diagram.customnodes.EllipseNodeStyleDescription;
import org.junit.jupiter.api.Test;
import org.eclipse.capella.tests.diagrams.AbstractEditableDiagramDescriptionTests;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;

/**
 * Checks the structure of the Mission and Capabilities Blank diagram.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramDescriptionTests extends AbstractEditableDiagramDescriptionTests {

    @Override
    protected IColorProvider getColorProvider(View view) {
        return new MCBViewDescriptionProvider().getColorProvider(view);
    }

    @Override
    protected IRepresentationDescriptionProvider getRepresentationDescriptionProvider() {
        return new MCBViewDiagramDescriptionProvider();
    }

    @Override
    protected IJavaServiceProvider getJavaServiceProvider() {
        return new MCBViewJavaServiceProvider();
    }
    @Test
    public void nodesShouldHaveCompletePalettesAndRemainUnsynchronized() {
        var nodes = this.diagramDescription.getNodeDescriptions();
        assertThat(nodes).extracting(node -> node.getName())
                .containsExactlyInAnyOrder("SystemActorNodeDescription", "MissionNodeDescription", "CapabilityNodeDescription", "RequirementNodeDescription");
        assertThat(nodes).allSatisfy(node -> {
            assertThat(node.getSynchronizationPolicy()).isEqualTo(SynchronizationPolicy.UNSYNCHRONIZED);
            assertThat(node.getUserResizable()).isEqualTo(UserResizableDirection.BOTH);
            assertThat(node.getPalette().getLabelEditTool()).isNotNull();
            assertThat(node.getPalette().getDeleteTool()).isNotNull();
            assertThat(node.getPalette().getQuickAccessTools()).extracting(tool -> tool.getName()).contains("Delete from Diagram");
            assertThat(node.getPalette().getEdgeTools()).isEmpty();
        });
        assertThat(this.diagramDescription.getPalette().getNodeTools()).extracting(tool -> tool.getName())
                .containsExactly("New System Actor", "New Mission", "New Capability", "New Requirement");
        for (String name : List.of("MissionNodeDescription", "CapabilityNodeDescription")) {
            var node = nodes.stream().filter(candidate -> name.equals(candidate.getName())).findFirst().orElseThrow();
            assertThat(node.getStyle()).isInstanceOf(EllipseNodeStyleDescription.class);
            assertThat(node.getDefaultWidthExpression()).isEqualTo("180");
            assertThat(node.getDefaultHeightExpression()).isEqualTo("70");
            assertThat(node.getReusedChildNodeDescriptions()).isEmpty();
            assertThat(node.getOutsideLabels()).hasSize(1);
        }
    }

    @Test
    @Override
    public void eachRequirementDescribesEdgeHasTheCommonPresentation() {
        // Nodes are delivered independently from edges: no partial edge mapping or tool is exposed.
        assertThat(this.diagramDescription.getEdgeDescriptions()).isEmpty();
        assertThat(this.diagramDescription.getNodeDescriptions()).allSatisfy(node -> assertThat(node.getPalette().getEdgeTools()).isEmpty());
    }
}

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
package org.eclipse.capella.diagram.common.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks that population tools are available only on nodes supporting children.
 *
 * @author jgout
 */
public class RelatedElementsToolProviderTests {

    @Test
    @DisplayName("GIVEN nodes supporting direct children, WHEN related element tools are added, THEN missing palettes are created and existing tools are preserved")
    public void addPopulationToolsCreatesMissingPalettes() {
        var builders = new DiagramBuilders();
        var existingSection = builders.newNodeToolSection().name("Existing tools").build();
        var existingPalette = builders.newNodePalette().toolSections(existingSection).build();
        var grandchild = builders.newNodeDescription().name("Grandchild").build();
        var child = builders.newNodeDescription().name("Child").childrenDescriptions(grandchild).build();
        var borderNode = builders.newNodeDescription().name("Border node").build();
        var parent = builders.newNodeDescription().name("Parent").palette(existingPalette)
                .childrenDescriptions(child).borderNodesDescriptions(borderNode).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(parent).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(parent.getPalette()).isSameAs(existingPalette);
        assertThat(parent.getPalette().getToolSections()).hasSize(2).contains(existingSection);
        assertThat(List.of(parent, child)).allSatisfy(node -> {
            assertThat(node.getPalette()).isNotNull();
            assertThat(node.getPalette().getToolSections())
                    .filteredOn(section -> RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION.equals(section.getName()))
                    .singleElement().satisfies(section -> assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                            .containsExactly("Show all contained elements", "Show all contained elements recursively"));
        });
        assertThat(grandchild.getPalette()).isNull();
        assertThat(borderNode.getPalette()).isNull();
    }

    @Test
    @DisplayName("GIVEN a node supporting reused children, WHEN related element tools are added, THEN its palette offers direct and recursive population")
    public void addPopulationToolsForReusedChildren() {
        var builders = new DiagramBuilders();
        var child = builders.newNodeDescription().name("Reused child").build();
        var parent = builders.newNodeDescription().name("Parent").build();
        parent.getReusedChildNodeDescriptions().add(child);
        var diagram = builders.newDiagramDescription().nodeDescriptions(parent, child).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(parent.getPalette()).isNotNull();
        assertThat(parent.getPalette().getToolSections()).singleElement().satisfies(section -> {
            assertThat(section.getName()).isEqualTo(RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION);
            assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                    .containsExactly("Show all contained elements", "Show all contained elements recursively");
        });
        assertThat(child.getPalette()).isNull();
    }

    @Test
    @DisplayName("GIVEN a node supporting only border nodes, WHEN related element tools are added, THEN its existing palette is unchanged")
    public void nodesWithoutChildrenKeepTheirPaletteUnchanged() {
        var builders = new DiagramBuilders();
        var existingSection = builders.newNodeToolSection().name("Existing tools").build();
        var palette = builders.newNodePalette().toolSections(existingSection).build();
        var borderNode = builders.newNodeDescription().name("Port").build();
        var node = builders.newNodeDescription().name("Border-only node").palette(palette).borderNodesDescriptions(borderNode).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(node).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(node.getPalette()).isSameAs(palette);
        assertThat(palette.getToolSections()).containsExactly(existingSection);
        assertThat(borderNode.getPalette()).isNull();
    }
}

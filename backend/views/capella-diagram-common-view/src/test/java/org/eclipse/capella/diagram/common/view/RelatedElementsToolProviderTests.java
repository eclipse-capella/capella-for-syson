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
 * Checks population tools on nodes with and without an existing palette.
 *
 * @author jgout
 */
public class RelatedElementsToolProviderTests {

    @Test
    @DisplayName("GIVEN nodes with and without palettes, WHEN related element tools are added, THEN missing palettes are created and existing tools are preserved")
    public void addPopulationToolsCreatesMissingPalettes() {
        var builders = new DiagramBuilders();
        var existingSection = builders.newNodeToolSection().name("Existing tools").build();
        var existingPalette = builders.newNodePalette().toolSections(existingSection).build();
        var child = builders.newNodeDescription().name("Child").build();
        var borderNode = builders.newNodeDescription().name("Border node").build();
        var parent = builders.newNodeDescription().name("Parent").palette(existingPalette)
                .childrenDescriptions(child).borderNodesDescriptions(borderNode).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(parent).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(parent.getPalette()).isSameAs(existingPalette);
        assertThat(parent.getPalette().getToolSections()).hasSize(2).contains(existingSection);
        assertThat(List.of(parent, child, borderNode)).allSatisfy(node -> {
            assertThat(node.getPalette()).isNotNull();
            assertThat(node.getPalette().getToolSections())
                    .filteredOn(section -> RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION.equals(section.getName()))
                    .singleElement().satisfies(section -> assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                            .containsExactly("Show all contained elements", "Show all contained elements recursively"));
        });
    }
}

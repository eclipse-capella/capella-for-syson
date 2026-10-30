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
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.junit.jupiter.api.Test;

/**
 * Checks that related elements tools are added to existing palettes on nodes supporting children or ports.
 *
 * @author jgout
 */
public class RelatedElementsToolProviderTests {

    @Test
    public void addPopulationToolsPreservesExistingPalettesOnParentAndDescendants() {
        var builders = new DiagramBuilders();
        var existingSection = builders.newNodeToolSection().name("Existing tools").build();
        var existingPalette = builders.newNodePalette().toolSections(existingSection).build();
        var childPalette = builders.newNodePalette().build();
        var grandchild = builders.newNodeDescription().name("Grandchild").palette(builders.newNodePalette().build()).build();
        var child = builders.newNodeDescription().name("Child").palette(childPalette).childrenDescriptions(grandchild).build();
        var borderNode = builders.newNodeDescription().name("Border node").palette(builders.newNodePalette().build()).build();
        var parent = builders.newNodeDescription().name("Parent").palette(existingPalette)
                .childrenDescriptions(child).borderNodesDescriptions(borderNode).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(parent).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(parent.getPalette().getToolSections()).hasSize(2).contains(existingSection);
        assertThat(List.of(parent, child)).allSatisfy(node -> {
            assertThat(node.getPalette()).isNotNull();
            assertThat(node.getPalette().getToolSections())
                    .filteredOn(section -> RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION.equals(section.getName()))
                    .singleElement().satisfies(section -> assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                            .containsExactly("Show all contained elements", "Show all contained elements recursively"));
        });
        assertThat(grandchild.getPalette().getToolSections()).isEmpty();
        assertThat(borderNode.getPalette().getToolSections()).isEmpty();
    }

    @Test
    public void addPopulationToolsForReusedChildren() {
        var builders = new DiagramBuilders();
        var palette = builders.newNodePalette().build();
        var child = builders.newNodeDescription().name("Reused child").palette(builders.newNodePalette().build()).build();
        var parent = builders.newNodeDescription().name("Parent").palette(palette).build();
        parent.getReusedChildNodeDescriptions().add(child);
        var diagram = builders.newDiagramDescription().nodeDescriptions(parent, child).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(parent.getPalette().getToolSections()).singleElement().satisfies(section -> {
            assertThat(section.getName()).isEqualTo(RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION);
            assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                    .containsExactly("Show all contained elements", "Show all contained elements recursively");
        });
        assertThat(child.getPalette().getToolSections()).isEmpty();
    }

    @Test
    public void nodesWithoutChildrenKeepTheirPaletteUnchanged() {
        var builders = new DiagramBuilders();
        var existingSection = builders.newNodeToolSection().name("Existing tools").build();
        var palette = builders.newNodePalette().toolSections(existingSection).build();
        var borderNode = builders.newNodeDescription().name("Port").palette(builders.newNodePalette().build()).build();
        var node = builders.newNodeDescription().name("Border-only node").palette(palette).borderNodesDescriptions(borderNode).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(node).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(palette.getToolSections()).containsExactly(existingSection);
        assertThat(borderNode.getPalette().getToolSections()).isEmpty();
    }

    @Test
    public void componentWithoutChildrenOffersOnlyPortsTool() {
        var builders = new DiagramBuilders();
        var palette = builders.newNodePalette().build();
        var port = builders.newNodeDescription().name("Port").palette(builders.newNodePalette().build()).build();
        var component = builders.newNodeDescription().name("Component").palette(palette)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage()))
                .borderNodesDescriptions(port).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(component).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(component.getPalette().getToolSections()).singleElement().satisfies(section -> {
            assertThat(section.getName()).isEqualTo(RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION);
            assertThat(section.getNodeTools()).extracting(NodeTool::getName).containsExactly("Show all ports");
        });
        assertThat(port.getPalette().getToolSections()).isEmpty();
    }

    @Test
    public void functionWithoutChildrenOffersOnlyPortsTool() {
        var builders = new DiagramBuilders();
        var palette = builders.newNodePalette().build();
        var port = builders.newNodeDescription().name("Reused port").palette(builders.newNodePalette().build()).build();
        var function = builders.newNodeDescription().name("Function").palette(palette)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage())).build();
        function.getReusedBorderNodeDescriptions().add(port);
        var diagram = builders.newDiagramDescription().nodeDescriptions(function, port).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(function.getPalette().getToolSections()).singleElement().satisfies(section -> {
            assertThat(section.getName()).isEqualTo(RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION);
            assertThat(section.getNodeTools()).extracting(NodeTool::getName).containsExactly("Show all ports");
        });
        assertThat(port.getPalette().getToolSections()).isEmpty();
    }

    @Test
    public void componentWithChildrenAndPortsOffersAllToolsInOneSection() {
        var builders = new DiagramBuilders();
        var palette = builders.newNodePalette().build();
        var child = builders.newNodeDescription().name("Child").palette(builders.newNodePalette().build()).build();
        var port = builders.newNodeDescription().name("Port").palette(builders.newNodePalette().build()).build();
        var component = builders.newNodeDescription().name("Component").palette(palette)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage()))
                .childrenDescriptions(child).borderNodesDescriptions(port).build();
        var diagram = builders.newDiagramDescription().nodeDescriptions(component).build();

        new RelatedElementsToolProvider().addNodeToolSections(diagram);

        assertThat(component.getPalette().getToolSections()).singleElement().satisfies(section -> {
            assertThat(section.getName()).isEqualTo(RelatedElementsToolProvider.RELATED_ELEMENTS_TOOL_SECTION);
            assertThat(section.getNodeTools()).extracting(NodeTool::getName)
                    .containsExactly("Show all contained elements", "Show all contained elements recursively", "Show all ports");
        });
    }
}

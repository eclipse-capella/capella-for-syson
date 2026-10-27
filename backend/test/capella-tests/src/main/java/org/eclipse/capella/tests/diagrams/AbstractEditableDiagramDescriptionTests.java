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

package org.eclipse.capella.tests.diagrams;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the structure of the editable Capella for SysON diagrams.
 *
 * @author gdaniel
 */
public abstract non-sealed class AbstractEditableDiagramDescriptionTests extends AbstractDiagramDescriptionTests {

    protected void assertDescribesTargets(String... expectedTargetNames) {
        var describes = this.diagramDescription.getEdgeDescriptions().stream()
                .filter(edge -> "DescribesEdgeDescription".equals(edge.getName()))
                .findFirst().orElseThrow();
        assertThat(describes.getTargetDescriptions()).extracting(DiagramElementDescription::getName)
                .containsExactlyInAnyOrder(expectedTargetNames);
        assertThat(describes.getSourceDescriptions()).extracting(DiagramElementDescription::getName)
                .containsExactly("RequirementNodeDescription");
        var requirement = (NodeDescription) describes.getSourceDescriptions().getFirst();
        assertThat(requirement.getEdgeTools()).singleElement().satisfies(tool ->
                assertThat(tool.getTargetElementDescriptions()).containsExactlyElementsOf(describes.getTargetDescriptions()));
    }

    @Test
    @DisplayName("Diagram palette offers direct and recursive population tools")
    public void diagramHasContainedElementsTools() {
        assertThat(this.diagramDescription.getPalette().getToolSections()).flatExtracting(section -> section.getNodeTools())
                .extracting(NodeTool::getName)
                .contains("Show all contained elements", "Show all contained elements recursively");
    }

    @Test
    @DisplayName("Component and function palettes offer ports only when the diagram describes border nodes")
    public void nodePalettesHaveRelatedElementsTools() {
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(node -> SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPartUsage()).equals(node.getDomainType())
                        || SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage()).equals(node.getDomainType()))
                .forEach(node -> {
                    assertThat(node.getPalette()).as("Palette of %s", node.getName()).isNotNull();
                    var tools = node.getPalette().getToolSections().stream().flatMap(section -> section.getNodeTools().stream()).map(NodeTool::getName).toList();
                    if (!node.getBorderNodesDescriptions().isEmpty() || !node.getReusedBorderNodeDescriptions().isEmpty()) {
                        assertThat(tools).contains("Show all ports");
                    } else {
                        assertThat(tools).doesNotContain("Show all ports");
                    }
                });
    }

    @Test
    public void endpointPalettesHaveConnectedElementsTool() {
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(node -> node.getPalette() != null)
                .filter(node -> this.diagramDescription.getEdgeDescriptions().stream()
                        .anyMatch(edge -> edge.getSourceDescriptions().contains(node) || edge.getTargetDescriptions().contains(node))
                        || !node.getBorderNodesDescriptions().isEmpty() || !node.getReusedBorderNodeDescriptions().isEmpty())
                .forEach(node -> assertThat(node.getPalette().getToolSections()).flatExtracting(section -> section.getNodeTools())
                        .extracting(NodeTool::getName).contains("Show all connected elements"));
    }

    @Test
    @DisplayName("Each EdgeDescription has reconnect tools")
    public void eachEdgeHasReconnectTools() {
        SoftAssertions softly = new SoftAssertions();
        this.diagramDescription.getEdgeDescriptions()
                .forEach(edgeDescription -> softly.assertThat(edgeDescription.getPalette())
                        .as("EdgeDescription %s should have a palette", edgeDescription.getName())
                        .isNotNull()
                        .extracting(EdgePalette::getEdgeReconnectionTools)
                        .as("EdgeDescription %s should have %s reconnection tools", edgeDescription.getName(), 2)
                        .asInstanceOf(InstanceOfAssertFactories.LIST)
                        .hasSize(2));
        softly.assertAll();
    }

    @Test
    @DisplayName("Each EdgeDescription with a center label expression has a direct edit tool")
    public void eachEdgeWithCenterLabelHasDirectEditTool() {
        SoftAssertions softly = new SoftAssertions();
        this.diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> edgeDescription.getCenterLabelExpression() != null && !edgeDescription.getCenterLabelExpression().isBlank())
                .forEach(edgeDescription -> softly.assertThat(edgeDescription.getPalette())
                        .as("EdgeDescription %s should have a palette", edgeDescription.getName())
                        .isNotNull()
                        .extracting(EdgePalette::getCenterLabelEditTool)
                        .as("EdgeDescription %s should have a center label edit tool", edgeDescription.getName())
                        .isNotNull());
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription with an inside label has a direct edit tool")
    public void eachNodeHasDirectEditTool() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(this.isCompartment().negate())
                .filter(nodeDescription -> nodeDescription.getInsideLabel() != null && nodeDescription.getInsideLabel().getLabelExpression() != null
                        && !nodeDescription.getInsideLabel().getLabelExpression().isBlank())
                .forEach(nodeDescription -> {
                    softly.assertThat(nodeDescription.getPalette())
                            .as("NodeDescription %s should have a palette", nodeDescription.getName())
                            .isNotNull();
                    if (nodeDescription.getPalette() != null) {
                        softly.assertThat(nodeDescription.getPalette().getLabelEditTool())
                                .as("NodeDescription %s should have a label edit tool", nodeDescription.getName())
                                .isNotNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription has a delete tool")
    public void eachNodeHasDeleteTool() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(this.isCompartment().negate())
                .forEach(nodeDescription -> {
                    softly.assertThat(nodeDescription.getPalette())
                            .as("NodeDescription %s should have a palette", nodeDescription.getName())
                            .isNotNull();
                    if (nodeDescription.getPalette() != null) {
                        softly.assertThat(nodeDescription.getPalette().getDeleteTool())
                                .as("NodeDescription %s should have a delete tool", nodeDescription.getName())
                                .isNotNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Diagram has a semantic drag & drop tool")
    public void diagramHasSemanticDragAndDropTool() {
        assertThat(this.diagramDescription.getPalette())
                .as("DiagramDescription should have a palette")
                .isNotNull()
                .extracting(DiagramPalette::getDropTool)
                .as("DiagramDescription should have a drop tool")
                .isNotNull();
    }
}

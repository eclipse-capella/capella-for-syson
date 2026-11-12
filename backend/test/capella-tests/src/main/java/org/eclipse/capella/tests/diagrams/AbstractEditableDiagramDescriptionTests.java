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

import java.util.Objects;
import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;
import org.junit.jupiter.api.Test;

/**
 * Checks the structure of the editable Capella for SysON diagrams.
 *
 * @author gdaniel
 */
public abstract non-sealed class AbstractEditableDiagramDescriptionTests extends AbstractDiagramDescriptionTests {

    @Test
    public void diagramHasRelatedElementsTools() {
        Optional<DiagramToolSection> relatedElementsToolSection = Optional.empty();
        if (this.diagramDescription.getPalette() != null) {
            relatedElementsToolSection = this.diagramDescription.getPalette().getToolSections().stream()
                    .filter(toolSection -> Objects.equals(toolSection.getName(), "Related Elements"))
                    .findFirst();
        }
        assertThat(relatedElementsToolSection).as("DiagramDescription should have a Related Elements tool section").isPresent();
        assertThat(relatedElementsToolSection.get().getNodeTools())
                .map(NodeTool::getName)
                .contains("Show all contained elements");
    }

    @Test
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
    public void diagramHasSemanticDragAndDropTool() {
        assertThat(this.diagramDescription.getPalette())
                .as("DiagramDescription should have a palette")
                .isNotNull()
                .extracting(DiagramPalette::getDropTool)
                .as("DiagramDescription should have a drop tool")
                .isNotNull();
    }
}

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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the structure of the read-only Capella for SysON diagrams.
 *
 * @author gdaniel
 */
public abstract non-sealed class AbstractReadOnlyDiagramDescriptionTests extends AbstractDiagramDescriptionTests {

    @Test
    @DisplayName("Each EdgeDescription has no reconnect tool")
    public void eachEdgeHasNoReconnectTools() {
        SoftAssertions softly = new SoftAssertions();
        this.diagramDescription.getEdgeDescriptions()
                .forEach(edgeDescription -> {
                    if (edgeDescription.getPalette() != null) {
                        softly.assertThat(edgeDescription.getPalette().getEdgeReconnectionTools())
                                .as("EdgeDescription %s should have no reconnection tools", edgeDescription.getName())
                                .isEmpty();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Each EdgeDescription with a center label expression has no direct edit tool")
    public void eachEdgeWithCenterLabelHasNoDirectEditTool() {
        SoftAssertions softly = new SoftAssertions();
        this.diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> edgeDescription.getCenterLabelExpression() != null && !edgeDescription.getCenterLabelExpression().isBlank())
                .forEach(edgeDescription -> {
                    if (edgeDescription.getPalette() != null) {
                        softly.assertThat(edgeDescription.getPalette().getCenterLabelEditTool())
                                .as("EdgeDescription %s should have no center label edit tool", edgeDescription.getName())
                                .isNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription with an inside label has no direct edit tool")
    public void eachNodeHasNoDirectEditTool() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(this.isCompartment().negate())
                .filter(nodeDescription -> nodeDescription.getInsideLabel() != null && nodeDescription.getInsideLabel().getLabelExpression() != null
                        && !nodeDescription.getInsideLabel().getLabelExpression().isBlank())
                .forEach(nodeDescription -> {
                    if (nodeDescription.getPalette() != null) {
                        softly.assertThat(nodeDescription.getPalette().getLabelEditTool())
                                .as("NodeDescription %s should have no label edit tool", nodeDescription.getName())
                                .isNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription has no delete tool")
    public void eachNodeHasNoDeleteTool() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(this.isCompartment().negate())
                .forEach(nodeDescription -> {
                    if (nodeDescription.getPalette() != null) {
                        softly.assertThat(nodeDescription.getPalette().getDeleteTool())
                                .as("NodeDescription %s should have no delete tool", nodeDescription.getName())
                                .isNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Diagram has no semantic drag & drop tool")
    public void diagramHasNoSemanticDragAndDropTool() {
        if (this.diagramDescription.getPalette() != null) {
            assertThat(this.diagramDescription.getPalette().getDropTool())
                    .as("DiagramDescription should have no drop tool")
                    .isNull();
        }
    }

}

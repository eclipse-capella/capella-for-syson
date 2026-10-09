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

import org.assertj.core.api.Assertions;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks the structure of specific the Capella for SysON diagram nodes.
 * <p>
 *
 * @author adieumegard
 */
public abstract class AbstractDiagramNodeDescriptionTests {

    protected DiagramDescription diagramDescription;

    /**
     * Enumeration listing Diagram families available in Capella. It is expected that each familly should have common
     * diagram look.
     * <ul>
     * <li>{@link DiagramFamilyEnum}.AB is for Architecture Blank Diagrams, includes OAB, SAB, LAB, PAB</li>
     * <li>{@link DiagramFamilyEnum}.BD is for Breakdown Diagrams, includes OABD, OEBD, SFBD, LFBD, PFBD</li>
     * <li>{@link DiagramFamilyEnum}.CAPABILITY is for Capability management Diagrams, includes OCB, MCB, CRB</li>
     * <li>{@link DiagramFamilyEnum}.DFB is for Data Flow Blank Diagrams, includes OAIB, SDFB, LDFB, PDFB</li>
     * </ul>
     */
    protected enum DiagramFamilyEnum {
        AB, BD, CAPABILITY, DFB
    }

    @BeforeEach
    public void setUp() {
        ViewBuilder viewBuilder = new ViewBuilder();
        View view = viewBuilder.build();
        IColorProvider colorProvider = this.getColorProvider(view);
        IRepresentationDescriptionProvider representationDescriptionProvider = this.getRepresentationDescriptionProvider();
        this.diagramDescription = (DiagramDescription) representationDescriptionProvider.create(colorProvider);
        view.getDescriptions().add(this.diagramDescription);
    }

    protected final InsideLabelPosition getExpectedInsideComponentLabelPosition() {
        return InsideLabelPosition.TOP_CENTER;
    }

    protected final InsideLabelPosition getExpectedInsideFunctionLabelPosition() {
        InsideLabelPosition result = null;
        switch (this.getDiagramFamily()) {
            case AB:
            case DFB:
                result = InsideLabelPosition.TOP_CENTER;
                break;
            case BD:
                result = InsideLabelPosition.MIDDLE_CENTER;
                break;
            case CAPABILITY:
                Assertions.fail("Capability diagrams should not have Function Nodes");
                break;
            default:
                Assertions.fail("Unknown diagram familly");
        }
        return result;
    }

    protected final boolean hasFunctionNode() {
        boolean result = true;
        switch (this.getDiagramFamily()) {
            case DiagramFamilyEnum.AB:
            case DiagramFamilyEnum.BD:
            case DiagramFamilyEnum.DFB:
                result = true;
                break;
            case DiagramFamilyEnum.CAPABILITY:
                result = false;
                break;
            default:
                Assertions.fail("Unknown diagram familly");
        }
        return result;
    }

    protected final boolean hasComponentNode() {
        boolean result = true;
        switch (this.getDiagramFamily()) {
            case DiagramFamilyEnum.AB:
            case DiagramFamilyEnum.CAPABILITY:
                result = true;
                break;
            case DiagramFamilyEnum.BD:
            case DiagramFamilyEnum.DFB:
                result = false;
                break;
            default:
                Assertions.fail("Unknown diagram familly");
        }
        return result;
    }

    protected abstract DiagramFamilyEnum getDiagramFamily();

    protected abstract IColorProvider getColorProvider(View view);

    protected abstract IRepresentationDescriptionProvider getRepresentationDescriptionProvider();

    protected abstract String getFunctionNodeDescriptionName();

    protected abstract String getComponentNodeDescriptionName();
    @Test
    public void eachFunctionNodeHasAdaptedPresentation() {
        this.diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> this.hasFunctionNode() && this.getFunctionNodeDescriptionName().equals(nodeDescription.getName()))
                .forEach(nodeDescription -> this.assertFunctionPresentation(nodeDescription));
    }

    @Test
    public void eachComponentNodeHasAdaptedPresentation() {
        this.diagramDescription.getNodeDescriptions().stream()
                .filter(nodeDescription -> this.hasComponentNode() && this.getComponentNodeDescriptionName().equals(nodeDescription.getName()))
                .forEach(nodeDescription -> this.assertComponentPresentation(nodeDescription));
    }

    private void assertFunctionPresentation(NodeDescription functionNodeDescription) {
        Assertions.assertThat(functionNodeDescription.getInsideLabel().getPosition())
                .as("Function node inside label should be " + this.getExpectedInsideFunctionLabelPosition().getName())
                .isEqualTo(this.getExpectedInsideFunctionLabelPosition());
    }

    private void assertComponentPresentation(NodeDescription componentNodeDescription) {
        Assertions.assertThat(componentNodeDescription.getInsideLabel().getPosition())
                .as("Component node inside label should be " + this.getExpectedInsideComponentLabelPosition().getName())
                .isEqualTo(this.getExpectedInsideComponentLabelPosition());
    }

}

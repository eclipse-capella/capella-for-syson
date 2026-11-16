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

package org.eclipse.capella.diagram.sfbd.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.sfbd.view.services.SFBDDiagramService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Package;
import org.junit.jupiter.api.Test;

/**
 * Tests the SFBD diagram services.
 *
 * @author adieumegard
 */
public class SFBDDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final SFBDDiagramService sfbdDiagramService = new SFBDDiagramService();

    @Test
    public void canCreateSFBDDiagramShouldReturnTrueOnSAFunction() {
        ActionUsage function = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.sfbdDiagramService.canCreateSFBDDiagram(function)).isTrue();
    }

    @Test
    public void canCreateSFBDDiagramShouldReturnFalseOnInvalidElements() {
        Package saFunctionsPackage = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getElement();
        assertThat(this.sfbdDiagramService.canCreateSFBDDiagram(saFunctionsPackage)).isFalse();

        Package nonSAFunctionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getElement();
        assertThat(this.sfbdDiagramService.canCreateSFBDDiagram(nonSAFunctionsPackage)).isFalse();

        ActionUsage nonSAFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.sfbdDiagramService.canCreateSFBDDiagram(nonSAFunction)).isFalse();
    }
}

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

package org.eclipse.capella.diagram.oabd.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.oabd.view.services.OABDDiagramService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Package;
import org.junit.jupiter.api.Test;

/**
 * Tests the OABD diagram services.
 *
 * @author gdaniel
 */
public class OABDDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final OABDDiagramService oabdDiagramService = new OABDDiagramService();

    @Test
    public void canCreateOABDDiagramShouldReturnTrueOnOAFunction() {
        ActionUsage function = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.oabdDiagramService.canCreateOABDDiagram(function)).isTrue();
    }

    @Test
    public void canCreateOABDDiagramShouldReturnFalseOnInvalidElements() {
        Package oaFunctionsPackage = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getElement();
        assertThat(this.oabdDiagramService.canCreateOABDDiagram(oaFunctionsPackage)).isFalse();

        Package nonOAFunctionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getElement();
        assertThat(this.oabdDiagramService.canCreateOABDDiagram(nonOAFunctionsPackage)).isFalse();

        ActionUsage nonOAFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.oabdDiagramService.canCreateOABDDiagram(nonOAFunction)).isFalse();
    }
}

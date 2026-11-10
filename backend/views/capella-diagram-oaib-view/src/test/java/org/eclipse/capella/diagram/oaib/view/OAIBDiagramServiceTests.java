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

package org.eclipse.capella.diagram.oaib.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.oaib.view.services.OAIBDiagramService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Package;
import org.junit.jupiter.api.Test;

/**
 * Tests the OAIB diagram services.
 *
 * @author gdaniel
 */
public class OAIBDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final OAIBDiagramService oaibDiagramService = new OAIBDiagramService(new IObjectSearchService.NoOp(), new IIdentityService.NoOp());

    @Test
    public void canCreateOAIBDiagramShouldReturnTrueOnOAFunction() {
        ActionUsage function = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.oaibDiagramService.canCreateOAIBDiagram(function)).isTrue();
    }

    @Test
    public void canCreateOAIBDiagramShouldReturnFalseOnInvalidElements() {
        Package oaFunctionsPackage = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getElement();
        assertThat(this.oaibDiagramService.canCreateOAIBDiagram(oaFunctionsPackage)).isFalse();

        Package nonOAFunctionsPackage = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getElement();
        assertThat(this.oaibDiagramService.canCreateOAIBDiagram(nonOAFunctionsPackage)).isFalse();

        ActionUsage nonOAFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.oaibDiagramService.canCreateOAIBDiagram(nonOAFunction)).isFalse();
    }

}

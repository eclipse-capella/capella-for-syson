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
package org.eclipse.capella.diagram.mcb.view.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Checks the contexts accepted by the Mission and Capabilities Blank diagram.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramServiceTests extends AbstractSemanticTests {

    private final MCBDiagramService diagramService = new MCBDiagramService();

    @Test
    public void isSystemAnalysisCapabilitiesPackageShouldAcceptCapabilitiesAndNestedPackages() {
        var capabilities = this.capellaModel.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        var nestedPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(capabilities, nestedPackage);

        assertThat(this.diagramService.isSystemAnalysisCapabilitiesPackage(capabilities)).isTrue();
        assertThat(this.diagramService.isSystemAnalysisCapabilitiesPackage(nestedPackage)).isTrue();
    }

    @Test
    public void isSystemAnalysisCapabilitiesPackageShouldRejectOtherContexts() {
        var data = this.capellaModel.getSystemAnalysisPerspective().getDataPackage().getElement();
        var operationalCapabilities = this.capellaModel.getOperationalAnalysisPerspective().getCapabilitiesPackage().getElement();

        assertThat(this.diagramService.isSystemAnalysisCapabilitiesPackage(data)).isFalse();
        assertThat(this.diagramService.isSystemAnalysisCapabilitiesPackage(operationalCapabilities)).isFalse();
    }
}

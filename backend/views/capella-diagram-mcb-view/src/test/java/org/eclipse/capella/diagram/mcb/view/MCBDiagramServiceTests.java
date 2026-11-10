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

package org.eclipse.capella.diagram.mcb.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.mcb.view.services.MCBDiagramService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Tests the MCB diagram services.
 *
 * @author gdaniel
 */
public class MCBDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final MCBDiagramService mcbDiagramService = new MCBDiagramService();

    @Test
    public void canCreateMCBDiagramShouldReturnTrueOnSACapabilitiesPackage() {
        org.eclipse.syson.sysml.Package capabilitiesPackage = this.capellaModel.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement();
        assertThat(this.mcbDiagramService.canCreateMCBDiagram(capabilitiesPackage)).isTrue();

        // The package should be part of the test fixture (see #348)
        Package capabilitiesSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(capabilitiesPackage, capabilitiesSubPackage);
        new ElementInitializerSwitch().doSwitch(capabilitiesSubPackage);
        assertThat(this.mcbDiagramService.canCreateMCBDiagram(capabilitiesSubPackage)).isTrue();
    }

    @Test
    public void canCreateMCBDiagramShouldReturnFalseOnInvalidElements() {
        Package nonSACapabilitiesPackage = this.capellaModel.getLogicalArchitecturePerspective().getCapabilitiesPackage().getElement();
        assertThat(this.mcbDiagramService.canCreateMCBDiagram(nonSACapabilitiesPackage)).isFalse();

        // The package should be part of the test fixture (see #348)
        Package nonSACapabilitiesSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(nonSACapabilitiesPackage, nonSACapabilitiesSubPackage);
        new ElementInitializerSwitch().doSwitch(nonSACapabilitiesSubPackage);
        assertThat(this.mcbDiagramService.canCreateMCBDiagram(nonSACapabilitiesSubPackage)).isFalse();
    }
}

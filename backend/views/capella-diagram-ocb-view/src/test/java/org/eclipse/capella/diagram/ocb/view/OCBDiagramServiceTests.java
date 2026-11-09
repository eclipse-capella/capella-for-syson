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

package org.eclipse.capella.diagram.ocb.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.ocb.view.services.OCBDiagramService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Tests the OCB diagram services.
 *
 * @author gdaniel
 */
public class OCBDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final OCBDiagramService ocbDiagramService = new OCBDiagramService(new IObjectSearchService.NoOp());

    @Test
    public void canCreateOCBDiagramShouldReturnTrueOnOACapabilitiesPackage() {
        Package capabilitiesPackage = this.capellaModel.getOperationalAnalysisPerspective().getCapabilitiesPackage().getElement();
        assertThat(this.ocbDiagramService.canCreateOCBDiagram(capabilitiesPackage)).isTrue();

        // The package should be part of the test fixture (see #348)
        Package capabilitiesSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(capabilitiesPackage, capabilitiesSubPackage);
        new ElementInitializerSwitch().doSwitch(capabilitiesSubPackage);
        assertThat(this.ocbDiagramService.canCreateOCBDiagram(capabilitiesSubPackage)).isTrue();
    }

    @Test
    public void canCreateOCBDiagramShouldReturnFalseOnInvalidElements() {
        Package nonOACapabilitiesPackage = this.capellaModel.getLogicalArchitecturePerspective().getCapabilitiesPackage().getElement();
        assertThat(this.ocbDiagramService.canCreateOCBDiagram(nonOACapabilitiesPackage)).isFalse();

        // The package should be part of the test fixture (see #348)
        Package nonOACapabilitiesSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(nonOACapabilitiesPackage, nonOACapabilitiesSubPackage);
        new ElementInitializerSwitch().doSwitch(nonOACapabilitiesSubPackage);
        assertThat(this.ocbDiagramService.canCreateOCBDiagram(nonOACapabilitiesSubPackage)).isFalse();
    }
}

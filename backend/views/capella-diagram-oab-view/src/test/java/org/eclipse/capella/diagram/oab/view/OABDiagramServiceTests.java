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

package org.eclipse.capella.diagram.oab.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.oab.view.services.OABDiagramService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Tests the OAB diagram services.
 *
 * @author gdaniel
 */
public class OABDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final OABDiagramService oabDiagramService = new OABDiagramService();

    @Test
    public void canCreateOABDiagramShouldReturnTrueOnOAComponentOrStructurePackage() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        assertThat(this.oabDiagramService.canCreateOABDiagram(structurePackage)).isTrue();

        // The package should be part of the test fixture (see #348)
        Package structureSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(structurePackage, structureSubPackage);
        new ElementInitializerSwitch().doSwitch(structureSubPackage);
        assertThat(this.oabDiagramService.canCreateOABDiagram(structureSubPackage)).isTrue();

        // The component should be part of the test fixture (see #348)
        PartUsage component = new CommonCreationService().createComponent(structurePackage);
        assertThat(this.oabDiagramService.canCreateOABDiagram(component)).isTrue();
    }

    @Test
    public void canCreateOABDiagramShouldReturnFalseOnInvalidElements() {
        Package nonOAStructurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        assertThat(this.oabDiagramService.canCreateOABDiagram(nonOAStructurePackage)).isFalse();

        // The package should be part of the test fixture (see #348)
        Package nonOAStructureSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(nonOAStructurePackage, nonOAStructureSubPackage);
        new ElementInitializerSwitch().doSwitch(nonOAStructureSubPackage);
        assertThat(this.oabDiagramService.canCreateOABDiagram(nonOAStructureSubPackage)).isFalse();

        // The component should be part of the test fixture (see #348)
        PartUsage nonOAComponent = new CommonCreationService().createComponent(nonOAStructurePackage);
        assertThat(this.oabDiagramService.canCreateOABDiagram(nonOAComponent)).isFalse();
    }
}

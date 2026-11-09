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

package org.eclipse.capella.diagram.lab.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.capella.diagram.lab.view.services.LABDiagramService;
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
 * Tests the LAB diagram services.
 *
 * @author gdaniel
 */
public class LABDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final LABDiagramService labDiagramService = new LABDiagramService(new IDiagramFilterService.NoOp());

    @Test
    public void canCreateLABDiagramShouldReturnTrueOnLAComponentOrStructurePackage() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        assertThat(this.labDiagramService.canCreateLABDiagram(structurePackage)).isTrue();

        // The package should be part of the test fixture (see #348)
        Package structureSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(structurePackage, structureSubPackage);
        new ElementInitializerSwitch().doSwitch(structureSubPackage);
        assertThat(this.labDiagramService.canCreateLABDiagram(structureSubPackage)).isTrue();

        // The component should be part of the test fixture (see #348)
        PartUsage component = new CommonCreationService().createComponent(structurePackage);
        assertThat(this.labDiagramService.canCreateLABDiagram(component)).isTrue();
    }

    @Test
    public void canCreateLABDiagramShouldReturnFalseOnInvalidElements() {
        Package nonLAStructurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        assertThat(this.labDiagramService.canCreateLABDiagram(nonLAStructurePackage)).isFalse();

        // The package should be part of the test fixture (see #348)
        Package nonLAStructureSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(nonLAStructurePackage, nonLAStructureSubPackage);
        new ElementInitializerSwitch().doSwitch(nonLAStructureSubPackage);
        assertThat(this.labDiagramService.canCreateLABDiagram(nonLAStructureSubPackage)).isFalse();

        // The component should be part of the test fixture (see #348)
        PartUsage nonLAComponent = new CommonCreationService().createComponent(nonLAStructurePackage);
        assertThat(this.labDiagramService.canCreateLABDiagram(nonLAComponent)).isFalse();
    }
}

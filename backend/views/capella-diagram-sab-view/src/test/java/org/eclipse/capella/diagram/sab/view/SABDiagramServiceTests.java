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

package org.eclipse.capella.diagram.sab.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.sab.view.services.SABDiagramService;
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
 * Tests the SAB diagram services.
 *
 * @author gdaniel
 */
public class SABDiagramServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final SABDiagramService sabDiagramService = new SABDiagramService();

    @Test
    public void canCreateSABDiagramShouldReturnTrueOnSAComponentOrStructurePackage() {
        Package structurePackage = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        assertThat(this.sabDiagramService.canCreateSABDiagram(structurePackage)).isTrue();

        // The package should be part of the test fixture (see #348)
        Package structureSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(structurePackage, structureSubPackage);
        new ElementInitializerSwitch().doSwitch(structureSubPackage);
        assertThat(this.sabDiagramService.canCreateSABDiagram(structureSubPackage)).isTrue();

        // The component should be part of the test fixture (see #348)
        PartUsage component = new CommonCreationService().createComponent(structurePackage);
        assertThat(this.sabDiagramService.canCreateSABDiagram(component)).isTrue();
    }

    @Test
    public void canCreateSABDiagramShouldReturnFalseOnInvalidElements() {
        Package nonSAStructurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        assertThat(this.sabDiagramService.canCreateSABDiagram(nonSAStructurePackage)).isFalse();

        // The package should be part of the test fixture (see #348)
        Package nonSAStructureSubPackage = SysmlFactory.eINSTANCE.createPackage();
        new MetamodelMutationElementService().addChildInParent(nonSAStructurePackage, nonSAStructureSubPackage);
        new ElementInitializerSwitch().doSwitch(nonSAStructureSubPackage);
        assertThat(this.sabDiagramService.canCreateSABDiagram(nonSAStructureSubPackage)).isFalse();

        // The component should be part of the test fixture (see #348)
        PartUsage nonSAComponent = new CommonCreationService().createComponent(nonSAStructureSubPackage);
        assertThat(this.sabDiagramService.canCreateSABDiagram(nonSAComponent)).isFalse();

    }

}

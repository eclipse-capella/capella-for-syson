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

package org.eclipse.capella.model.transverse.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests the move of semantic elements.
 *
 * @author gdaniel
 */
public class ElementMoveTests extends AbstractSemanticTests {

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final CommonMoveService commonMoveService = new CommonMoveService();

    @Test
    public void moveComponentInComponent() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.commonCreationService.createComponent(structurePackage);
        PartUsage component2 = this.commonCreationService.createComponent(structurePackage);
        assertThat(structurePackage.getOwnedElement()).contains(component1, component2);

        this.commonMoveService.moveSemanticElement(component1, component2);
        assertThat(component2.getOwnedElement()).contains(component1);
        assertThat(structurePackage.getOwnedElement())
                .doesNotContain(component1)
                .contains(component2);
    }
}

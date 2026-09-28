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
package org.eclipse.capella.model.services.operational.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link OAMutationService}.
 *
 * @author tbezierslafosse
 */
public class OAMutationServiceTests extends AbstractSemanticTests {

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final OAMutationService oaMutationService = new OAMutationService();

    @Test
    public void createActorActivityWhenParentIsOperationalEntityShouldAllocateItToEntity() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage entity = this.commonCreationService.createActor(structurePackage);

        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);

        assertThat(activity).isNotNull();
        assertThat(activity.getDeclaredName()).startsWith("OA ");
        assertThat(this.commonQueryService.isOperationalActivity(activity)).isTrue();
        assertThat(activity.getOwner()).isEqualTo(this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement());
        assertThat(this.commonQueryService.getAllocatedFunctions(entity)).contains(activity);
    }

    @Test
    public void createComponentActivityWhenParentIsNotInOperationalAnalysisShouldNotCreateIt() {
        Package structurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component = this.commonCreationService.createComponent(structurePackage);

        assertThat(this.oaMutationService.createOperationalActivityOA(component)).isNull();
    }

}

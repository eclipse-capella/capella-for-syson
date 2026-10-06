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
import static org.eclipse.capella.model.transverse.services.CommonNamingService.ACTOR_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMPONENT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.FUNCTION_INPUTPORT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.FUNCTION_OUTPUTPORT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.FUNCTION_PORT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.REQUIREMENT_DEFAULT_DECLAREDNAME_PREFIX;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests for the naming of Arcadia elements.
 *
 * @author adieumegard
 */
public class CommonNamingServiceTests extends AbstractSemanticTests {

    private static final String WHITE_SPACE = " ";

    private final CommonNamingService commonNamingService = new CommonNamingService();

    private final CommonCreationService commonCreationService = new CommonCreationService();

    @Test
    public void createComponentShouldNameStartingAtOne() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage component1 = this.commonCreationService.createComponent(parent);
        PartUsage component2 = this.commonCreationService.createComponent(parent);
        PartUsage component11 = this.commonCreationService.createComponent(component1);
        assertThat(component1.getDeclaredName()).isEqualTo(COMPONENT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
        assertThat(component2.getDeclaredName()).isEqualTo(COMPONENT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "3");
        assertThat(component11.getDeclaredName()).isEqualTo(COMPONENT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
    }

    @Test
    public void createActorShouldNameStartingAtOne() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage actor1 = this.commonCreationService.createActor(parent);
        PartUsage actor2 = this.commonCreationService.createActor(parent);
        PartUsage actor11 = this.commonCreationService.createActor(actor1);
        assertThat(actor1.getDeclaredName()).isEqualTo(ACTOR_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
        assertThat(actor2.getDeclaredName()).isEqualTo(ACTOR_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "3");
        assertThat(actor11.getDeclaredName()).isEqualTo(ACTOR_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
    }

    @Test
    public void createComponentPortShouldNameStartingAtOne() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage actor1 = this.commonCreationService.createActor(parent);
        PortUsage componentPortIn = this.commonCreationService.createComponentPort(actor1, FeatureDirectionKind.IN);
        PortUsage componentPortInOut = this.commonCreationService.createComponentPort(actor1, FeatureDirectionKind.INOUT);
        PortUsage componentPortOut = this.commonCreationService.createComponentPort(actor1, FeatureDirectionKind.OUT);
        assertThat(componentPortIn.getDeclaredName()).isEqualTo(COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
        assertThat(componentPortInOut.getDeclaredName()).isEqualTo(COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
        assertThat(componentPortOut.getDeclaredName()).isEqualTo(COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "3");
    }

    @Test
    public void createFunctionPortShouldNameStartingAtOne() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage actor1 = this.commonCreationService.createActor(parent);
        ActionUsage function = this.commonCreationService.createFunction(actor1);
        ItemUsage functionPortIn = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.IN);
        ItemUsage functionPortInOut = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.INOUT);
        ItemUsage functionPortOut = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.OUT);
        assertThat(functionPortIn.getDeclaredName()).isEqualTo(FUNCTION_INPUTPORT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
        assertThat(functionPortInOut.getDeclaredName()).isEqualTo(FUNCTION_PORT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
        assertThat(functionPortOut.getDeclaredName()).isEqualTo(FUNCTION_OUTPUTPORT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "3");
    }

    @Test
    public void createRequirementShouldNameStartingAtOne() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        RequirementUsage req1 = this.commonCreationService.createRequirement(parent);
        RequirementUsage req2 = this.commonCreationService.createRequirement(parent);
        assertThat(req1.getDeclaredName()).isEqualTo(REQUIREMENT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
        assertThat(req2.getDeclaredName()).isEqualTo(REQUIREMENT_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
    }

}

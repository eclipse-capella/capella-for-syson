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
import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMMUNICATION_MEAN_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.INTERACTION_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.OPERATIONAL_ACTIVITY_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.OPERATIONAL_ACTOR_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.OPERATIONAL_PROCESS_DEFAULT_DECLAREDNAME_PREFIX;

import java.util.List;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link OAMutationService}.
 *
 * @author tbezierslafosse
 */
public class OAMutationServiceTests extends AbstractSemanticTests {

    private static final String WHITE_SPACE = " ";

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final OAMutationService oaMutationService = new OAMutationService();

    @Test
    public void createActorActivityWhenParentIsOperationalEntityShouldAllocateItToEntity() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage entity = this.commonCreationService.createActor(structurePackage);

        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);

        assertThat(activity).isNotNull();
        assertThat(activity.getDeclaredName()).isEqualTo(OPERATIONAL_ACTOR_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
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

    @Test
    public void createCommunicationMeanComponentExchangeOAShouldNameItStartingAtOne() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage sourceComponent = this.commonCreationService.createComponent(structurePackage);
        PartUsage targetComponent = this.commonCreationService.createComponent(structurePackage);
        InterfaceUsage communicationMean = this.oaMutationService.createCommunicationMeanComponentExchangeOA(sourceComponent, targetComponent);
        InterfaceUsage communicationMean2 = this.oaMutationService.createCommunicationMeanComponentExchangeOA(sourceComponent, targetComponent);

        assertThat(communicationMean.getDeclaredName()).isEqualTo(COMMUNICATION_MEAN_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + 1);
        assertThat(communicationMean2.getDeclaredName()).isEqualTo(COMMUNICATION_MEAN_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + 2);
        assertThat(communicationMean.getOwner()).isEqualTo(structurePackage);
        assertThat(communicationMean2.getOwner()).isEqualTo(structurePackage);
    }

    @Test
    public void createCommunicationMeanComponentExchangeOAShouldLocateItOnCommonComponentAncestor() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage component1 = this.commonCreationService.createComponent(structurePackage);
        PartUsage component2 = this.commonCreationService.createComponent(structurePackage);
        PartUsage component11 = this.commonCreationService.createComponent(component1);
        PartUsage component12 = this.commonCreationService.createComponent(component2);
        InterfaceUsage communicationMean = this.oaMutationService.createCommunicationMeanComponentExchangeOA(component11, component12);
        InterfaceUsage communicationMean2 = this.oaMutationService.createCommunicationMeanComponentExchangeOA(component2, component12);

        // This test should be updated as component exchanges should be located on the common ancestor component
        assertThat(communicationMean.getOwner()).isEqualTo(structurePackage);
        assertThat(communicationMean2.getOwner()).isEqualTo(component2);
    }

    @Test
    public void createInteractionOAShouldBeNamedStartingAtOne() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage entity = this.commonCreationService.createActor(structurePackage);
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);
        ActionUsage activity2 = this.oaMutationService.createOperationalActivityOA(entity);
        FlowUsage interactionOA = this.oaMutationService.createInteractionOA(activity, activity2);
        FlowUsage interactionOA2 = this.oaMutationService.createInteractionOA(activity, activity2);

        assertThat(activity).isNotNull();
        assertThat(this.commonQueryService.isFunctionalExchange(interactionOA));
        assertThat(interactionOA.getDeclaredName()).isEqualTo(INTERACTION_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
        assertThat(interactionOA2.getDeclaredName()).isEqualTo(INTERACTION_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
    }

    @Test
    public void createInteractionOAShouldBeLocatedOnCommonOActivityAncestor() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage entity = this.commonCreationService.createActor(structurePackage);
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);
        ActionUsage activity2 = this.oaMutationService.createOperationalActivityOA(entity);
        ActionUsage activity21 = this.oaMutationService.createOperationalActivityOA(activity2);
        ActionUsage activity22 = this.oaMutationService.createOperationalActivityOA(activity2);
        FlowUsage interactionOA = this.oaMutationService.createInteractionOA(activity, activity2);
        FlowUsage interactionOA2 = this.oaMutationService.createInteractionOA(activity, activity2);
        FlowUsage interactionOA3 = this.oaMutationService.createInteractionOA(activity, activity21);
        FlowUsage interactionOA4 = this.oaMutationService.createInteractionOA(activity22, activity21);

        assertThat(interactionOA.getOwner()).isEqualTo(this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement());
        assertThat(interactionOA2.getOwner()).isEqualTo(this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement());
        assertThat(interactionOA3.getOwner()).isEqualTo(this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement());
        assertThat(interactionOA4.getOwner()).isEqualTo(activity2);
    }

    @Test
    public void createOperationalActivityOAShouldBeNamedStartingAtOne() {
        Package oaStructurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage component = this.commonCreationService.createComponent(oaStructurePackage);
        ActionUsage operationalActivityOA = this.oaMutationService.createOperationalActivityOA(component);
        ActionUsage operationalActivityOA2 = this.oaMutationService.createOperationalActivityOA(component);

        assertThat(this.commonQueryService.isFunctionalExchange(operationalActivityOA));
        assertThat(operationalActivityOA.getDeclaredName()).isEqualTo(OPERATIONAL_ACTIVITY_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
        assertThat(operationalActivityOA2.getDeclaredName()).isEqualTo(OPERATIONAL_ACTIVITY_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
    }

    @Test
    public void createOperationalActivityOAShouldBeCreateOnlyOnOAPerspective() {
        Package saStructurePackage = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        PartUsage saComponent = this.commonCreationService.createComponent(saStructurePackage);
        ActionUsage saOperationalActivityOA = this.oaMutationService.createOperationalActivityOA(saComponent);
        Package laStructurePackage = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage laComponent = this.commonCreationService.createComponent(laStructurePackage);
        ActionUsage laOperationalActivityOA = this.oaMutationService.createOperationalActivityOA(laComponent);
        Package paStructurePackage = this.capellaModel.getPhysicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage paComponent = this.commonCreationService.createComponent(paStructurePackage);
        ActionUsage paOperationalActivityOA = this.oaMutationService.createOperationalActivityOA(paComponent);

        assertThat(saOperationalActivityOA).isNull();
        assertThat(laOperationalActivityOA).isNull();
        assertThat(paOperationalActivityOA).isNull();
    }

    @Test
    public void createOperationalProcessOAShouldBeNamedStartingAtOne() {
        Package structurePackage = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        PartUsage entity = this.commonCreationService.createActor(structurePackage);
        ActionUsage activity = this.oaMutationService.createOperationalActivityOA(entity);
        ActionUsage activity2 = this.oaMutationService.createOperationalActivityOA(entity);
        ActionUsage activity21 = this.oaMutationService.createOperationalActivityOA(activity2);
        ActionUsage activity22 = this.oaMutationService.createOperationalActivityOA(activity2);
        FlowUsage interactionOA = this.oaMutationService.createInteractionOA(activity, activity2);
        FlowUsage interactionOA2 = this.oaMutationService.createInteractionOA(activity, activity2);
        FlowUsage interactionOA3 = this.oaMutationService.createInteractionOA(activity, activity21);
        FlowUsage interactionOA4 = this.oaMutationService.createInteractionOA(activity22, activity21);
        ActionUsage operationalProcessOA = this.oaMutationService.createOperationalProcessOA(entity, List.of(interactionOA, interactionOA2, interactionOA3));
        ActionUsage operationalProcessOA2 = this.oaMutationService.createOperationalProcessOA(entity, List.of(interactionOA, interactionOA2, interactionOA4));

        assertThat(this.commonQueryService.isFunctionalChain(operationalProcessOA));
        assertThat(operationalProcessOA.getDeclaredName()).isEqualTo(OPERATIONAL_PROCESS_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "1");
        assertThat(operationalProcessOA2.getDeclaredName()).isEqualTo(OPERATIONAL_PROCESS_DEFAULT_DECLAREDNAME_PREFIX + WHITE_SPACE + "2");
    }

}

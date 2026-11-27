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
package org.eclipse.capella.application.configuration.explorer.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_ACTOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_MISSION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_REQUIREMENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.FUNCTION_PACKAGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.PACKAGE;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.fixtures.ArcadiaPerspective;
import org.eclipse.capella.tests.fixtures.OperationalAnalysisPerspective;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.ChildCreationDescription;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests allowed explorer child creation descriptions against real Capella models.
 *
 * @author tbezierslafosse
 */
public class ArcadiaChildCreationDescriptionSwitchTests extends AbstractSemanticTests {

    private final ArcadiaChildCreationDescriptionSwitch childCreationDescriptionSwitch = new ArcadiaChildCreationDescriptionSwitch();

    private final CommonCreationService creationService = new CommonCreationService();

    @Test
    public void doSwitchWhenPackageHasCommonRoleShouldReturnAllowedChildren() {
        for (var perspective : this.perspectives()) {
            assertThat(this.allowedTypes(perspective.getRequirementsPackage().getElement())).containsExactly(ARCADIA_REQUIREMENT);
            assertThat(this.allowedTypes(perspective.getDataPackage().getElement())).containsExactly(ARCADIA_EXCHANGE_ITEM);
            assertThat(this.allowedTypes(perspective.getInterfacesPackage().getElement())).containsExactly(ARCADIA_EXCHANGE_ITEM);
        }
    }

    @Test
    public void doSwitchWhenPackageContainsCapabilitiesShouldOfferMissionsOnlyInSystemAnalysis() {
        assertThat(this.allowedTypes(this.capellaModel.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement()))
                .containsExactly(ARCADIA_CAPABILITY, ARCADIA_MISSION);
        for (var perspective : List.of(this.capellaModel.getOperationalAnalysisPerspective(), this.capellaModel.getLogicalArchitecturePerspective(),
                this.capellaModel.getPhysicalArchitecturePerspective())) {
            assertThat(this.allowedTypes(perspective.getCapabilitiesPackage().getElement())).containsExactly(ARCADIA_CAPABILITY);
        }
    }

    @Test
    public void doSwitchWhenRootFunctionExistsShouldOfferOnlyFunctionalChains() {
        for (var perspective : this.perspectives()) {
            assertThat(this.allowedTypes(perspective.getFunctionsPackage().getElement())).containsExactly(ARCADIA_FUNCTIONAL_CHAIN);
        }
    }

    @Test
    public void doSwitchWhenRootFunctionIsMissingShouldOfferFunctionsAndFunctionalChains() {
        for (var perspective : this.perspectives()) {
            EcoreUtil.remove(perspective.getFunctionsPackage().getRootFunction().getElement().eContainer());

            assertThat(this.allowedTypes(perspective.getFunctionsPackage().getElement())).containsExactly(ARCADIA_FUNCTION, ARCADIA_FUNCTIONAL_CHAIN);
        }
    }

    @Test
    public void doSwitchWhenContainerIsFunctionOrFunctionSubPackageShouldReturnFunctionOptions() {
        for (var perspective : this.perspectives()) {
            var root = perspective.getFunctionsPackage().getRootFunction().getElement();
            var pkg = this.creationService.createPackage(root);
            pkg.setDeclaredName("Renamed function package");
            var nested = this.creationService.createPackage(pkg);

            for (var container : List.of(root, pkg, nested)) {
                assertThat(this.allowedTypes(container)).containsExactly(ARCADIA_FUNCTION, FUNCTION_PACKAGE, ARCADIA_FUNCTIONAL_CHAIN);
            }
        }
    }

    @Test
    public void doSwitchWhenContainerIsStructurePackageShouldRespectPerspective() {
        assertThat(this.allowedTypes(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement()))
                .containsExactly(ARCADIA_ACTOR, PACKAGE);
        for (var perspective : List.of(this.capellaModel.getOperationalAnalysisPerspective(), this.capellaModel.getLogicalArchitecturePerspective(),
                this.capellaModel.getPhysicalArchitecturePerspective())) {
            assertThat(this.allowedTypes(perspective.getStructurePackage().getElement())).containsExactly(ARCADIA_COMPONENT, ARCADIA_ACTOR, PACKAGE);
        }
    }

    @Test
    public void doSwitchWhenContainerIsStructureSubPackageShouldRespectPerspective() {
        for (var perspective : this.perspectives()) {
            var pkg = this.creationService.createPackage(perspective.getStructurePackage().getElement());
            pkg.setDeclaredName("Renamed structure package");
            var nested = this.creationService.createPackage(pkg);
            var expectedTypes = List.of(PACKAGE);
            if (perspective instanceof OperationalAnalysisPerspective) {
                expectedTypes = List.of(ARCADIA_COMPONENT, ARCADIA_ACTOR, PACKAGE);
            }

            for (var container : List.of(pkg, nested)) {
                assertThat(this.allowedTypes(container)).containsExactlyElementsOf(expectedTypes);
            }
        }
    }

    @Test
    public void doSwitchWhenContainerIsParticipantShouldRespectActorFlagsAndPerspective() {
        for (var perspective : this.perspectives()) {
            var structure = perspective.getStructurePackage().getElement();
            var component = this.creationService.createComponent(structure);
            var actor = this.creationService.createActor(structure);
            var expectedActorTypes = List.of(ARCADIA_ACTOR);
            if (perspective instanceof OperationalAnalysisPerspective) {
                expectedActorTypes = List.of(ARCADIA_COMPONENT, ARCADIA_ACTOR);
            }

            assertThat(this.allowedTypes(component)).containsExactly(ARCADIA_COMPONENT, ARCADIA_ACTOR);
            assertThat(this.allowedTypes(actor)).containsExactlyElementsOf(expectedActorTypes);

            new CommonUpdateService().setBooleanAttribute(actor, CommonQueryService.ARCADIA_PREFIX + ARCADIA_COMPONENT, CommonQueryService.ARCADIA_IS_HUMAN, true);
            assertThat(this.allowedTypes(actor)).isEmpty();
        }
    }

    @Test
    public void doSwitchWhenContainerHasNoCreationOptionsShouldReturnEmptyList() {
        for (var perspective : this.perspectives()) {
            var capability = this.creationService.createCapability(perspective.getCapabilitiesPackage().getElement());
            var chain = this.creationService.createFunctionalChain(perspective.getFunctionsPackage().getRootFunction().getElement(), List.of());

            assertThat(this.allowedTypes(perspective.getElement())).isEmpty();
            assertThat(this.allowedTypes(capability)).isEmpty();
            assertThat(this.allowedTypes(chain)).isEmpty();
        }
        assertThat(this.allowedTypes(SysmlFactory.eINSTANCE.createPackage())).isEmpty();
    }

    @Test
    public void doSwitchWhenPackagesHaveCommonRolesShouldPreserveRolePrecedence() {
        for (var perspective : this.perspectives()) {
            var structurePackage = this.creationService.createPackage(perspective.getStructurePackage().getElement());
            structurePackage.setDeclaredName(CommonQueryService.REQUIREMENTS_PACKAGE);
            assertThat(this.allowedTypes(structurePackage)).containsExactly(ARCADIA_REQUIREMENT);

            structurePackage.setDeclaredName(CommonQueryService.CAPABILITIES_PACKAGE);
            assertThat(this.allowedTypes(structurePackage))
                    .containsExactlyElementsOf(this.allowedTypes(perspective.getCapabilitiesPackage().getElement()));

            var functionPackage = this.creationService.createPackage(perspective.getFunctionsPackage().getRootFunction().getElement());
            functionPackage.setDeclaredName(CommonQueryService.REQUIREMENTS_PACKAGE);
            assertThat(this.allowedTypes(functionPackage)).containsExactly(ARCADIA_FUNCTION, FUNCTION_PACKAGE, ARCADIA_FUNCTIONAL_CHAIN);
        }
    }

    @Test
    public void doSwitchShouldReturnPerspectiveLabelsAndIcons() {
        var expectedActorIcons = Map.of(
                this.capellaModel.getOperationalAnalysisPerspective().getElement(), "ActorHuman",
                this.capellaModel.getSystemAnalysisPerspective().getElement(), "SystemActor",
                this.capellaModel.getLogicalArchitecturePerspective().getElement(), "LogicalActor",
                this.capellaModel.getPhysicalArchitecturePerspective().getElement(), "PhysicalActor");
        var expectedFunctionIcons = Map.of(
                this.capellaModel.getOperationalAnalysisPerspective().getElement(), "OperationalActivity",
                this.capellaModel.getSystemAnalysisPerspective().getElement(), "SystemFunction",
                this.capellaModel.getLogicalArchitecturePerspective().getElement(), "LogicalFunction",
                this.capellaModel.getPhysicalArchitecturePerspective().getElement(), "PhysicalFunction");
        for (var perspective : this.perspectives()) {
            var root = perspective.getFunctionsPackage().getRootFunction().getElement();
            var functionLabel = ARCADIA_FUNCTION;
            var packageLabel = "Function Package";
            var chainLabel = ARCADIA_FUNCTIONAL_CHAIN;
            var chainIcon = ARCADIA_FUNCTIONAL_CHAIN;
            var actorLabel = ARCADIA_ACTOR;
            if (perspective instanceof OperationalAnalysisPerspective) {
                functionLabel = "Operational Activity";
                packageLabel = "Operational Activity Package";
                chainLabel = "Operational Process";
                chainIcon = "OperationalProcess";
                actorLabel = "Human Actor";
            }

            assertThat(this.childCreationDescriptionSwitch.doSwitch(root)).containsExactlyInAnyOrder(
                    this.expectedDescription(ARCADIA_FUNCTION, functionLabel, expectedFunctionIcons.get(perspective.getElement())),
                    this.expectedDescription(FUNCTION_PACKAGE, packageLabel, "LogicalFunctionPkg"),
                    this.expectedDescription(ARCADIA_FUNCTIONAL_CHAIN, chainLabel, chainIcon));
            assertThat(this.childCreationDescriptionSwitch.doSwitch(perspective.getStructurePackage().getElement()))
                    .contains(this.expectedDescription(ARCADIA_ACTOR, actorLabel, expectedActorIcons.get(perspective.getElement())));
        }
    }

    private List<ArcadiaPerspective> perspectives() {
        return List.of(this.capellaModel.getOperationalAnalysisPerspective(), this.capellaModel.getSystemAnalysisPerspective(),
                this.capellaModel.getLogicalArchitecturePerspective(), this.capellaModel.getPhysicalArchitecturePerspective());
    }

    private List<String> allowedTypes(Element container) {
        return this.childCreationDescriptionSwitch.doSwitch(container).stream()
                .map(description -> description.id().substring(CapellaEditService.ID_PREFIX.length()))
                .toList();
    }

    private ChildCreationDescription expectedDescription(String type, String label, String icon) {
        return new ChildCreationDescription(CapellaEditService.ID_PREFIX + type, label, List.of("/icons/full/obj16/" + icon + ".svg"));
    }
}

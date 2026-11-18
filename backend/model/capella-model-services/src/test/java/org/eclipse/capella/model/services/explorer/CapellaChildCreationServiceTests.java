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
package org.eclipse.capella.model.services.explorer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_ACTOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_REQUIREMENT;

import java.util.List;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.fixtures.ArcadiaPerspective;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests explorer creation rules against real Capella models.
 *
 * @author tbezierslafosse
 */
public class CapellaChildCreationServiceTests {

    private static final SemanticDataTestFixture SEMANTIC_DATA_TEST_FIXTURE = new SemanticDataTestFixture();

    private final CapellaChildCreationService childCreationService = new CapellaChildCreationService();

    private final CommonQueryService queryService = new CommonQueryService();

    private final CommonCreationService creationService = new CommonCreationService();

    private CapellaModel capellaModel;

    @BeforeEach
    public void setUp() {
        this.capellaModel = SEMANTIC_DATA_TEST_FIXTURE.createCapellaModel();
    }

    @Test
    public void requirementsShouldBeCreatedOnlyInRequirementsPackages() {
        for (var perspective : this.perspectives()) {
            var pkg = perspective.getRequirementsPackage().getElement();
            var requirement = this.create(pkg, ARCADIA_REQUIREMENT);
            assertThat(requirement).isInstanceOf(org.eclipse.syson.sysml.RequirementUsage.class);
            assertThat(requirement.getOwner()).isSameAs(pkg);
            assertThat(requirement.getDeclaredName()).startsWith("Requirement");
            assertThat(this.queryService.isArcadiaElement(requirement)).isFalse();
            var functions = perspective.getFunctionsPackage().getRootFunction().getElement();
            var relationships = List.copyOf(functions.getOwnedRelationship());
            assertThat(this.childCreationService.createChild(functions, ARCADIA_REQUIREMENT)).isEmpty();
            assertThat(functions.getOwnedRelationship()).containsExactlyElementsOf(relationships);
            assertThat(this.childCreationService.createChild(requirement, ARCADIA_REQUIREMENT)).isEmpty();
        }
    }

    @Test
    public void packagesShouldOfferOnlyAllowedChildrenInEveryPerspective() {
        for (var perspective : this.perspectives()) {
            assertThat(this.allowedTypes(perspective.getElement())).isEmpty();
            assertThat(this.allowedTypes(perspective.getInterfacesPackage().getElement())).containsExactly("ExchangeItem");
            assertThat(this.allowedTypes(perspective.getDataPackage().getElement())).containsExactly("ExchangeItem");
            assertThat(this.allowedTypes(perspective.getRequirementsPackage().getElement())).containsExactly(ARCADIA_REQUIREMENT);
            var capabilityTypes = this.allowedTypes(perspective.getCapabilitiesPackage().getElement());
            assertThat(capabilityTypes).contains("Capability");
            assertThat(this.allowedTypes(perspective.getFunctionsPackage().getElement())).hasSize(2).doesNotContain("FunctionPackage");
            assertThat(this.allowedTypes(perspective.getFunctionsPackage().getRootFunction().getElement())).hasSize(3).contains("FunctionPackage");
        }
        assertThat(this.allowedTypes(this.capellaModel.getSystemAnalysisPerspective().getCapabilitiesPackage().getElement()))
                .containsExactly("Capability");
        assertThat(this.allowedTypes(this.capellaModel.getLogicalArchitecturePerspective().getCapabilitiesPackage().getElement()))
                .containsExactly("Capability");
        assertThat(this.allowedTypes(this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getElement()))
                .containsExactly("Function", "FunctionalChain");
    }

    @Test
    public void functionPackagesShouldCreateChildrenInSelectedPackage() {
        for (var perspective : this.perspectives()) {
            var root = perspective.getFunctionsPackage().getRootFunction().getElement();
            var pkg = (Package) this.create(root, "FunctionPackage");
            assertThat(pkg.getOwner()).isSameAs(root);
            assertThat(this.allowedTypes(pkg)).hasSize(3);
            var child = this.create(pkg, "Function");
            assertThat(child.getOwner()).isSameAs(pkg);
            assertThat(this.queryService.isFunction(child)).isTrue();
            var chain = this.create(pkg, "FunctionalChain");
            assertThat(chain.getOwner()).isSameAs(pkg);
            assertThat(this.queryService.isFunctionalChain(chain)).isTrue();
            assertThat(this.create(pkg, "FunctionPackage").getOwner()).isSameAs(pkg);
            assertThat(this.create(root, "FunctionalChain").getOwner()).isSameAs(root);
            assertThat(this.create(perspective.getFunctionsPackage().getElement(), "Function").getOwner()).isSameAs(root);
        }
    }

    @Test
    public void capabilityAndExchangeItemShouldHaveCorrectOwnersAndTypes() {
        for (var perspective : this.perspectives()) {
            var pkg = perspective.getCapabilitiesPackage().getElement();
            var capability = this.create(pkg, "Capability");
            assertThat(capability.getOwner()).isSameAs(pkg);
            assertThat(this.queryService.isCapability(capability)).isTrue();
            var interfaces = perspective.getInterfacesPackage().getElement();
            var item = this.create(interfaces, "ExchangeItem");
            assertThat(item.getOwner()).isSameAs(interfaces);
            assertThat(this.queryService.isExchangeItem(item)).isTrue();
        }
    }

    @Test
    public void missionsShouldBeRejectedInEveryPerspectiveWithoutMutation() {
        for (var perspective : this.perspectives()) {
            var capabilities = perspective.getCapabilitiesPackage().getElement();
            var relationships = List.copyOf(capabilities.getOwnedRelationship());
            assertThat(this.allowedTypes(capabilities)).containsExactly("Capability");
            assertThat(this.childCreationService.createChild(capabilities, "Mission")).isEmpty();
            assertThat(capabilities.getOwnedRelationship()).containsExactlyElementsOf(relationships);
        }
    }

    @Test
    public void componentsShouldRespectSystemAndActorCreationRules() {
        var sa = this.capellaModel.getSystemAnalysisPerspective();
        var structure = sa.getStructurePackage().getElement();
        assertThat(this.allowedTypes(structure)).containsExactly(ARCADIA_ACTOR);
        var actor = this.create(structure, ARCADIA_ACTOR);
        assertThat(this.allowedTypes(actor)).containsExactly(ARCADIA_ACTOR);
        assertThat(this.create(actor, ARCADIA_ACTOR).getOwner()).isSameAs(actor);
        var system = structure.getOwnedElement().stream().filter(this.queryService::isComponent)
                .filter(element -> !this.queryService.isComponentActor(element)).findFirst().orElseThrow();
        assertThat(this.create(system, ARCADIA_COMPONENT).getOwner()).isSameAs(system);
        assertThat(this.create(system, ARCADIA_ACTOR).getOwner()).isSameAs(structure);
        var la = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        var component = this.create(la, ARCADIA_COMPONENT);
        assertThat(component.getOwner()).isNotSameAs(la);
        assertThat(this.create(component, ARCADIA_COMPONENT).getOwner()).isSameAs(component);
        var pa = this.capellaModel.getPhysicalArchitecturePerspective().getStructurePackage().getElement();
        var physicalComponent = this.create(pa, ARCADIA_COMPONENT);
        assertThat(physicalComponent.getOwner()).isSameAs(pa);
        assertThat(this.queryService.isComponent(physicalComponent)).isTrue();
        assertThat(this.create(physicalComponent, ARCADIA_COMPONENT).getOwner()).isSameAs(physicalComponent);
    }

    @Test
    public void operationalActorsShouldBeHumanAndRejectNestedParticipants() {
        var structure = this.capellaModel.getOperationalAnalysisPerspective().getStructurePackage().getElement();
        var entity = this.create(structure, ARCADIA_COMPONENT);
        assertThat(this.allowedTypes(entity)).containsExactly(ARCADIA_COMPONENT, ARCADIA_ACTOR);
        var nestedEntity = this.create(entity, ARCADIA_COMPONENT);
        assertThat(nestedEntity.getOwner()).isSameAs(entity);
        var actor = this.create(entity, ARCADIA_ACTOR);
        assertThat(actor.getOwner()).isSameAs(entity);
        assertThat(this.queryService.isComponentHumanActor(actor)).isTrue();
        assertThat(this.allowedTypes(actor)).isEmpty();
        assertThat(this.childCreationService.createChild(actor, ARCADIA_COMPONENT)).isEmpty();
    }

    @Test
    public void forbiddenRequestsShouldLeaveModelUnchanged() {
        for (var perspective : this.perspectives()) {
            var pkg = perspective.getElement();
            var originalChildren = List.copyOf(pkg.getOwnedRelationship());
            assertThat(this.childCreationService.createChild(pkg, "Function")).isEmpty();
            assertThat(pkg.getOwnedRelationship()).containsExactlyElementsOf(originalChildren);
            var functions = perspective.getFunctionsPackage().getElement();
            assertThat(this.childCreationService.createChild(functions, "FunctionPackage")).isEmpty();
        }
        var la = this.capellaModel.getLogicalArchitecturePerspective();
        var actor = this.creationService.createActor(la.getStructurePackage().getElement());
        assertThat(this.childCreationService.createChild(actor, ARCADIA_COMPONENT)).isEmpty();
    }

    private List<ArcadiaPerspective> perspectives() {
        return List.of(this.capellaModel.getOperationalAnalysisPerspective(), this.capellaModel.getSystemAnalysisPerspective(),
                this.capellaModel.getLogicalArchitecturePerspective(), this.capellaModel.getPhysicalArchitecturePerspective());
    }

    private List<String> allowedTypes(Element container) {
        return this.childCreationService.getAllowedChildTypes(container);
    }

    private Element create(Element container, String type) {
        return this.childCreationService.createChild(container, type).orElseThrow();
    }
}

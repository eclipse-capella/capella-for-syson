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
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.capella.application.configuration.explorer.filters.CapellaTreeFilterProvider;
import org.eclipse.capella.model.services.operational.analysis.OAExplorerFilterServiceProvider;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.api.ISysONResourceService;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests port visibility in the Capella explorer with real Arcadia elements.
 *
 * @author tbezierslafosse
 */
public class CapellaExplorerFilterServiceTests extends AbstractSemanticTests {

    private final CommonCreationService creationService = new CommonCreationService();

    private final CommonQueryService queryService = new CommonQueryService();

    private final CapellaExplorerFilterService filterService = new CapellaExplorerFilterService(mock(ISysONResourceService.class), List.of(new OAExplorerFilterServiceProvider()));

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void activityPortsShouldAlwaysBeHiddenWithoutChangingInteractions(boolean hidePorts) {
        var perspective = this.capellaModel.getOperationalAnalysisPerspective();
        var root = perspective.getFunctionsPackage().getRootFunction().getElement();
        var source = this.creationService.createFunction(root);
        var target = this.creationService.createFunction(root);
        var interaction = this.creationService.createFunctionalExchange(source, target);
        assertThat(interaction).isNotNull();
        var sourcePort = this.queryService.getFunctionalExchangeSource(interaction);
        var targetPort = this.queryService.getFunctionalExchangeTarget(interaction);
        assertThat(sourcePort).isNotNull();
        assertThat(targetPort).isNotNull();
        var exchangeItem = this.creationService.createNewExchangeItem(perspective.getDataPackage().getElement());
        List<Object> elements = List.of(source, sourcePort, target, targetPort, interaction, exchangeItem);
        var semanticRoot = this.capellaModel.getResource().getContents().getFirst();
        var originalModel = EcoreUtil.copy(semanticRoot);
        List<String> activeFilters = List.of();
        if (hidePorts) {
            activeFilters = List.of(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID);
        }

        assertThat(this.filterService.applyFilters(new IEditingContext.NoOp(), elements, activeFilters))
                .containsExactly(source, target, interaction, exchangeItem);
        assertThat(elements).hasSize(6);
        assertThat(EcoreUtil.equals(semanticRoot, originalModel)).isTrue();
        assertThat(sourcePort.getOwner()).isSameAs(source);
        assertThat(targetPort.getOwner()).isSameAs(target);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void otherPortsShouldOnlyBeHiddenWhenFilterIsActive(boolean hidePorts) {
        var perspectives = List.of(this.capellaModel.getOperationalAnalysisPerspective(), this.capellaModel.getSystemAnalysisPerspective(),
                this.capellaModel.getLogicalArchitecturePerspective(), this.capellaModel.getPhysicalArchitecturePerspective());
        List<String> activeFilters = List.of();
        if (hidePorts) {
            activeFilters = List.of(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID);
        }
        for (var perspective : perspectives) {
            var component = this.creationService.createComponent(perspective.getStructurePackage().getElement());
            var componentPort = this.creationService.createComponentPort(component, FeatureDirectionKind.IN);
            var actor = this.creationService.createActor(perspective.getStructurePackage().getElement());
            var actorPort = this.creationService.createComponentPort(actor, FeatureDirectionKind.OUT);
            var function = this.creationService.createFunction(perspective.getFunctionsPackage().getRootFunction().getElement());
            var inputPort = this.creationService.createFunctionPort(function, FeatureDirectionKind.IN);
            var outputPort = this.creationService.createFunctionPort(function, FeatureDirectionKind.OUT);
            List<Object> elements = List.of(component, componentPort, actor, actorPort, function, inputPort, outputPort);
            List<Object> expectedElements = elements;
            if (hidePorts) {
                expectedElements = List.of(component, actor, function);
            } else if (this.queryService.isOperationalActivity(function)) {
                expectedElements = List.of(component, componentPort, actor, actorPort, function);
            }

            assertThat(this.filterService.applyVisibilityFilters(new IEditingContext.NoOp(), elements, activeFilters)).containsExactlyElementsOf(expectedElements);
            assertThat(elements).hasSize(7);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void membershipChildrenShouldAlwaysHideActivityPorts(boolean hidePorts) {
        var root = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var activity = this.creationService.createFunction(root);
        var port = this.creationService.createFunctionPort(activity, FeatureDirectionKind.IN);
        var child = this.creationService.createFunction(activity);
        List<String> activeFilters = List.of();
        if (hidePorts) {
            activeFilters = List.of(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID);
        }

        assertThat(this.filterService.applyFilters(new IEditingContext.NoOp(), List.of(port.getOwningMembership(), child.getOwningMembership()), activeFilters))
                .containsExactly(child);
        assertThat(this.filterService.applyFilters(new IEditingContext.NoOp(), List.of(port.getOwningMembership()), activeFilters)).isEmpty();
        assertThat(port.getOwner()).isSameAs(activity);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void applyVisibilityFiltersShouldPreserveAncestorResourcesNamespacesAndMemberships(boolean hidePorts) {
        var semanticRoot = this.capellaModel.getResource().getContents().getFirst();
        var rootActivity = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var activity = this.creationService.createFunction(rootActivity);
        var membership = activity.getOwningMembership();
        List<String> activeFilters = List.of();
        if (hidePorts) {
            activeFilters = List.of(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID);
        }

        var ancestors = List.of(this.capellaModel.getResource(), semanticRoot, membership);
        assertThat(this.filterService.applyVisibilityFilters(new IEditingContext.NoOp(), ancestors, activeFilters))
                .containsExactlyElementsOf(ancestors);
        assertThat(activity.getOwningMembership()).isSameAs(membership);
        assertThat(activity.getOwner()).isSameAs(rootActivity);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void activityPortsShouldFollowOptionalFilterWithoutOAProvider(boolean hidePorts) {
        var root = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var activity = this.creationService.createFunction(root);
        var port = this.creationService.createFunctionPort(activity, FeatureDirectionKind.IN);
        var genericService = new CapellaExplorerFilterService(mock(ISysONResourceService.class), List.of());
        List<String> activeFilters = List.of();
        List<Object> expectedElements = List.of(activity, port);
        if (hidePorts) {
            activeFilters = List.of(CapellaTreeFilterProvider.HIDE_PORTS_TREE_ITEM_FILTER_ID);
            expectedElements = List.of(activity);
        }

        assertThat(genericService.applyVisibilityFilters(new IEditingContext.NoOp(), List.of(activity, port), activeFilters))
                .containsExactlyElementsOf(expectedElements);
    }
}

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

import java.util.stream.Stream;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureTyping;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the common semantic element query service.
 *
 * @author Jerome Gout
 */
public class CommonQueryServiceTests extends AbstractSemanticTests {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    @Test
    public void getRequirementsShouldIncludeNativeAndArcadiaRequirementsFromTheSamePerspective() {
        var creationService = new CommonCreationService();
        var logicalContext = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        var nativeRequirement = creationService.createRequirement(logicalContext);
        var arcadiaRequirement = creationService.createRequirement(logicalContext);
        var otherPerspective = creationService.createRequirement(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement());
        var library = new ArcadiaLibraryServices();
        library.typeWithLibrary(arcadiaRequirement, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_REQUIREMENT,
                SysmlPackage.eINSTANCE.getRequirementDefinition());
        library.typeWithLibrary(otherPerspective, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_REQUIREMENT,
                SysmlPackage.eINSTANCE.getRequirementDefinition());

        assertThat(this.commonQueryService.getRequirements(logicalContext)).contains(nativeRequirement, arcadiaRequirement).doesNotContain(otherPerspective);
        assertThat(this.commonQueryService.isRequirement(nativeRequirement)).isTrue();
        assertThat(this.commonQueryService.isArcadiaRequirement(nativeRequirement)).isFalse();
    }

    @Test
    public void getRealizableElementsShouldFollowCandidateRetypingAndRejectUntypedSource() {
        var creationService = new CommonCreationService();
        var source = creationService.createComponent(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        var candidate = creationService.createComponent(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement());
        var typing = candidate.getOwnedRelationship().stream().filter(FeatureTyping.class::isInstance).map(FeatureTyping.class::cast).findFirst().orElseThrow();
        var originalType = typing.getType();
        assertThat(this.commonQueryService.getRealizableElements(source)).contains(candidate);

        typing.setType(null);

        assertThat(this.commonQueryService.getRealizableElements(source)).doesNotContain(candidate);
        typing.setType(originalType);
        assertThat(this.commonQueryService.getRealizableElements(source)).contains(candidate);
        source.getOwnedRelationship().stream().filter(FeatureTyping.class::isInstance).map(FeatureTyping.class::cast).forEach(sourceTyping -> sourceTyping.setType(null));
        assertThat(this.commonQueryService.getRealizableElements(source)).isEmpty();
    }

    @Test
    public void getComponentsWhenIndexInstalledShouldPreserveCandidatesAfterRetyping() {
        var creationService = new CommonCreationService();
        var parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        var first = creationService.createComponent(parent);
        creationService.createComponent(parent);
        var expected = this.commonQueryService.getComponents(parent);
        var resourceSet = parent.eResource().getResourceSet();
        var adapter = new ArcadiaElementContentAdapter(resourceSet);
        resourceSet.eAdapters().add(adapter);
        try {
            assertThat(this.commonQueryService.getComponents(parent)).containsExactlyInAnyOrderElementsOf(expected);
            var typing = first.getOwnedRelationship().stream().filter(FeatureTyping.class::isInstance).map(FeatureTyping.class::cast).findFirst().orElseThrow();
            var type = typing.getType();
            typing.setType(null);
            assertThat(this.commonQueryService.getComponents(parent)).doesNotContain(first);
            typing.setType(type);
            assertThat(this.commonQueryService.getComponents(parent)).containsExactlyInAnyOrderElementsOf(expected);
        } finally {
            resourceSet.eAdapters().remove(adapter);
        }
    }

    @Test
    public void getAllocatedFunctionsWhenIndexInstalledShouldResolvePerformedFunction() {
        var creationService = new CommonCreationService();
        var perspective = this.capellaModel.getLogicalArchitecturePerspective();
        var component = creationService.createComponent(perspective.getStructurePackage().getElement());
        var function = creationService.createFunction(perspective.getFunctionsPackage().getRootFunction().getElement());
        var resourceSet = component.eResource().getResourceSet();
        var adapter = new ArcadiaElementContentAdapter(resourceSet);
        resourceSet.eAdapters().add(adapter);
        try {
            new CommonUpdateService().setPerformAction(component, function);
            assertThat(this.commonQueryService.getAllocatedFunctions(component)).containsExactly(function);
        } finally {
            resourceSet.eAdapters().remove(adapter);
        }
    }

    @Test
    public void isFunctionPortWhenIndexInstalledShouldRecognizeArcadiaExchangeItemInFunction() {
        var creationService = new CommonCreationService();
        var parent = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var resourceSet = parent.eResource().getResourceSet();
        var adapter = new ArcadiaElementContentAdapter(resourceSet);
        resourceSet.eAdapters().add(adapter);
        try {
            var port = creationService.createFunctionPort(parent, FeatureDirectionKind.IN);
            var exchangeItem = creationService.createNewExchangeItem(this.commonQueryService.getFunctionsPackage(parent).orElseThrow());
            assertThat(adapter.getType(port)).contains(CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_EXCHANGE_ITEM);
            assertThat(this.commonQueryService.isFunctionPort(port)).isTrue();
            assertThat(this.commonQueryService.isFunctionPort(exchangeItem)).isFalse();
            assertThat(this.commonQueryService.getExchangeItems(parent)).contains(port, exchangeItem);
        } finally {
            resourceSet.eAdapters().remove(adapter);
        }
    }

    @Test
    public void getArcadiaPerspectivePackageShouldReturnTheRequestedPerspectivePackage() {
        var context = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        var expected = this.capellaModel.getSystemAnalysisPerspective().getElement();

        var result = this.commonQueryService.getArcadiaPerspectivePackage(context, ArcadiaEngineeringPerspective.SystemAnalysis);

        assertThat(result).contains(expected);
    }

    @Test
    public void getRealizableElementsShouldOnlyReturnMatchingFunctionsFromPreviousPerspective() {
        var source = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var expected = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var earlier = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var later = this.capellaModel.getPhysicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.commonQueryService.getRealizableElements(source))
                .contains(expected)
                .doesNotContain(earlier, source, later);
    }

    @Test
    public void getRealizableElementsShouldBeEmptyForOperationalAnalysis() {
        var source = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.commonQueryService.getRealizableElements(source)).isEmpty();
    }

    @Test
    public void getRealizableElementsWhenSourceIsActorShouldOnlyReturnActors() {
        var creationService = new CommonCreationService();
        var source = creationService.createActor(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        var targetPackage = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        var actor = creationService.createActor(targetPackage);
        var component = creationService.createComponent(targetPackage);

        assertThat(this.commonQueryService.getRealizableElements(source)).contains(actor).doesNotContain(component);
    }

    @Test
    public void getRealizableElementsWhenSourceIsComponentShouldExcludeActors() {
        var creationService = new CommonCreationService();
        var source = creationService.createComponent(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        var targetPackage = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        var actor = creationService.createActor(targetPackage);
        var component = creationService.createComponent(targetPackage);

        assertThat(this.commonQueryService.getRealizableElements(source)).contains(component).doesNotContain(actor);
    }

    @Test
    public void getRealizableElementsWhenSourceIsFunctionPortShouldExcludeExchangeItemsOutsideFunctions() {
        var creationService = new CommonCreationService();
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var sourcePort = creationService.createFunctionPort(logicalFunction, FeatureDirectionKind.IN);
        var candidatePort = creationService.createFunctionPort(systemFunction, FeatureDirectionKind.IN);
        var targetPackage = this.commonQueryService.getFunctionsPackage(systemFunction).orElseThrow();
        var exchangeItem = creationService.createNewExchangeItem(targetPackage);
        exchangeItem.setDirection(FeatureDirectionKind.IN);

        assertThat(this.commonQueryService.getRealizableElements(sourcePort))
                .contains(candidatePort)
                .doesNotContain(exchangeItem);
    }

    private static Stream<Arguments> getRealizableElementsForPortsShouldRespectDirectionCompatibility() {
        return Stream.of(
                Arguments.of(FeatureDirectionKind.IN, FeatureDirectionKind.IN, true),
                Arguments.of(FeatureDirectionKind.IN, FeatureDirectionKind.OUT, false),
                Arguments.of(FeatureDirectionKind.IN, FeatureDirectionKind.INOUT, true),
                Arguments.of(FeatureDirectionKind.IN, null, true),
                Arguments.of(FeatureDirectionKind.OUT, FeatureDirectionKind.IN, false),
                Arguments.of(FeatureDirectionKind.OUT, FeatureDirectionKind.OUT, true),
                Arguments.of(FeatureDirectionKind.OUT, FeatureDirectionKind.INOUT, true),
                Arguments.of(FeatureDirectionKind.OUT, null, true),
                Arguments.of(FeatureDirectionKind.INOUT, FeatureDirectionKind.IN, true),
                Arguments.of(FeatureDirectionKind.INOUT, FeatureDirectionKind.OUT, true),
                Arguments.of(FeatureDirectionKind.INOUT, FeatureDirectionKind.INOUT, true),
                Arguments.of(FeatureDirectionKind.INOUT, null, true),
                Arguments.of(null, FeatureDirectionKind.IN, true),
                Arguments.of(null, FeatureDirectionKind.OUT, true),
                Arguments.of(null, FeatureDirectionKind.INOUT, true),
                Arguments.of(null, null, true));
    }

    @ParameterizedTest
    @MethodSource
    public void getRealizableElementsForPortsShouldRespectDirectionCompatibility(FeatureDirectionKind sourceDirection, FeatureDirectionKind candidateDirection, boolean compatible) {
        var creationService = new CommonCreationService();
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        // we cannot pass directly direction variables to createFunctionPort because null value which would raise an NPE.
        var sourceFunctionPort = creationService.createFunctionPort(logicalFunction, FeatureDirectionKind.IN);
        var candidateFunctionPort = creationService.createFunctionPort(systemFunction, FeatureDirectionKind.IN);
        // setDirection allows to unset the direction with a null value.
        sourceFunctionPort.setDirection(sourceDirection);
        candidateFunctionPort.setDirection(candidateDirection);
        var logicalComponent = creationService.createComponent(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        var systemComponent = creationService.createComponent(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement());
        var sourceComponentPort = creationService.createComponentPort(logicalComponent, FeatureDirectionKind.IN);
        var candidateComponentPort = creationService.createComponentPort(systemComponent, FeatureDirectionKind.IN);
        sourceComponentPort.setDirection(sourceDirection);
        candidateComponentPort.setDirection(candidateDirection);

        assertThat(this.commonQueryService.getRealizableElements(sourceFunctionPort).contains(candidateFunctionPort)).isEqualTo(compatible);
        assertThat(this.commonQueryService.getRealizableElements(sourceComponentPort).contains(candidateComponentPort)).isEqualTo(compatible);
    }


    @Test
    public void getRealizableElementsWhenSourceIsFunctionalExchangeShouldExcludeFunctions() {
        var creationService = new CommonCreationService();
        var logicalRoot = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemRoot = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var logicalSource = creationService.createFunction(logicalRoot);
        var logicalTarget = creationService.createFunction(logicalRoot);
        var systemSource = creationService.createFunction(systemRoot);
        var systemTarget = creationService.createFunction(systemRoot);
        var logicalExchange = creationService.createFunctionalExchange(logicalSource, logicalTarget);
        var systemExchange = creationService.createFunctionalExchange(systemSource, systemTarget);

        assertThat(logicalExchange).isNotNull();
        assertThat(systemExchange).isNotNull();
        assertThat(this.commonQueryService.getRealizableElements(logicalExchange))
                .contains(systemExchange)
                .doesNotContain(systemRoot, systemSource, systemTarget);
    }

}

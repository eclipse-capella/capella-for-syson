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
import org.eclipse.syson.sysml.FeatureDirectionKind;
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

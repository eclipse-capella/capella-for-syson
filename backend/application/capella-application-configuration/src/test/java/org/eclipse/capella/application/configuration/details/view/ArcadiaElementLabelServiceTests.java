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
package org.eclipse.capella.application.configuration.details.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.application.configuration.details.view.services.ArcadiaElementLabelService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.junit.jupiter.api.Test;

/**
 * Tests perspective-specific Arcadia labels.
 *
 * @author Jerome Gout
 */
public class ArcadiaElementLabelServiceTests extends AbstractSemanticTests {

    private final ArcadiaElementLabelService service = new ArcadiaElementLabelService();

    @Test
    public void getRealizesWidgetLabelValueShouldUseNameFromRealizedPerspective() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var physicalFunction = this.capellaModel.getPhysicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.getRealizesWidgetLabelValue(operationalFunction)).isEmpty();
        assertThat(this.service.getRealizesWidgetLabelValue(systemFunction)).isEqualTo("Realized Operational Activities");
        assertThat(this.service.getRealizesWidgetLabelValue(logicalFunction)).isEqualTo("Realized System Functions");
        assertThat(this.service.getRealizesWidgetLabelValue(physicalFunction)).isEqualTo("Realized Logical Functions");
    }

    @Test
    public void getRealizesWidgetLabelValueShouldMatchFunctionPortDirection() {
        var function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var input = creationService.createFunctionPort(function, FeatureDirectionKind.IN);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);

        assertThat(this.service.getRealizesWidgetLabelValue(input)).isEqualTo("Realized Function Input Ports");
        assertThat(this.service.getRealizesWidgetLabelValue(output)).isEqualTo("Realized Function Output Ports");
    }

    @Test
    public void getRealizesWidgetLabelValueWhenFunctionPortIsInSystemAnalysisShouldBeEmpty() {
        var function = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var input = creationService.createFunctionPort(function, FeatureDirectionKind.IN);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);

        assertThat(this.service.getRealizesWidgetLabelValue(input)).isEmpty();
        assertThat(this.service.getRealizesWidgetLabelValue(output)).isEmpty();
        assertThat(this.service.hasRealizesWidget(input)).isFalse();
        assertThat(this.service.hasRealizesWidget(output)).isFalse();
    }

    @Test
    public void hasRealizesWidgetShouldFollowLabelAvailability() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.hasRealizesWidget(operationalFunction)).isFalse();
        assertThat(this.service.hasRealizesWidget(systemFunction)).isTrue();
    }

    @Test
    public void getRealizesWidgetLabelValueShouldReturnLabelOrEmptyString() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.getRealizesWidgetLabelValue(operationalFunction)).isEmpty();
        assertThat(this.service.getRealizesWidgetLabelValue(systemFunction)).isEqualTo("Realized Operational Activities");
    }
}

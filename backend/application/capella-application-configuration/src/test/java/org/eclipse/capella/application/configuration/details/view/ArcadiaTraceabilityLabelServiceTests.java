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

import java.util.List;

import org.eclipse.capella.application.configuration.details.view.referencewidget.RealizingReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.services.ArcadiaTraceabilityLabelService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceFactory;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests perspective-specific Arcadia labels.
 *
 * @author Jerome Gout
 */
public class ArcadiaTraceabilityLabelServiceTests extends AbstractSemanticTests {

    private final ArcadiaTraceabilityLabelService service = new ArcadiaTraceabilityLabelService();

    @Test
    public void getRealizedWidgetLabelValueShouldUseNameFromRealizedPerspective() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var physicalFunction = this.capellaModel.getPhysicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.getRealizedWidgetLabelValue(operationalFunction)).isEmpty();
        assertThat(this.service.getRealizedWidgetLabelValue(systemFunction)).isEqualTo("Realized Operational Activities");
        assertThat(this.service.getRealizedWidgetLabelValue(logicalFunction)).isEqualTo("Realized System Functions");
        assertThat(this.service.getRealizedWidgetLabelValue(physicalFunction)).isEqualTo("Realized Logical Functions");
    }

    @Test
    public void getRealizedWidgetLabelValueShouldMatchFunctionPortDirection() {
        var function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var input = creationService.createFunctionPort(function, FeatureDirectionKind.IN);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);

        assertThat(this.service.getRealizedWidgetLabelValue(input)).isEqualTo("Realized Function Input Ports");
        assertThat(this.service.getRealizedWidgetLabelValue(output)).isEqualTo("Realized Function Output Ports");
    }

    @Test
    public void getRealizedWidgetLabelValueWhenFunctionPortIsInSystemAnalysisShouldBeEmpty() {
        var function = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var input = creationService.createFunctionPort(function, FeatureDirectionKind.IN);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);

        assertThat(this.service.getRealizedWidgetLabelValue(input)).isEmpty();
        assertThat(this.service.getRealizedWidgetLabelValue(output)).isEmpty();
        assertThat(this.service.hasRealizedWidget(input)).isFalse();
        assertThat(this.service.hasRealizedWidget(output)).isFalse();
    }

    @Test
    public void hasRealizedWidgetShouldFollowLabelAvailability() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.hasRealizedWidget(operationalFunction)).isFalse();
        assertThat(this.service.hasRealizedWidget(systemFunction)).isTrue();
    }

    @Test
    public void getRealizedWidgetLabelValueShouldReturnLabelOrEmptyString() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.getRealizedWidgetLabelValue(operationalFunction)).isEmpty();
        assertThat(this.service.getRealizedWidgetLabelValue(systemFunction)).isEqualTo("Realized Operational Activities");
    }

    @Test
    public void hasRealizingWidgetShouldFollowReferenceAvailability() {
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var updateService = new CommonUpdateService();

        updateService.setRealized(realizer, realized);
        assertThat(this.service.hasRealizingWidget(realized)).isTrue();
        var provider = new RealizingReferenceWidgetProvider();
        var variables = new VariableManager();
        variables.put(VariableManager.SELF, realized);
        var description = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        assertThat(provider.getReferenceValue(description, null, variables)).isEqualTo(List.of(realizer));
        assertThat(provider.getReferenceOptions(description, null, variables)).isEmpty();
        assertThat(provider.handleRemove(description, realized, realizer)).isInstanceOf(Failure.class);
        assertThat(provider.handleClear(description, realized)).isInstanceOf(Failure.class);
        assertThat(provider.getReferenceValue(description, null, variables)).isEqualTo(List.of(realizer));
        updateService.clearRealized(realizer);
        assertThat(this.service.hasRealizingWidget(SysmlFactory.eINSTANCE.createActionUsage())).isFalse();
        assertThat(this.service.hasRealizingWidget(null)).isFalse();
    }

    @Test
    public void getRealizingWidgetLabelValueShouldUseNameFromNextPerspective() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var component = new CommonCreationService().createComponent(this.capellaModel.getPhysicalArchitecturePerspective().getStructurePackage().getElement());

        assertThat(this.service.getRealizingWidgetLabelValue(operationalFunction)).isEqualTo("Realizing System Functions");
        assertThat(this.service.getRealizingWidgetLabelValue(systemFunction)).isEqualTo("Realizing Logical Functions");
        assertThat(this.service.getRealizingWidgetLabelValue(logicalFunction)).isEqualTo("Realizing Physical Functions");
        assertThat(this.service.getRealizingWidgetLabelValue(component)).isEqualTo("Realizing Configuration Items");
    }

    @Test
    public void getRealizingWidgetLabelValueShouldMatchFunctionPortDirection() {
        var function = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var input = creationService.createFunctionPort(function, FeatureDirectionKind.IN);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);

        assertThat(this.service.getRealizingWidgetLabelValue(input)).isEqualTo("Realizing Function Input Ports");
        assertThat(this.service.getRealizingWidgetLabelValue(output)).isEqualTo("Realizing Function Output Ports");
    }

}

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
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_ELEMENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_IS_REALIZED_BY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;

import java.util.List;

import org.eclipse.capella.application.configuration.details.view.referencewidget.IsRealizedByReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.services.ArcadiaTraceabilityLabelService;
import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceFactory;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.junit.jupiter.api.Test;

/**
 * Tests perspective-specific Arcadia labels.
 *
 * @author Jerome Gout
 */
public class ArcadiaTraceabilityLabelServiceTests extends AbstractSemanticTests {

    private final ArcadiaTraceabilityLabelService service = new ArcadiaTraceabilityLabelService();

    @Test
    public void hasIsRealizedByWidgetShouldFollowReferenceAvailability() {
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var updateService = new CommonUpdateService();

        assertThat(this.service.hasIsRealizedByWidget(realized)).isFalse();
        updateService.setFeatureReferenceValues(realized, ARCADIA_PREFIX + ARCADIA_ELEMENT, ARCADIA_IS_REALIZED_BY,
                List.of(realizer), SysmlPackage.eINSTANCE.getOccurrenceUsage());
        assertThat(this.service.hasIsRealizedByWidget(realized)).isTrue();
        var provider = new IsRealizedByReferenceWidgetProvider();
        var variables = new VariableManager();
        variables.put(VariableManager.SELF, realized);
        var description = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        assertThat(provider.getReferenceValue(description, null, variables)).isEqualTo(List.of(realizer));
        assertThat(provider.getReferenceOptions(description, null, variables)).isEmpty();
        assertThat(provider.handleItemRemoved(description, null, variables)).isInstanceOf(Failure.class);
        assertThat(provider.handleClearReference(description, null, variables)).isInstanceOf(Failure.class);
        assertThat(provider.getReferenceValue(description, null, variables)).isEqualTo(List.of(realizer));
        updateService.deleteReference(realized, ARCADIA_IS_REALIZED_BY);
        assertThat(this.service.hasIsRealizedByWidget(realized)).isFalse();
        assertThat(this.service.hasIsRealizedByWidget(SysmlFactory.eINSTANCE.createActionUsage())).isFalse();
        assertThat(this.service.hasIsRealizedByWidget(null)).isFalse();
    }

    @Test
    public void getRealizingLabelShouldUseNameFromNextPerspective() {
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var component = new CommonCreationService().createComponent(this.capellaModel.getPhysicalArchitecturePerspective().getStructurePackage().getElement());

        assertThat(this.service.getRealizingLabel(operationalFunction)).isEqualTo("Realizing System Functions");
        assertThat(this.service.getRealizingLabel(systemFunction)).isEqualTo("Realizing Logical Functions");
        assertThat(this.service.getRealizingLabel(logicalFunction)).isEqualTo("Realizing Physical Functions");
        assertThat(this.service.getRealizingLabel(component)).isEqualTo("Realizing Configuration Items");
    }

    @Test
    public void getRealizingLabelWhenNameIsUnavailableShouldUseGenericLabel() {
        var physicalFunction = this.capellaModel.getPhysicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        assertThat(this.service.getRealizingLabel(physicalFunction)).isEqualTo("Realizing");

        var component = new CommonCreationService().createComponent(this.capellaModel.getPhysicalArchitecturePerspective().getStructurePackage().getElement());
        this.capellaModel.getPhysicalArchitecturePerspective().getElement().setDeclaredName(ArcadiaEngineeringPerspective.EPBS.getLabel());

        assertThat(this.service.getRealizingLabel(component)).isEqualTo("Realizing");
        assertThat(this.service.getRealizingLabel(SysmlFactory.eINSTANCE.createActionUsage())).isEqualTo("Realizing");
        assertThat(this.service.getRealizingLabel(null)).isEqualTo("Realizing");
    }

    @Test
    public void getRealizingLabelShouldMatchFunctionPortDirection() {
        var function = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var input = creationService.createFunctionPort(function, FeatureDirectionKind.IN);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);

        assertThat(this.service.getRealizingLabel(input)).isEqualTo("Realizing Function Input Ports");
        assertThat(this.service.getRealizingLabel(output)).isEqualTo("Realizing Function Output Ports");
    }

}

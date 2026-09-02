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

import org.eclipse.capella.tests.fixtures.FunctionsPackage;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.ConnectionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Tests the modification of semantic elements.
 *
 * @author gdaniel
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ElementModificationTests extends AbstractSemanticTests {

    private final CommonCreationService commonCreationService = new CommonCreationService();

    private final CommonUpdateService commonUpdateService = new CommonUpdateService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final MetamodelMutationElementService metamodelMutationElementService = new MetamodelMutationElementService();

    @Test
    public void setFeatureDirectionShouldUpdatePortConnectedThroughConnectionUsage() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage sourceComponent = this.commonCreationService.createComponent(parent);
        PortUsage sourcePort = this.commonCreationService.createComponentPort(sourceComponent, FeatureDirectionKind.OUT);
        PartUsage targetComponent = this.commonCreationService.createComponent(parent);
        PortUsage targetPort = this.commonCreationService.createComponentPort(targetComponent, FeatureDirectionKind.IN);
        ConnectionUsage connection = this.metamodelMutationElementService.createConnectionUsage(sourcePort, targetPort, sourceComponent, targetComponent, parent);

        this.commonUpdateService.setFeatureDirection(sourcePort, FeatureDirectionKind.IN);

        assertThat(connection).isNotNull();
        assertThat(sourcePort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(targetPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
    }

    @Test
    public void setFeatureDirectionShouldUpdatePortsConnectedInCascade() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage firstComponent = this.commonCreationService.createComponent(parent);
        PortUsage firstPort = this.commonCreationService.createComponentPort(firstComponent, FeatureDirectionKind.OUT);
        PartUsage secondComponent = this.commonCreationService.createComponent(parent);
        PortUsage secondPort = this.commonCreationService.createComponentPort(secondComponent, FeatureDirectionKind.IN);
        PartUsage thirdComponent = this.commonCreationService.createComponent(parent);
        PortUsage thirdPort = this.commonCreationService.createComponentPort(thirdComponent, FeatureDirectionKind.OUT);
        this.commonCreationService.createComponentExchange(firstPort, secondPort);
        this.commonCreationService.createComponentExchange(secondPort, thirdPort);

        this.commonUpdateService.setFeatureDirection(firstPort, FeatureDirectionKind.IN);

        assertThat(firstPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(secondPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(thirdPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
    }

    @Test
    public void setFeatureDirectionShouldNotUpdatePortsInConnectionCycle() {
        Package parent = this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
        PartUsage firstComponent = this.commonCreationService.createComponent(parent);
        PortUsage firstPort = this.commonCreationService.createComponentPort(firstComponent, FeatureDirectionKind.OUT);
        PartUsage secondComponent = this.commonCreationService.createComponent(parent);
        PortUsage secondPort = this.commonCreationService.createComponentPort(secondComponent, FeatureDirectionKind.IN);
        PartUsage thirdComponent = this.commonCreationService.createComponent(parent);
        PortUsage thirdPort = this.commonCreationService.createComponentPort(thirdComponent, FeatureDirectionKind.OUT);
        PartUsage fourthComponent = this.commonCreationService.createComponent(parent);
        PortUsage fourthPort = this.commonCreationService.createComponentPort(fourthComponent, FeatureDirectionKind.IN);
        this.commonCreationService.createComponentExchange(firstPort, secondPort);
        this.commonCreationService.createComponentExchange(secondPort, thirdPort);
        this.commonCreationService.createComponentExchange(thirdPort, fourthPort);
        this.commonCreationService.createComponentExchange(fourthPort, firstPort);

        this.commonUpdateService.setFeatureDirection(firstPort, FeatureDirectionKind.IN);

        assertThat(firstPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(secondPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(thirdPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(fourthPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
    }

    @Test
    public void setFeatureDirectionOnFunctionalExchangeTargetShouldUpdateSourcePortDirectionAndFunctionalExchangeEnds() {
        FunctionsPackage functionsPackage = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage();
        ActionUsage rootFunction = functionsPackage.getRootFunction().getElement();
        ActionUsage sourceFunction = this.commonCreationService.createFunction(rootFunction);
        ItemUsage sourcePort = this.commonCreationService.createFunctionPort(sourceFunction, FeatureDirectionKind.OUT);
        ActionUsage targetFunction = this.commonCreationService.createFunction(rootFunction);
        ItemUsage targetPort = this.commonCreationService.createFunctionPort(targetFunction, FeatureDirectionKind.IN);
        FlowUsage functionalExchange = this.commonCreationService.createFunctionalExchange(sourcePort, targetPort);

        this.commonUpdateService.setFeatureDirection(targetPort, FeatureDirectionKind.OUT);

        assertThat(sourcePort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(targetPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(this.commonQueryService.getFunctionalExchangeSource(functionalExchange)).isEqualTo(targetPort);
        assertThat(this.commonQueryService.getFunctionalExchangeTarget(functionalExchange)).isEqualTo(sourcePort);
    }

    @Test
    public void setStatusKindShouldSetTheStatusKindOfTheElement() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.commonUpdateService.setStatusKind(rootFunction, "open");

        assertThat(this.commonQueryService.getStatusStringValue(rootFunction)).isEqualTo("open");
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.commonQueryService::isStatusInfo)
                .hasSize(1);
        assertThat(this.commonQueryService.getStatus(rootFunction))
                .extracting(Element::getDeclaredName)
                .isEqualTo("open");

    }

    @Test
    public void setStatusKindThenUnsetStatusShouldSetTheStatusThenUnsetIt() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.commonUpdateService.setStatusKind(rootFunction, "open");
        assertThat(this.commonQueryService.getStatusStringValue(rootFunction)).isEqualTo("open");

        this.commonUpdateService.unSetUsageStatusKind(rootFunction);
        assertThat(this.commonQueryService.getStatusStringValue(rootFunction)).isEmpty();
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.commonQueryService::isStatusInfo)
                .isEmpty();
        assertThat(this.commonQueryService.getStatus(rootFunction)).isNull();
    }

    @Test
    public void setStatusKindTwiceShouldSetTheLatestStatus() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.commonUpdateService.setStatusKind(rootFunction, "open");
        assertThat(this.commonQueryService.getStatusStringValue(rootFunction)).isEqualTo("open");
        this.commonUpdateService.setStatusKind(rootFunction, "tbd");

        assertThat(this.commonQueryService.getStatusStringValue(rootFunction)).isEqualTo("tbd");
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.commonQueryService::isStatusInfo)
                .hasSize(1);
        assertThat(this.commonQueryService.getStatus(rootFunction))
                .extracting(Element::getDeclaredName)
                .isEqualTo("tbd");
    }

    @Test
    public void setStatusKindWithInvalidStatusShouldNotSetTheStatus() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.commonUpdateService.setStatusKind(rootFunction, "test");
        assertThat(this.commonQueryService.getStatusStringValue(rootFunction)).isEmpty();
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.commonQueryService::isStatusInfo)
                .isEmpty();
        assertThat(this.commonQueryService.getStatus(rootFunction)).isNull();
    }
}

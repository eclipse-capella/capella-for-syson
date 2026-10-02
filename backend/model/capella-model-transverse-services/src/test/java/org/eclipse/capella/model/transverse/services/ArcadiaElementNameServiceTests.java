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

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;

/**
 * Tests perspective-specific Arcadia element names.
 *
 * @author Jerome Gout
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ArcadiaElementNameServiceTests extends AbstractSemanticTests {

    private final ArcadiaElementNameService service = new ArcadiaElementNameService();

    @Test
    public void getElementNameShouldFollowSemanticBrowserLabelTable() {
        var libraryServices = new ArcadiaLibraryServices();
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createActionUsage(), libraryServices::typeWithArcadiaFunction),
                "Operational Activity", "System Function", "Logical Function", "Physical Function", null);
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createActionUsage(), libraryServices::typeWithArcadiaFunctionalChain),
                "Operational Process", "Functional Chain", "Functional Chain", "Functional Chain", null);
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createOccurrenceUsage(), libraryServices::typeWithArcadiaCapability),
                "Operational Capability", "Capability", "Capability Realization", "Capability Realization", "Capability Realization");
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createFlowUsage(), libraryServices::typeWithArcadiaFunctionalExchange),
                "Interaction", "Functional Exchange", "Functional Exchange", "Functional Exchange", null);
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createItemUsage(), libraryServices::typeWithExchangeItem),
                null, "Function Port", "Function Port", "Function Port", null);
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createPartUsage(), libraryServices::typeWithArcadiaComponent),
                "Operational Entity", "System Component", "Logical Component", "Physical Component", "Configuration Item");
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createInterfaceUsage(), libraryServices::typeWithArcadiaComponentExchange),
                "Communication Mean", "Component Exchange", "Component Exchange", "Component Exchange", null);
        this.assertNames(this.createTypedElement(SysmlFactory.eINSTANCE.createPortUsage(), libraryServices::typeWithArcadiaComponentPort),
                null, "Component Port", "Component Port", "Component Port", null);
    }

    @Test
    public void getElementNameWhenGivenAnElementShouldUseItsArcadiaType() {
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.getElementName(systemFunction, ArcadiaEngineeringPerspective.SystemAnalysis)).contains("System Function");
    }

    @Test
    public void getElementNameWhenFunctionPortUsesSysmlTypeShouldReturnFunctionPortNames() {
        var sysml = SysmlFactory.eINSTANCE.createPackage();
        sysml.setDeclaredName("SysML");
        var exchangeItem = SysmlFactory.eINSTANCE.createItemDefinition();
        exchangeItem.setDeclaredName("ExchangeItem");
        new MetamodelMutationElementService().addChildInParent(sysml, exchangeItem);
        var port = SysmlFactory.eINSTANCE.createItemUsage();
        new UtilService().setFeatureTyping(port, exchangeItem);

        this.assertNames(port, null, "Function Port", "Function Port", "Function Port", null);
    }

    private Usage createTypedElement(Usage usage, Consumer<Usage> typeElement) {
        new MetamodelMutationElementService().addChildInParent(this.capellaModel.getElement(), usage);
        typeElement.accept(usage);
        return usage;
    }

    private void assertNames(Element element, String operational, String system, String logical, String physical, String epbs) {
        assertThat(this.service.getElementName(element, ArcadiaEngineeringPerspective.OperationalAnalysis)).isEqualTo(Optional.ofNullable(operational));
        assertThat(this.service.getElementName(element, ArcadiaEngineeringPerspective.SystemAnalysis)).isEqualTo(Optional.ofNullable(system));
        assertThat(this.service.getElementName(element, ArcadiaEngineeringPerspective.LogicalArchitecture)).isEqualTo(Optional.ofNullable(logical));
        assertThat(this.service.getElementName(element, ArcadiaEngineeringPerspective.PhysicalArchitecture)).isEqualTo(Optional.ofNullable(physical));
        assertThat(this.service.getElementName(element, ArcadiaEngineeringPerspective.EPBS)).isEqualTo(Optional.ofNullable(epbs));
    }
}

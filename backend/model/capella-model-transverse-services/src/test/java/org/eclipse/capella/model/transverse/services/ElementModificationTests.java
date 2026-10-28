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

import java.util.List;

import org.eclipse.capella.tests.fixtures.FunctionsPackage;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.ConnectionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.LiteralInfinity;
import org.eclipse.syson.sysml.LiteralInteger;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.MultiplicityRange;
import org.eclipse.syson.sysml.OperatorExpression;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    private final UtilService utilService = new UtilService();

    @Test
    public void setFeatureReferenceValuesShouldHandleSingleMultipleAndEmptyLibraryValues() {
        ActionUsage realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage firstRealized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage secondRealized = this.commonCreationService.createFunction(firstRealized);

        this.commonUpdateService.setFeatureReferenceValues(realizer, "Arcadia::ArcadiaElement", "realizes", List.of(firstRealized), SysmlPackage.eINSTANCE.getOccurrenceUsage());
        assertThat(this.commonQueryService.getFeatureReferenceExpression(realizer, "realizes")).containsInstanceOf(FeatureReferenceExpression.class);

        this.commonUpdateService.setFeatureReferenceValues(realizer, "Arcadia::ArcadiaElement", "realizes", List.of(firstRealized, secondRealized),
                SysmlPackage.eINSTANCE.getOccurrenceUsage());

        var expression = this.commonQueryService.getFeatureReferenceExpression(realizer, "realizes").orElseThrow();
        assertThat(expression).isInstanceOf(OperatorExpression.class);
        assertThat(((OperatorExpression) expression).getOperator()).isEqualTo(",");
        assertThat(this.commonQueryService.extractAllFeatures(expression)).containsExactly(firstRealized, secondRealized);

        this.commonUpdateService.setFeatureReferenceValues(realizer, "Arcadia::ArcadiaElement", "realizes", List.of(), SysmlPackage.eINSTANCE.getOccurrenceUsage());

        assertThat(this.commonQueryService.getFeatureReferenceExpression(realizer, "realizes")).isEmpty();
        assertThat(realizer.getNestedReference()).filteredOn(reference -> "realizes".equals(reference.getName()))
                .singleElement().satisfies(reference -> assertThat(reference.getOwnedRedefinition()).hasSize(1));
    }

    @Test
    public void setBooleanAttributeShouldPreserveLiteralValuesThroughLibraryRedefinition() {
        var component = this.commonCreationService.createComponent(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());

        this.commonUpdateService.setBooleanAttribute(component, "Arcadia::Component", "isActor", true);

        var expression = this.commonQueryService.getFeatureReferenceExpression(component, "isActor").orElseThrow();
        assertThat(expression).isInstanceOf(LiteralBoolean.class);
        assertThat(((LiteralBoolean) expression).isValue()).isTrue();

        this.commonUpdateService.setBooleanAttribute(component, "Arcadia::Component", "isActor", false);

        assertThat(this.commonQueryService.getFeatureReferenceExpression(component, "isActor")).containsSame(expression);
        assertThat(((LiteralBoolean) expression).isValue()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = FeatureDirectionKind.class, names = { "IN", "OUT" })
    public void setFunctionPortExchangeItemsShouldBindSingleAndMultipleItems(FeatureDirectionKind direction) {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ItemUsage port = this.commonCreationService.createFunctionPort(function, direction);
        ItemUsage firstItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        ItemUsage secondItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        var typing = port.getOwnedTyping().getFirst();
        String name = port.getDeclaredName();

        assertThat(this.commonUpdateService.setFunctionPortExchangeItems(port, firstItem)).isSameAs(port);

        assertThat(port.getOwnedMember()).filteredOn(FeatureReferenceExpression.class::isInstance).hasSize(1);
        assertThat(this.commonQueryService.extractAllFeatures(this.utilService.getValuation(port).getValue())).containsExactly(firstItem);

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(firstItem, secondItem));

        var value = this.utilService.getValuation(port).getValue();
        assertThat(value).isInstanceOf(OperatorExpression.class);
        assertThat(((OperatorExpression) value).getOperator()).isEqualTo(",");
        assertThat(this.commonQueryService.extractAllFeatures(value)).containsExactly(firstItem, secondItem);
        assertThat(port.getMultiplicity()).isInstanceOf(MultiplicityRange.class);
        assertThat(((MultiplicityRange) port.getMultiplicity()).getLowerBound()).isInstanceOf(LiteralInteger.class);
        assertThat(((LiteralInteger) ((MultiplicityRange) port.getMultiplicity()).getLowerBound()).getValue()).isZero();
        assertThat(((MultiplicityRange) port.getMultiplicity()).getUpperBound()).isInstanceOf(LiteralInfinity.class);
        assertThat(port.getDirection()).isEqualTo(direction);
        assertThat(port.getDeclaredName()).isEqualTo(name);
        assertThat(port.getOwnedTyping()).containsExactly(typing);
    }

    @Test
    public void setFunctionPortExchangeItemsWhenPortIsReferenceUsageShouldBindItems() {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var port = SysmlFactory.eINSTANCE.createReferenceUsage();
        port.setDirection(FeatureDirectionKind.OUT);
        this.metamodelMutationElementService.addChildInParent(function, port);
        new ArcadiaLibraryServices().typeWithExchangeItem(port);
        ItemUsage firstItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        ItemUsage secondItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(firstItem, secondItem));

        var value = this.utilService.getValuation(port).getValue();
        assertThat(this.commonQueryService.extractAllFeatures(value)).containsExactly(firstItem, secondItem);
        assertThat(port.getOwnedRedefinition()).isEmpty();
        assertThat(port.getNestedUsage()).isEmpty();
    }

    @Test
    public void setFunctionPortExchangeItemsShouldReplaceExistingItemsForMultipleSelections() {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ItemUsage port = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.IN);
        ItemUsage firstItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        ItemUsage secondItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        ItemUsage thirdItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        this.commonUpdateService.setFunctionPortExchangeItems(port, firstItem);

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(secondItem, thirdItem));

        var value = this.utilService.getValuation(port).getValue();
        assertThat(this.commonQueryService.extractAllFeatures(value)).containsExactly(secondItem, thirdItem);
    }

    @Test
    public void setFunctionPortExchangeItemsWhenOtherValuesExistShouldOnlyReplaceExchangeItemBinding() {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ItemUsage port = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.OUT);
        ItemUsage firstItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        ItemUsage secondItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        FeatureValue literalValue = SysmlFactory.eINSTANCE.createFeatureValue();
        var literal = SysmlFactory.eINSTANCE.createLiteralBoolean();
        literal.setValue(true);
        literalValue.getOwnedRelatedElement().add(literal);
        port.getOwnedRelationship().add(literalValue);
        FeatureValue otherReference = SysmlFactory.eINSTANCE.createFeatureValue();
        var expression = SysmlFactory.eINSTANCE.createFeatureReferenceExpression();
        var membership = SysmlFactory.eINSTANCE.createMembership();
        membership.setMemberElement(function);
        expression.getOwnedRelationship().add(membership);
        otherReference.getOwnedRelatedElement().add(expression);
        port.getOwnedRelationship().add(otherReference);

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(firstItem, secondItem));
        FeatureValue oldBinding = port.getOwnedRelationship().stream()
                .filter(FeatureValue.class::isInstance).map(FeatureValue.class::cast)
                .filter(value -> value != literalValue && value != otherReference).findFirst().orElseThrow();

        this.commonUpdateService.setFunctionPortExchangeItems(port, secondItem);

        assertThat(port.getOwnedRelationship()).contains(literalValue, otherReference).doesNotContain(oldBinding);
        assertThat(literalValue.getValue()).isSameAs(literal);
        assertThat(literal.isValue()).isTrue();
        assertThat(otherReference.getValue()).isSameAs(expression);
        assertThat(expression.getReferent()).isSameAs(function);
        assertThat(port.getOwnedRelationship()).filteredOn(FeatureValue.class::isInstance).hasSize(3);
        assertThat(port.getOwnedRelationship().stream()
                .filter(FeatureValue.class::isInstance).map(FeatureValue.class::cast)
                .filter(value -> value != literalValue && value != otherReference)
                .flatMap(value -> this.commonQueryService.extractAllFeatures(value.getValue()).stream()).toList()).containsExactly(secondItem, firstItem);
    }

    @Test
    public void setFunctionPortExchangeItemsWhenItemIsInNestedInterfacesPackageShouldBindItem() {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ItemUsage port = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.IN);
        Package nestedPackage = SysmlFactory.eINSTANCE.createPackage();
        this.metamodelMutationElementService.addChildInParent(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement(), nestedPackage);
        ItemUsage exchangeItem = this.commonCreationService.createNewExchangeItem(nestedPackage);

        this.commonUpdateService.setFunctionPortExchangeItems(port, exchangeItem);

        assertThat(this.commonQueryService.extractAllFeatures(this.utilService.getValuation(port).getValue())).containsExactly(exchangeItem);
    }

    @Test
    public void setFunctionPortExchangeItemsWhenItemsAreFromAnotherPerspectiveShouldPreserveBindings() {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ItemUsage port = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.IN);
        ItemUsage localItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        Package otherInterfaces = this.capellaModel.getSystemAnalysisPerspective().getInterfacesPackage().getElement();
        ItemUsage firstForeignItem = this.commonCreationService.createNewExchangeItem(otherInterfaces);
        ItemUsage secondForeignItem = this.commonCreationService.createNewExchangeItem(otherInterfaces);
        this.commonUpdateService.setFunctionPortExchangeItems(port, localItem);
        var valuation = this.utilService.getValuation(port);

        this.commonUpdateService.setFunctionPortExchangeItems(port, firstForeignItem);
        assertThat(this.utilService.getValuation(port)).isSameAs(valuation);

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(firstForeignItem));
        assertThat(this.utilService.getValuation(port)).isSameAs(valuation);

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(firstForeignItem, secondForeignItem));
        assertThat(this.utilService.getValuation(port)).isSameAs(valuation);

        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of(localItem, firstForeignItem));
        assertThat(this.utilService.getValuation(port)).isSameAs(valuation);
        assertThat(this.commonQueryService.extractAllFeatures(valuation.getValue())).containsExactly(localItem);
    }

    @Test
    public void setFunctionPortExchangeItemsShouldIgnoreInvalidTargetsAndSources() {
        ActionUsage function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ItemUsage port = this.commonCreationService.createFunctionPort(function, FeatureDirectionKind.IN);
        ItemUsage exchangeItem = this.commonCreationService.createNewExchangeItem(this.capellaModel.getLogicalArchitecturePerspective().getInterfacesPackage().getElement());
        this.commonUpdateService.setFunctionPortExchangeItems(port, exchangeItem);
        var valuation = this.utilService.getValuation(port);

        this.commonUpdateService.setFunctionPortExchangeItems(port, SysmlFactory.eINSTANCE.createItemUsage());
        this.commonUpdateService.setFunctionPortExchangeItems(port, this.commonCreationService.createNewExchangeItem(function.getOwner()));
        this.commonUpdateService.setFunctionPortExchangeItems(port, this.commonCreationService.createNewExchangeItem(
                this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement()));
        this.commonUpdateService.setFunctionPortExchangeItems(port, port);
        this.commonUpdateService.setFunctionPortExchangeItems(port, null);
        this.commonUpdateService.setFunctionPortExchangeItems(port, List.of());
        this.commonUpdateService.setFunctionPortExchangeItems(exchangeItem, exchangeItem);

        assertThat(this.utilService.getValuation(port)).isSameAs(valuation);
        assertThat(this.utilService.getValuation(exchangeItem)).isNull();
    }

    @Test
    public void setStatusKindWhenTypeIndexInstalledShouldReplaceAndRemoveStatus() {
        var function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var resourceSet = function.eResource().getResourceSet();
        var adapter = new ArcadiaElementContentAdapter(resourceSet);
        resourceSet.eAdapters().add(adapter);
        try {
            this.commonUpdateService.setStatusKind(function, "open");
            assertThat(this.commonQueryService.getStatusStringValue(function)).isEqualTo("open");
            this.commonUpdateService.setStatusKind(function, "tbd");
            assertThat(this.commonQueryService.getStatusStringValue(function)).isEqualTo("tbd");
            this.commonUpdateService.unSetUsageStatusKind(function);
            assertThat(this.commonQueryService.getStatus(function)).isNull();
        } finally {
            resourceSet.eAdapters().remove(adapter);
        }
    }

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

    @Test
    public void setRealizesShouldUpdateRealizesAndIsRealizedBy() {
        ActionUsage realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        this.commonUpdateService.setRealizes(realizer, realized);

        assertThat(this.commonQueryService.getRealizes(realizer)).containsExactly(realized);
        assertThat(this.commonQueryService.getIsRealizedBy(realized)).containsExactly(realizer);
    }

    @Test
    public void setRealizesWhenSelectionsChangeShouldMergeSingleValuesAndReplaceMultipleSelections() {
        ActionUsage realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage firstRealized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage secondRealized = this.commonCreationService.createFunction(firstRealized);
        ActionUsage thirdRealized = this.commonCreationService.createFunction(firstRealized);
        this.commonUpdateService.setRealizes(realizer, firstRealized);

        this.commonUpdateService.setRealizes(realizer, secondRealized);
        this.commonUpdateService.setRealizes(realizer, List.of(secondRealized));

        assertThat(this.commonQueryService.getRealizes(realizer)).containsExactly(secondRealized, firstRealized);

        this.commonUpdateService.setRealizes(realizer, List.of(firstRealized, thirdRealized));

        assertThat(this.commonQueryService.getRealizes(realizer)).containsExactly(firstRealized, thirdRealized);
        assertThat(this.commonQueryService.getIsRealizedBy(secondRealized)).isEmpty();
        assertThat(this.commonQueryService.getIsRealizedBy(thirdRealized)).containsExactly(realizer);

        this.commonUpdateService.setRealizes(realizer, null);

        assertThat(this.commonQueryService.getRealizes(realizer)).containsExactly(firstRealized, thirdRealized);
        assertThat(this.commonQueryService.getIsRealizedBy(firstRealized)).containsExactly(realizer);
        assertThat(this.commonQueryService.getIsRealizedBy(secondRealized)).isEmpty();
        assertThat(this.commonQueryService.getIsRealizedBy(thirdRealized)).containsExactly(realizer);

        this.commonUpdateService.setRealizes(realizer, firstRealized);
        this.commonUpdateService.setRealizes(realizer, List.of());

        assertThat(this.commonQueryService.getRealizes(realizer)).containsExactly(firstRealized, thirdRealized);
        assertThat(this.commonQueryService.getIsRealizedBy(firstRealized)).containsExactly(realizer);
    }

    @Test
    public void removeRealizesShouldPreserveOtherReferences() {
        ActionUsage realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage firstRealized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage secondRealized = this.commonCreationService.createFunction(firstRealized);
        this.commonUpdateService.setRealizes(realizer, List.of(firstRealized, secondRealized));

        this.commonUpdateService.removeRealizes(realizer, firstRealized);

        assertThat(this.commonQueryService.getRealizes(realizer)).containsExactly(secondRealized);
        assertThat(this.commonQueryService.getIsRealizedBy(firstRealized)).isEmpty();
        assertThat(this.commonQueryService.getIsRealizedBy(secondRealized)).containsExactly(realizer);
    }

    @Test
    public void removeRealizesShouldPreserveOtherIsRealizedBy() {
        ActionUsage firstRealizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage secondRealizer = this.commonCreationService.createFunction(firstRealizer);
        ActionUsage realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        this.commonUpdateService.setRealizes(firstRealizer, realized);
        this.commonUpdateService.setRealizes(secondRealizer, realized);

        this.commonUpdateService.removeRealizes(firstRealizer, realized);

        assertThat(this.commonQueryService.getRealizes(firstRealizer)).isEmpty();
        assertThat(this.commonQueryService.getRealizes(secondRealizer)).containsExactly(realized);
        assertThat(this.commonQueryService.getIsRealizedBy(realized)).containsExactly(secondRealizer);
    }

    @Test
    public void clearRealizesShouldRemoveRealizesAndIsRealizedBy() {
        ActionUsage realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage firstRealized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        ActionUsage secondRealized = this.commonCreationService.createFunction(firstRealized);
        this.commonUpdateService.setRealizes(realizer, List.of(firstRealized, secondRealized));

        this.commonUpdateService.clearRealizes(realizer);

        assertThat(this.commonQueryService.getRealizes(realizer)).isEmpty();
        assertThat(this.commonQueryService.getIsRealizedBy(firstRealized)).isEmpty();
        assertThat(this.commonQueryService.getIsRealizedBy(secondRealized)).isEmpty();
    }
}

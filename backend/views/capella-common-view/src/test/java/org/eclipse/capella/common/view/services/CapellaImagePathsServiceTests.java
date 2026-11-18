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
package org.eclipse.capella.common.view.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.model.transverse.services.ArcadiaLibraryServices;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.services.ComposedLabelService;
import org.eclipse.sirius.components.emf.services.ComposedEMFLabelService;
import org.eclipse.sirius.components.emf.services.DefaultEMFLabelService;
import org.eclipse.sirius.components.emf.services.DefaultLabelFeatureProvider;
import org.eclipse.sirius.web.application.object.services.DefaultLabelService;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.eclipse.syson.sysml.provider.SysmlItemProviderAdapterFactory;
import org.junit.jupiter.api.Test;

/**
 * Tests Arcadia icon dispatch and fallback labels.
 *
 * @author tbezierslafosse
 */
public class CapellaImagePathsServiceTests extends AbstractSemanticTests {

    private final ILabelService labelService = new ComposedLabelService(List.of(), new DefaultLabelService(List.of(),
            new ComposedEMFLabelService(new DefaultEMFLabelService(List.of(SysmlItemProviderAdapterFactory::new), new DefaultLabelFeatureProvider()), List.of())));

    private final CapellaImagePathsService service = new CapellaImagePathsService(this.labelService);

    @Test
    public void getImagePathsShouldReturnIconsForArcadiaElements() {
        var model = this.capellaModel.getSystemAnalysisPerspective();
        var creationService = new CommonCreationService();
        var component = creationService.createComponent(model.getStructurePackage().getElement());
        var targetComponent = creationService.createComponent(model.getStructurePackage().getElement());
        var componentPort = creationService.createComponentPort(component, FeatureDirectionKind.OUT);
        var targetComponentPort = creationService.createComponentPort(targetComponent, FeatureDirectionKind.IN);
        var actor = creationService.createActor(model.getStructurePackage().getElement());
        var humanActor = creationService.createActor(model.getStructurePackage().getElement());
        new CommonUpdateService().setBooleanAttribute(humanActor, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_COMPONENT,
                CommonQueryService.ARCADIA_IS_HUMAN, true);
        var function = model.getFunctionsPackage().getRootFunction().getElement();
        var targetFunction = creationService.createFunction(function);
        var output = creationService.createFunctionPort(function, FeatureDirectionKind.OUT);
        var input = creationService.createFunctionPort(targetFunction, FeatureDirectionKind.IN);
        var functionalExchange = creationService.createFunctionalExchange(output, input);
        var requirement = creationService.createRequirement(model.getRequirementsPackage().getElement());
        new ArcadiaLibraryServices().typeWithLibrary(requirement, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_REQUIREMENT,
                SysmlPackage.eINSTANCE.getRequirementDefinition());

        var expectedIcons = Map.ofEntries(
                Map.entry(component, "SystemComponent"),
                Map.entry(actor, "SystemActor"),
                Map.entry(humanActor, "ActorHuman"),
                Map.entry(function, "SystemFunction"),
                Map.entry(componentPort, "FlowPort"),
                Map.entry(creationService.createComponentExchange(componentPort, targetComponentPort), "ComponentExchange"),
                Map.entry(functionalExchange, "FunctionalExchange"),
                Map.entry(creationService.createFunctionalChain(function, List.of(functionalExchange)), "FunctionalChain"),
                Map.entry(creationService.createNewExchangeItem(model.getInterfacesPackage().getElement()), "ExchangeItem"),
                Map.entry(input, "FunctionInputPort"),
                Map.entry(output, "FunctionOutputPort"),
                Map.entry(creationService.createCapability(model.getCapabilitiesPackage().getElement()), "Capability"),
                Map.entry(creationService.createMission(model.getCapabilitiesPackage().getElement()), "Mission"),
                Map.entry(requirement, "Requirement"),
                Map.entry(creationService.createDescribes(requirement, function), "Describes"));
        assertThat(expectedIcons).allSatisfy((element, icon) ->
                assertThat(this.service.getImagePaths(element)).as(element.getDeclaredName()).containsExactly("/icons/full/obj16/" + icon + ".svg"));
    }

    @Test
    public void getImagePathsShouldReturnPackageAndNativeElementIcons() {
        var model = this.capellaModel.getSystemAnalysisPerspective();
        var mutationService = new MetamodelMutationElementService();
        var nativeRequirement = new CommonCreationService().createRequirement(model.getRequirementsPackage().getElement());
        var nativeAction = SysmlFactory.eINSTANCE.createActionUsage();
        mutationService.addChildInParent(model.getFunctionsPackage().getElement(), nativeAction);
        var nestedPackage = SysmlFactory.eINSTANCE.createPackage();
        nestedPackage.setDeclaredName("Nested");
        mutationService.addChildInParent(model.getStructurePackage().getElement(), nestedPackage);

        var functionPackage = new CommonCreationService().createPackage(model.getFunctionsPackage().getRootFunction().getElement());
        functionPackage.setDeclaredName("Nested functions");

        var expectedIcons = Map.ofEntries(
                Map.entry(functionPackage, "FunctionPkg"),
                Map.entry(model.getStructurePackage().getElement(), "ComponentPkg"),
                Map.entry(model.getFunctionsPackage().getElement(), "FunctionPkg"),
                Map.entry(model.getCapabilitiesPackage().getElement(), "CapabilitiesPkg"),
                Map.entry(model.getInterfacesPackage().getElement(), "InterfacesPkg"),
                Map.entry(model.getDataPackage().getElement(), "DataPkg"),
                Map.entry(model.getRequirementsPackage().getElement(), "RequirementsPkg"),
                Map.entry(nativeRequirement, "Requirement"),
                Map.entry(nativeAction, "ActionUsage"),
                Map.entry(nestedPackage, "ComponentPkg"));
        assertThat(expectedIcons).allSatisfy((element, icon) ->
                assertThat(this.service.getImagePaths(element)).as(element.getDeclaredName()).containsExactly("/icons/full/obj16/" + icon + ".svg"));
    }
}

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
package org.eclipse.capella.model.services.logical.architecture;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.diagram.common.view.services.ViewCreateService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

import java.util.Optional;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;


/**
 * Logical Architecture (LA) related mutation service. This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class LARepresentationMutationService {

    private static final String WHITE_SPACE = " ";

    private final TransverseMutationService transverseMutationService;

    private final LAMutationService laMutationService;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final LAQueryService laQueryService;

    private final ViewCreateService viewCreateService;

    private final LibraryServices libraryServices;

    private final MetamodelMutationElementService metamodelMutationElementService;

    public LARepresentationMutationService(IFeedbackMessageService feedbackMessageService,
        IReadOnlyObjectPredicate readOnlyService, IObjectSearchService objectSearchService, ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService) {
        this.transverseMutationService = new TransverseMutationService();
        this.transverseQueryService = new TransverseQueryService();
        this.laMutationService = new LAMutationService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.laQueryService = new LAQueryService();
        this.viewCreateService = new ViewCreateService(objectSearchService, readOnlyService, showDiagramsInheritedMembersService, feedbackMessageService);
        this.libraryServices = new LibraryServices();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
    }

    public FlowUsage createFunctionalExchange(Feature source, Feature target) {
        Feature sourcePort = this.getOrCreateFunctionPort(source, FeatureDirectionKind.OUT);
        Feature targetPort = this.getOrCreateFunctionPort(target, FeatureDirectionKind.IN);

        if (sourcePort != null && targetPort != null) {
            FlowUsage functionalExchange = this.viewCreateService.createFlowUsage(sourcePort, targetPort);
            this.elementInitializerSwitch.doSwitch(functionalExchange);
            this.libraryServices.typeWithArcadiaFunctionalExchange(functionalExchange);
            long existingElementsCount = this.transverseQueryService.existingElementsCount(functionalExchange);
            functionalExchange.setDeclaredName(ARCADIA_FUNCTIONAL_EXCHANGE + WHITE_SPACE + existingElementsCount);
            return functionalExchange;

        }
        return null;
    }

    private Feature getOrCreateFunctionPort(Feature feature, FeatureDirectionKind direction) {
        if (this.laQueryService.isFunction(feature)) {
            return this.createFunctionPort((ActionUsage) feature, direction);
        }
        return feature;
    }

    public PartUsage createComponent(Element parent, boolean isActor) {
        String name = "C";
        Element targetContainer = parent;
        if (!(parent instanceof PartUsage)) {
            targetContainer = this.laQueryService.toComponentsPackage(parent);
        }
        PartUsage partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
        if (isActor) {
            name = "A";
            this.transverseMutationService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_ACTOR, true);
        }
        this.elementInitializerSwitch.doSwitch(partUsage);
        this.libraryServices.typeWithArcadiaComponent(partUsage);
        long existingElementsCount = this.transverseQueryService.existingElementsCount(partUsage);
        partUsage.setDeclaredName(name + WHITE_SPACE + existingElementsCount);
        return partUsage;
    }

    public RequirementUsage createRequirement(Element parent) {
        String name = "Requirement";

        Element targetContainer = this.laQueryService.toRequirementsPackage(parent);
        RequirementUsage requirementUsage = SysmlFactory.eINSTANCE.createRequirementUsage();

        this.metamodelMutationElementService.addChildInParent(targetContainer, requirementUsage);
        this.elementInitializerSwitch.doSwitch(requirementUsage);
        this.libraryServices.typeWithArcadiaRequirement(requirementUsage);

        long existingElementsCount = this.transverseQueryService.existingElementsCount(requirementUsage);
        requirementUsage.setDeclaredName(name + WHITE_SPACE + existingElementsCount);
        return requirementUsage;
    }

    public Element createNewFunctionInComponent(PartUsage container) {
        Package functionsPackage = this.laQueryService.toFunctionsPackage(container);
        ActionUsage actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
        this.metamodelMutationElementService.addChildInParent(functionsPackage, actionUsage);
        this.libraryServices.typeWithArcadiaFunction(actionUsage);
        this.laMutationService.setPerformedActionUsage(container, actionUsage);
        this.elementInitializerSwitch.doSwitch(actionUsage);
        this.libraryServices.typeWithArcadiaFunction(actionUsage);
        this.setNewFunctionName(actionUsage);
        return actionUsage;
    }

    public Element createNewFunctionInFunction(ActionUsage parent) {
        ActionUsage actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
        this.metamodelMutationElementService.addChildInParent(parent, actionUsage);
        this.libraryServices.typeWithArcadiaFunction(actionUsage);
        this.elementInitializerSwitch.doSwitch(actionUsage);
        this.libraryServices.typeWithArcadiaFunction(actionUsage);
        this.setNewFunctionName(actionUsage);
        return actionUsage;
    }

    public void createNewSameLevelFunction(ActionUsage selectedFunction, IEditingContext editingContext) {
        Optional<ActionUsage> optParentFunction = this.laQueryService.getParentFunction(selectedFunction);
        optParentFunction.ifPresent(this::createNewFunctionInFunction);

        ActionUsage actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
        Package functionsPackage = this.laQueryService.toFunctionsPackage(selectedFunction);
        this.metamodelMutationElementService.addChildInParent(functionsPackage, actionUsage);
        this.libraryServices.typeWithArcadiaFunction(actionUsage);
        this.elementInitializerSwitch.doSwitch(actionUsage);
        this.libraryServices.typeWithArcadiaFunction(actionUsage);
        this.setNewFunctionName(actionUsage);
    }

    private void setNewFunctionName(ActionUsage actionUsage) {
        actionUsage.setDeclaredName(ARCADIA_FUNCTION + WHITE_SPACE + this.transverseQueryService.existingElementsCount(actionUsage));
    }

    public Feature createFunctionPort(ActionUsage container, FeatureDirectionKind direction) {

        ItemUsage itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        itemUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, itemUsage);
        this.elementInitializerSwitch.doSwitch(itemUsage);
        this.libraryServices.typeWithExchangeItem(itemUsage);
        String defaultName = switch (direction) {
            case IN -> "FIP";
            case OUT -> "FOP";
            default -> "FP";
        };
        itemUsage.setDeclaredName(defaultName + WHITE_SPACE + this.transverseQueryService.existingElementsCount(itemUsage));

        return itemUsage;

    }

    public ActionUsage createNewFunctionalChain(Element container, Object selectedObjects) {
        Package functionsPackage = this.laQueryService.toFunctionsPackage(container);
        ActionUsage actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
        this.metamodelMutationElementService.addChildInParent(functionsPackage, actionUsage);
        this.libraryServices.typeWithArcadiaFunctionalChain(actionUsage);
        this.elementInitializerSwitch.doSwitch(actionUsage);
        actionUsage.setDeclaredName(ARCADIA_FUNCTIONAL_CHAIN + WHITE_SPACE + this.transverseQueryService.existingElementsCount(actionUsage));
        this.laMutationService.setArcadiaReferenceFeature(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES, selectedObjects, SysmlPackage.eINSTANCE.getFlowUsage().getName());
        return actionUsage;
    }
}

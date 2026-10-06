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

import static org.eclipse.capella.model.transverse.services.CommonNamingService.ACTOR_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMPONENT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMPONENT_EXCHANGE_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.FUNCTIONAL_CHAIN_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.FUNCTIONAL_EXCHANGE_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.FUNCTION_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.REQUIREMENT_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_INVOLVED_COMPONENTS;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;

import java.util.List;
import java.util.Optional;

import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.Flow;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.OccurrenceUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PayloadFeature;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common semantic element creation service.
 *
 * @author gdaniel
 */
public class CommonCreationService {

    private final CommonQueryService commonQueryService;

    private final CommonUpdateService commonUpdateService;

    private final ArcadiaLibraryServices arcadiaLibraryServices;

    private final MetamodelMutationElementService metamodelMutationElementService;

    private final CommonNamingService commonNamingService;

    private final Logger logger = LoggerFactory.getLogger(CommonCreationService.class);

    public CommonCreationService() {
        this.commonQueryService = new CommonQueryService();
        this.commonUpdateService = new CommonUpdateService();
        this.arcadiaLibraryServices = new ArcadiaLibraryServices();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.commonNamingService = new CommonNamingService();
    }

    public Feature createCapabilityGeneralization(Usage sourceCapability, Usage targetCapability) {
        if (this.commonQueryService.getGeneralizationReferenceValue(sourceCapability).contains(targetCapability)) {
            return sourceCapability;
        }
        var generalization = SysmlFactory.eINSTANCE.createSubsetting();
        sourceCapability.getOwnedRelationship().add(generalization);
        generalization.setSubsettingFeature(sourceCapability);
        generalization.setSubsettedFeature(targetCapability);
        this.metamodelMutationElementService.initialize(generalization);
        return sourceCapability;
    }

    public Usage createCapabilityInvolvement(Usage capability, PartUsage component) {
        if (this.commonQueryService.getFeatureReferenceValue(capability, ARCADIA_INVOLVED_COMPONENTS).contains(component)) {
            return capability;
        }
        return this.commonUpdateService.setArcadiaReferenceFeature(capability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, component, SysmlPackage.eINSTANCE.getPartUsage().getName());
    }

    public ItemUsage createNewExchangeItem(Element parent) {
        ItemUsage itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        this.metamodelMutationElementService.addChildInParent(parent, itemUsage);
        this.arcadiaLibraryServices.typeWithExchangeItem(itemUsage);
        this.metamodelMutationElementService.initialize(itemUsage);
        this.commonNamingService.setElementDefaultDeclaredName(itemUsage, ARCADIA_EXCHANGE_ITEM);

        return itemUsage;
    }

    public RequirementUsage createRequirement(Element parent) {
        RequirementUsage requirementUsage = null;
        Optional<Package> optionalRequirementsPackage = this.commonQueryService.getRequirementsPackage(parent);
        if (optionalRequirementsPackage.isPresent()) {
            requirementUsage = SysmlFactory.eINSTANCE.createRequirementUsage();

            this.metamodelMutationElementService.addChildInParent(optionalRequirementsPackage.get(), requirementUsage);
            this.metamodelMutationElementService.initialize(requirementUsage);
            // Use native SysML v2 RequirementUsage without Arcadia typing

            this.commonNamingService.setElementDefaultDeclaredName(requirementUsage, REQUIREMENT_DEFAULT_DECLAREDNAME_PREFIX);
        }
        return requirementUsage;
    }

    public PartUsage createComponent(Element parent) {
        PartUsage partUsage = null;
        Optional<Element> optionalTargetContainer = Optional.of(parent);
        if (!this.commonQueryService.isComponent(parent)) {
            optionalTargetContainer = this.commonQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (optionalTargetContainer.isPresent()) {
            Element targetContainer = optionalTargetContainer.get();
            partUsage = SysmlFactory.eINSTANCE.createPartUsage();
            this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
            this.metamodelMutationElementService.initialize(partUsage);
            this.arcadiaLibraryServices.typeWithArcadiaComponent(partUsage);

            this.commonNamingService.setElementDefaultDeclaredName(partUsage, COMPONENT_DEFAULT_DECLAREDNAME_PREFIX);
        }
        return partUsage;
    }

    private PortUsage getOrCreateComponentPort(Feature feature, FeatureDirectionKind direction) {
        PortUsage result = null;
        if (this.commonQueryService.isComponent(feature)) {
            result = this.createComponentPort((PartUsage) feature, direction);
        } else if (this.commonQueryService.isComponentPort(feature)) {
            result = (PortUsage) feature;
        }
        return result;
    }

    public PortUsage createComponentPort(PartUsage container, FeatureDirectionKind direction) {
        container.unsetDirection();
        PortUsage portUsage = SysmlFactory.eINSTANCE.createPortUsage();
        portUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, portUsage);
        this.metamodelMutationElementService.initialize(portUsage);
        this.arcadiaLibraryServices.typeWithArcadiaComponentPort(portUsage);

        this.commonNamingService.setElementDefaultDeclaredName(portUsage, COMPONENT_PORT_DEFAULT_DECLAREDNAME_PREFIX);
        return portUsage;
    }

    public PartUsage createActor(Element parent) {
        PartUsage partUsage = null;
        Optional<Element> optionalTargetContainer = Optional.of(parent);
        if (!this.commonQueryService.isComponent(parent)) {
            optionalTargetContainer = this.commonQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (optionalTargetContainer.isPresent()) {
            Element targetContainer = optionalTargetContainer.get();
            partUsage = SysmlFactory.eINSTANCE.createPartUsage();
            this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
            this.commonUpdateService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_ACTOR, true);
            this.metamodelMutationElementService.initialize(partUsage);
            this.arcadiaLibraryServices.typeWithArcadiaComponent(partUsage);

            this.commonNamingService.setElementDefaultDeclaredName(partUsage, ACTOR_DEFAULT_DECLAREDNAME_PREFIX);
        }
        return partUsage;

    }

    public ActionUsage createFunction(Element parent) {
        ActionUsage actionUsage = null;
        Optional<Element> optionalParent = Optional.ofNullable(parent)
                .filter(this.commonQueryService::isFunction)
                .or(() -> this.commonQueryService.getFunctionsPackage(parent)
                        .flatMap(this.commonQueryService::getRootFunction));
        if (optionalParent.isPresent()) {
            actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
            this.metamodelMutationElementService.addChildInParent(optionalParent.get(), actionUsage);
            this.arcadiaLibraryServices.typeWithArcadiaFunction(actionUsage);
            this.metamodelMutationElementService.initialize(actionUsage);

            this.commonNamingService.setElementDefaultDeclaredName(actionUsage, FUNCTION_DEFAULT_DECLAREDNAME_PREFIX);

            Optional<PartUsage> optionalAllocatingComponent = this.findAllocatingComponent(parent);
            if (optionalAllocatingComponent.isPresent()) {
                this.commonUpdateService.setPerformAction(optionalAllocatingComponent.get(), actionUsage);
            } else {
                this.logger.atWarn()
                        .setMessage("Cannot find allocating component for function {}")
                        .addArgument(actionUsage.getElementId())
                        .addKeyValue("actionUsageId", actionUsage.getElementId())
                        .addKeyValue("parentId", optionalParent.get().getElementId())
                        .log();
            }
        }
        return actionUsage;
    }

    public ItemUsage getOrCreateFunctionPort(Feature feature, FeatureDirectionKind direction) {
        ItemUsage result = null;
        if (this.commonQueryService.isFunction(feature)) {
            result = this.createFunctionPort((ActionUsage) feature, direction);
        } else if (this.commonQueryService.isFunctionPort(feature)) {
            result = (ItemUsage) feature;
        }
        return result;
    }

    public ItemUsage createFunctionPort(ActionUsage container, FeatureDirectionKind direction) {
        ItemUsage itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        itemUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, itemUsage);
        this.metamodelMutationElementService.initialize(itemUsage);
        this.arcadiaLibraryServices.typeWithExchangeItem(itemUsage);
        String defaultName = this.commonNamingService.getFunctionPortDefaultDeclaredNamePrefix(direction);
        this.commonNamingService.setElementDefaultDeclaredName(itemUsage, defaultName);

        return itemUsage;
    }

    public FlowUsage createFunctionalExchange(Feature source, Feature target) {
        Optional<Package> optionalSourceFunctionsPackage = this.commonQueryService.getFunctionsPackage(source);
        Optional<Package> optionalTargetFunctionsPackage = this.commonQueryService.getFunctionsPackage(target);

        if (optionalSourceFunctionsPackage.isPresent() && optionalSourceFunctionsPackage.equals(optionalTargetFunctionsPackage)) {

            if (this.commonQueryService.canCreateFunctionalExchange(source, target)) {

                Feature sourcePort = this.getOrCreateFunctionPort(source, FeatureDirectionKind.OUT);
                Feature targetPort = this.getOrCreateFunctionPort(target, FeatureDirectionKind.IN);

                Optional<Namespace> optionalFunctionalExchangeParent = this.commonQueryService.findClosestCommonAncestor(source, target,
                        e -> this.commonQueryService.isFunction(e) || this.commonQueryService.isFunctionsPackage(e));
                if (optionalFunctionalExchangeParent.isPresent()) {

                    // We can't use diagramMutationElementService#createFlowUsage here because the way SysON computes FlowUsage container doesn't work with Capella for SysON.
                    FlowUsage functionalExchange = this.metamodelMutationElementService.createFlowUsage(sourcePort, targetPort, source, target, optionalFunctionalExchangeParent.get());

                    this.metamodelMutationElementService.initialize(functionalExchange);
                    this.arcadiaLibraryServices.typeWithArcadiaFunctionalExchange(functionalExchange);

                    this.commonNamingService.setElementDefaultDeclaredName(functionalExchange, FUNCTIONAL_EXCHANGE_DEFAULT_DECLAREDNAME_PREFIX);
                    return functionalExchange;
                }

            }
        }
        return null;
    }

    public InterfaceUsage createComponentExchange(Feature source, Feature target) {
        Optional<Package> optionalSourceStructurePackage = this.commonQueryService.getStructurePackage(source);
        Optional<Package> optionalTargetStructurePackage = this.commonQueryService.getStructurePackage(target);

        if (optionalSourceStructurePackage.isPresent() && optionalSourceStructurePackage.equals(optionalTargetStructurePackage)) {

            if (this.commonQueryService.canCreateComponentExchange(source, target)) {

                PortUsage sourcePort = this.getOrCreateComponentPort(source, FeatureDirectionKind.OUT);
                PortUsage targetPort = this.getOrCreateComponentPort(target, FeatureDirectionKind.IN);

                Optional<Namespace> optionalComponentExchangeParent = this.commonQueryService.findClosestCommonAncestor(source, target,
                        e -> this.commonQueryService.isComponent(e) || this.commonQueryService.isStructurePackage(e));
                if (optionalComponentExchangeParent.isPresent()) {
                    InterfaceUsage componentExchange = this.metamodelMutationElementService.createInterfaceUsage(sourcePort, targetPort, source, target, optionalComponentExchangeParent.get());
                    this.metamodelMutationElementService.initialize(componentExchange);
                    this.arcadiaLibraryServices.typeWithArcadiaComponentExchange(componentExchange);

                    this.commonNamingService.setElementDefaultDeclaredName(componentExchange, COMPONENT_EXCHANGE_DEFAULT_DECLAREDNAME_PREFIX);
                    return componentExchange;
                }
            }
        }
        return null;
    }

    public AllocationUsage createDescribes(Element source, Element target) {
        AllocationUsage allocation = this.metamodelMutationElementService.createAllocateEdge(source, target);
        this.metamodelMutationElementService.initialize(allocation);
        return allocation;
    }

    public OccurrenceUsage createOperationalCapability(Element parent) {
        return this.commonQueryService.getCapabilitiesPackage(parent)
                .map(capabilitiesPackage -> {
                    var capability = SysmlFactory.eINSTANCE.createOccurrenceUsage();
                    this.metamodelMutationElementService.addChildInParent(capabilitiesPackage, capability);
                    this.metamodelMutationElementService.initialize(capability);
                    this.arcadiaLibraryServices.typeWithArcadiaCapability(capability);
                    return capability;
                })
                .orElse(null);
    }

    public ActionUsage createFunctionalChain(Element container, List<Object> selectedObjects) {
        ActionUsage actionUsage = null;
        List<Element> selectedElements = selectedObjects.stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .toList();
        Optional<Namespace> optionalCommonAncestor = this.commonQueryService.findClosestCommonAncestor(selectedElements,
                candidate -> this.commonQueryService.isFunction(candidate) || this.commonQueryService.isFunctionsPackage(candidate));
        if (optionalCommonAncestor.isPresent()) {
            actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
            this.metamodelMutationElementService.addChildInParent(optionalCommonAncestor.get(), actionUsage);
            this.arcadiaLibraryServices.typeWithArcadiaFunctionalChain(actionUsage);
            this.metamodelMutationElementService.initialize(actionUsage);

            this.commonNamingService.setElementDefaultDeclaredName(actionUsage, FUNCTIONAL_CHAIN_DEFAULT_DECLAREDNAME_PREFIX);
            this.commonUpdateService.setArcadiaReferenceFeature(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES, selectedObjects,
                    SysmlPackage.eINSTANCE.getFlowUsage().getName());
        }
        return actionUsage;
    }

    public PayloadFeature createPayloadFeature(Flow flow) {
        PayloadFeature payloadFeature = SysmlFactory.eINSTANCE.createPayloadFeature();
        this.metamodelMutationElementService.addChildInParent(flow, payloadFeature);
        return payloadFeature;
    }

    private Optional<PartUsage> findAllocatingComponent(Element parent) {
        Optional<PartUsage> allocatingComponent = Optional.empty();
        if (this.commonQueryService.isComponent(parent)) {
            allocatingComponent = Optional.of((PartUsage) parent);
        } else if (this.commonQueryService.isFunction(parent)) {
            allocatingComponent = this.commonQueryService.getAllocatingComponent((ActionUsage) parent);
        }
        return allocatingComponent;
    }
}

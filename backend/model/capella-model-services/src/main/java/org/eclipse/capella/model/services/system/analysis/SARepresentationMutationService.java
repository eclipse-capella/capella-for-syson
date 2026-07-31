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
package org.eclipse.capella.model.services.system.analysis;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.ReferenceSubsetting;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

import java.util.List;
import java.util.Optional;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_HUMAN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_REQUIREMENT;

/**
 * System Analysis (SA) related mutation service.
 * This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class SARepresentationMutationService {

    private static final String WHITE_SPACE = " ";

    private final TransverseMutationService transverseMutationService;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final UtilService utilService;

    private final SAQueryService saQueryService;

    private final MetamodelMutationElementService metamodelMutationElementService;

    private final SAMutationService saMutationService;

    public SARepresentationMutationService() {
        this.transverseMutationService = new TransverseMutationService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.transverseQueryService = new TransverseQueryService();
        this.utilService = new UtilService();
        this.saQueryService = new SAQueryService();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.saMutationService = new SAMutationService();
    }

    public PartUsage createSystemActor(Element parent) {
        return this.createActor(parent, false);
    }

    public PartUsage createSystemComponent(Element parent) {
        var targetContainer = this.getComponentTargetContainer(parent);
        PartUsage partUsage = null;
        if (targetContainer != null) {
            partUsage = SysmlFactory.eINSTANCE.createPartUsage();
            this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
            this.elementInitializerSwitch.doSwitch(partUsage);
            this.typeWithArcadiaLibrary(partUsage, ARCADIA_COMPONENT, SysmlPackage.eINSTANCE.getPartDefinition());
            this.transverseMutationService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_ACTOR, false);
            partUsage.setDeclaredName("C" + WHITE_SPACE + this.countDirectSystemComponents(targetContainer));
        }
        return partUsage;
    }

    private Element getComponentTargetContainer(Element parent) {
        Element targetContainer = null;
        if (parent instanceof PartUsage partUsage && this.saQueryService.isSystemOfInterest(partUsage)) {
            targetContainer = parent;
        } else if (parent instanceof PartUsage partUsage && partUsage.getOwner() instanceof PartUsage && this.saQueryService.isSystemComponent(partUsage)) {
            targetContainer = parent;
        }
        return targetContainer;
    }

    private long countDirectSystemComponents(Element targetContainer) {
        return targetContainer.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.saQueryService::isSystemComponent)
                .count();
    }

    private PartUsage createActor(Element parent, boolean isHuman) {
        var name = "A";
        var targetContainer = this.getActorTargetContainer(parent);
        if (targetContainer == null) {
            return null;
        }
        long existingActorsCount = this.countDirectActors(targetContainer);
        var partUsage = SysmlFactory.eINSTANCE.createPartUsage();
        this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
        this.elementInitializerSwitch.doSwitch(partUsage);
        this.typeWithArcadiaLibrary(partUsage, ARCADIA_COMPONENT, SysmlPackage.eINSTANCE.getPartDefinition());
        this.transverseMutationService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_ACTOR, true);
        this.transverseMutationService.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_HUMAN, isHuman);
        partUsage.setDeclaredName(name + WHITE_SPACE + (existingActorsCount + 1));
        return partUsage;
    }

    private Element getActorTargetContainer(Element parent) {
        Element targetContainer = null;
        if (parent instanceof PartUsage partUsage && !this.saQueryService.isSystemOfInterest(partUsage)) {
            targetContainer = parent;
        } else if (this.saQueryService.isSystemAnalysisStructurePackage(parent)) {
            targetContainer = parent;
        } else {
            targetContainer = this.findOwningSystemAnalysisStructurePackage(parent);
        }
        if (targetContainer == null) {
            targetContainer = this.saQueryService.toComponentsPackage(parent);
        }
        return targetContainer;
    }

    private Package findOwningSystemAnalysisStructurePackage(EObject eObject) {
        Package result = null;
        if (eObject instanceof Package packageElement && this.saQueryService.isSystemAnalysisStructurePackage(packageElement)) {
            result = packageElement;
        } else if (eObject != null) {
            result = this.findOwningSystemAnalysisStructurePackage(eObject.eContainer());
        }
        return result;
    }

    private long countDirectActors(Element targetContainer) {
        return targetContainer.getOwnedElement().stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.transverseQueryService::isComponentActor)
                .count();
    }

    public RequirementUsage createRequirement(Element parent) {
        String name = "Requirement";
        var targetContainer = this.saQueryService.toRequirementsPackage(parent);
        if (targetContainer == null) {
            return null;
        }
        var requirementUsage = SysmlFactory.eINSTANCE.createRequirementUsage();
        this.metamodelMutationElementService.addChildInParent(targetContainer, requirementUsage);
        this.elementInitializerSwitch.doSwitch(requirementUsage);
        this.typeWithArcadiaLibrary(requirementUsage, ARCADIA_REQUIREMENT, SysmlPackage.eINSTANCE.getRequirementDefinition());
        long existingElementsCount = this.transverseQueryService.existingElementsCount(requirementUsage);
        requirementUsage.setDeclaredName(name + WHITE_SPACE + existingElementsCount);
        return requirementUsage;
    }

    public Element createNewFunction(Element parent) {
        Element newFunction = null;
        if (parent instanceof PartUsage container) {
            var functionsPackage = this.saQueryService.toFunctionsPackage(container);
            if (functionsPackage != null) {
                var actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
                this.metamodelMutationElementService.addChildInParent(functionsPackage, actionUsage);
                this.createPerformActionUsage(container, actionUsage);
                this.elementInitializerSwitch.doSwitch(actionUsage);
                this.typeWithArcadiaLibrary(actionUsage, ARCADIA_FUNCTION, SysmlPackage.eINSTANCE.getActionDefinition());
                this.setNewFunctionName(actionUsage);
                newFunction = actionUsage;
            }
        } else if (parent instanceof ActionUsage parentFunction) {
            var actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
            this.metamodelMutationElementService.addChildInParent(parentFunction, actionUsage);
            this.findAllocatingComponent(parentFunction).ifPresent(component -> this.createPerformActionUsage(component, actionUsage));
            this.elementInitializerSwitch.doSwitch(actionUsage);
            this.typeWithArcadiaLibrary(actionUsage, ARCADIA_FUNCTION, SysmlPackage.eINSTANCE.getActionDefinition());
            this.setNewFunctionName(actionUsage);
            newFunction = actionUsage;
        }
        return newFunction;
    }

    public Feature createInputFunctionPort(Element parent) {
        return this.createFunctionPort((ActionUsage) parent, FeatureDirectionKind.IN);
    }

    public Feature createOutputFunctionPort(Element parent) {
        return this.createFunctionPort((ActionUsage) parent, FeatureDirectionKind.OUT);
    }

    public FlowUsage createFunctionalExchange(Feature source, Feature target) {
        FlowUsage functionalExchange = null;
        if (this.canCreateFunctionalExchange(source, target)) {
            var sourcePort = this.getOrCreateFunctionPort(source, FeatureDirectionKind.OUT);
            var targetPort = this.getOrCreateFunctionPort(target, FeatureDirectionKind.IN);
            var container = this.getFunctionalExchangeContainer(sourcePort, targetPort);
            functionalExchange = this.metamodelMutationElementService.createFlowUsage(sourcePort, targetPort, null, null, container);
            this.elementInitializerSwitch.doSwitch(functionalExchange);
            this.typeWithArcadiaLibrary(functionalExchange, ARCADIA_FUNCTIONAL_EXCHANGE, SysmlPackage.eINSTANCE.getFlowDefinition());
            functionalExchange.setDeclaredName("FE" + WHITE_SPACE + this.transverseQueryService.existingElementsCount(functionalExchange));
        }
        return functionalExchange;
    }

    public InterfaceUsage createComponentExchange(Feature source, Feature target) {
        InterfaceUsage componentExchange = null;
        var sourceStructurePackage = this.findOwningSystemAnalysisStructurePackage(source);
        var targetStructurePackage = this.findOwningSystemAnalysisStructurePackage(target);
        if (this.canCreateComponentExchange(source, target) && sourceStructurePackage != null && sourceStructurePackage == targetStructurePackage) {
            var sourcePort = this.getOrCreateComponentPort(source, FeatureDirectionKind.OUT);
            var targetPort = this.getOrCreateComponentPort(target, FeatureDirectionKind.IN);
            componentExchange = SysmlFactory.eINSTANCE.createInterfaceUsage();
            this.metamodelMutationElementService.addChildInParent(sourceStructurePackage, componentExchange);
            componentExchange.getOwnedRelationship().add(this.createConnectionEnd(sourcePort));
            componentExchange.getOwnedRelationship().add(this.createConnectionEnd(targetPort));
            this.elementInitializerSwitch.doSwitch(componentExchange);
            this.typeWithArcadiaLibrary(componentExchange, ARCADIA_COMPONENT_EXCHANGE, SysmlPackage.eINSTANCE.getInterfaceDefinition());
            componentExchange.setDeclaredName("CE" + WHITE_SPACE + this.transverseQueryService.existingElementsCount(componentExchange));
        }
        return componentExchange;
    }

    public AllocationUsage createDescribes(Element source, Element target) {
        AllocationUsage describes = null;
        if (this.canCreateDescribes(source, target)) {
            describes = SysmlFactory.eINSTANCE.createAllocationUsage();
            this.metamodelMutationElementService.addChildInParent(this.saQueryService.toRequirementsPackage(source), describes);
            describes.getOwnedRelationship().add(this.createReferenceSubsetting((Feature) source));
            describes.getOwnedRelationship().add(this.createReferenceSubsetting((Feature) target));
        }
        return describes;
    }

    public boolean canCreateDescribes(Element source, Element target) {
        boolean validSourceAndTarget = source instanceof RequirementUsage && target instanceof Feature;
        boolean distinctEndpoints = source != target;
        boolean validEndpoints = validSourceAndTarget && distinctEndpoints && this.isSameSemanticScope(source, target);
        return validEndpoints && this.isValidDescribesTarget(target) && !this.existsDescribes(source, target);
    }

    public boolean canReconnectDescribes(AllocationUsage describes, Element newEndpoint, boolean isSource) {
        var source = this.saQueryService.getDescribesSource(describes);
        var target = this.saQueryService.getDescribesTarget(describes);
        if (isSource) {
            source = newEndpoint;
        } else {
            target = newEndpoint;
        }
        boolean validSourceAndTarget = source instanceof RequirementUsage && target instanceof Feature;
        boolean distinctEndpoints = source != target;
        boolean validEndpoints = validSourceAndTarget && distinctEndpoints && this.isSameSemanticScope(source, target);
        return validEndpoints && this.isValidDescribesTarget(target) && !this.existsOtherDescribes(describes, source, target);
    }

    private boolean isSameSemanticScope(Element source, Element target) {
        return this.getRoot(source) == this.getRoot(target) && this.isInSystemAnalysisScope(source) && this.isInSystemAnalysisScope(target);
    }

    private boolean isInSystemAnalysisScope(Element element) {
        var qualifiedName = element.getQualifiedName();
        return qualifiedName != null && (qualifiedName.contains("'System Analysis'::Structure::")
                || qualifiedName.contains("'System Analysis'::Functions::")
                || qualifiedName.contains("'System Analysis'::Requirements::"));
    }

    private EObject getRoot(EObject eObject) {
        var root = eObject;
        while (root != null && root.eContainer() != null) {
            root = root.eContainer();
        }
        return root;
    }

    private boolean isValidDescribesTarget(Element target) {
        return this.isValidDescribesNodeTarget(target) || this.isValidDescribesEdgeTarget(target);
    }

    private boolean isValidDescribesNodeTarget(Element target) {
        return target instanceof RequirementUsage
                || this.isValidDescribesComponentTarget(target)
                || this.isValidDescribesPortTarget(target)
                || this.isValidDescribesFunctionTarget(target);
    }

    private boolean isValidDescribesPortTarget(Element target) {
        return target instanceof PortUsage portUsage && this.transverseQueryService.isComponentPort(portUsage)
                || target instanceof ItemUsage itemUsage && this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM).test(itemUsage);
    }

    private boolean isValidDescribesFunctionTarget(Element target) {
        return target instanceof ActionUsage actionUsage && this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION).test(actionUsage);
    }

    private boolean isValidDescribesComponentTarget(Element target) {
        boolean validComponent = false;
        if (target instanceof PartUsage partUsage) {
            validComponent = this.saQueryService.isSystemOfInterest(partUsage) || this.transverseQueryService.isComponentActor(partUsage) || this.saQueryService.isSystemComponent(partUsage);
        }
        return validComponent;
    }

    private boolean isValidDescribesEdgeTarget(Element target) {
        return target instanceof InterfaceUsage interfaceUsage && this.transverseQueryService.isComponentExchange(interfaceUsage)
                || target instanceof FlowUsage flowUsage && this.saQueryService.getFunctionalExchanges(flowUsage).contains(flowUsage);
    }

    private boolean existsDescribes(Element source, Element target) {
        var root = this.getRoot(source);
        return this.saQueryService.getDescribes(root).stream()
                .anyMatch(describes -> source.equals(this.saQueryService.getDescribesSource(describes)) && target.equals(this.saQueryService.getDescribesTarget(describes)));
    }

    private boolean existsOtherDescribes(AllocationUsage currentDescribes, Element source, Element target) {
        var root = this.getRoot(source);
        return this.saQueryService.getDescribes(root).stream()
                .filter(describes -> describes != currentDescribes)
                .anyMatch(describes -> source.equals(this.saQueryService.getDescribesSource(describes)) && target.equals(this.saQueryService.getDescribesTarget(describes)));
    }

    private org.eclipse.syson.sysml.EndFeatureMembership createConnectionEnd(PortUsage referencedPort) {
        var endFeatureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
        var endFeature = SysmlFactory.eINSTANCE.createFeature();
        endFeature.setIsEnd(true);
        endFeatureMembership.getOwnedRelatedElement().add(endFeature);
        endFeature.getOwnedRelationship().add(this.createReferenceSubsetting(referencedPort));
        return endFeatureMembership;
    }

    private ReferenceSubsetting createReferenceSubsetting(Feature referencedFeature) {
        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(referencedFeature);
        return referenceSubsetting;
    }

    private PortUsage getOrCreateComponentPort(Feature feature, FeatureDirectionKind direction) {
        PortUsage portUsage = null;
        if (feature instanceof PortUsage existingPort) {
            portUsage = existingPort;
        } else if (feature instanceof PartUsage) {
            portUsage = this.createComponentPort(feature, direction);
        }
        return portUsage;
    }

    private boolean canCreateComponentExchange(Feature source, Feature target) {
        return this.getComponentOwner(source).isPresent()
                && this.getComponentOwner(target).isPresent()
                && !this.getComponentOwner(source).equals(this.getComponentOwner(target));
    }

    private Optional<PartUsage> getComponentOwner(Feature feature) {
        Optional<PartUsage> owner = Optional.empty();
        if (feature instanceof PartUsage partUsage && this.transverseQueryService.isComponent(partUsage)) {
            owner = Optional.of(partUsage);
        } else if (feature instanceof PortUsage portUsage && this.transverseQueryService.isComponentPort(portUsage) && portUsage.getOwner() instanceof PartUsage partUsage) {
            owner = Optional.of(partUsage);
        }
        return owner;
    }

    public ActionUsage createNewFunctionalChain(Element container, Object selectedObjects) {
        var selectedFunctionalExchanges = this.toSelectedFunctionalExchanges(container, selectedObjects);
        ActionUsage functionalChain = null;
        var functionsPackage = this.saQueryService.toFunctionsPackage(container);
        if (functionsPackage != null && !selectedFunctionalExchanges.isEmpty()) {
            functionalChain = SysmlFactory.eINSTANCE.createActionUsage();
            this.metamodelMutationElementService.addChildInParent(functionsPackage, functionalChain);
            this.elementInitializerSwitch.doSwitch(functionalChain);
            this.typeWithArcadiaLibrary(functionalChain, ARCADIA_FUNCTIONAL_CHAIN, SysmlPackage.eINSTANCE.getActionDefinition());
            functionalChain.setDeclaredName(ARCADIA_FUNCTIONAL_CHAIN + WHITE_SPACE + this.transverseQueryService.existingElementsCount(functionalChain));
            this.saMutationService.createInvolvedFunctionalExchangesReference(functionalChain, selectedFunctionalExchanges);
        }
        return functionalChain;
    }

    private List<FlowUsage> toSelectedFunctionalExchanges(Element container, Object selectedObjects) {
        var reachableFunctionalExchanges = this.saQueryService.getFunctionalExchanges(container);
        List<FlowUsage> selectedFunctionalExchanges = List.of();
        if (selectedObjects instanceof List<?> selectedValues) {
            selectedFunctionalExchanges = selectedValues.stream()
                    .filter(FlowUsage.class::isInstance)
                    .map(FlowUsage.class::cast)
                    .filter(reachableFunctionalExchanges::contains)
                    .toList();
        } else if (selectedObjects instanceof FlowUsage flowUsage && reachableFunctionalExchanges.contains(flowUsage)) {
            selectedFunctionalExchanges = List.of(flowUsage);
        }
        return selectedFunctionalExchanges;
    }

    public PortUsage createInputComponentPort(Element parent) {
        return this.createComponentPort(parent, FeatureDirectionKind.IN);
    }

    public PortUsage createOutputComponentPort(Element parent) {
        return this.createComponentPort(parent, FeatureDirectionKind.OUT);
    }

    public PortUsage createInOutComponentPort(Element parent) {
        return this.createComponentPort(parent, FeatureDirectionKind.INOUT);
    }

    private PortUsage createComponentPort(Element parent, FeatureDirectionKind direction) {
        var portUsage = this.transverseMutationService.createPortUsage(parent);
        this.typeWithArcadiaLibrary(portUsage, ARCADIA_COMPONENT_PORT, SysmlPackage.eINSTANCE.getPortDefinition());
        portUsage.setDeclaredName("CP " + this.transverseQueryService.existingElementsCount(portUsage));
        portUsage.setDirection(direction);
        return portUsage;
    }

    private Feature createFunctionPort(ActionUsage container, FeatureDirectionKind direction) {
        var itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        itemUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, itemUsage);
        this.elementInitializerSwitch.doSwitch(itemUsage);
        this.typeWithArcadiaLibrary(itemUsage, TransverseQueryService.ARCADIA_EXCHANGE_ITEM, SysmlPackage.eINSTANCE.getItemDefinition());
        String defaultName = switch (direction) {
            case IN -> "FIP";
            case OUT -> "FOP";
            default -> "FP";
        };
        itemUsage.setDeclaredName(defaultName + WHITE_SPACE + this.transverseQueryService.existingElementsCount(itemUsage));
        return itemUsage;
    }

    private Feature getOrCreateFunctionPort(Feature feature, FeatureDirectionKind direction) {
        Feature result = null;
        if (feature instanceof ActionUsage actionUsage) {
            result = this.createFunctionPort(actionUsage, direction);
        } else if (feature != null) {
            result = feature;
        }
        return result;
    }

    private boolean canCreateFunctionalExchange(Feature source, Feature target) {
        var sourceFunction = this.getFunctionOwner(source);
        var targetFunction = this.getFunctionOwner(target);
        boolean validOwners = sourceFunction.isPresent() && targetFunction.isPresent() && !sourceFunction.equals(targetFunction);
        return validOwners && this.canUseFunctionEndpoint(source, FeatureDirectionKind.OUT) && this.canUseFunctionEndpoint(target, FeatureDirectionKind.IN);
    }

    private boolean canUseFunctionEndpoint(Feature feature, FeatureDirectionKind direction) {
        boolean validEndpoint = feature instanceof ActionUsage;
        if (feature != null && !(feature instanceof ActionUsage)) {
            validEndpoint = this.isFunctionPort(feature, direction);
        }
        return validEndpoint;
    }

    private Optional<ActionUsage> getFunctionOwner(Feature feature) {
        Optional<ActionUsage> owner = Optional.empty();
        if (feature instanceof ActionUsage actionUsage) {
            owner = Optional.of(actionUsage);
        } else if (feature != null) {
            owner = this.saQueryService.getOwningFunction(feature);
        }
        return owner;
    }

    private boolean isFunctionPort(Feature feature, FeatureDirectionKind direction) {
        return feature != null && direction == feature.getDirection() && this.saQueryService.getOwningFunction(feature).isPresent();
    }

    private Namespace getFunctionalExchangeContainer(Feature sourcePort, Feature targetPort) {
        var sourceFunction = this.saQueryService.getOwningFunction(sourcePort).orElse(null);
        var targetFunction = this.saQueryService.getOwningFunction(targetPort).orElse(null);
        Namespace container = this.getCommonParentFunction(sourceFunction, targetFunction);
        if (container == null) {
            container = this.getRootFunction(sourceFunction);
        }
        if (container == null) {
            container = this.saQueryService.toFunctionsPackage(sourcePort);
        }
        return container;
    }

    private ActionUsage getCommonParentFunction(ActionUsage sourceFunction, ActionUsage targetFunction) {
        ActionUsage commonParent = null;
        var sourceAncestors = this.getAncestorFunctions(sourceFunction);
        Element current = targetFunction;
        while (commonParent == null && current != null) {
            if (sourceAncestors.contains(current)) {
                commonParent = (ActionUsage) current;
            } else {
                current = current.getOwner();
            }
        }
        return commonParent;
    }

    private java.util.Set<ActionUsage> getAncestorFunctions(ActionUsage function) {
        java.util.Set<ActionUsage> ancestors = new java.util.LinkedHashSet<>();
        Element current = function;
        while (current != null) {
            if (current instanceof ActionUsage actionUsage) {
                ancestors.add(actionUsage);
            }
            current = current.getOwner();
        }
        return ancestors;
    }

    private ActionUsage getRootFunction(ActionUsage function) {
        ActionUsage rootFunction = function;
        Element current = function;
        while (current != null) {
            if (current instanceof ActionUsage actionUsage) {
                rootFunction = actionUsage;
            }
            current = current.getOwner();
        }
        return rootFunction;
    }

    private void setNewFunctionName(ActionUsage actionUsage) {
        actionUsage.setDeclaredName("Function " + this.transverseQueryService.existingElementsCount(actionUsage));
    }

    private void createPerformActionUsage(PartUsage component, ActionUsage function) {
        Membership membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        component.getOwnedRelationship().add(membership);
        var performActionUsage = SysmlFactory.eINSTANCE.createPerformActionUsage();
        membership.getOwnedRelatedElement().add(performActionUsage);
        ReferenceSubsetting referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(function);
        performActionUsage.getOwnedRelationship().add(referenceSubsetting);
    }

    private Optional<PartUsage> findAllocatingComponent(ActionUsage function) {
        EObject root = function;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        java.util.List<PartUsage> components = new java.util.ArrayList<>();
        root.eAllContents().forEachRemaining(child -> {
            if (child instanceof PartUsage partUsage) {
                components.add(partUsage);
            }
        });
        return components.stream()
                .filter(component -> component.getNestedUsage().stream()
                        .filter(PerformActionUsage.class::isInstance)
                        .map(PerformActionUsage.class::cast)
                        .map(this.saQueryService::getPerformedAction)
                        .flatMap(Optional::stream)
                        .anyMatch(function::equals))
                .findFirst();
    }

    private void typeWithArcadiaLibrary(Usage usage, String arcadiaTypeName, EClass librarySysMLElementType) {
        var elementType = this.utilService.getAllReachable(usage, librarySysMLElementType).stream()
                .filter(librarySysMLElementType::isInstance)
                .filter(Type.class::isInstance)
                .map(Type.class::cast)
                .filter(element -> (ARCADIA_PREFIX + arcadiaTypeName).equals(element.getQualifiedName()))
                .findFirst()
                .orElse(null);
        if (elementType == null) {
            elementType = this.findReachableTypeByContainment(usage, librarySysMLElementType, arcadiaTypeName);
        }
        this.utilService.setFeatureTyping(usage, elementType);
    }

    private Type findReachableTypeByContainment(Usage usage, EClass librarySysMLElementType, String arcadiaTypeName) {
        EObject root = usage;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        List<Type> types = new java.util.ArrayList<>();
        root.eAllContents().forEachRemaining(child -> {
            if (librarySysMLElementType.isInstance(child) && child instanceof Type type) {
                types.add(type);
            }
        });
        return types.stream()
                .filter(element -> (ARCADIA_PREFIX + arcadiaTypeName).equals(element.getQualifiedName()))
                .findFirst()
                .orElse(null);
    }

}

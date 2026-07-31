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

import org.eclipse.capella.model.services.transverse.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.ReferenceSubsetting;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.FUNCTIONS_PACKAGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.PATH_SEPARATOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.REQUIREMENTS_PACKAGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.STRUCTURE_PACKAGE;

/**
 * System Analysis (SA) related mutation service.
 * It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class SAQueryService {

    private static final String SYSTEM_ANALYSIS_PACKAGE_NAME = "'System Analysis'";

    private static final String SYSTEM_OF_INTEREST_NAME = "system";

    private final TransverseQueryService transverseQueryService;

    private final UtilService utilService;

    public SAQueryService() {
        this.transverseQueryService = new TransverseQueryService();
        this.utilService = new UtilService();
    }

    public List<PartUsage> getSubComponents(EObject eObject) {
        List<PartUsage> allPartUsage = new ArrayList<>();
        if (eObject instanceof Package pkg) {
            Package componentsPackage = this.toComponentsPackage(pkg);
            if (componentsPackage != null) {
                allPartUsage = this.getDirectSubComponents(componentsPackage, componentsPackage.getQualifiedName());
            }
        } else if (this.transverseQueryService.isComponent(eObject) && eObject instanceof PartUsage partUsage) {
            allPartUsage = this.getDirectSubComponents(partUsage, partUsage.getQualifiedName());
        }
        return allPartUsage;
    }

    public List<PartUsage> getSystemOfInterest(EObject eObject) {
        return this.getSystemOfInterestCandidates(eObject).stream()
                .findFirst()
                .stream()
                .toList();
    }

    public List<PartUsage> getSystemActors(EObject eObject) {
        return this.getSubComponents(eObject).stream()
                .filter(this.transverseQueryService::isComponentActor)
                .toList();
    }

    public List<PartUsage> getSystemComponents(EObject eObject) {
        return this.getSubComponents(eObject).stream()
                .filter(partUsage -> !this.transverseQueryService.isComponentActor(partUsage))
                .filter(partUsage -> !this.isSystemOfInterest(partUsage))
                .toList();
    }

    public boolean isSystemComponent(PartUsage partUsage) {
        return this.transverseQueryService.isComponent(partUsage)
                && !this.isActor(partUsage)
                && !this.isSystemOfInterest(partUsage)
                && this.isInSystemAnalysisStructure(partUsage);
    }

    private boolean isActor(PartUsage partUsage) {
        return this.transverseQueryService.isComponentActor(partUsage) || partUsage.getNestedReference().stream()
                .anyMatch(referenceUsage -> ARCADIA_IS_ACTOR.equals(referenceUsage.getName())
                        && referenceUsage.getOwnedMember().stream()
                                .filter(LiteralBoolean.class::isInstance)
                                .map(LiteralBoolean.class::cast)
                                .map(LiteralBoolean::isValue)
                                .findFirst()
                                .orElse(false));
    }

    public boolean isSystemAnalysisStructurePackage(Object element) {
        return element instanceof Package packageElt
                && STRUCTURE_PACKAGE.equals(packageElt.getDeclaredName())
                && this.isSystemAnalysisPerspectivePackage(packageElt);
    }

    private boolean isSystemAnalysisPerspectivePackage(Package packageElt) {
        return this.transverseQueryService.getArcadiaPerspectivePackage(packageElt)
                .map(parentPackage -> ArcadiaEngineeringPerspective.SystemAnalysis == ArcadiaEngineeringPerspective.fromValue(parentPackage.getDeclaredName()))
                .orElse(false);
    }

    private List<PartUsage> getSystemOfInterestCandidates(EObject eObject) {
        return this.getDirectOwnedPartUsages(eObject).stream()
                .filter(this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .filter(this::isInSystemAnalysisStructure)
                .filter(this::isSystemOfInterest)
                .toList();
    }

    private List<PartUsage> getDirectOwnedPartUsages(EObject eObject) {
        if (eObject instanceof Element element) {
            return element.getOwnedElement().stream()
                    .filter(PartUsage.class::isInstance)
                    .map(PartUsage.class::cast)
                    .toList();
        }
        return List.of();
    }

    public List<ActionUsage> getSubFunctions(EObject eObject) {
        List<ActionUsage> subFunctions = new ArrayList<>();
        if (eObject instanceof PartUsage partUsage) {
            subFunctions.addAll(this.getAllocatedFunctions(partUsage));
        } else if (eObject instanceof ActionUsage actionUsage) {
            subFunctions.addAll(actionUsage.getNestedAction().stream()
                    .filter(ActionUsage.class::isInstance)
                    .map(ActionUsage.class::cast)
                    .filter(this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION))
                    .toList());
        }
        return subFunctions;
    }

    public List<Feature> getFunctionPorts(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return actionUsage.getOwnedElement().stream()
                    .filter(Feature.class::isInstance)
                    .map(Feature.class::cast)
                    .filter(this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM))
                    .filter(feature -> FeatureDirectionKind.IN == feature.getDirection() || FeatureDirectionKind.OUT == feature.getDirection())
                    .toList();
        }
        return List.of();
    }

    public List<InterfaceUsage> getComponentExchanges(EObject eObject) {
        List<InterfaceUsage> componentExchanges = new ArrayList<>();
        if (eObject instanceof InterfaceUsage interfaceUsage) {
            componentExchanges.add(interfaceUsage);
        }
        eObject.eAllContents().forEachRemaining(child -> {
            if (child instanceof InterfaceUsage interfaceUsage) {
                componentExchanges.add(interfaceUsage);
            }
        });
        return componentExchanges.stream()
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE))
                .filter(this::isSystemAnalysisComponentExchange)
                .toList();
    }

    public PortUsage getComponentExchangeSourcePort(InterfaceUsage componentExchange) {
        return this.getComponentExchangeEnd(componentExchange, 0);
    }

    public PortUsage getComponentExchangeTargetPort(InterfaceUsage componentExchange) {
        return this.getComponentExchangeEnd(componentExchange, 1);
    }

    private PortUsage getComponentExchangeEnd(InterfaceUsage componentExchange, int index) {
        var ends = Optional.ofNullable(componentExchange.getConnectorEnd())
                .stream()
                .flatMap(List::stream)
                .map(this::getReferencedPort)
                .filter(PortUsage.class::isInstance)
                .map(PortUsage.class::cast)
                .toList();
        PortUsage result = null;
        if (ends.size() > index) {
            result = ends.get(index);
        }
        return result;
    }

    public List<FlowUsage> getFunctionalExchanges(EObject eObject) {
        EObject searchRoot = eObject;
        if (!(eObject instanceof FlowUsage)) {
            searchRoot = Optional.ofNullable(this.toFunctionsPackage(eObject)).map(EObject.class::cast).orElse(eObject);
        }
        List<FlowUsage> functionalExchanges = new ArrayList<>();
        if (searchRoot instanceof FlowUsage flowUsage) {
            functionalExchanges.add(flowUsage);
        }
        searchRoot.eAllContents().forEachRemaining(child -> {
            if (child instanceof FlowUsage flowUsage) {
                functionalExchanges.add(flowUsage);
            }
        });
        return functionalExchanges.stream()
                .filter(this::isValidFunctionalExchange)
                .toList();
    }

    public Boolean isFunctionalChain(EObject eObject) {
        return eObject instanceof ActionUsage actionUsage
                && this.transverseQueryService.checkType(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN);
    }

    public List<ActionUsage> getFunctionalChains(EObject eObject) {
        EObject searchRoot = Optional.ofNullable(this.toFunctionsPackage(eObject)).map(EObject.class::cast).orElse(eObject);
        List<ActionUsage> functionalChains = new ArrayList<>();
        if (searchRoot instanceof ActionUsage actionUsage) {
            functionalChains.add(actionUsage);
        }
        searchRoot.eAllContents().forEachRemaining(child -> {
            if (child instanceof ActionUsage actionUsage) {
                functionalChains.add(actionUsage);
            }
        });
        return functionalChains.stream()
                .filter(this::isFunctionalChain)
                .toList();
    }

    public List<FlowUsage> getInvolvedFunctionalExchanges(ActionUsage functionalChain) {
        List<FlowUsage> involvedFunctionalExchanges = this.transverseQueryService.getFeatureReferenceValue(functionalChain, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES).stream()
                .filter(FlowUsage.class::isInstance)
                .map(FlowUsage.class::cast)
                .filter(this::isValidFunctionalExchange)
                .toList();
        if (involvedFunctionalExchanges.isEmpty()) {
            involvedFunctionalExchanges = functionalChain.getNestedReference().stream()
                    .filter(referenceUsage -> ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES.equals(referenceUsage.getName()))
                    .flatMap(referenceUsage -> this.getReferencedFlowUsages(referenceUsage).stream())
                    .filter(this::isValidFunctionalExchange)
                    .toList();
        }
        return involvedFunctionalExchanges;
    }

    private List<FlowUsage> getReferencedFlowUsages(ReferenceUsage referenceUsage) {
        return referenceUsage.getOwnedRelationship().stream()
                .filter(org.eclipse.syson.sysml.Membership.class::isInstance)
                .map(org.eclipse.syson.sysml.Membership.class::cast)
                .map(org.eclipse.syson.sysml.Membership::getMemberElement)
                .filter(FlowUsage.class::isInstance)
                .map(FlowUsage.class::cast)
                .toList();
    }

    public List<ActionUsage> getInvolvedFunctions(ActionUsage functionalChain) {
        LinkedHashSet<ActionUsage> involvedFunctions = new LinkedHashSet<>();
        this.getInvolvedFunctionalExchanges(functionalChain).forEach(functionalExchange -> {
            Optional.ofNullable(this.getFunctionalExchangeSource(functionalExchange))
                    .flatMap(this::getOwningFunction)
                    .ifPresent(involvedFunctions::add);
            Optional.ofNullable(this.getFunctionalExchangeTarget(functionalExchange))
                    .flatMap(this::getOwningFunction)
                    .ifPresent(involvedFunctions::add);
        });
        return List.copyOf(involvedFunctions);
    }

    public List<ActionUsage> getFunctionalChainsImpliedIn(FlowUsage functionalExchange) {
        return this.getFunctionalChains(functionalExchange).stream()
                .filter(functionalChain -> this.getInvolvedFunctionalExchanges(functionalChain).contains(functionalExchange))
                .toList();
    }

    public List<ActionUsage> getFunctionalChainsImpliedIn(ActionUsage function) {
        return this.getFunctionalChains(function).stream()
                .filter(functionalChain -> this.getInvolvedFunctions(functionalChain).contains(function))
                .toList();
    }

    public List<RequirementUsage> getRequirements(EObject eObject) {
        return this.transverseQueryService.getRequirements(eObject);
    }

    public List<AllocationUsage> getDescribes(EObject eObject) {
        List<AllocationUsage> allocations = new ArrayList<>();
        if (eObject instanceof AllocationUsage allocationUsage) {
            allocations.add(allocationUsage);
        }
        eObject.eAllContents().forEachRemaining(child -> {
            if (child instanceof AllocationUsage allocationUsage) {
                allocations.add(allocationUsage);
            }
        });
        return allocations.stream()
                .filter(allocationUsage -> this.getDescribesSource(allocationUsage) instanceof RequirementUsage)
                .toList();
    }

    public Element getDescribesSource(AllocationUsage allocationUsage) {
        Element source = allocationUsage.getSource().stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .findFirst()
                .orElse(null);
        if (source == null) {
            source = this.getDescribesEndpoint(allocationUsage, 0);
        }
        return source;
    }

    public Element getDescribesTarget(AllocationUsage allocationUsage) {
        Element target = allocationUsage.getTarget().stream()
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .findFirst()
                .orElse(null);
        if (target == null) {
            target = this.getDescribesEndpoint(allocationUsage, 1);
        }
        return target;
    }

    private Element getDescribesEndpoint(AllocationUsage allocationUsage, int index) {
        return allocationUsage.getOwnedRelationship().stream()
                .filter(ReferenceSubsetting.class::isInstance)
                .map(ReferenceSubsetting.class::cast)
                .map(ReferenceSubsetting::getReferencedFeature)
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .skip(index)
                .findFirst()
                .orElse(null);
    }

    public Feature getFunctionalExchangeSource(FlowUsage functionalExchange) {
        return this.unwrapFeature(functionalExchange.getSourceOutputFeature());
    }

    public Feature getFunctionalExchangeTarget(FlowUsage functionalExchange) {
        return this.unwrapFeature(functionalExchange.getTargetInputFeature());
    }

    private Feature unwrapFeature(Feature feature) {
        Feature result = feature;
        if (feature != null) {
            result = feature.getOwnedRedefinition().stream()
                    .map(Redefinition::getRedefinedFeature)
                    .filter(Feature.class::isInstance)
                    .findFirst()
                    .orElse(feature);
        }
        return result;
    }

    public PartUsage getComponentExchangeSource(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(interfaceUsage.getConnectorEnd())
                .stream()
                .flatMap(List::stream)
                .map(this::getReferencedPort)
                .map(Element::getOwner)
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .findFirst()
                .orElse(null);
    }

    public PartUsage getComponentExchangeTarget(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(interfaceUsage.getConnectorEnd())
                .stream()
                .flatMap(List::stream)
                .skip(1)
                .map(this::getReferencedPort)
                .map(Element::getOwner)
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .findFirst()
                .orElse(null);
    }

    public Package toComponentsPackage(EObject eObject) {
        return this.findSystemAnalysisPackage(eObject, STRUCTURE_PACKAGE);
    }

    public Package toRequirementsPackage(EObject eObject) {
        return this.findSystemAnalysisPackage(eObject, REQUIREMENTS_PACKAGE);
    }

    public Package toFunctionsPackage(EObject eObject) {
        return this.findSystemAnalysisPackage(eObject, FUNCTIONS_PACKAGE);
    }

    private Package findSystemAnalysisPackage(EObject eObject, String packageName) {
        var allPackage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPackage());
        Package systemAnalysisPackage = allPackage.stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> pkg.getQualifiedName().endsWith(SYSTEM_ANALYSIS_PACKAGE_NAME + PATH_SEPARATOR + packageName))
                .findFirst()
                .orElse(null);
        if (systemAnalysisPackage == null) {
            systemAnalysisPackage = this.findSiblingPackage(eObject, packageName);
        }
        return systemAnalysisPackage;
    }

    private Package findSiblingPackage(EObject eObject, String packageName) {
        Package result = null;
        if (eObject instanceof Package packageElement && "System Analysis".equals(packageElement.getDeclaredName())) {
            result = packageElement.getOwnedElement().stream()
                    .filter(Package.class::isInstance)
                    .map(Package.class::cast)
                    .filter(pkg -> packageName.equals(pkg.getDeclaredName()))
                    .findFirst()
                    .orElse(null);
        } else if (eObject != null) {
            result = this.findSiblingPackage(eObject.eContainer(), packageName);
        }
        return result;
    }

    public List<ActionUsage> getAllocatedFunctions(PartUsage partUsage) {
        return this.getPerformedActions(partUsage, this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION));
    }

    public List<ActionUsage> getPerformedActions(Usage usage, java.util.function.Predicate<? super ActionUsage> predicate) {
        return usage.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .map(this::getPerformedAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(predicate)
                .toList();
    }

    public Optional<ActionUsage> getPerformedAction(PerformActionUsage performActionUsage) {
        Optional<ActionUsage> result = Optional.ofNullable(performActionUsage.getPerformedAction())
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast);
        if (result.isEmpty()) {
            result = performActionUsage.getOwnedRelationship().stream()
                    .filter(ReferenceSubsetting.class::isInstance)
                    .map(ReferenceSubsetting.class::cast)
                    .map(ReferenceSubsetting::getReferencedFeature)
                    .filter(ActionUsage.class::isInstance)
                    .map(ActionUsage.class::cast)
                    .findFirst();
        }
        return result;
    }

    public boolean isSystemOfInterest(PartUsage partUsage) {
        return this.isSystemOfInterestCandidate(partUsage)
                && this.isDirectlyOwnedBySystemAnalysisStructure(partUsage)
                && this.isInSystemAnalysisStructure(partUsage)
                && this.isFirstDirectNonActorComponent(partUsage);
    }

    private boolean isSystemOfInterestCandidate(PartUsage partUsage) {
        return this.transverseQueryService.isComponent(partUsage) && !this.isActor(partUsage);
    }

    private boolean isDirectlyOwnedBySystemAnalysisStructure(PartUsage partUsage) {
        return partUsage.getOwner() instanceof Package packageElement && STRUCTURE_PACKAGE.equals(packageElement.getDeclaredName());
    }

    private boolean isFirstDirectNonActorComponent(PartUsage partUsage) {
        boolean result = false;
        if (partUsage.getOwner() instanceof Package packageElement) {
            result = packageElement.getOwnedElement().stream()
                    .filter(PartUsage.class::isInstance)
                    .map(PartUsage.class::cast)
                    .filter(this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                    .filter(candidate -> !this.isActor(candidate))
                    .findFirst()
                    .filter(partUsage::equals)
                    .isPresent();
        }
        return result;
    }

    private boolean isInSystemAnalysisStructure(PartUsage partUsage) {
        String qualifiedName = partUsage.getQualifiedName();
        return qualifiedName != null && (qualifiedName.contains(SYSTEM_ANALYSIS_PACKAGE_NAME + PATH_SEPARATOR + STRUCTURE_PACKAGE + PATH_SEPARATOR)
                || qualifiedName.contains("'" + SYSTEM_ANALYSIS_PACKAGE_NAME + "'" + PATH_SEPARATOR + STRUCTURE_PACKAGE + PATH_SEPARATOR));
    }

    private boolean isSystemAnalysisComponentExchange(InterfaceUsage interfaceUsage) {
        PartUsage source = this.getComponentExchangeSource(interfaceUsage);
        PartUsage target = this.getComponentExchangeTarget(interfaceUsage);
        return source != null && target != null && this.isInSystemAnalysisStructure(source) && this.isInSystemAnalysisStructure(target);
    }

    private boolean isValidFunctionalExchange(FlowUsage flowUsage) {
        Feature source = this.getFunctionalExchangeSource(flowUsage);
        Feature target = this.getFunctionalExchangeTarget(flowUsage);
        return this.isFunctionPort(source, FeatureDirectionKind.OUT) && this.isFunctionPort(target, FeatureDirectionKind.IN);
    }

    private boolean isFunctionPort(Feature feature, FeatureDirectionKind direction) {
        boolean result = false;
        if (feature != null && direction == feature.getDirection()) {
            result = this.isOwnedByFunction(feature);
        }
        return result;
    }

    private boolean isOwnedByFunction(Feature feature) {
        return this.getOwningFunction(feature).isPresent();
    }

    public Optional<ActionUsage> getOwningFunction(Feature feature) {
        Optional<ActionUsage> result = Optional.empty();
        if (feature instanceof Usage usage && usage.getOwningUsage() instanceof ActionUsage actionUsage) {
            result = Optional.of(actionUsage);
        } else if (feature.getOwner() instanceof ActionUsage actionUsage) {
            result = Optional.of(actionUsage);
        } else {
            EObject container = feature.eContainer();
            while (result.isEmpty() && container != null) {
                if (container instanceof ActionUsage actionUsage) {
                    result = Optional.of(actionUsage);
                } else {
                    container = container.eContainer();
                }
            }
        }
        return result;
    }

    public Optional<PartUsage> getAllocatingComponent(ActionUsage actionUsage) {
        return this.getAllocatingComponents(actionUsage).stream().findFirst();
    }

    public List<PartUsage> getAllocatingComponents(ActionUsage actionUsage) {
        EObject root = actionUsage;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        List<PartUsage> components = new ArrayList<>();
        root.eAllContents().forEachRemaining(child -> {
            if (child instanceof PartUsage partUsage) {
                components.add(partUsage);
            }
        });
        return components.stream()
                .filter(component -> this.hasPerformedAction(component, actionUsage))
                .toList();
    }

    private boolean hasPerformedAction(PartUsage component, ActionUsage actionUsage) {
        boolean result = this.getPerformedActions(component, actionUsage::equals).contains(actionUsage);
        if (!result) {
            List<PerformActionUsage> performActionUsages = new ArrayList<>();
            component.eAllContents().forEachRemaining(child -> {
                if (child instanceof PerformActionUsage performActionUsage) {
                    performActionUsages.add(performActionUsage);
                }
            });
            result = performActionUsages.stream()
                    .map(this::getPerformedAction)
                    .flatMap(Optional::stream)
                    .anyMatch(actionUsage::equals);
        }
        return result;
    }

    private PortUsage getReferencedPort(Feature connectorEnd) {
        return Optional.ofNullable(connectorEnd.getOwnedReferenceSubsetting())
                .map(ReferenceSubsetting::getReferencedFeature)
                .filter(PortUsage.class::isInstance)
                .map(PortUsage.class::cast)
                .orElse(null);
    }

    private java.util.function.Predicate<? super Feature> isTypedWith(String qualifiedName) {
        return feature -> feature.getType().stream().anyMatch(type -> type != null && qualifiedName.equals(type.getQualifiedName()));
    }

    private List<PartUsage> getDirectSubComponents(EObject context, String ownerQualifiedName) {
        if (context == null || ownerQualifiedName == null) {
            return List.of();
        }
        List<PartUsage> directSubComponents = this.utilService.getAllReachable(context, SysmlPackage.eINSTANCE.getPartUsage()).stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .filter(this::isInSystemAnalysisStructure)
                .filter(partUsage -> this.isDirectChild(ownerQualifiedName, partUsage.getQualifiedName()))
                .toList();
        if (directSubComponents.isEmpty() && context instanceof Element element) {
            directSubComponents = element.getOwnedElement().stream()
                    .filter(PartUsage.class::isInstance)
                    .map(PartUsage.class::cast)
                    .filter(this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                    .filter(this::isInSystemAnalysisStructure)
                    .toList();
        }
        return directSubComponents;
    }

    private boolean isDirectChild(String ownerQualifiedName, String candidateQualifiedName) {
        if (candidateQualifiedName == null || !candidateQualifiedName.startsWith(ownerQualifiedName + PATH_SEPARATOR)) {
            return false;
        }
        String remaining = candidateQualifiedName.substring((ownerQualifiedName + PATH_SEPARATOR).length());
        return !remaining.isEmpty() && !remaining.contains(PATH_SEPARATOR);
    }

}

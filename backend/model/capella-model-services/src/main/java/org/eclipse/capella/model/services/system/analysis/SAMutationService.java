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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;

/**
 * System Analysis semantic mutation service.
 *
 * @author tbezierslafosse
 */
public class SAMutationService {

    private final SAQueryService saQueryService;

    private final TransverseMutationService transverseMutationService;

    private final DeleteService deleteService;

    public SAMutationService() {
        this.saQueryService = new SAQueryService();
        this.transverseMutationService = new TransverseMutationService();
        this.deleteService = new DeleteService();
    }

    public ActionUsage deleteFunction(ActionUsage function) {
        var deletedFunctions = new LinkedHashSet<ActionUsage>();
        deletedFunctions.add(function);
        function.eAllContents().forEachRemaining(child -> {
            if (child instanceof ActionUsage actionUsage) {
                deletedFunctions.add(actionUsage);
            }
        });
        var root = this.getRoot(function);
        var deletedElements = new LinkedHashSet<Element>();
        deletedFunctions.forEach(deletedFunction -> deletedElements.addAll(this.collectElementTree(deletedFunction)));
        var deletedFunctionalExchanges = this.saQueryService.getFunctionalExchanges(root).stream()
                .filter(functionalExchange -> this.isRelatedToDeletedFunction(functionalExchange, deletedFunctions))
                .toList();
        deletedElements.addAll(deletedFunctionalExchanges);
        this.deleteDescribesReferencing(root, deletedElements);
        this.repairFunctionalChains(root, Set.copyOf(deletedFunctionalExchanges));
        this.deleteExternalFunctionAllocations(deletedFunctions, Set.of());
        deletedFunctionalExchanges.forEach(this.deleteService::deleteFromModel);
        this.deleteService.deleteFromModel(function);
        return function;
    }

    public Feature deleteComponentPort(Feature port) {
        var root = this.getRoot(port);
        var deletedComponentExchanges = this.saQueryService.getComponentExchanges(root).stream()
                .filter(componentExchange -> port.equals(this.saQueryService.getComponentExchangeSourcePort(componentExchange))
                        || port.equals(this.saQueryService.getComponentExchangeTargetPort(componentExchange)))
                .toList();
        this.deleteDescribesReferencing(root, Set.of(port));
        deletedComponentExchanges.forEach(this.deleteService::deleteFromModel);
        this.deleteService.deleteFromModel(port);
        return port;
    }

    public Feature deleteFunctionPort(Feature port) {
        var root = this.getRoot(port);
        var deletedFunctionalExchanges = this.saQueryService.getFunctionalExchanges(root).stream()
                .filter(functionalExchange -> port.equals(this.saQueryService.getFunctionalExchangeSource(functionalExchange))
                        || port.equals(this.saQueryService.getFunctionalExchangeTarget(functionalExchange)))
                .toList();
        this.deleteDescribesReferencing(root, Set.of(port));
        this.repairFunctionalChains(root, Set.copyOf(deletedFunctionalExchanges));
        deletedFunctionalExchanges.forEach(this.deleteService::deleteFromModel);
        this.deleteService.deleteFromModel(port);
        return port;
    }

    public RequirementUsage deleteRequirement(RequirementUsage requirement) {
        var root = this.getRoot(requirement);
        this.deleteDescribesReferencing(root, Set.of(requirement));
        this.deleteService.deleteFromModel(requirement);
        return requirement;
    }

    public FlowUsage deleteFunctionalExchange(FlowUsage functionalExchange) {
        var root = this.getRoot(functionalExchange);
        this.deleteDescribesReferencing(root, Set.of(functionalExchange));
        this.repairFunctionalChains(root, Set.of(functionalExchange));
        this.deleteService.deleteFromModel(functionalExchange);
        return functionalExchange;
    }

    public InterfaceUsage deleteComponentExchange(InterfaceUsage componentExchange) {
        var root = this.getRoot(componentExchange);
        this.deleteDescribesReferencing(root, Set.of(componentExchange));
        this.deleteService.deleteFromModel(componentExchange);
        return componentExchange;
    }

    public PartUsage deleteSystemActor(PartUsage actor) {
        return this.deleteSystemComponent(actor);
    }

    public void moveFunctionToComponent(ActionUsage function, PartUsage targetComponent) {
        var root = this.getRoot(function);
        var components = new ArrayList<PartUsage>();
        root.eAllContents().forEachRemaining(child -> {
            if (child instanceof PartUsage partUsage) {
                components.add(partUsage);
            }
        });
        components.forEach(component -> this.deletePerformedActionUsages(component, function));
        this.createPerformActionUsage(targetComponent, function);
    }

    public PartUsage deleteSystemComponent(PartUsage component) {
        var deletedComponents = this.collectComponentTree(component);
        var deletedElements = this.collectElementTree(component);
        var deletedFunctions = this.collectAllocatedFunctions(deletedComponents);
        deletedFunctions.forEach(function -> deletedElements.addAll(this.collectElementTree(function)));
        var root = this.getRoot(component);
        var deletedFunctionalExchanges = this.saQueryService.getFunctionalExchanges(root).stream()
                .filter(functionalExchange -> this.isRelatedToDeletedFunction(functionalExchange, deletedFunctions))
                .toList();
        var deletedComponentExchanges = this.saQueryService.getComponentExchanges(root).stream()
                .filter(componentExchange -> deletedComponents.contains(this.saQueryService.getComponentExchangeSource(componentExchange))
                        || deletedComponents.contains(this.saQueryService.getComponentExchangeTarget(componentExchange)))
                .toList();
        deletedElements.addAll(deletedFunctionalExchanges);
        deletedElements.addAll(deletedComponentExchanges);
        this.deleteDescribesReferencing(root, deletedElements);
        this.repairFunctionalChains(root, Set.copyOf(deletedFunctionalExchanges));
        this.deleteExternalFunctionAllocations(deletedFunctions, deletedComponents);
        deletedFunctionalExchanges.forEach(this.deleteService::deleteFromModel);
        deletedComponentExchanges.forEach(this.deleteService::deleteFromModel);
        deletedFunctions.forEach(this.deleteService::deleteFromModel);
        this.deleteService.deleteFromModel(component);
        return component;
    }

    private void deleteDescribesReferencing(EObject root, Set<Element> deletedElements) {
        this.saQueryService.getDescribes(root).stream()
                .filter(describes -> deletedElements.contains(this.saQueryService.getDescribesSource(describes))
                        || deletedElements.contains(this.saQueryService.getDescribesTarget(describes)))
                .forEach(this.deleteService::deleteFromModel);
    }

    private void repairFunctionalChains(EObject root, Set<FlowUsage> deletedFunctionalExchanges) {
        this.saQueryService.getFunctionalChains(root).forEach(functionalChain -> {
            var involvedFunctionalExchanges = this.saQueryService.getInvolvedFunctionalExchanges(functionalChain);
            var remainingFunctionalExchanges = involvedFunctionalExchanges.stream()
                    .filter(functionalExchange -> !deletedFunctionalExchanges.contains(functionalExchange))
                    .toList();
            if (remainingFunctionalExchanges.isEmpty()) {
                this.deleteService.deleteFromModel(functionalChain);
            } else if (remainingFunctionalExchanges.size() < involvedFunctionalExchanges.size()) {
                this.transverseMutationService.deleteReference(functionalChain, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);
                this.createInvolvedFunctionalExchangesReference(functionalChain, remainingFunctionalExchanges);
            }
        });
    }

    private void deleteExternalFunctionAllocations(Set<ActionUsage> deletedFunctions, Set<PartUsage> deletedComponents) {
        deletedFunctions.forEach(function -> this.saQueryService.getAllocatingComponents(function).stream()
                .filter(component -> !deletedComponents.contains(component))
                .forEach(component -> this.deletePerformedActionUsages(component, function)));
    }

    private void deletePerformedActionUsages(PartUsage component, ActionUsage function) {
        component.eAllContents().forEachRemaining(child -> {
            if (child instanceof PerformActionUsage performActionUsage && this.saQueryService.getPerformedAction(performActionUsage)
                    .filter(function::equals)
                    .isPresent()) {
                this.deleteService.deleteFromModel(performActionUsage);
            }
        });
    }

    private Set<ActionUsage> collectAllocatedFunctions(Set<PartUsage> components) {
        var functions = new LinkedHashSet<ActionUsage>();
        components.forEach(component -> this.saQueryService.getAllocatedFunctions(component).forEach(function -> {
            functions.add(function);
            function.eAllContents().forEachRemaining(child -> {
                if (child instanceof ActionUsage actionUsage) {
                    functions.add(actionUsage);
                }
            });
        }));
        return functions;
    }

    private boolean isRelatedToDeletedFunction(FlowUsage functionalExchange, Set<ActionUsage> deletedFunctions) {
        return Optional.ofNullable(this.saQueryService.getFunctionalExchangeSource(functionalExchange))
                .flatMap(this.saQueryService::getOwningFunction)
                .filter(deletedFunctions::contains)
                .isPresent()
                || Optional.ofNullable(this.saQueryService.getFunctionalExchangeTarget(functionalExchange))
                .flatMap(this.saQueryService::getOwningFunction)
                .filter(deletedFunctions::contains)
                .isPresent();
    }

    private Set<Element> collectElementTree(Element element) {
        var elements = new LinkedHashSet<Element>();
        elements.add(element);
        element.eAllContents().forEachRemaining(child -> {
            if (child instanceof Element childElement) {
                elements.add(childElement);
            }
        });
        return elements;
    }

    private Set<PartUsage> collectComponentTree(PartUsage component) {
        var components = new LinkedHashSet<PartUsage>();
        components.add(component);
        component.eAllContents().forEachRemaining(child -> {
            if (child instanceof PartUsage partUsage) {
                components.add(partUsage);
            }
        });
        return components;
    }

    private EObject getRoot(EObject eObject) {
        EObject root = eObject;
        while (root.eContainer() != null) {
            root = root.eContainer();
        }
        return root;
    }

    /**
     * Associates the selected functional exchanges with a functional chain.
     *
     * @author tbezierslafosse
     */
    public void createInvolvedFunctionalExchangesReference(ActionUsage functionalChain, List<FlowUsage> selectedFunctionalExchanges) {
        var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
        functionalChain.getOwnedRelationship().add(featureMembership);
        var referenceUsage = SysmlFactory.eINSTANCE.createReferenceUsage();
        referenceUsage.setDeclaredName(ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);
        featureMembership.getOwnedRelatedElement().add(referenceUsage);
        selectedFunctionalExchanges.forEach(functionalExchange -> {
            var membership = SysmlFactory.eINSTANCE.createMembership();
            membership.setMemberElement(functionalExchange);
            referenceUsage.getOwnedRelationship().add(membership);
        });
    }

    private void createPerformActionUsage(PartUsage component, ActionUsage function) {
        var membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        component.getOwnedRelationship().add(membership);
        var performActionUsage = SysmlFactory.eINSTANCE.createPerformActionUsage();
        membership.getOwnedRelatedElement().add(performActionUsage);
        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(function);
        performActionUsage.getOwnedRelationship().add(referenceSubsetting);
    }
}

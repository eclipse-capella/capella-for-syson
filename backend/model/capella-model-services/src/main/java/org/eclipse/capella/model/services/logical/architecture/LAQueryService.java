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

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EndFeatureMembership;
import org.eclipse.syson.sysml.EnumerationUsage;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureChaining;
import org.eclipse.syson.sysml.FlowEnd;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.ReferenceSubsetting;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.VariantMembership;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.FUNCTIONS_PACKAGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.PATH_SEPARATOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.STRUCTURE_PACKAGE;

/**
 * Logical Architecture (LA) related query service. It is important to note that this service must retain its empty
 * constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class LAQueryService {

    public static final String STATUS = "status";

    public static final String STATUS_KIND = "StatusKind";

    private final TransverseQueryService transverseQueryService;

    public LAQueryService() {

        this.transverseQueryService = new TransverseQueryService();

    }

    public Boolean isFunction(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return this.transverseQueryService.checkType(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTION);
        }
        return false;
    }

    public Boolean isFunctionalChain(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return this.transverseQueryService.checkType(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN);
        }
        return false;
    }

    public Boolean isFunctionPort(EObject eObject) {
        if (eObject instanceof Usage usage) {
            var parent = usage.getOwningUsage();
            return this.isExchangeItem(usage) && this.isFunction(parent);
        }
        return false;
    }

    public Boolean isFunctionalExchange(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return this.transverseQueryService.checkType(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_EXCHANGE);
        }
        return false;
    }

    public Boolean isExchangeItem(EObject eObject) {
        if (eObject instanceof Usage usage) {
            return this.transverseQueryService.checkType(usage, ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM);
        }
        return false;
    }

    public String getArcadiaElementName(EObject eObject) {
        if (eObject instanceof Usage usage) {
            return usage.getName();
        }
        return null;
    }

    public List<PartUsage> getSubComponents(EObject eObject) {
        List<Element> allPartUsage = new ArrayList<>();
        if (eObject instanceof Package pkg) {
            Package componentsPackage = this.toComponentsPackage(pkg);
            allPartUsage = componentsPackage.getMember();
        } else if (this.transverseQueryService.isComponent(eObject) && eObject instanceof PartUsage partUsage) {
            allPartUsage = partUsage.getMember();
        }
        return allPartUsage.stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .toList();
    }

    public List<PartUsage> getComponents(EObject eObject) {
        var allPartUsage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPartUsage());
        return allPartUsage.stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .toList();
    }

    /**
     * Retrieve the component that allocates the given function, if any.
     *
     * @param function
     *            the {@link ActionUsage} to check.
     * @return an {@link Optional} containing the allocating {@link PartUsage}, or empty if none.
     */
    public Optional<PartUsage> getAllocatingComponent(ActionUsage function) {
        return this.getComponents(function)
                .stream()
                .filter(component -> this.getAllocatedFunctions(component).contains(function))
                .findFirst();
    }

    public List<ActionUsage> getFunctionalChains(EObject eObject) {
        var allFlowUsage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getActionUsage());
        return allFlowUsage.stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN))
                .toList();
    }

    public List<ActionUsage> getFunctions(EObject eObject) {
        var allActionUsage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getActionUsage());
        return allActionUsage.stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION))
                .toList();
    }

    public List<ItemUsage> getExchangeItems(EObject eObject) {
        var allItemUsage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getItemUsage());
        return allItemUsage.stream()
                .filter(ItemUsage.class::isInstance)
                .map(ItemUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM))
                .toList();
    }

    public List<ActionUsage> getSubFunctions(EObject eObject) {
        List<ActionUsage> subFunctions = new ArrayList<>();
        if (eObject instanceof PartUsage partUsage) {
            subFunctions.addAll(this.getAllocatedFunctions(partUsage));
        } else if (eObject instanceof ActionUsage actionUsage) {
            subFunctions.addAll(actionUsage.getNestedAction().stream().filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION))
                    .toList());
        }
        return subFunctions;
    }

    /**
     * Retrieve the parent functions of the given function.
     *
     * @param eObject
     *            the candidate {@link EObject}.
     * @return the list of parent {@link ActionUsage} instances.
     */
    private List<ActionUsage> getParentFunctions(EObject eObject) {
        if (!(eObject instanceof ActionUsage actionUsage)) {
            return List.of();
        }

        URI targetUri = EcoreUtil.getURI(actionUsage);

        return this.getFunctions(actionUsage).stream()
                .filter(Objects::nonNull)
                .filter(candidate -> candidate != actionUsage)
                .filter(candidate -> this.getSubFunctions(candidate).stream()
                        .anyMatch(sub -> sub == actionUsage
                                || (targetUri != null && targetUri.equals(EcoreUtil.getURI(sub)))))
                .toList();
    }

    /**
     * Retrieve the first parent function of the given function, if any.
     *
     * @param eObject
     *            the candidate {@link EObject}.
     * @return an {@link Optional} containing the first parent function if found, otherwise an empty {@link Optional}.
     */
    public Optional<ActionUsage> getParentFunction(EObject eObject) {
        return this.getParentFunctions(eObject).stream().findFirst();
    }

    public List<FlowUsage> getFunctionalExchanges(EObject eObject) {
        var allFlowUsage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getFlowUsage());
        return allFlowUsage.stream()
                .filter(FlowUsage.class::isInstance)
                .map(FlowUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_EXCHANGE))
                .toList();
    }

    public List<Feature> getExchangePorts(Usage exchange) {
        return exchange.getOwnedRelationship().stream()
                .filter(EndFeatureMembership.class::isInstance)
                .map(EndFeatureMembership.class::cast)
                .flatMap(efm -> efm.getOwnedRelatedElement().stream())
                .filter(FlowEnd.class::isInstance)
                .map(FlowEnd.class::cast)
                .flatMap(flowEnd -> flowEnd.getOwnedRelationship().stream())
                .filter(EndFeatureMembership.class::isInstance)
                .map(EndFeatureMembership.class::cast)
                .flatMap(efm -> efm.getOwnedRelatedElement().stream())
                .filter(ReferenceUsage.class::isInstance)
                .map(ReferenceUsage.class::cast)
                .flatMap(referenceUsage -> referenceUsage.getOwnedRelationship().stream())
                .filter(Redefinition.class::isInstance)
                .map(Redefinition.class::cast)
                .map(Redefinition::getRedefinedFeature)
                .toList();
    }

    public Package toComponentsPackage(EObject eObject) {
        var allPackage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPackage());
        return allPackage.stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> pkg.getQualifiedName().endsWith("'Logical Architecture'" + PATH_SEPARATOR + STRUCTURE_PACKAGE))
                .findFirst()
                .orElse(null);
    }

    public Package toFunctionsPackage(EObject eObject) {
        var allPackage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPackage());
        return allPackage.stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> pkg.getQualifiedName().endsWith("'Logical Architecture'" + PATH_SEPARATOR + FUNCTIONS_PACKAGE))
                .findFirst()
                .orElse(null);
    }

    public Package toRequirementsPackage(EObject eObject) {
        var allPackage = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPackage());
        return allPackage.stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(pkg -> pkg.getQualifiedName().endsWith("'Logical Architecture'::Requirements"))
                .findFirst()
                .orElse(null);
    }

    private Predicate<? super Feature> isTypedWith(String qualifiedName) {
        return element -> element.getType().stream().anyMatch(t -> t != null && qualifiedName != null && qualifiedName.equals(t.getQualifiedName()));
    }

    public List<Feature> getFunctionPorts(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return actionUsage.getParameter().stream().toList();
        }
        return List.of();
    }
    public List<ActionUsage> getAllocatedFunctions(PartUsage partUsage) {
        return this.getPerformedActions(partUsage, this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION));
    }

    public List<ActionUsage> getPerformedActions(Usage usage, Predicate<? super ActionUsage> predicate) {
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
        Optional<ActionUsage> actionUsage = Optional.empty();
        var performedAction = performActionUsage.getPerformedAction();
        if (performActionUsage.equals(performedAction)) {
            ReferenceSubsetting referenceSubSetting = performActionUsage.getOwnedReferenceSubsetting();
            if (referenceSubSetting != null && referenceSubSetting.getReferencedFeature() != null) {
                var feature = referenceSubSetting.getReferencedFeature();
                if (!feature.getOwnedFeatureChaining().isEmpty()) {
                    EList<FeatureChaining> ownedFeatureChaining = feature.getOwnedFeatureChaining();
                    FeatureChaining lastFeatureChaining = ownedFeatureChaining.get(Math.max(0, ownedFeatureChaining.size() - 1));
                    Feature chainingFeature = lastFeatureChaining.getChainingFeature();
                    if (chainingFeature instanceof ActionUsage) {
                        actionUsage = Optional.of((ActionUsage) chainingFeature);
                    }
                }
            }
        } else {
            actionUsage = Optional.of(performedAction);
        }
        return actionUsage;
    }

    public List<FlowUsage> getInvolvedFunctionalExchanges(ActionUsage actionUsage) {
        List<Feature> features = this.transverseQueryService.getFeatureReferenceValue(actionUsage, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);
        return features.stream().filter(FlowUsage.class::isInstance).map(FlowUsage.class::cast).toList();
    }

    public List<FlowUsage> getReferencingFunctionalExchange(ActionUsage function) {
        List<FlowUsage> result = List.of();
        if (this.isFunction(function)) {
            List<FlowUsage> functionalExchanges = this.getFunctionalExchanges(function);
            result = functionalExchanges.stream()
                    .filter(functionalExchange -> functionalExchange.getTarget().contains(function))
                    .toList();
        }

        return result;
    }

    public List<FlowUsage> getReferencedFunctionalExchange(ActionUsage function) {
        List<FlowUsage> result = List.of();
        if (this.isFunction(function)) {
            List<FlowUsage> functionalExchanges = this.getFunctionalExchanges(function);
            result = functionalExchanges.stream()
                    .filter(functionalExchange -> functionalExchange.getSource().contains(function))
                    .toList();
        }

        return result;
    }

    public List<ActionUsage> getFunctionalChainsImpliedIn(FlowUsage functionalExchange) {
        List<ActionUsage> functionalChains = this.getFunctionalChains(functionalExchange);
        return functionalChains.stream().filter(fc -> {
            return this.getInvolvedFunctionalExchanges(fc).contains(functionalExchange);
        }).toList();
    }

    public List<ActionUsage> getFunctionalChainsImpliedIn(ActionUsage function) {
        List<ActionUsage> functionalChains = this.getFunctionalChains(function);
        return functionalChains.stream().filter(fc -> {
            return this.getInvolvedFunctions(fc).contains(function);
        }).toList();
    }

    public List<ActionUsage> getInvolvedFunctions(ActionUsage actionUsage) {
        Set<ActionUsage> involvedFunctions = new HashSet<>();
        this.getInvolvedFunctionalExchanges(actionUsage).forEach(flowUsage -> {
            Optional.ofNullable(flowUsage.getSourceFeature())
                    .filter(ActionUsage.class::isInstance)
                    .map(ActionUsage.class::cast)
                    .ifPresent(involvedFunctions::add);
            flowUsage.getTargetFeature().stream()
                    .filter(ActionUsage.class::isInstance)
                    .map(ActionUsage.class::cast)
                    .forEach(involvedFunctions::add);
        });
        return List.copyOf(involvedFunctions);
    }

    public List<ActionUsage> getFunctionalChainsInvolvingFunction(ActionUsage function) {
        List<ActionUsage> functionalChains = this.getFunctionalChains(function);
        return functionalChains.stream()
                .filter(functionalChain -> this.getInvolvedFunctions(functionalChain).contains(function))
                .toList();
    }

    /**
     * Retrieve the status element associated with the given feature.
     *
     * @param feature
     *            the {@link Feature} to inspect.
     * @return the status {@link Element} if found, otherwise null.
     */
    public Element getStatus(Feature feature) {
        return feature.getOwnedElement().stream()
            .filter(MetadataUsage.class::isInstance)
            .map(MetadataUsage.class::cast)
            .filter(this.transverseQueryService::isStatusInfo)
            .flatMap(metadataUsage -> this.transverseQueryService.getFeatureReferenceValue(metadataUsage, STATUS).stream())
            .findFirst()
            .orElse(null);
    }

    public String getStatusStringValue(Usage usage) {
        var element = this.getStatus(usage);
        return Optional.ofNullable(element).map(Element::getDeclaredName).orElse("");
    }

    public List<EnumerationUsage> getStatusKindEnum(IEditingContext editingContext) {
        var resourceSet = ((EditingContext) editingContext).getDomain().getResourceSet();
        var statusKindEnum = resourceSet.getResources().stream()
                .flatMap(res -> {
                    Iterable<EObject> iterable = () -> EcoreUtil.getAllContents(res, true);
                    return StreamSupport.stream(iterable.spliterator(), false);
                })
                .filter(obj -> obj instanceof org.eclipse.syson.sysml.EnumerationDefinition)
                .map(org.eclipse.syson.sysml.EnumerationDefinition.class::cast)
                .filter(enumDef -> STATUS_KIND.equals(enumDef.getDeclaredName())).findFirst();

        return statusKindEnum.get()
                .getOwnedRelationship()
                .stream()
                .filter(relationship -> relationship instanceof VariantMembership)
                .map(VariantMembership.class::cast)
                .flatMap(variantMembership -> variantMembership.getOwnedRelatedElement().stream())
                .filter(EnumerationUsage.class::isInstance)
                .map(EnumerationUsage.class::cast)
                .toList();
    }

    public List<String> getStatusKindEnumLiterals(IEditingContext editingContext) {

        return this.getStatusKindEnum(editingContext)
                .stream()
                .map(EnumerationUsage::getDeclaredName)
                .toList();
    }

    public List<FlowUsage> getRelatedFunctionalExchanges(EObject self) {
        if (self instanceof ActionUsage actionUsage && this.isFunction(actionUsage)) {
            var referencingFunctionalExchanges = this.getReferencingFunctionalExchange(actionUsage)
                    .stream();

            var referencedFunctionalExchanges = this.getReferencedFunctionalExchange(actionUsage)
                    .stream();

            return Stream.concat(referencingFunctionalExchanges, referencedFunctionalExchanges)
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    public List<ActionUsage> getReferencedAndReferencingFunctions(EObject self) {
        if (self instanceof ActionUsage actionUsage && this.isFunction(actionUsage)) {
            var referencingFunctions = this.getReferencingFunctionalExchange(actionUsage)
                    .stream()
                    .map(FlowUsage::getSource)
                    .flatMap(List::stream);

            var referencedFunctions = this.getReferencedFunctionalExchange(actionUsage)
                    .stream()
                    .map(FlowUsage::getTarget)
                    .flatMap(List::stream);

            return Stream.concat(referencedFunctions, referencingFunctions)
                    .filter(ActionUsage.class::isInstance)
                    .map(ActionUsage.class::cast)
                    .distinct()
                    .toList();
        }
        return List.of();
    }

    public Object getFunctionalExchangeSource(EObject self) {
        if (self instanceof FlowUsage flowUsage) {
            return flowUsage.getSource().stream().findFirst().orElse(null);
        }
        return null;
    }

    public Object getFunctionalExchangeTarget(EObject self) {
        if (self instanceof FlowUsage flowUsage) {
            return flowUsage.getTarget().stream().findFirst().orElse(null);
        }
        return null;
    }
}

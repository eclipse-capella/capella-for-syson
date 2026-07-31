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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.Flow;
import org.eclipse.syson.sysml.FlowEnd;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PayloadFeature;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import static org.eclipse.capella.model.services.logical.architecture.LAQueryService.STATUS;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.MODELING_METADATA_STATUS_INFO;

/**
 * Logical Architecture (LA) related mutation service. It is important to note that this service must retain its empty
 * constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class LAMutationService {


    private final TransverseMutationService transverseMutationService;

    private final DeleteService deleteService;

    private final TransverseQueryService transverseQueryService;

    private final LAQueryService laQueryService;

    private final LibraryServices libraryService;

    private final MetamodelMutationElementService metamodelMutationElementService;

    public LAMutationService() {
        this.transverseMutationService = new TransverseMutationService();
        this.deleteService = new DeleteService();
        this.transverseQueryService = new TransverseQueryService();
        this.laQueryService = new LAQueryService();
        this.libraryService = new LibraryServices();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
    }

    public Usage setBooleanAttribute(Usage usage, boolean newValue, String prefix, String attributeName) {
        this.transverseMutationService.setBooleanAttribute(usage, prefix, attributeName, newValue);
        return usage;
    }

    // partly copied from SysON org.eclipse.syson.diagram.common.view.services.ViewCreateService.createPerform(Element)
    public Usage setPerformedActionUsage(PartUsage usage, ActionUsage function) {
        this.createPerformActionUsage(usage, function);
        return usage;
    }

    public void deletePerformedActionUsage(PartUsage usage, ActionUsage actionUsage) {
        this.getPerformActionUsage(usage, actionUsage::equals)
                .forEach(this.deleteService::deleteFromModel);
    }

    private List<? extends ActionUsage> getPerformActionUsage(PartUsage partUsage, Predicate<? super ActionUsage> predicate) {
        return partUsage.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .filter(performActionUsage -> predicate.test(this.laQueryService.getPerformedAction(performActionUsage).orElse(null)))
                .toList();
    }

    // partly copied from SysON org.eclipse.syson.diagram.common.view.services.ViewCreateService.createPerform(Element)
    // Used if the new value is a list of function to allocate
    public Usage setPerformedActionUsage(PartUsage usage, List<ActionUsage> functions) {
        // We first delete existing allocated function
        if (functions.size() > 1) {
            this.getPerformActionUsage(usage, performAction -> performAction != null && this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION).test(performAction))
                    .forEach(this.deleteService::deleteFromModel);
        }

        functions.forEach(function -> {
            this.createPerformActionUsage(usage, function);
        });
        return usage;
    }

    public Usage deleteFunction(ActionUsage function) {
        Set<FlowUsage> relatedElements = new HashSet<>();
        List<Feature> functionPorts = this.laQueryService.getFunctionPorts(function);
        functionPorts.forEach(port -> this.collectRelatedFunctionalExchanges(port, relatedElements));
        relatedElements.forEach(this.deleteService::deleteFromModel);
        this.deleteService.deleteFromModel(function);
        return function;
    }

    private void collectRelatedFunctionalExchanges(EObject eObject, Set<FlowUsage> relatedElements) {
        var optAdapter = eObject.eAdapters().stream().filter(ECrossReferenceAdapter.class::isInstance).map(ECrossReferenceAdapter.class::cast).findFirst();
        if (optAdapter.isPresent()) {
            ECrossReferenceAdapter referenceAdapter = optAdapter.get();
            Collection<Setting> inverseReferences = referenceAdapter.getInverseReferences(eObject);
            for (Setting setting : inverseReferences) {
                EObject relatedElement = setting.getEObject();
                if (relatedElement instanceof Redefinition redefinition) {
                    Optional.of(redefinition)
                            .map(Redefinition::getRedefiningFeature)
                            .map(Feature::getOwner)
                            .filter(FlowEnd.class::isInstance)
                            .map(FlowEnd.class::cast)
                            .map(Feature::getOwner)
                            .filter(FlowUsage.class::isInstance)
                            .map(FlowUsage.class::cast)
                            .filter(this.laQueryService::isFunctionalExchange)
                            .ifPresent(relatedElements::add);
                }
            }
        }
    }

    private void createPerformActionUsage(PartUsage usage, ActionUsage function) {
        // create the perform action
        var featureMember = this.createMembership(usage);
        usage.getOwnedRelationship().add(featureMember);
        var perform = SysmlFactory.eINSTANCE.createPerformActionUsage();
        featureMember.getOwnedRelatedElement().add(perform);

        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(function);
        perform.getOwnedRelationship().add(referenceSubsetting);
    }

    /**
     * Set a reference defined in the Arcadia library.
     *
     * @param usage
     *         the current Usage owining the reference.
     * @param prefix
     *         the reference name prefix (the containing namespace).
     * @param referenceName
     *         the reference name.
     * @param newValue
     *         the reference new value (a list of object or a single value).
     * @param referenceUsageType
     *         the type of the referenced usage: (ItemUsage, FlowUsage etc.)
     * @return the current usage for convenience.
     */
    public Usage setArcadiaReferenceFeature(Usage usage, String prefix, String referenceName, Object newValue, String referenceUsageType) {
        var optionalType = Optional.ofNullable(SysmlPackage.eINSTANCE.getEClassifier(referenceUsageType)).filter(EClass.class::isInstance).map(EClass.class::cast);
        if (optionalType.isPresent()) {
            var type = optionalType.get();
            if (newValue instanceof List<?> newValues) {
                // Sirius Web provides a list with one element if zero or one element is already set. If two elements are
                // set, Sirius web provide the new feature values list.
                List<Feature> features = newValues.stream().filter(Feature.class::isInstance).map(Feature.class::cast).toList();
                if (features.size() > 1) {
                    this.transverseMutationService.setFeatureReferenceValues(usage, prefix, referenceName, features, type);
                } else {
                    features.forEach(feature -> this.transverseMutationService.addFeatureReferenceValue(usage, prefix, referenceName, feature, type));
                }

            } else if (newValue instanceof Feature feature) {
                this.transverseMutationService.addFeatureReferenceValue(usage, prefix, referenceName, feature, type);
            }
        }
        return usage;
    }

    /**
     * Copied from org.eclipse.syson.diagram.common.view.services.ViewCreateService.createMembership(Element).
     *
     * @param element
     *         the given {@link Element}.
     * @return the newly created {@link Membership}.
     */
    public Membership createMembership(Element element) {
        Membership membership = null;
        if (element instanceof Package) {
            membership = SysmlFactory.eINSTANCE.createOwningMembership();
        } else {
            membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        }
        element.getOwnedRelationship().add(membership);
        return membership;
    }

    public FlowUsage setFunctionalExchangePayload(FlowUsage flowUsage, Object newValue) {
        if (newValue instanceof List<?> newValues) {
            List<ItemUsage> exchangeItems = newValues.stream()
                    .filter(ItemUsage.class::isInstance)
                    .map(ItemUsage.class::cast)
                    .toList();
            // Sirius Web provides a list with one element if zero or one element is already set. If two elements are
            // set, Sirius web provide the new feature values list.
            if (exchangeItems.size() > 1) {
                this.setExchangeItem(flowUsage, exchangeItems);
            } else {
                exchangeItems.forEach(exchangeItem -> this.addNewExchangeItem(flowUsage, exchangeItem));
            }

        } else if (newValue instanceof ItemUsage exchangeItem) {
            this.addNewExchangeItem(flowUsage, exchangeItem);
        }

        return flowUsage;
    }

    private void setExchangeItem(FlowUsage flowUsage, List<ItemUsage> exchangeItems) {
        Optional.ofNullable(flowUsage.getPayloadFeature()).ifPresent(this.deleteService::deleteFromModel);
        exchangeItems.forEach(exchangeItem -> this.addNewExchangeItem(flowUsage, exchangeItem));
    }

    private void addNewExchangeItem(FlowUsage flowUsage, ItemUsage exchangeItem) {
        PayloadFeature payloadFeature = Optional.ofNullable(flowUsage.getPayloadFeature()).orElseGet(() -> this.createPayloadFeature(flowUsage));
        FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        featureTyping.setSpecific(payloadFeature);
        payloadFeature.getOwnedRelationship().add(featureTyping);
        featureTyping.setType(exchangeItem);
    }

    public PayloadFeature createPayloadFeature(Flow flow) {
        PayloadFeature payloadFeature = SysmlFactory.eINSTANCE.createPayloadFeature();
        this.metamodelMutationElementService.addChildInParent(flow, payloadFeature);
        return payloadFeature;
    }

    public Feature setStatusKind(Feature feature, Object newValue,
            IEditingContext editingContext) {
        this.unSetUsageStatusKind(feature);
        if (newValue != null) {
            this.laQueryService.getStatusKindEnum(editingContext).stream()
                .filter(Objects::nonNull)
                .filter(statusKind -> newValue.equals(statusKind.getDeclaredName()))
                .findFirst()
                .ifPresent(newStatusEnumElt -> {
                    var metaDataUsage = SysmlFactory.eINSTANCE.createMetadataUsage();
                    this.metamodelMutationElementService.addChildInParent(feature, metaDataUsage);
                    this.libraryService.typeWithLibrary(metaDataUsage, MODELING_METADATA_STATUS_INFO, SysmlPackage.eINSTANCE.getMetadataDefinition());
                    this.transverseMutationService.setFeatureReferenceValues(metaDataUsage, MODELING_METADATA_STATUS_INFO, STATUS, List.of(newStatusEnumElt), SysmlPackage.eINSTANCE.getAttributeUsage());
                });
        }
        return feature;
    }

    public void unSetUsageStatusKind(Feature feature) {
        feature.getOwnedElement().stream()
            .filter(MetadataUsage.class::isInstance)
            .map(MetadataUsage.class::cast)
            .filter(this.transverseQueryService::isStatusInfo)
            .findFirst()
            .ifPresent(this.deleteService::deleteFromModel);
    }

    // For now, the source and target port are read-only
//    public Feature setComponentExchangeEnd(InterfaceUsage componentExchange, boolean isSource, Feature portFeature) {
//
//        // Copied from org.eclipse.syson.diagram.common.view.services.ViewCreateService.createInterfaceUsage(PortUsage,
//        // PortUsage)
//        int index = 0;
//        if(!isSource && !componentExchange.getOwnedRelationship().isEmpty()) {
//            index = 1;
//        }
//        EndFeatureMembership endFeatureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
//        componentExchange.getOwnedRelationship().add(index, endFeatureMembership);
//        Feature endFeature = SysmlFactory.eINSTANCE.createFeature();
//        endFeature.setIsEnd(true);
//        endFeatureMembership.getOwnedRelatedElement().add(endFeature);
//        this.elementInitializerSwitch.doSwitch(endFeature);
//        ReferenceSubsetting sourceReferenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
//        endFeature.getOwnedRelationship().add(sourceReferenceSubsetting);
//        this.elementInitializerSwitch.doSwitch(sourceReferenceSubsetting);
//        sourceReferenceSubsetting.setReferencedFeature(portFeature);
//        return portFeature;
//    }
}

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

import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_CAPABILITY;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_DESCRIPTION;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_INVOLVED_COMPONENTS;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.MODELING_METADATA_STATUS_INFO;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.PATH_SEPARATOR;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Documentation;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.ParameterMembership;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PayloadFeature;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.Subsetting;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common semantic element update service.
 *
 * @author gdaniel
 */
public class CommonUpdateService {

    // We depend on this service only for some SysML business usage. It should be located in another SysON common
    // service since it does not depend on the view.
    // @technical-debt
    private final UtilService utilService;

    private final TransverseQueryService transverseQueryService;

    private final ArcadiaLibraryServices arcadiaLibraryServices;

    private final MetamodelMutationElementService metamodelMutationElementService;

    private final CommonDeletionService commonDeletionService;

    private final Logger logger = LoggerFactory.getLogger(CommonUpdateService.class);

    public CommonUpdateService() {
        this.utilService = new UtilService();
        this.transverseQueryService = new TransverseQueryService();
        this.arcadiaLibraryServices = new ArcadiaLibraryServices();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.commonDeletionService = new CommonDeletionService();
    }

    public Element setElementDescription(Element element, String newDescription) {
        var descriptionDoc = element.getDocumentation().stream()
                .filter(documentation -> ARCADIA_DESCRIPTION.equals(documentation.getDeclaredName()))
                .findFirst()
                .orElseGet(() -> {
                    Documentation documentation = SysmlFactory.eINSTANCE.createDocumentation();
                    documentation.setDeclaredName(ARCADIA_DESCRIPTION);
                    this.metamodelMutationElementService.addChildInParent(element, documentation);
                    return documentation;
                });
        descriptionDoc.setBody(newDescription);
        return element;
    }

    public Feature setCapabilityGeneralisationSource(Subsetting generalization, Usage oldCapability, Usage newCapability) {
        Usage capability;
        if (!this.transverseQueryService.isCapability(newCapability)) {
            capability = oldCapability;
        } else if (this.transverseQueryService.getGeneralizationReferenceValue(newCapability).contains(generalization.getSubsettedFeature())) {
            this.logger.atWarn()
                    .setMessage("Cannot reconnect capability generalization source because it would create a duplicate link")
                    .addKeyValue("generalizationId", generalization.getElementId())
                    .addKeyValue("oldCapabilityId", oldCapability.getElementId())
                    .addKeyValue("newCapabilityId", newCapability.getElementId())
                    .log();
            capability = oldCapability;
        } else {
            capability = newCapability;
            generalization.setSubsettingFeature(newCapability);
            generalization.setSpecific(newCapability);
            newCapability.getOwnedRelationship().add(generalization);
        }

        return capability;
    }

    public Feature setCapabilityGeneralisationTarget(Subsetting generalization, Usage oldCapability, Usage newCapability) {
        Usage capability;
        if (!this.transverseQueryService.isCapability(newCapability)) {
            capability = oldCapability;
        } else if (this.transverseQueryService.getGeneralizationReferenceValue(generalization.getSubsettingFeature()).contains(newCapability)) {
            this.logger.atWarn()
                    .setMessage("Cannot reconnect capability generalization target because it would create a duplicate link")
                    .addKeyValue("generalizationId", generalization.getElementId())
                    .addKeyValue("oldCapabilityId", oldCapability.getElementId())
                    .addKeyValue("newCapabilityId", newCapability.getElementId())
                    .log();
            capability = oldCapability;
        } else {
            capability = newCapability;
            generalization.setSubsettedFeature(newCapability);
            generalization.setGeneral(newCapability);
        }
        return capability;
    }

    public Usage setCapabilityInvolvementTarget(Usage capability, PartUsage oldComponent, PartUsage newComponent) {
        if (this.transverseQueryService.getFeatureReferenceValue(capability, ARCADIA_INVOLVED_COMPONENTS).contains(newComponent)) {
            this.logger.atWarn()
                    .setMessage("Cannot reconnect capability involvement target because it would create a duplicate link")
                    .addKeyValue("capabilityId", capability.getElementId())
                    .addKeyValue("oldComponentId", oldComponent.getElementId())
                    .addKeyValue("newComponentId", newComponent.getElementId())
                    .log();
            return capability;
        }
        this.deleteFeaturesFromReference(capability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, SysmlPackage.eINSTANCE.getPartUsage(), List.of(oldComponent));
        this.addFeatureReferenceValue(capability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, newComponent, SysmlPackage.eINSTANCE.getPartUsage());
        return capability;
    }

    public Usage setCapabilityInvolvementSource(Usage oldCapability, Usage newCapability, PartUsage component) {
        if (this.transverseQueryService.getFeatureReferenceValue(newCapability, ARCADIA_INVOLVED_COMPONENTS).contains(component)) {
            this.logger.atWarn()
                    .setMessage("Cannot reconnect capability involvement source because it would create a duplicate link")
                    .addKeyValue("oldCapabilityId", oldCapability.getElementId())
                    .addKeyValue("newCapabilityId", newCapability.getElementId())
                    .addKeyValue("componentId", component.getElementId())
                    .log();
            return oldCapability;
        }
        this.deleteFeaturesFromReference(oldCapability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, SysmlPackage.eINSTANCE.getPartUsage(), List.of(component));
        if (this.transverseQueryService.getInvolvedComponents(oldCapability).isEmpty()) {
            this.deleteReference(oldCapability, ARCADIA_INVOLVED_COMPONENTS);
        }
        this.addFeatureReferenceValue(newCapability, ARCADIA_PREFIX + ARCADIA_CAPABILITY,
                ARCADIA_INVOLVED_COMPONENTS, component, SysmlPackage.eINSTANCE.getPartUsage());
        return newCapability;
    }

    public Usage setBooleanAttribute(Usage usage, String prefix, String attributeName, boolean newValue) {
        Optional<LiteralBoolean> optionalExitingValue = this.transverseQueryService.getFeatureReferenceExpression(usage, attributeName)
                .filter(LiteralBoolean.class::isInstance)
                .map(LiteralBoolean.class::cast);

        // If the value is already set, we retrieve it to set the new value.
        var descriptionValue = optionalExitingValue.orElseGet(() -> this.createLiteralBooleanAttribute(usage, prefix, attributeName));
        descriptionValue.setValue(newValue);
        return usage;
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
                    this.setFeatureReferenceValues(usage, prefix, referenceName, features, type);
                } else {
                    features.forEach(feature -> this.addFeatureReferenceValue(usage, prefix, referenceName, feature, type));
                }

            } else if (newValue instanceof Feature feature) {
                this.addFeatureReferenceValue(usage, prefix, referenceName, feature, type);
            }
        }
        return usage;
    }

    public void deleteFeaturesFromReference(Usage usage, String prefix, String attributeName, EClass referencedFeatureType, List<Feature> features) {
        var newValues = new ArrayList<>(this.transverseQueryService.getFeatureReferenceValue(usage, attributeName));
        features.forEach(newValues::remove);
        this.setFeatureReferenceValues(usage, prefix, attributeName, newValues, referencedFeatureType);
    }

    private Optional<Usage> retrieveUsageFromReferenceName(Usage usage, String referenceName) {
        return usage.getNestedUsage().stream()
                .filter(nestedUsage -> referenceName.equals(nestedUsage.getName()))
                .findFirst();
    }

    public void deleteReference(Usage usage, String referenceName) {
        this.retrieveUsageFromReferenceName(usage, referenceName)
                .ifPresent(this.commonDeletionService::delete);
    }

    public void setFeatureReferenceValues(Usage usage, String libraryPrefix, String attributeName, List<Feature> newValues, EClass referencedFeatureType) {
        this.deleteReference(usage, attributeName);
        this.createFeatureReference(usage, libraryPrefix, attributeName, newValues, referencedFeatureType);
    }

    public void addFeatureReferenceValue(Usage usage, String libraryPrefix, String attributeName, Feature newValue, EClass referencedFeatureType) {
        List<Feature> newValues = new ArrayList<>(this.transverseQueryService.getFeatureReferenceValue(usage, attributeName));
        newValues.add(newValue);
        this.deleteReference(usage, attributeName);
        this.createFeatureReference(usage, libraryPrefix, attributeName, newValues, referencedFeatureType);
    }

    private LiteralBoolean createLiteralBooleanAttribute(Usage usage, String prefix, String attributeName) {
        LiteralBoolean literalBoolean = SysmlFactory.eINSTANCE.createLiteralBoolean();
        this.redefineFeature(usage, prefix, attributeName, List.of(literalBoolean), SysmlPackage.eINSTANCE.getAttributeUsage());
        return literalBoolean;
    }

    private void redefineFeature(Usage usage, String prefix, String attributeName, List<Expression> values, EClass referencedFeatureType) {
        String libraryFeatureAbsolutePath = prefix + PATH_SEPARATOR + attributeName;

        // Step 1 : We try to retrieve the Reference Usage if it is already defined.
        Optional<ReferenceUsage> optionalReferenceUsage = usage.getNestedReference().stream().filter(referenceUsage -> attributeName.equals(referenceUsage.getName())).findFirst()
                .or(() -> this.createReferenceUsage(usage, referencedFeatureType, libraryFeatureAbsolutePath));

        // Step 2 : for each values, we create the Feature Value and add it in the reference usage relationships.
        if (optionalReferenceUsage.isPresent()) {

            //For multivalued (multi reference expression, we add them in an operator expression.
            if (values.size() > 1) {
                var operatorExpression = SysmlFactory.eINSTANCE.createOperatorExpression();
                var featureValue = SysmlFactory.eINSTANCE.createFeatureValue();
                featureValue.getOwnedRelatedElement().add(operatorExpression);
                optionalReferenceUsage.get().getOwnedRelationship().add(featureValue);
                for (Expression value : values) {
                    var parameter = this.createParameter(value);
                    operatorExpression.getOwnedRelationship().add(parameter);
                }
            } else if (!values.isEmpty()) {
                var featureValue = SysmlFactory.eINSTANCE.createFeatureValue();
                featureValue.getOwnedRelatedElement().add(values.get(0));
                optionalReferenceUsage.get().getOwnedRelationship().add(featureValue);
            }

        }
    }

    private ParameterMembership createParameter(Expression value) {
        ParameterMembership parameterMembership = SysmlFactory.eINSTANCE.createParameterMembership();
        Feature feature = SysmlFactory.eINSTANCE.createFeature();
        feature.setDirection(FeatureDirectionKind.IN);
        parameterMembership.getOwnedRelatedElement().add(feature);
        FeatureValue featureValue = SysmlFactory.eINSTANCE.createFeatureValue();
        feature.getOwnedRelationship().add(featureValue);
        featureValue.getOwnedRelatedElement().add(value);
        return parameterMembership;
    }

    private Optional<ReferenceUsage> createReferenceUsage(Usage usage, EClass referencedFeatureType, String libraryFeatureAbsolutePath) {

        // Step 1 : We retrieve the Usage defined in the library.
        Optional<Usage> optionalUsage = this.utilService.getAllReachable(usage, referencedFeatureType).stream().filter(Usage.class::isInstance).map(Usage.class::cast)
                .filter(currentUsage -> libraryFeatureAbsolutePath.equals(currentUsage.getQualifiedName())).findFirst();
        if (optionalUsage.isPresent()) {

            // Step 2 : we create the Reference Usage
            var featureMembership = SysmlFactory.eINSTANCE.createFeatureMembership();
            usage.getOwnedRelationship().add(featureMembership);
            ReferenceUsage referenceUsage = SysmlFactory.eINSTANCE.createReferenceUsage();
            featureMembership.getOwnedRelatedElement().add(referenceUsage);

            // Step 3 : We set the redefinition referencing the library usage (ref usage or attribute usage).
            Redefinition redefinition = SysmlFactory.eINSTANCE.createRedefinition();
            redefinition.setRedefinedFeature(optionalUsage.get());
            redefinition.setRedefiningFeature(referenceUsage);
            referenceUsage.getOwnedRelationship().add(redefinition);
            return Optional.of(referenceUsage);
        }
        return Optional.empty();
    }

    /**
     * Redefine a multi-valued reference attribute from a library in a given usage.
     *
     * @param usage
     *         The usage where the redefinition will be applied
     * @param targets
     *         The list of target elements you want to reference
     */
    private void createFeatureReference(Usage usage, String libraryPrefix, String attributeName, List<?> targets, EClass referencedFeatureType) {
        List<Expression> values = new ArrayList<>();
        for (Object target : targets) {
            if (target instanceof Feature targetFeature) {
                FeatureReferenceExpression featureReferenceExpression = SysmlFactory.eINSTANCE.createFeatureReferenceExpression();
                var membership = SysmlFactory.eINSTANCE.createMembership();
                featureReferenceExpression.getOwnedRelationship().add(membership);
                membership.setMemberElement(targetFeature);
                values.add(featureReferenceExpression);
            }
        }
        this.redefineFeature(usage, libraryPrefix, attributeName, values, referencedFeatureType);
    }

    public Feature setFeatureDirection(Feature feature, Object newValue) {
        String literalValue = newValue.toString();
        if (newValue instanceof EEnumLiteral newValEnumLiteral) {
            literalValue = newValEnumLiteral.getLiteral();
        }
        FeatureDirectionKind direction = FeatureDirectionKind.get(literalValue);
        if (direction != null) {
            feature.setDirection(direction);
        } else {
            feature.unsetDirection();
        }
        return feature;
    }

    public Element setPerformAction(Element ownerElement, ActionUsage function) {
        PerformActionUsage performActionUsage = this.createPerformAction(ownerElement);

        var referenceSubsetting = SysmlFactory.eINSTANCE.createReferenceSubsetting();
        referenceSubsetting.setReferencedFeature(function);
        performActionUsage.getOwnedRelationship().add(referenceSubsetting);

        return ownerElement;
    }

    /**
     * Creates a perform action in the provided {@code ownerElement}.
     *
     * @param ownerElement
     *         the element containing the perform action
     * @return the perform action
     * @technical-debt this method is copied from org.eclipse.syson.diagram.services.DiagramMutationElementService, since the SysON implementation cannot be reused in this class (it requires a
     *         Spring context we don't want to have in this service)
     */
    private PerformActionUsage createPerformAction(Element ownerElement) {
        Membership featureMember = this.metamodelMutationElementService.createMembership(ownerElement);
        ownerElement.getOwnedRelationship().add(featureMember);
        PerformActionUsage performAction = SysmlFactory.eINSTANCE.createPerformActionUsage();
        featureMember.getOwnedRelatedElement().add(performAction);
        return performAction;
    }

    /**
     * Sets the status kind of the provided {@code feature} to {@code newValue}.
     * <p>
     * This method expects the provided {@code feature} to be part of a {@link org.eclipse.emf.ecore.resource.ResourceSet} containing the Arcadia library. If it is not the case then the status kind
     * won't be set.
     *
     * @param feature
     *         the feature to set
     * @param newValue
     *         the status kind value to set
     * @return the updated feature
     */
    public Feature setStatusKind(Feature feature, String newValue) {
        this.unSetUsageStatusKind(feature);
        if (newValue != null) {
            this.transverseQueryService.getStatusKindEnum(feature).stream()
                    .filter(Objects::nonNull)
                    .filter(statusKind -> newValue.equals(statusKind.getDeclaredName()))
                    .findFirst()
                    .ifPresent(newStatusEnumElt -> {
                        var metaDataUsage = SysmlFactory.eINSTANCE.createMetadataUsage();
                        this.metamodelMutationElementService.addChildInParent(feature, metaDataUsage);
                        this.arcadiaLibraryServices.typeWithLibrary(metaDataUsage, MODELING_METADATA_STATUS_INFO, SysmlPackage.eINSTANCE.getMetadataDefinition());
                        this.setFeatureReferenceValues(metaDataUsage, MODELING_METADATA_STATUS_INFO, STATUS, List.of(newStatusEnumElt),
                                SysmlPackage.eINSTANCE.getAttributeUsage());
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
                .ifPresent(this.commonDeletionService::delete);
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
        Optional.ofNullable(flowUsage.getPayloadFeature()).ifPresent(this.commonDeletionService::delete);
        exchangeItems.forEach(exchangeItem -> this.addNewExchangeItem(flowUsage, exchangeItem));
    }

    private void addNewExchangeItem(FlowUsage flowUsage, ItemUsage exchangeItem) {
        PayloadFeature payloadFeature = Optional.ofNullable(flowUsage.getPayloadFeature()).orElseGet(() -> {
            PayloadFeature newPayloadFeature = SysmlFactory.eINSTANCE.createPayloadFeature();
            this.metamodelMutationElementService.addChildInParent(flowUsage, newPayloadFeature);
            return newPayloadFeature;
        });
        FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        featureTyping.setSpecific(payloadFeature);
        payloadFeature.getOwnedRelationship().add(featureTyping);
        featureTyping.setType(exchangeItem);
    }
}

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

import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_DESCRIPTION;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTIONAL_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.MODELING_METADATA_STATUS_INFO;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.PATH_SEPARATOR;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Documentation;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.Flow;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.OccurrenceUsage;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.ParameterMembership;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PayloadFeature;
import org.eclipse.syson.sysml.PerformActionUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transverse mutation service. It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class TransverseMutationService {

    private static final String WHITE_SPACE = " ";

    private final Logger logger = LoggerFactory.getLogger(TransverseMutationService.class);

    // We depends on this service only for some SysML business usage. It should be located in another syson common
    // services since it does not depend on the view.
    // @technical-debt

    private final UtilService utilService;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final ArcadiaLibraryServices arcadiaLibraryServices;

    private final MetamodelMutationElementService metamodelMutationElementService;

    private final CapellaDeleteService capellaDeleteService;

    public TransverseMutationService() {
        this.utilService = new UtilService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.transverseQueryService = new TransverseQueryService();
        this.arcadiaLibraryServices = new ArcadiaLibraryServices();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.capellaDeleteService = new CapellaDeleteService();

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
                .ifPresent(this::delete);
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
            }
            else if (!values.isEmpty()) {
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

    public ItemUsage createNewExchangeItem(Element parent) {
        ItemUsage itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        this.metamodelMutationElementService.addChildInParent(parent, itemUsage);
        this.arcadiaLibraryServices.typeWithExchangeItem(itemUsage);
        this.elementInitializerSwitch.doSwitch(itemUsage);
        itemUsage.setDeclaredName(ARCADIA_EXCHANGE_ITEM + this.transverseQueryService.existingElementsCount(itemUsage));

        return itemUsage;
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

    public Element delete(Element element) {
        return this.capellaDeleteService.deleteFromModel(element);
    }

    public RequirementUsage createRequirement(Element parent) {
        RequirementUsage requirementUsage = null;
        Optional<Package> optionalRequirementsPackage = this.transverseQueryService.getRequirementsPackage(parent);
        if (optionalRequirementsPackage.isPresent()) {
            String name = "Requirement";
            requirementUsage = SysmlFactory.eINSTANCE.createRequirementUsage();

            this.metamodelMutationElementService.addChildInParent(optionalRequirementsPackage.get(), requirementUsage);
            this.elementInitializerSwitch.doSwitch(requirementUsage);
            // Use native SysML v2 RequirementUsage without Arcadia typing

            long existingElementsCount = this.transverseQueryService.existingElementsCount(requirementUsage);
            requirementUsage.setDeclaredName(name + WHITE_SPACE + existingElementsCount);
        }
        return requirementUsage;
    }

    public PartUsage createComponent(Element parent) {
        PartUsage partUsage = null;
        Optional<Element> optionalTargetContainer = Optional.of(parent);
        if (!this.transverseQueryService.isComponent(parent)) {
            optionalTargetContainer = this.transverseQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (optionalTargetContainer.isPresent()) {
            Element targetContainer = optionalTargetContainer.get();
            partUsage = SysmlFactory.eINSTANCE.createPartUsage();
            this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
            this.elementInitializerSwitch.doSwitch(partUsage);
            this.arcadiaLibraryServices.typeWithArcadiaComponent(partUsage);
            long existingElementsCount = this.transverseQueryService.existingElementsCount(partUsage);
            partUsage.setDeclaredName("C" + WHITE_SPACE + existingElementsCount);
        }
        return partUsage;
    }

    private PortUsage getOrCreateComponentPort(Feature feature, FeatureDirectionKind direction) {
        PortUsage result = null;
        if (this.transverseQueryService.isComponent(feature)) {
            result = this.createComponentPort((PartUsage) feature, direction);
        } else if (this.transverseQueryService.isComponentPort(feature)) {
            result = (PortUsage) feature;
        }
        return result;
    }

    public PortUsage createComponentPort(PartUsage container, FeatureDirectionKind direction) {
        container.unsetDirection();
        PortUsage portUsage = SysmlFactory.eINSTANCE.createPortUsage();
        portUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, portUsage);
        this.elementInitializerSwitch.doSwitch(portUsage);
        this.arcadiaLibraryServices.typeWithArcadiaComponentPort(portUsage);
        portUsage.setDeclaredName("CP " + this.transverseQueryService.existingElementsCount(portUsage));
        return portUsage;
    }

    public PartUsage createActor(Element parent) {
        PartUsage partUsage = null;
        Optional<Element> optionalTargetContainer = Optional.of(parent);
        if (!this.transverseQueryService.isComponent(parent)) {
            optionalTargetContainer = this.transverseQueryService.getStructurePackage(parent)
                    .map(Element.class::cast);
        }
        if (optionalTargetContainer.isPresent()) {
            Element targetContainer = optionalTargetContainer.get();
            partUsage = SysmlFactory.eINSTANCE.createPartUsage();
            this.metamodelMutationElementService.addChildInParent(targetContainer, partUsage);
            this.setBooleanAttribute(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT, ARCADIA_IS_ACTOR, true);
            this.elementInitializerSwitch.doSwitch(partUsage);
            this.arcadiaLibraryServices.typeWithArcadiaComponent(partUsage);
            long existingElementsCount = this.transverseQueryService.existingElementsCount(partUsage);
            partUsage.setDeclaredName("A" + WHITE_SPACE + existingElementsCount);
        }
        return partUsage;

    }

    public ActionUsage createFunction(Element parent) {
        ActionUsage actionUsage = null;
        Optional<Element> optionalParent = Optional.ofNullable(parent)
                .filter(this.transverseQueryService::isFunction)
                .or(() -> this.transverseQueryService.getRootFunction(parent));
        if (optionalParent.isPresent()) {
            actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
            this.metamodelMutationElementService.addChildInParent(optionalParent.get(), actionUsage);
            this.arcadiaLibraryServices.typeWithArcadiaFunction(actionUsage);
            this.elementInitializerSwitch.doSwitch(actionUsage);
            actionUsage.setDeclaredName(ARCADIA_FUNCTION + WHITE_SPACE + this.transverseQueryService.existingElementsCount(actionUsage));

            Optional<PartUsage> optionalAllocatingComponent = this.findAllocatingComponent(parent);
            if (optionalAllocatingComponent.isPresent()) {
                this.setPerformAction(optionalAllocatingComponent.get(), actionUsage);
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
        if (this.transverseQueryService.isFunction(feature)) {
            result = this.createFunctionPort((ActionUsage) feature, direction);
        } else if (this.transverseQueryService.isFunctionPort(feature)) {
            result = (ItemUsage) feature;
        }
        return result;
    }

    public ItemUsage createFunctionPort(ActionUsage container, FeatureDirectionKind direction) {

        ItemUsage itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        itemUsage.setDirection(direction);
        this.metamodelMutationElementService.addChildInParent(container, itemUsage);
        this.elementInitializerSwitch.doSwitch(itemUsage);
        this.arcadiaLibraryServices.typeWithExchangeItem(itemUsage);
        String defaultName = switch (direction) {
            case IN -> "FIP";
            case OUT -> "FOP";
            default -> "FP";
        };
        itemUsage.setDeclaredName(defaultName + WHITE_SPACE + this.transverseQueryService.existingElementsCount(itemUsage));

        return itemUsage;

    }

    public FlowUsage createFunctionalExchange(Feature source, Feature target) {
        Optional<Package> optionalSourceFunctionsPackage = this.transverseQueryService.getFunctionsPackage(source);
        Optional<Package> optionalTargetFunctionsPackage = this.transverseQueryService.getFunctionsPackage(target);

        if (optionalSourceFunctionsPackage.isPresent() && optionalSourceFunctionsPackage.equals(optionalTargetFunctionsPackage)) {

            if (this.transverseQueryService.canCreateFunctionalExchange(source, target)) {

                Feature sourcePort = this.getOrCreateFunctionPort(source, FeatureDirectionKind.OUT);
                Feature targetPort = this.getOrCreateFunctionPort(target, FeatureDirectionKind.IN);

                Optional<Namespace> optionalFunctionalExchangeParent = this.transverseQueryService.findClosestCommonAncestor(source, target, e -> this.transverseQueryService.isFunction(e) || this.transverseQueryService.isFunctionsPackage(e));
                if (optionalFunctionalExchangeParent.isPresent()) {

                    // We can't use diagramMutationElementService#createFlowUsage here because the way SysON computes FlowUsage container doesn't work with Capella for SysON.
                    FlowUsage functionalExchange = this.metamodelMutationElementService.createFlowUsage(sourcePort, targetPort, source, target, optionalFunctionalExchangeParent.get());

                    this.elementInitializerSwitch.doSwitch(functionalExchange);
                    this.arcadiaLibraryServices.typeWithArcadiaFunctionalExchange(functionalExchange);
                    long existingElementsCount = this.transverseQueryService.existingElementsCount(functionalExchange);
                    functionalExchange.setDeclaredName(ARCADIA_FUNCTIONAL_EXCHANGE + WHITE_SPACE + existingElementsCount);
                    return functionalExchange;
                }

            }
        }
        return null;
    }

    public InterfaceUsage createComponentExchange(Feature source, Feature target) {
        Optional<Package> optionalSourceStructurePackage = this.transverseQueryService.getStructurePackage(source);
        Optional<Package> optionalTargetStructurePackage = this.transverseQueryService.getStructurePackage(target);

        if (optionalSourceStructurePackage.isPresent() && optionalSourceStructurePackage.equals(optionalTargetStructurePackage)) {

            if (this.transverseQueryService.canCreateComponentExchange(source, target)) {

                PortUsage sourcePort = this.getOrCreateComponentPort(source, FeatureDirectionKind.OUT);
                PortUsage targetPort = this.getOrCreateComponentPort(target, FeatureDirectionKind.IN);

                InterfaceUsage componentExchange = this.metamodelMutationElementService.createInterfaceUsage(sourcePort, targetPort, source, target, optionalSourceStructurePackage.get());
                this.elementInitializerSwitch.doSwitch(componentExchange);
                this.arcadiaLibraryServices.typeWithArcadiaComponentExchange(componentExchange);
                long existingElementsCount = this.transverseQueryService.existingElementsCount(componentExchange);
                componentExchange.setDeclaredName(ARCADIA_COMPONENT_EXCHANGE + " " + existingElementsCount);
                return componentExchange;

            }
        }
        return null;
    }

    public AllocationUsage createDescribes(Element source, Element target) {
        // This method should rely on MetamodelMutationElementService once syson#2441 is fixed.
        var owner = source.getOwner();
        var ownerMembership = SysmlFactory.eINSTANCE.createOwningMembership();
        owner.getOwnedRelationship().add(ownerMembership);
        var allocation = SysmlFactory.eINSTANCE.createAllocationUsage();
        ownerMembership.getOwnedRelatedElement().add(allocation);
        this.addEndToAllocateEdge(allocation, source);
        this.addEndToAllocateEdge(allocation, target);
        return allocation;
    }

    public OccurrenceUsage createOperationalCapability(Element parent) {
        return this.transverseQueryService.getCapabilitiesPackage(parent)
                .map(capabilitiesPackage -> {
                    var capability = SysmlFactory.eINSTANCE.createOccurrenceUsage();
                    this.metamodelMutationElementService.addChildInParent(capabilitiesPackage, capability);
                    this.elementInitializerSwitch.doSwitch(capability);
                    this.arcadiaLibraryServices.typeWithArcadiaCapability(capability);
                    return capability;
                })
                .orElse(null);
    }

    private void addEndToAllocateEdge(AllocationUsage edge, Element end) {
        // This method should be removed once syson#2441 is fixed.
        if (end instanceof Usage usage) {
            var featureMembership = SysmlFactory.eINSTANCE.createEndFeatureMembership();
            edge.getOwnedRelationship().add(featureMembership);
            var feature = SysmlFactory.eINSTANCE.createFeature();
            featureMembership.getOwnedRelatedElement().add(feature);
            var reference = SysmlFactory.eINSTANCE.createReferenceSubsetting();
            feature.getOwnedRelationship().add(reference);
            reference.setReferencedFeature(usage);
        }
    }

    public ActionUsage createFunctionalChain(Element container, Object selectedObjects) {
        ActionUsage actionUsage = null;
        Optional<Package> optionalFunctionsPackage = this.transverseQueryService.getFunctionsPackage(container);
        if (optionalFunctionsPackage.isPresent()) {
            actionUsage = SysmlFactory.eINSTANCE.createActionUsage();
            this.metamodelMutationElementService.addChildInParent(optionalFunctionsPackage.get(), actionUsage);
            this.arcadiaLibraryServices.typeWithArcadiaFunctionalChain(actionUsage);
            this.elementInitializerSwitch.doSwitch(actionUsage);
            actionUsage.setDeclaredName(ARCADIA_FUNCTIONAL_CHAIN + WHITE_SPACE + this.transverseQueryService.existingElementsCount(actionUsage));
            this.setArcadiaReferenceFeature(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES, selectedObjects,
                    SysmlPackage.eINSTANCE.getFlowUsage().getName());
        }
        return actionUsage;
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
        // create the perform action
        Membership featureMember = this.metamodelMutationElementService.createMembership(ownerElement);
        ownerElement.getOwnedRelationship().add(featureMember);
        PerformActionUsage performAction = SysmlFactory.eINSTANCE.createPerformActionUsage();
        featureMember.getOwnedRelatedElement().add(performAction);
        return performAction;
    }

    private Optional<PartUsage> findAllocatingComponent(Element parent) {
        Optional<PartUsage> allocatingComponent = Optional.empty();
        if (this.transverseQueryService.isComponent(parent)) {
            allocatingComponent = Optional.of((PartUsage) parent);
        } else if (this.transverseQueryService.isFunction(parent)) {
            allocatingComponent = this.transverseQueryService.getAllocatingComponent((ActionUsage) parent);
        }
        return allocatingComponent;
    }

    public Feature setStatusKind(Feature feature, Object newValue,
            IEditingContext editingContext) {
        this.unSetUsageStatusKind(feature);
        if (newValue != null) {
            this.transverseQueryService.getStatusKindEnum(editingContext).stream()
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
                .ifPresent(this::delete);
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
        Optional.ofNullable(flowUsage.getPayloadFeature()).ifPresent(this::delete);
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

    public void deletePerformedActionUsage(PartUsage usage, ActionUsage actionUsage) {
        this.getPerformActionUsage(usage, actionUsage::equals)
                .forEach(this::delete);
    }

    private List<? extends ActionUsage> getPerformActionUsage(PartUsage partUsage, Predicate<? super ActionUsage> predicate) {
        return partUsage.getNestedUsage().stream()
                .filter(PerformActionUsage.class::isInstance)
                .map(PerformActionUsage.class::cast)
                .filter(performActionUsage -> predicate.test(this.transverseQueryService.getPerformedAction(performActionUsage).orElse(null)))
                .toList();
    }
}

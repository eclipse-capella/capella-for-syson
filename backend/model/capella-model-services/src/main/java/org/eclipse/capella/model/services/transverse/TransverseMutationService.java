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
package org.eclipse.capella.model.services.transverse;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.Documentation;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.ParameterMembership;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.ReferenceSubsetting;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_DESCRIPTION;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.PATH_SEPARATOR;

/**
 * Transverse mutation service. It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class TransverseMutationService {

    // We depends on this service only for some SysML business usage. It should be located in another syson common
    // services since it does not depend on the view.
    // @technical-debt

    private final UtilService utilService;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final DeleteService deleteService;

    private final MetamodelMutationElementService metamodelMutationElementService;

    public TransverseMutationService() {
        this.utilService = new UtilService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.transverseQueryService = new TransverseQueryService();
        this.deleteService = new DeleteService();
        this.metamodelMutationElementService = new MetamodelMutationElementService();

    }

    public PortUsage createPortUsage(Element parent) {

        // create a new port on given part usage
        var newPortUsage = SysmlFactory.eINSTANCE.createPortUsage();
        this.metamodelMutationElementService.addChildInParent(parent, newPortUsage);
        this.elementInitializerSwitch.doSwitch(newPortUsage);
        return newPortUsage;
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

    public void setBooleanAttribute(Usage usage, String prefix, String attributeName, boolean newValue) {
        Optional<LiteralBoolean> optionalExitingValue = this.transverseQueryService.getFeatureReferenceExpression(usage, attributeName)
                .filter(LiteralBoolean.class::isInstance)
                .map(LiteralBoolean.class::cast);

        // If the value is already set, we retrieve it to set the new value.
        var descriptionValue = optionalExitingValue.orElseGet(() -> this.createLiteralBooleanAttribute(usage, prefix, attributeName));
        descriptionValue.setValue(newValue);
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
                .ifPresent(this.deleteService::deleteFromModel);
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

    public Usage deleteComponent(PartUsage component) {
        Set<InterfaceUsage> relatedElements = new HashSet<>();
        List<PortUsage> componentPorts = this.transverseQueryService.getComponentPorts(component);
        componentPorts.forEach(port -> this.collectRelatedComponentExchanges(port, relatedElements));
        relatedElements.forEach(this.deleteService::deleteFromModel);
        this.deleteService.deleteFromModel(component);
        return component;
    }

    private void collectRelatedComponentExchanges(EObject eObject, Set<InterfaceUsage> relatedElements) {
        var optAdapter = eObject.eAdapters().stream().filter(ECrossReferenceAdapter.class::isInstance).map(ECrossReferenceAdapter.class::cast).findFirst();
        if (optAdapter.isPresent()) {
            ECrossReferenceAdapter referenceAdapter = optAdapter.get();
            Collection<EStructuralFeature.Setting> inverseReferences = referenceAdapter.getInverseReferences(eObject);
            for (EStructuralFeature.Setting setting : inverseReferences) {
                EObject relatedElement = setting.getEObject();
                if (relatedElement instanceof ReferenceSubsetting referenceSubsetting) {
                    Optional.of(referenceSubsetting)
                            .map(ReferenceSubsetting::getSubsettingFeature)
                            .map(Feature::getOwner)
                            .filter(InterfaceUsage.class::isInstance)
                            .map(InterfaceUsage.class::cast)
                            .filter(this.transverseQueryService::isComponentExchange)
                            .ifPresent(relatedElements::add);
                }
            }
        }
    }
}

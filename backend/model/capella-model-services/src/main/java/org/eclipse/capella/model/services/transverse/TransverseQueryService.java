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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.ConnectorAsUsage;
import org.eclipse.syson.sysml.Documentation;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureChainExpression;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.OperatorExpression;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Transverse mutation service. It is important to note that this service must retain its empty constructor and should
 * not have constructors with parameters.
 *
 * @author frouene
 */
public class TransverseQueryService {
    public static final String PATH_SEPARATOR = "::";

    public static final String ARCADIA_PREFIX = "Arcadia" + PATH_SEPARATOR;

    public static final String ARCADIA_COMPONENT = "Component";

    public static final String ARCADIA_ACTOR = "Actor";

    public static final String ARCADIA_FUNCTION = "Function";

    public static final String ARCADIA_COMPONENT_PORT = "ComponentPort";

    public static final String ARCADIA_COMPONENT_EXCHANGE = "ComponentExchange";

    public static final String ARCADIA_FUNCTIONAL_EXCHANGE = "FunctionalExchange";

    public static final String ARCADIA_FUNCTIONAL_CHAIN = "FunctionalChain";

    public static final String ARCADIA_EXCHANGE_ITEM = "ExchangeItem";

    public static final String ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES = "involvedFunctionalExchanges";

    public static final String ARCADIA_REQUIREMENT = "ArcadiaRequirement";

    public static final String ARCADIA_IS_ACTOR = "isActor";

    public static final String ARCADIA_IS_HUMAN = "isHuman";

    public static final String ARCADIA_DESCRIPTION = "description";

    public static final String MODELING_METADATA_STATUS_INFO = "ModelingMetadata::StatusInfo";

    public static final String STRUCTURE_PACKAGE = "Structure";

    public static final String FUNCTIONS_PACKAGE = "Functions";
    
    public static final String REQUIREMENTS_PACKAGE = "Requirements";

    private final UtilService utilService;

    public TransverseQueryService() {
        this.utilService = new UtilService();
    }

    public Boolean isArcadiaElement(EObject eObject) {
        // We want to exclude MetadataUsage who are not an Arcadia Element but are used to type them.
        if (eObject instanceof Feature feature && !(feature instanceof MetadataUsage)) {
            return this.getArcadiaType(feature).isPresent();
        }
        return false;
    }

    public Optional<String> getArcadiaType(EObject eObject) {
        if (eObject instanceof Feature feature && feature.getType() != null) {
            return feature.getType().stream().map(Element::getQualifiedName).filter(name -> name != null && name.startsWith(ARCADIA_PREFIX)).findFirst();
        }
        return Optional.empty();
    }

    public List<Feature> getTarget(ConnectorAsUsage connector) {
        return connector.getTargetFeature().stream()
                .filter(Objects::nonNull)
                .map(Feature::getFeatureTarget)
                .toList();
    }

    public Predicate<? super Feature> isTypedWith(String qualifiedName) {
        return element -> element.getType().stream().anyMatch(t -> t != null && qualifiedName != null && qualifiedName.equals(t.getQualifiedName()));
    }

    public Feature getSource(ConnectorAsUsage connectorAsUsage) {
        Feature sourceFeature = connectorAsUsage.getSourceFeature();
        if (sourceFeature != null) {
            return sourceFeature.getFeatureTarget();
        }
        return null;
    }

    public Optional<ArcadiaEngineeringPerspective> getArcadiaPerspective(Element element) {
        return this.getArcadiaPerspectivePackage(element).map(Element::getDeclaredName).map(ArcadiaEngineeringPerspective::fromValue);
    }

    /**
     * Returns the Owning Perspective Package for the given Element (Logical Architecture, Physical Architecture etc.).
     *
     * @param element
     *            the SysML element to retrieve the parent Perspective package.
     * @return an Optional containing the package if found.
     */
    public Optional<Package> getArcadiaPerspectivePackage(Element element) {
        Optional<Package> optionalPackage = Optional.empty();
        if (element != null) {
            EObject eContainer = element.eContainer();
            if (eContainer instanceof Package parentPkg && this.isArcadiaPerspectivePackage(parentPkg)) {
                optionalPackage = Optional.of(parentPkg);
            } else if (eContainer instanceof Element parentElement) {
                optionalPackage = this.getArcadiaPerspectivePackage(parentElement);
            }
        }
        return optionalPackage;
    }

    private boolean isArcadiaPerspectivePackage(Package parentPkg) {
        return ArcadiaEngineeringPerspective.containsValue(parentPkg.getDeclaredName());
    }

    public long existingElementsCount(Element element) {
        String arcadiaType = this.getArcadiaType(element).orElse("");
        List<EObject> allReachableInResource = this.getAllReachableInResource(element, element.eClass());
        return allReachableInResource.stream()
                .filter(member -> arcadiaType.equals(this.getArcadiaType(member).orElse("")))
                .count();
    }

    /**
     * Rely on SysON UtilService#getAllReachable but restricted to the same resource.
     *
     * @param eObject
     *            the eObject in the resource to look for.
     * @param type
     *            the searched {@link EClass}
     * @return the reachable objects.
     */
    public List<EObject> getAllReachableInResource(EObject eObject, EClass type) {
        List<EObject> allReachable = this.utilService.getAllReachable(eObject, type);
        return allReachable.stream().filter(element -> eObject.eResource() == element.eResource()).toList();
    }

    public Optional<Expression> getFeatureReferenceExpression(Usage usage, String referenceName) {
        return usage.getNestedUsage().stream().filter(nestedUsage -> referenceName.equals(nestedUsage.getName()))
                .map(Usage::getOwnedMember)
                .flatMap(List::stream)
                .filter(Expression.class::isInstance)
                .map(Expression.class::cast)
                .findFirst();
    }

    /**
     * Provides the values for the given reference on the given usage.
     *
     * @param usage
     *            the Usage.
     * @param referenceName
     *            the reference name in the Arcadia Lib.
     * @return the list of values.
     */
    public List<Feature> getFeatureReferenceValue(Usage usage, String referenceName) {
        var optionalExpression = this.getFeatureReferenceExpression(usage, referenceName);
        if (optionalExpression.isPresent()) {
            return this.extractAllFeatures(optionalExpression.get());
        }
        return List.of();
    }

    public List<Feature> extractAllFeatures(Expression expression) {
        List<Feature> features = new ArrayList<>();

        if (expression instanceof FeatureReferenceExpression featureReferenceExpression) {
            features.add(featureReferenceExpression.getReferent());
        }
        else if (expression instanceof FeatureChainExpression featureChainExpression) {
            features.add(featureChainExpression.getTargetFeature());
        }
        else if (expression instanceof OperatorExpression operatorExpression) {
            // Recurse through all arguments of the operator (e.g., the elements in [fx1, fx2])
            for (Expression arg : operatorExpression.getArgument()) {
                features.addAll(this.extractAllFeatures(arg));
            }
        }
        return features;
    }

    public DiagramDescription findOwningDiagramDescription(DiagramElementDescription diagramElementDescription) {
        return this.findOwningDiagram(diagramElementDescription, DiagramDescription.class);
    }

    private <T extends EObject> T findOwningDiagram(EObject current, Class<T> type) {
        T result = null;
        if (type.isInstance(current)) {
            result = type.cast(current);
        } else {
            EObject parent = current.eContainer();
            if (parent != null) {
                result = this.findOwningDiagram(parent, type);
            }
        }
        return result;
    }

    public List<NodeDescription> getDiagramNodeDescriptions(String diagramName, IViewDiagramElementFinder cache) {
        return cache.getNodeDescriptions().stream().filter(node ->
                this.findOwningDiagramDescription(node).getTitleExpression().equals(diagramName))
                .toList();
    }

    public List<EEnumLiteral> getExchangeItemEnumLiterals(Element element, String eAttributeName) {
        EStructuralFeature eStructuralFeature = element.eClass().getEStructuralFeature(eAttributeName);
        List<EEnumLiteral> candidates = new ArrayList<>();
        if (eStructuralFeature instanceof EAttribute eAttribute
                && eAttribute.getEAttributeType() instanceof EEnum eEnum) {
            List<EEnumLiteral> eLiterals = eEnum.getELiterals().stream().filter(enumLiteral -> !enumLiteral.getLiteral().equals(FeatureDirectionKind.INOUT.getLiteral())).toList();
            candidates.addAll(eLiterals);
        }
        return candidates;
    }

    public boolean isStatusInfo(Usage usage) {
        return this.isTypedWith(MODELING_METADATA_STATUS_INFO).test(usage);
    }

    public String getArcadiaElementDescription(EObject eObject) {
        return Optional.ofNullable(eObject)
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .map(Element::getDocumentation)
                .stream()
                .flatMap(List::stream)
                .filter(documentation -> ARCADIA_DESCRIPTION.equals(documentation.getDeclaredName()))
                .map(Documentation::getBody)
                .findFirst()
                .orElse("");
    }

    public Boolean isRequirement(EObject eObject) {
        if (eObject instanceof RequirementUsage requirementUsage) {
            return this.checkType(requirementUsage, ARCADIA_PREFIX + ARCADIA_REQUIREMENT);
        }
        return false;
    }

    public Boolean isComponent(EObject eObject) {
        if (eObject instanceof PartUsage partUsage) {
            return this.checkType(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT);
        }
        return false;
    }

    public Boolean isComponentExchange(EObject eObject) {
        if (eObject instanceof InterfaceUsage interfaceUsage) {
            return this.checkType(interfaceUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE);
        }
        return false;
    }

    public Boolean isComponentPort(EObject eObject) {
        if (eObject instanceof PortUsage portUsage) {
            return this.checkType(portUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT_PORT);
        }
        return false;
    }

    public Boolean checkType(Feature feature, String expectedType) {
        return feature.getType().stream().anyMatch(t -> {
            return t != null && t.getQualifiedName() != null && t.getQualifiedName().equals(expectedType);
        });
    }

    public boolean isStructurePackage(Object element) {
        return element instanceof Package packageElt
                && STRUCTURE_PACKAGE.equals(packageElt.getDeclaredName());
    }

    public boolean isFunctionsPackage(Object element) {
        return element instanceof Package packageElt
                && FUNCTIONS_PACKAGE.equals(packageElt.getDeclaredName());
    }

    public Boolean isComponentActor(EObject eObject) {
        if (eObject instanceof PartUsage partUsage) {
            return this.isActor().test(partUsage);
        }
        return false;
    }

    public Boolean isComponentHumanActor(EObject eObject) {
        if (eObject instanceof PartUsage partUsage) {
            return this.isActor().test(partUsage) && this.isHuman().test(partUsage);
        }
        return false;
    }

    public Boolean getHumanCheckboxValue(EObject eObject) {
        if (eObject instanceof PartUsage partUsage) {
            return this.isHuman().test(partUsage);
        }
        return false;
    }

    private Predicate<PartUsage> isActor() {
        return partUsage -> partUsage.getNestedReference().stream().anyMatch(attr -> ARCADIA_IS_ACTOR.equals(attr.getName())
                && attr.getOwnedMember()
                .stream()
                .findFirst()
                .filter(LiteralBoolean.class::isInstance)
                .map(LiteralBoolean.class::cast)
                .map(LiteralBoolean::isValue)
                .orElse(false));
    }

    private Predicate<PartUsage> isHuman() {
        return partUsage -> partUsage.getNestedReference().stream().anyMatch(attr -> ARCADIA_IS_HUMAN.equals(attr.getName())
                && attr.getOwnedMember()
                .stream()
                .findFirst()
                .filter(LiteralBoolean.class::isInstance)
                .map(LiteralBoolean.class::cast)
                .map(LiteralBoolean::isValue)
                .orElse(false));
    }

    public List<InterfaceUsage> getComponentExchanges(EObject eObject) {
        var allPartUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getInterfaceUsage());
        return allPartUsage.stream()
                .filter(InterfaceUsage.class::isInstance)
                .map(InterfaceUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE))
                .toList();
    }

    public List<PortUsage> getComponentPorts(EObject eObject) {
        List<PortUsage> portUsages = List.of();
        if (eObject instanceof PartUsage partUsage) {
            portUsages = partUsage.getNestedPort().stream()
                    .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT_PORT))
                    .toList();
        }
        return portUsages;
    }

    public List<RequirementUsage> getRequirements(EObject eObject) {
        var allRequirementUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getRequirementUsage());
        return allRequirementUsage.stream()
                .filter(RequirementUsage.class::isInstance)
                .map(RequirementUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_REQUIREMENT))
                .toList();
    }

    public List<AllocationUsage> getAllocationUsage(EObject eObject) {
        var allAllocationUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getAllocationUsage());
        return allAllocationUsage.stream()
                .filter(AllocationUsage.class::isInstance)
                .map(AllocationUsage.class::cast)
                .toList();
    }

    public boolean isInFeature(Feature feature) {
        return FeatureDirectionKind.IN.equals(feature.getDirection());
    }

    public boolean isOutFeature(Feature feature) {
        return FeatureDirectionKind.OUT.equals(feature.getDirection());
    }

    public boolean isInOutFeature(Feature feature) {
        return FeatureDirectionKind.INOUT.equals(feature.getDirection());
    }

}

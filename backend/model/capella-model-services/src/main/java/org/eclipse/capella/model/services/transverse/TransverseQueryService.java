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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.model.services.transverse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.syson.model.services.aql.ModelQueryAQLService;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.ConnectorAsUsage;
import org.eclipse.syson.sysml.Documentation;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EndFeatureMembership;
import org.eclipse.syson.sysml.EnumerationUsage;
import org.eclipse.syson.sysml.Expression;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureChainExpression;
import org.eclipse.syson.sysml.FeatureChaining;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.FlowEnd;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.LiteralBoolean;
import org.eclipse.syson.sysml.MetadataUsage;
import org.eclipse.syson.sysml.OperatorExpression;
import org.eclipse.syson.sysml.OccurrenceUsage;
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
import org.eclipse.syson.sysml.VariantMembership;
import org.eclipse.syson.sysml.helper.EMFUtils;

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

    public static final String ARCADIA_CAPABILITY = "Capability";

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

    public static final String CAPABILITIES_PACKAGE = "Capabilities";

    public static final String FUNCTIONS_PACKAGE = "Functions";

    public static final String REQUIREMENTS_PACKAGE = "Requirements";

    public static final String STATUS = "status";

    public static final String STATUS_KIND = "StatusKind";

    private final ModelQueryAQLService modelQueryAQLService;

    private final UtilService utilService;

    public TransverseQueryService() {
        this.modelQueryAQLService = new ModelQueryAQLService();
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
        return this.getArcadiaPerspectivePackage(element).map(Element::getDeclaredName).flatMap(ArcadiaEngineeringPerspective::fromLabel);
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
        if (element instanceof Package pack && this.isArcadiaPerspectivePackage(pack)) {
            optionalPackage = Optional.of(pack);
        } else if (element != null) {
            EObject eContainer = element.eContainer();
            if (eContainer instanceof Package parentPack && this.isArcadiaPerspectivePackage(parentPack)) {
                optionalPackage = Optional.of(parentPack);
            } else if (eContainer instanceof Element parentElement) {
                optionalPackage = this.getArcadiaPerspectivePackage(parentElement);
            }
        }
        return optionalPackage;
    }

    private boolean isArcadiaPerspectivePackage(Package parentPkg) {
        return ArcadiaEngineeringPerspective.fromLabel(parentPkg.getDeclaredName()).isPresent();
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

    /**
     * Retrieve the status element associated with the given feature.
     *
     * @param feature
     *         the {@link Feature} to inspect.
     * @return the status {@link Element} if found, otherwise null.
     */
    public Element getStatus(Feature feature) {
        return feature.getOwnedElement().stream()
                .filter(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filter(this::isStatusInfo)
                .flatMap(metadataUsage -> this.getFeatureReferenceValue(metadataUsage, STATUS).stream())
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

    public String getArcadiaElementName(EObject eObject) {
        if (eObject instanceof Usage usage) {
            return usage.getName();
        }
        return null;
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
        return eObject instanceof RequirementUsage;
    }

    public Boolean isArcadiaRequirement(EObject eObject) {
        return eObject instanceof RequirementUsage requirementUsage
                && this.checkType(requirementUsage, ARCADIA_PREFIX + ARCADIA_REQUIREMENT);
    }

    public Boolean isComponent(EObject eObject) {
        if (eObject instanceof PartUsage partUsage) {
            return this.checkType(partUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT);
        }
        return false;
    }

    public Boolean isCapability(EObject eObject) {
        return eObject instanceof OccurrenceUsage occurrenceUsage
                && this.checkType(occurrenceUsage, ARCADIA_PREFIX + ARCADIA_CAPABILITY);
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

    public Boolean isExchangeItem(EObject eObject) {
        if (eObject instanceof Usage usage) {
            return this.checkType(usage, ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM);
        }
        return false;
    }

    public Boolean isFunctionalExchange(EObject eObject) {
        if (eObject instanceof FlowUsage flowUsage) {
            return this.checkType(flowUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_EXCHANGE);
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

    public Boolean isFunction(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return this.checkType(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTION);
        }
        return false;
    }

    public Boolean isFunctionalChain(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return this.checkType(actionUsage, ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN);
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

    public boolean isOperationalAnalysisPerspective(Element element) {
        return this.getArcadiaPerspective(element)
                        .filter(ArcadiaEngineeringPerspective.OperationalAnalysis::equals)
                        .isPresent();
    }

    public boolean isOperationalAnalysisCapabilitiesPackage(Object element) {
        return element instanceof Package packageElt
                && CAPABILITIES_PACKAGE.equals(packageElt.getDeclaredName())
                && this.isOperationalAnalysisPerspective(packageElt);
    }

    public boolean isFunctionsPackage(Object element) {
        return element instanceof Package packageElt
                && FUNCTIONS_PACKAGE.equals(packageElt.getDeclaredName());
    }

    public boolean isRequirementsPackage(Object element) {
        return element instanceof Package packageElt
                && REQUIREMENTS_PACKAGE.equals(packageElt.getDeclaredName());
    }

    /**
     * Checks if the given element is a Package (but not one of the special Arcadia packages like Structure, Functions, Requirements).
     */
    public boolean isUserPackage(Object element) {
        if (element instanceof Package packageElt) {
            String name = packageElt.getDeclaredName();
            return name != null
                    && !STRUCTURE_PACKAGE.equals(name)
                    && !FUNCTIONS_PACKAGE.equals(name)
                    && !REQUIREMENTS_PACKAGE.equals(name);
        }
        return false;
    }

    /**
     * Get the formatted label for a Requirement header.
     * Format: "id - name" with fallbacks:
     * - If no id and name is empty/default: "Requirement"
     * - If name is empty/default but id exists: just the id
     * - Otherwise: "id - name"
     */
    public String getRequirementLabel(RequirementUsage requirement) {
        String reqId = requirement.getReqId();
        String name = requirement.getName();

        boolean hasId = reqId != null && !reqId.isBlank();
        boolean hasName = name != null && !name.isBlank() && !this.isDefaultRequirementName(name);

        String result;
        if (!hasId && !hasName) {
            result = "Requirement";
        } else if (hasId && !hasName) {
            result = reqId;
        } else if (!hasId && hasName) {
            result = name;
        } else {
            result = reqId + " - " + name;
        }
        return result;
    }

    /**
     * Get the requirement text (the "shall" statement).
     */
    public String getRequirementText(RequirementUsage requirement) {
        var textList = requirement.getText();
        if (textList != null && !textList.isEmpty()) {
            return String.join(" ", textList);
        }
        return "";
    }

    private boolean isDefaultRequirementName(String name) {
        if (name == null) {
            return true;
        }
        String lower = name.toLowerCase().trim();
        return lower.equals("requirement") || lower.matches("requirement\\s*\\d+");
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

    public Boolean isNotComponentHumanActor(EObject eObject) {
        return !this.isComponentHumanActor(eObject);
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

    public List<FlowUsage> getFunctionalExchanges(EObject eObject) {
        var allFlowUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getFlowUsage());
        return allFlowUsage.stream()
                .filter(this::isFunctionalExchange)
                .map(FlowUsage.class::cast)
                .toList();
    }

    public Element getFunctionalExchangeSource(FlowUsage functionalExchange) {
        return this.modelQueryAQLService.getSourceFlowUsageEdge(functionalExchange);
    }

    public ActionUsage getFunctionalExchangeSourceFunction(FlowUsage flowUsage) {
        return Optional.ofNullable(this.getFunctionalExchangeSource(flowUsage))
                .map(Element::getOwner)
                .filter(this::isFunction)
                .map(ActionUsage.class::cast)
                .orElse(null);
    }

    public Element getFunctionalExchangeTarget(FlowUsage functionalExchange) {
        return this.modelQueryAQLService.getTargetFlowUsageEdge(functionalExchange);
    }

    public ActionUsage getFunctionalExchangeTargetFunction(FlowUsage flowUsage) {
        return Optional.ofNullable(this.getFunctionalExchangeTarget(flowUsage))
                .map(Element::getOwner)
                .filter(this::isFunction)
                .map(ActionUsage.class::cast)
                .orElse(null);
    }

    public List<FlowUsage> getIncomingFunctionalExchanges(ActionUsage function) {
        List<FlowUsage> result = List.of();
        if (this.isFunction(function)) {
            List<FlowUsage> functionalExchanges = this.getFunctionalExchanges(function);
            result = functionalExchanges.stream()
                    .filter(functionalExchange -> Objects.equals(this.getFunctionalExchangeTargetFunction(functionalExchange), function))
                    .toList();
        }

        return result;
    }

    public List<FlowUsage> getOutgoingFunctionalExchanges(ActionUsage function) {
        List<FlowUsage> result = List.of();
        if (this.isFunction(function)) {
            List<FlowUsage> functionalExchanges = this.getFunctionalExchanges(function);
            result = functionalExchanges.stream()
                    .filter(functionalExchange -> Objects.equals(this.getFunctionalExchangeSourceFunction(functionalExchange), function))
                    .toList();
        }

        return result;
    }

    public List<ActionUsage> getFunctions(EObject eObject) {
        var allActionUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getActionUsage());
        return allActionUsage.stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION))
                .toList();
    }

    public List<Feature> getFunctionPorts(EObject eObject) {
        if (eObject instanceof ActionUsage actionUsage) {
            return actionUsage.getParameter().stream().toList();
        }
        return List.of();
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
     *         the candidate {@link EObject}.
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
     *         the candidate {@link EObject}.
     * @return an {@link Optional} containing the first parent function if found, otherwise an empty {@link Optional}.
     */
    public Optional<ActionUsage> getParentFunction(EObject eObject) {
        return this.getParentFunctions(eObject).stream().findFirst();
    }

    public List<ActionUsage> getFunctionalChains(EObject eObject) {
        var allFlowUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getActionUsage());
        return allFlowUsage.stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN))
                .toList();
    }

    public List<ActionUsage> getFunctionalChainsImpliedIn(FlowUsage functionalExchange) {
        List<ActionUsage> functionalChains = this.getFunctionalChains(functionalExchange);
        return functionalChains.stream()
                .filter(functionalChain -> this.getInvolvedFunctionalExchanges(functionalChain).contains(functionalExchange))
                .toList();
    }

    public List<ActionUsage> getFunctionalChainsImpliedIn(ActionUsage function) {
        List<ActionUsage> functionalChains = this.getFunctionalChains(function);
        return functionalChains.stream()
                .filter(functionalChain -> this.getInvolvedFunctions(functionalChain).contains(function))
                .toList();
    }

    public List<ActionUsage> getInvolvedFunctions(ActionUsage functionalChain) {
        Set<ActionUsage> involvedFunctions = new HashSet<>();
        this.getInvolvedFunctionalExchanges(functionalChain).forEach(flowUsage -> {
            Optional.ofNullable(this.getFunctionalExchangeSourceFunction(flowUsage))
                    .ifPresent(involvedFunctions::add);
            Optional.ofNullable(this.getFunctionalExchangeTargetFunction(flowUsage))
                    .ifPresent(involvedFunctions::add);
        });
        return List.copyOf(involvedFunctions);
    }

    public List<FlowUsage> getInvolvedFunctionalExchanges(ActionUsage actionUsage) {
        List<Feature> features = this.getFeatureReferenceValue(actionUsage, ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);
        return features.stream().filter(FlowUsage.class::isInstance).map(FlowUsage.class::cast).toList();
    }

    public List<InterfaceUsage> getComponentExchanges(EObject eObject) {
        var allPartUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getInterfaceUsage());
        return allPartUsage.stream()
                .filter(this::isComponentExchange)
                .map(InterfaceUsage.class::cast)
                .toList();
    }

    public PortUsage getComponentExchangeSource(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(this.modelQueryAQLService.getConnectorSource(interfaceUsage))
                .filter(this::isComponentPort)
                .map(PortUsage.class::cast)
                .orElse(null);
    }

    public PortUsage getComponentExchangeTarget(InterfaceUsage interfaceUsage) {
        return Optional.ofNullable(this.modelQueryAQLService.getConnectorTarget(interfaceUsage))
                .flatMap(targets -> targets.stream().findFirst())
                .filter(this::isComponentPort)
                .map(PortUsage.class::cast)
                .orElse(null);
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
                .toList();
    }

    public List<RequirementUsage> getArcadiaRequirements(EObject eObject) {
        return this.getRequirements(eObject).stream()
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_REQUIREMENT))
                .toList();
    }

    public List<AllocationUsage> getDescribes(EObject eObject) {
        var allAllocationUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getAllocationUsage());
        return allAllocationUsage.stream()
                .filter(AllocationUsage.class::isInstance)
                .map(AllocationUsage.class::cast)
                .filter(allocationUsage -> this.isRequirement(this.getDescribesSource(allocationUsage)))
                .toList();
    }

    public Element getDescribesSource(AllocationUsage describes) {
        return new ModelQueryAQLService().getSourceAllocateEdge(describes);
    }

    public Element getDescribesTarget(AllocationUsage describes) {
        return new ModelQueryAQLService().getTargetAllocateEdge(describes);
    }

    public List<PartUsage> getComponents(EObject eObject) {
        var allPartUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getPartUsage());
        return allPartUsage.stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .toList();
    }

    public List<PartUsage> getSubComponents(EObject eObject) {
        List<Element> allPartUsage = new ArrayList<>();
        if (eObject instanceof Package pkg) {
            Optional<Package> optionalStructurePackage = this.getStructurePackage(pkg);
            allPartUsage = optionalStructurePackage.map(Package::getMember).orElse(new BasicEList<>());
        } else if (this.isComponent(eObject) && eObject instanceof PartUsage partUsage) {
            allPartUsage = partUsage.getMember();
        }
        return allPartUsage.stream()
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_COMPONENT))
                .toList();
    }

    public Optional<PartUsage> getParentComponent(EObject element) {
        Optional<PartUsage> parent = Optional.empty();
        if (element instanceof PartUsage component && component.getOwningUsage() instanceof PartUsage parentComponent
                && this.isComponent(parentComponent)) {
            parent = Optional.of(parentComponent);
        } else if (element instanceof PartUsage component && component.getOwner() instanceof PartUsage parentComponent
                && this.isComponent(parentComponent)) {
            parent = Optional.of(parentComponent);
        }
        return parent;
    }

    /**
     * Retrieve component node candidates for the LAB diagram. At diagram root (Structure Package), return all reachable components so nested components can be displayed directly. Inside a represented
     * component node, keep the direct-subcomponents behavior.
     *
     * @param eObject
     *         the current semantic context.
     * @return component candidates for node display.
     */
    public List<PartUsage> allNestedComponents(EObject eObject) {
        if (eObject instanceof Package) {
            return this.getComponents(eObject);
        }
        return this.getSubComponents(eObject);
    }

    /**
     * Retrieve the component that allocates the given function, if any.
     *
     * @param function
     *         the {@link ActionUsage} to check.
     * @return an {@link Optional} containing the allocating {@link PartUsage}, or empty if none.
     */
    public Optional<PartUsage> getAllocatingComponent(ActionUsage function) {
        return this.getComponents(function)
                .stream()
                .filter(component -> this.getAllocatedFunctions(component).contains(function))
                .findFirst();
    }

    public List<ActionUsage> getAllocatedFunctions(PartUsage partUsage) {
        return this.getPerformedActions(partUsage, this.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION));
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

    public boolean isInFeature(Feature feature) {
        return FeatureDirectionKind.IN.equals(feature.getDirection());
    }

    public boolean isOutFeature(Feature feature) {
        return FeatureDirectionKind.OUT.equals(feature.getDirection());
    }

    public boolean isInOutFeature(Feature feature) {
        return FeatureDirectionKind.INOUT.equals(feature.getDirection());
    }

    public boolean isDescribes(Object eObject) {
        if (eObject instanceof AllocationUsage allocationUsage) {
            return allocationUsage.getSource().stream()
                    .anyMatch(source -> Objects.equals(this.getArcadiaType(source).map(type -> type.replaceFirst(ARCADIA_PREFIX, "")).orElse(""), ARCADIA_REQUIREMENT));
        }
        return false;
    }

    public boolean canCreateFunctionalExchange(Feature source, Feature target) {
        var sourceFunction = EMFUtils.getFirstAncestor(ActionUsage.class, source, this::isFunction);
        var targetFunction = EMFUtils.getFirstAncestor(ActionUsage.class, target, this::isFunction);
        boolean validOwners = sourceFunction.isPresent() && targetFunction.isPresent() && !sourceFunction.equals(targetFunction);
        boolean validSource = this.isFunction(source) || (this.isFunctionPort(source) && source.getDirection() == FeatureDirectionKind.OUT);
        boolean validTarget = this.isFunction(target) || (this.isFunctionPort(target) && target.getDirection() == FeatureDirectionKind.IN);
        return validOwners && validSource && validTarget;
    }

    public boolean canCreateComponentExchange(Feature source, Feature target) {
        var sourceComponent = EMFUtils.getFirstAncestor(PartUsage.class, source, this::isComponent);
        var targetComponent = EMFUtils.getFirstAncestor(PartUsage.class, target, this::isComponent);
        return sourceComponent.isPresent() && targetComponent.isPresent() && !sourceComponent.equals(targetComponent);
    }

    public Optional<Package> getStructurePackage(Element element) {
        return this.getArcadiaPerspectiveOwnedPackage(element, STRUCTURE_PACKAGE);
    }

    public Optional<Package> getFunctionsPackage(Element element) {
        return this.getArcadiaPerspectiveOwnedPackage(element, FUNCTIONS_PACKAGE);
    }

    public Optional<ActionUsage> getRootFunction(Element element) {
        return this.getFunctionsPackage(element)
                .stream()
                .flatMap(functionsPackage -> functionsPackage.getOwnedElement().stream())
                .filter(this::isFunction)
                .map(ActionUsage.class::cast)
                .findFirst();
    }

    public Optional<Package> getRequirementsPackage(Element element) {
        return this.getArcadiaPerspectiveOwnedPackage(element, REQUIREMENTS_PACKAGE);
    }

    public Optional<Package> getCapabilitiesPackage(Element element) {
        return this.getArcadiaPerspectiveOwnedPackage(element, CAPABILITIES_PACKAGE);
    }

    public List<OccurrenceUsage> getCapabilities(EObject context) {
        return this.getAllReachableInResource(context, SysmlPackage.eINSTANCE.getOccurrenceUsage()).stream()
            .filter(OccurrenceUsage.class::isInstance)
            .map(OccurrenceUsage.class::cast)
            .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_CAPABILITY))
            .toList();
    }

    /**
     * Returns the package named {@code packageName} and owned by the provided {@code element}'s perspective package.
     *
     * @param element
     *         the element in the perspective
     * @param packageName
     *         the name of the perspective owned package to retrieve
     * @return an optional containing the package if it exists, or {@link Optional#empty()} otherwise
     */
    private Optional<Package> getArcadiaPerspectiveOwnedPackage(Element element, String packageName) {
        return this.getArcadiaPerspectivePackage(element)
                .map(Package::getOwnedMember)
                .orElse(new BasicEList<>())
                .stream()
                .filter(Package.class::isInstance)
                .map(Package.class::cast)
                .filter(ownedPackage -> Objects.equals(ownedPackage.getDeclaredName(), packageName))
                .findFirst();
    }

    public List<ItemUsage> getExchangeItems(EObject eObject) {
        var allItemUsage = this.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getItemUsage());
        return allItemUsage.stream()
                .filter(ItemUsage.class::isInstance)
                .map(ItemUsage.class::cast)
                .filter(this.isTypedWith(ARCADIA_PREFIX + ARCADIA_EXCHANGE_ITEM))
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

}

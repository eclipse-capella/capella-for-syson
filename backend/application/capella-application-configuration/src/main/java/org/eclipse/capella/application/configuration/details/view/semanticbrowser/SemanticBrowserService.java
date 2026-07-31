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
package org.eclipse.capella.application.configuration.details.view.semanticbrowser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.forms.TreeNode;
import org.eclipse.sirius.components.forms.components.TreeComponent;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.web.application.UUIDParser;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.eclipse.sirius.web.domain.boundedcontexts.semanticdata.SemanticData;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;

/**
 * Semantic Browser service.
 *
 * @author ntinsalhi
 */
@Service
public class SemanticBrowserService {

    private static final String FUNCTION_CATEGORY_BREAKDOWN = "Breakdown";

    private static final String FUNCTION_CATEGORY_ALLOCATING_LOGICAL_ACTOR = "Allocating Logical Actor";

    private static final String FUNCTION_CATEGORY_ALLOCATING_LOGICAL_COMPONENT = "Allocating Logical Component";

    private static final String FUNCTION_CATEGORY_FUNCTIONAL_CHAINS = "Functional Chains";

    private static final String FUNCTION_CATEGORY_INCOMING_FUNC_EXCHANGES = "Incoming Functional Exchanges";

    private static final String FUNCTION_CATEGORY_IN_FLOW = "In Flow Ports";

    private static final String FUNCTION_CATEGORY_OUT_FLOW = "Out Flow Ports";

    private static final String FUNCTION_CATEGORY_OUTGOING_FUNC_EXCHANGES = "Outgoing Functional Exchanges";

    private final IIdentityService identityService;

    private final IRepresentationMetadataSearchService representationMetadataSearchService;

    private final IRepresentationSearchService representationSearchService;

    private final TransverseQueryService transverseQueryService;

    public SemanticBrowserService(IRepresentationMetadataSearchService representationMetadataSearchService, IIdentityService identityService,
            IRepresentationSearchService representationSearchService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.representationMetadataSearchService = Objects.requireNonNull(representationMetadataSearchService);
        this.representationSearchService = Objects.requireNonNull(representationSearchService);
        this.transverseQueryService = new TransverseQueryService();
    }

    public List<String> collectExpandedNodeIndexes(VariableManager variableManager) {
        List<String> result = new ArrayList<>();
        List<TreeNode> nodes = variableManager.get(TreeComponent.NODES_VARIABLE, List.class).orElse(List.of());
        List<TreeNode> rootNodes = nodes.stream().filter(node -> Objects.isNull(node.getParentId())).toList();

        IntStream.range(0, rootNodes.size()).forEach(i -> {
            String rootIndex = String.valueOf(i);
            result.add(rootIndex);
            collectChildrenIndexes(rootNodes.get(i), nodes, rootIndex, result);
        });

        return result;
    }

    private void collectChildrenIndexes(TreeNode parent, List<TreeNode> allNodes, String parentIndex, List<String> result) {

        List<TreeNode> children = getNodeChildren(parent, allNodes);

        IntStream.range(0, children.size()).forEach(i -> {
            String childIndex = parentIndex + "/" + i;
            result.add(childIndex);
            collectChildrenIndexes(children.get(i), allNodes, childIndex, result);
        });
    }

    private List<TreeNode> getNodeChildren(TreeNode node, List<TreeNode> allNodes) {
        return allNodes.stream().filter(n -> Objects.nonNull(n.getParentId()) && n.getParentId().equals(node.getId())).toList();
    }

    public List<?> getCurrentCategories(EObject eObject) {
        if (eObject instanceof ActionUsage function && !this.transverseQueryService.getSubFunctions(function).isEmpty()) {
            return List.of(FUNCTION_CATEGORY_BREAKDOWN);
        }
        return List.of();
    }

    public List<?> getAllRelatedRepresentationMetadata(Object currentElement, IEditingContext editingContext) {
        List<?> result = List.of();
        var optionalSemanticDataId = new UUIDParser().parse(editingContext.getId());
        List<RepresentationMetadata> allRepresentationMetadata = this.representationMetadataSearchService.findAllRepresentationMetadataBySemanticData(
                AggregateReference.to(optionalSemanticDataId.get()));
        String currentElementId = this.identityService.getId(currentElement);
        return allRepresentationMetadata.stream().filter(this.containsCurrentElement(currentElementId, editingContext)).toList();
    }

    private Predicate<? super RepresentationMetadata> containsCurrentElement(String currentElementId, IEditingContext editingContext) {
        return representationMetadata -> representationSearchService.findById(editingContext, representationMetadata.getRepresentationMetadataId().toString(), Diagram.class)
                .filter(diagram -> diagram.getNodes().stream().anyMatch(node -> containsNodeRecursive(node, currentElementId)))
                .isPresent();
    }

    public Optional<RepresentationMetadata> getDiagramRepresentationMetadata(String editingContextId, Diagram diagram) {
        var optionalSemanticDataId = new UUIDParser().parse(editingContextId);
        if (optionalSemanticDataId.isPresent()) {
            var semanticData = AggregateReference.<SemanticData, UUID> to(optionalSemanticDataId.get());
            return this.representationMetadataSearchService.findMetadataById(semanticData, UUID.fromString(diagram.getId()));
        }
        return Optional.empty();
    }

    private boolean containsNodeRecursive(Node node, String targetId) {
        if (node.getTargetObjectId().equals(targetId)) {
            return true;
        }
        return node.getChildNodes().stream().anyMatch(child -> containsNodeRecursive(child, targetId));
    }

    public List<?> getParentCategoryElement(EObject element) {
        List<?> result = new ArrayList<>();

        if (element instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(actionUsage)) {
            var optParentFunction = this.transverseQueryService.getParentFunction(actionUsage);
            if (optParentFunction.isPresent()) {
                result = List.of(optParentFunction.get());
            } else {
                result = this.transverseQueryService.getFunctionsPackage(actionUsage)
                        .map(List::of)
                        .orElse(List.of());
            }
        }

        return result;
    }

    public List<?> getCurrentCategoryElements(EObject element, String category) {
        List<?> result = List.of();
        if (element instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(actionUsage)) {
            if (category.equals(FUNCTION_CATEGORY_BREAKDOWN)) {
                result = this.transverseQueryService.getSubFunctions(actionUsage);
            }
        }

        return result;
    }

    public List<?> getReferencingElementsCategories(EObject element) {
        List<String> result = new ArrayList<>();
        if (element instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(actionUsage)) {
            var functionComponent = this.transverseQueryService.getAllocatingComponent(actionUsage);
            var referencingFunctionalExchanges = this.transverseQueryService.getIncomingFunctionalExchanges(actionUsage);
            var functionalChains = this.transverseQueryService.getFunctionalChainsImpliedIn(actionUsage);

            if (functionComponent.isPresent()) {
                if (this.transverseQueryService.isComponentActor(functionComponent.get())) {
                    result.add(FUNCTION_CATEGORY_ALLOCATING_LOGICAL_ACTOR);
                } else {
                    result.add(FUNCTION_CATEGORY_ALLOCATING_LOGICAL_COMPONENT);
                }
            }

            if (!functionalChains.isEmpty()) {
                result.add(FUNCTION_CATEGORY_FUNCTIONAL_CHAINS);
            }

            if (!referencingFunctionalExchanges.isEmpty()) {
                result.addAll(List.of(FUNCTION_CATEGORY_INCOMING_FUNC_EXCHANGES, FUNCTION_CATEGORY_IN_FLOW));
            }

        }
        return result;
    }

    public List<?> getReferencingCategoryElements(EObject element, String category) {
        List<?> result = List.of();
        if (element instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(element)) {
            result = this.getFunctionReferencingCategoryElements(actionUsage, category);
        }
        return result;
    }

    private List<?> getFunctionReferencingCategoryElements(ActionUsage function, String category) {
        return switch (category) {
            case FUNCTION_CATEGORY_ALLOCATING_LOGICAL_ACTOR, FUNCTION_CATEGORY_ALLOCATING_LOGICAL_COMPONENT ->
                this.transverseQueryService.getAllocatingComponent(function).map(List::of).orElse(List.of());
            case FUNCTION_CATEGORY_FUNCTIONAL_CHAINS -> this.transverseQueryService.getFunctionalChainsImpliedIn(function);
            case FUNCTION_CATEGORY_INCOMING_FUNC_EXCHANGES -> this.transverseQueryService.getIncomingFunctionalExchanges(function);
            case FUNCTION_CATEGORY_IN_FLOW -> this.transverseQueryService.getFunctionPorts(function).stream().filter(this.transverseQueryService::isInFeature).toList();
            default -> List.of();
        };
    }

    public List<?> getFunctionalExchangeCategoryReferencedBy(FlowUsage flowUsage) {
        return flowUsage.getSource();
    }

    public List<?> getReferencedElementsCategories(EObject element) {
        List<String> result = new ArrayList<>();
        if (element instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(actionUsage)) {
            var referencingFunctionalExchanges = this.transverseQueryService.getOutgoingFunctionalExchanges(actionUsage);

            if (!referencingFunctionalExchanges.isEmpty()) {
                result.addAll(List.of(FUNCTION_CATEGORY_OUTGOING_FUNC_EXCHANGES, FUNCTION_CATEGORY_OUT_FLOW));
            }

        }
        return result;
    }

    public List<?> getReferencedCategoryElements(EObject element, String category) {
        List<?> result = List.of();
        if (element instanceof ActionUsage actionUsage && this.transverseQueryService.isFunction(element)) {
            result = this.getFunctionReferencedCategoryElements(actionUsage, category);
        }
        return result;
    }

    private List<?> getFunctionReferencedCategoryElements(ActionUsage function, String category) {
        return switch (category) {
            case FUNCTION_CATEGORY_OUTGOING_FUNC_EXCHANGES -> this.transverseQueryService.getOutgoingFunctionalExchanges(function);
            case FUNCTION_CATEGORY_OUT_FLOW -> this.transverseQueryService.getFunctionPorts(function).stream().filter(this.transverseQueryService::isOutFeature).toList();
            default -> List.of();
        };
    }

    public List<?> getFunctionalExchangeCategoryIsReferencing(FlowUsage flowUsage) {
        return flowUsage.getTarget();
    }
}

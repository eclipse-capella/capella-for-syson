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
package org.eclipse.capella.diagram.customization.styles;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BinaryOperator;

import org.eclipse.capella.diagram.customization.styles.api.IFunctionalChainStyleCustomizer;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationEventProcessor;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.EdgeAppearanceHandler;
import org.eclipse.sirius.components.collaborative.diagrams.RectangularNodeAppearanceHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramPostProcessor;
import org.eclipse.sirius.components.collaborative.editingcontext.RepresentationEventProcessorEntry;
import org.eclipse.sirius.components.collaborative.representations.api.IRepresentationEventProcessorRegistry;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.EdgeStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.emf.diagram.IDiagramIdProvider;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;
import org.springframework.stereotype.Service;

/**
 * Computes automatic chain colors and memberships once after a supported diagram refresh.
 * All allocation and semantic indexes are scoped to one invocation.
 *
 * @author Jerome Gout
 */
@Service
public class FunctionalChainStylePostProcessor implements IDiagramPostProcessor {

    private final List<IFunctionalChainStyleCustomizer> customizers;

    private final IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService;

    private final IDiagramIdProvider diagramIdProvider;

    private final IObjectSearchService objectSearchService;

    private final IRepresentationEventProcessorRegistry representationEventProcessorRegistry;

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final FunctionalChainColorAllocator functionalChainColorAllocator = new FunctionalChainColorAllocator();

    public FunctionalChainStylePostProcessor(IViewDiagramDescriptionSearchService viewDiagramDescriptionSearchService, IDiagramIdProvider diagramIdProvider,
            IObjectSearchService objectSearchService, IRepresentationEventProcessorRegistry representationEventProcessorRegistry, List<IFunctionalChainStyleCustomizer> customizers) {
        this.customizers = List.copyOf(customizers);
        this.viewDiagramDescriptionSearchService = Objects.requireNonNull(viewDiagramDescriptionSearchService);
        this.diagramIdProvider = Objects.requireNonNull(diagramIdProvider);
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.representationEventProcessorRegistry = Objects.requireNonNull(representationEventProcessorRegistry);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, DiagramContext diagramContext) {
        return this.viewDiagramDescriptionSearchService.findById(editingContext, diagramContext.diagram().getDescriptionId()).flatMap(this::findCustomizer).isPresent();
    }

    private Optional<IFunctionalChainStyleCustomizer> findCustomizer(DiagramDescription description) {
        return this.customizers.stream().filter(customizer -> customizer.canHandle(description)).findFirst();
    }

    @Override
    public Optional<Diagram> postProcess(IEditingContext editingContext, DiagramContext diagramContext) {
        Diagram diagram = diagramContext.diagram();
        return this.viewDiagramDescriptionSearchService.findById(editingContext, diagram.getDescriptionId())
                .flatMap(description -> this.findCustomizer(description).map(customizer -> this.apply(editingContext, diagram, description, customizer)));
    }

    private Diagram apply(IEditingContext context, Diagram diagram, DiagramDescription description, IFunctionalChainStyleCustomizer customizer) {
        if (FunctionalChainStyleConstants.COLORS.isEmpty()) {
            return diagram;
        }
        Set<String> functionalChainDescriptionIds = new HashSet<>();
        Set<String> functionDescriptionIds = new HashSet<>();
        Set<String> functionalExchangeDescriptionIds = new HashSet<>();
        description.getNodeDescriptions().forEach(node -> this.collectNodeDescriptionIds(node, functionalChainDescriptionIds, functionDescriptionIds, customizer));
        description.getEdgeDescriptions().stream().filter(customizer::isFunctionalExchangeEdgeDescription)
                .forEach(edge -> functionalExchangeDescriptionIds.add(this.diagramIdProvider.getId(edge)));
        List<Node> diagramNodes = this.allNodes(diagram);
        Map<String, Optional<ActionUsage>> resolvedFunctions = new HashMap<>();
        var chains = new TreeMap<String, ActionUsage>();
        for (Node node : diagramNodes) {
            if (functionalChainDescriptionIds.contains(node.getDescriptionId())) {
                this.objectSearchService.getObject(context, node.getTargetObjectId())
                        .filter(this::isArcadiaFunctionalChain)
                        .map(ActionUsage.class::cast)
                        .ifPresent(chain -> chains.put(node.getTargetObjectId(), chain));
            } else if (functionDescriptionIds.contains(node.getDescriptionId())) {
                resolvedFunctions.computeIfAbsent(node.getTargetObjectId(), id -> this.objectSearchService.getObject(context, id)
                        .filter(ActionUsage.class::isInstance).map(ActionUsage.class::cast));
            }
        }
        Map<String, Optional<FlowUsage>> resolvedExchanges = this.resolveFunctionalExchanges(context, diagram.getEdges(), functionalExchangeDescriptionIds);
        Map<String, String> previousColors = this.previousColors(context, diagram, functionalChainDescriptionIds);
        Map<String, String> colors = this.functionalChainColorAllocator.allocate(chains.keySet(), previousColors, FunctionalChainStyleConstants.COLORS);
        Map<Object, Set<String>> memberships = this.memberships(chains);
        var updatedNodes = diagram.getNodes().stream()
                .map(node -> this.updateNodeStyle(node, functionalChainDescriptionIds, functionDescriptionIds, resolvedFunctions, colors, memberships, customizer)).toList();
        var updateEdges = diagram.getEdges().stream()
                .map(edge -> this.updateEdgeStyle(edge, functionalExchangeDescriptionIds, resolvedExchanges, colors, memberships, customizer)).toList();
        return Diagram.newDiagram(diagram)
                .nodes(updatedNodes)
                .edges(updateEdges)
                .build();
    }

    private Map<String, Optional<FlowUsage>> resolveFunctionalExchanges(IEditingContext context, List<Edge> edges, Set<String> functionalExchangeDescriptionIds) {
        Map<String, Optional<FlowUsage>> resolvedExchanges = new HashMap<>();
        edges.stream().filter(edge -> functionalExchangeDescriptionIds.contains(edge.getDescriptionId()))
                .forEach(edge -> resolvedExchanges.computeIfAbsent(edge.getTargetObjectId(), id -> this.objectSearchService.getObject(context, id)
                        .filter(FlowUsage.class::isInstance).map(FlowUsage.class::cast)));
        return resolvedExchanges;
    }

    private boolean isArcadiaFunctionalChain(Object object) {
        return object instanceof ActionUsage chain && this.commonQueryService.isFunctionalChain(chain);
    }

    private void collectNodeDescriptionIds(NodeDescription node, Set<String> functionalChainDescriptionIds, Set<String> functionDescriptionIds, IFunctionalChainStyleCustomizer customizer) {
        if (customizer.isFunctionalChainNodeDescription(node)) {
            functionalChainDescriptionIds.add(this.diagramIdProvider.getId(node));
        } else if (customizer.isFunctionNodeDescription(node)) {
            functionDescriptionIds.add(this.diagramIdProvider.getId(node));
        }
        node.getChildrenDescriptions().forEach(child -> this.collectNodeDescriptionIds(child, functionalChainDescriptionIds, functionDescriptionIds, customizer));
        node.getBorderNodesDescriptions().forEach(child -> this.collectNodeDescriptionIds(child, functionalChainDescriptionIds, functionDescriptionIds, customizer));
    }

    private List<Node> allNodes(Diagram diagram) {
        var pending = new ArrayDeque<>(diagram.getNodes());
        List<Node> nodes = new ArrayList<>();
        while (!pending.isEmpty()) {
            Node node = pending.removeFirst();
            nodes.add(node);
            pending.addAll(node.getChildNodes());
            pending.addAll(node.getBorderNodes());
        }
        return nodes;
    }

    private Map<String, String> previousColors(IEditingContext context, Diagram diagram, Set<String> functionalChainDescriptionIds) {
        Map<String, String> colors = new HashMap<>();
        Optional.ofNullable(this.representationEventProcessorRegistry.get(context.getId(), diagram.getId()))
                .map(RepresentationEventProcessorEntry::getRepresentationEventProcessor)
                .map(IRepresentationEventProcessor::getRepresentation)
                .filter(Diagram.class::isInstance)
                .map(Diagram.class::cast)
                .ifPresent(previous -> this.allNodes(previous).forEach(node -> {
                    if (functionalChainDescriptionIds.contains(node.getDescriptionId())
                            && !node.getCustomizedStyleProperties().contains(RectangularNodeAppearanceHandler.BACKGROUND)
                            && node.getStyle() instanceof RectangularNodeStyle style && FunctionalChainStyleConstants.COLORS.contains(style.getBackground())) {
                        colors.merge(node.getTargetObjectId(), style.getBackground(), BinaryOperator.minBy(Comparator.naturalOrder()));
                    }
                }));
        return colors;
    }

    private Map<Object, Set<String>> memberships(Map<String, ActionUsage> chains) {
        Map<Object, Set<String>> memberships = new HashMap<>();
        Map<FlowUsage, Set<ActionUsage>> endpoints = new HashMap<>();
        chains.forEach((id, chain) -> {
            for (var exchange : new HashSet<>(this.commonQueryService.getInvolvedFunctionalExchanges(chain))) {
                memberships.computeIfAbsent(exchange, key -> new HashSet<>()).add(id);
                endpoints.computeIfAbsent(exchange, this::endpointFunctions).forEach(function -> memberships.computeIfAbsent(function, key -> new HashSet<>()).add(id));
            }
        });
        return memberships;
    }

    private Set<ActionUsage> endpointFunctions(FlowUsage exchange) {
        Set<ActionUsage> functions = new HashSet<>();
        Optional.ofNullable(this.commonQueryService.getFunctionalExchangeSourceFunction(exchange)).ifPresent(functions::add);
        Optional.ofNullable(this.commonQueryService.getFunctionalExchangeTargetFunction(exchange)).ifPresent(functions::add);
        return functions;
    }

    private Node updateNodeStyle(Node node, Set<String> functionalChainDescriptionIds, Set<String> functionDescriptionIds, Map<String, Optional<ActionUsage>> resolvedFunctions,
            Map<String, String> colors, Map<Object, Set<String>> memberships, IFunctionalChainStyleCustomizer customizer) {
        var children = node.getChildNodes().stream().map(child -> this.updateNodeStyle(child, functionalChainDescriptionIds, functionDescriptionIds, resolvedFunctions, colors, memberships, customizer)).toList();
        var borders = node.getBorderNodes().stream().map(child -> this.updateNodeStyle(child, functionalChainDescriptionIds, functionDescriptionIds, resolvedFunctions, colors, memberships, customizer)).toList();
        var style = node.getStyle();
        if (style instanceof RectangularNodeStyle rectangle) {
            if (functionalChainDescriptionIds.contains(node.getDescriptionId())) {
                style = this.updateFunctionalChainStyle(node, rectangle, colors);
            } else if (functionDescriptionIds.contains(node.getDescriptionId())) {
                style = this.updateFunctionStyle(node, rectangle, resolvedFunctions, memberships, colors, customizer);
            }
        }
        if (style == node.getStyle() && children.equals(node.getChildNodes()) && borders.equals(node.getBorderNodes())) {
            return node;
        }
        return Node.newNode(node).style(style).childNodes(children).borderNodes(borders).build();
    }

    private RectangularNodeStyle updateFunctionalChainStyle(Node node, RectangularNodeStyle style, Map<String, String> colors) {
        boolean hasCustomBackground = node.getCustomizedStyleProperties().contains(RectangularNodeAppearanceHandler.BACKGROUND);
        if (hasCustomBackground || !colors.containsKey(node.getTargetObjectId())) {
            return style;
        }
        return RectangularNodeStyle.newRectangularNodeStyle(style)
                .background(colors.get(node.getTargetObjectId()))
                .build();
    }

    private RectangularNodeStyle updateFunctionStyle(
            Node node,
            RectangularNodeStyle style,
            Map<String, Optional<ActionUsage>> resolvedFunctions,
            Map<Object, Set<String>> memberships,
            Map<String, String> colors,
            IFunctionalChainStyleCustomizer customizer) {

        ActionUsage function = resolvedFunctions
                .getOrDefault(node.getTargetObjectId(), Optional.empty())
                .orElse(null);
        Set<String> involvedChains = memberships.getOrDefault(function, Set.of());
        if (involvedChains.isEmpty()) {
            return style;
        }
        return this.functionStyle(style, node.getCustomizedStyleProperties(), involvedChains, colors, customizer);
    }

    private RectangularNodeStyle functionStyle(RectangularNodeStyle rectangle, Set<String> custom, Set<String> involved, Map<String, String> colors, IFunctionalChainStyleCustomizer customizer) {
        var customFunctionStyle = customizer.customizeFunctionStyle(rectangle, this.singleChainColor(involved, colors));
        var builder = RectangularNodeStyle.newRectangularNodeStyle(rectangle);
        if (!custom.contains(RectangularNodeAppearanceHandler.BORDER_COLOR)) {
            builder.borderColor(customFunctionStyle.getBorderColor());
        }
        if (!custom.contains(RectangularNodeAppearanceHandler.BORDER_SIZE)) {
            builder.borderSize(customFunctionStyle.getBorderSize());
        }
        if (!custom.contains(RectangularNodeAppearanceHandler.BORDER_RADIUS)) {
            builder.borderRadius(customFunctionStyle.getBorderRadius());
        }
        return builder.build();
    }

    private Edge updateEdgeStyle(Edge edge, Set<String> functionalExchangeDescriptionIds, Map<String, Optional<FlowUsage>> resolvedExchanges,
            Map<String, String> colors, Map<Object, Set<String>> memberships, IFunctionalChainStyleCustomizer customizer) {
        Set<String> involved = memberships.getOrDefault(resolvedExchanges.getOrDefault(edge.getTargetObjectId(), Optional.empty()).orElse(null), Set.of());
        if (!functionalExchangeDescriptionIds.contains(edge.getDescriptionId()) || involved.isEmpty()) {
            return edge;
        }
        var automatic = customizer.customizeFunctionalExchangeStyle(edge.getStyle(), this.singleChainColor(involved, colors));
        var builder = EdgeStyle.newEdgeStyle(edge.getStyle());
        Set<String> custom = edge.getCustomizedStyleProperties();
        if (!custom.contains(EdgeAppearanceHandler.COLOR)) {
            builder.color(automatic.getColor());
        }
        if (!custom.contains(EdgeAppearanceHandler.SIZE)) {
            builder.size(automatic.getSize());
        }
        if (!custom.contains(EdgeAppearanceHandler.TARGET_ARROW)) {
            builder.targetArrow(automatic.getTargetArrow());
        }
        return Edge.newEdge(edge).style(builder.build()).build();
    }

    private Optional<String> singleChainColor(Set<String> involved, Map<String, String> colors) {
        if (involved.size() == 1) {
            return Optional.of(colors.get(involved.iterator().next()));
        }
        return Optional.empty();
    }
}

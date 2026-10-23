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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.capella.diagram.customization.styles.api.IFunctionalChainStyleCustomizer;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.sirius.components.collaborative.api.IRepresentationEventProcessor;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.EdgeAppearanceHandler;
import org.eclipse.sirius.components.collaborative.diagrams.RectangularNodeAppearanceHandler;
import org.eclipse.sirius.components.collaborative.editingcontext.RepresentationEventProcessorEntry;
import org.eclipse.sirius.components.collaborative.representations.api.IRepresentationEventProcessorRegistry;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.ArrowStyle;
import org.eclipse.sirius.components.diagrams.CollapsingState;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.DiagramStyle;
import org.eclipse.sirius.components.diagrams.Edge;
import org.eclipse.sirius.components.diagrams.EdgeStyle;
import org.eclipse.sirius.components.diagrams.EdgeType;
import org.eclipse.sirius.components.diagrams.FreeFormLayoutStrategy;
import org.eclipse.sirius.components.diagrams.LineStyle;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.NodeType;
import org.eclipse.sirius.components.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.components.diagrams.ViewModifier;
import org.eclipse.sirius.components.diagrams.components.BorderNodePosition;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramFactory;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.emf.diagram.IDiagramIdProvider;
import org.eclipse.sirius.components.view.emf.diagram.api.IViewDiagramDescriptionSearchService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.Disposables;

/**
 * Exercises the post-processor with real Capella semantics and native graphical objects.
 *
 * @author Jerome Gout
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class FunctionalChainStylePostProcessorTests {

    private static final String SAB_VIEW_ID = "SystemAnalysisBlank";

    private static final String SAB_DESCRIPTION_NAME = "SAB - System Analysis Blank";

    private static final String SAB_CHAIN = "SABFunctionalChainNodeDescription";

    private static final String SAB_FUNCTION = "SABFunctionNodeDescription";

    private static final String SAB_EXCHANGE = "SABFunctionalExchangeEdgeDescription";

    private static final SemanticDataTestFixture SEMANTIC_DATA = new SemanticDataTestFixture();

    private CapellaModel capellaModel;

    private final CommonCreationService creation = new CommonCreationService();

    private final IEditingContext context = new IEditingContext.NoOp();

    private final Map<String, Object> semanticObjects = new HashMap<>();

    private IRepresentationEventProcessorRegistry registry;

    private IObjectSearchService objectSearch;

    private FunctionalChainStylePostProcessor processor;

    private DiagramDescription description;

    private ActionUsage first;

    private ActionUsage second;

    private FlowUsage exchange;

    @BeforeEach
    public void setUp() {
        this.capellaModel = SEMANTIC_DATA.createCapellaModel();
        this.semanticObjects.clear();
        var component = this.creation.createComponent(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement());
        this.first = this.creation.createFunction(component);
        this.second = this.creation.createFunction(this.first);
        this.exchange = this.creation.createFunctionalExchange(this.first, this.second);
        this.semanticObjects.put("first", this.first);
        this.semanticObjects.put("second", this.second);
        this.semanticObjects.put("exchange", this.exchange);
        this.description = DiagramFactory.eINSTANCE.createDiagramDescription();
        this.description.setName(SAB_DESCRIPTION_NAME);
        var resource = new ResourceImpl(URI.createURI("emf:///" + UUID.nameUUIDFromBytes(SAB_VIEW_ID.getBytes(StandardCharsets.UTF_8))));
        resource.getContents().add(this.description);
        for (String name : List.of(SAB_CHAIN, SAB_FUNCTION)) {
            var node = DiagramFactory.eINSTANCE.createNodeDescription();
            node.setName(name);
            this.description.getNodeDescriptions().add(node);
        }
        var edge = DiagramFactory.eINSTANCE.createEdgeDescription();
        edge.setName(SAB_EXCHANGE);
        this.description.getEdgeDescriptions().add(edge);
        this.objectSearch = mock(IObjectSearchService.class);
        when(this.objectSearch.getObject(any(), anyString())).thenAnswer(invocation -> Optional.ofNullable(this.semanticObjects.get(invocation.getArgument(1))));
        this.registry = mock(IRepresentationEventProcessorRegistry.class);
        this.processor = this.processorFor(List.of(this.sabCustomizer()));
    }

    private IFunctionalChainStyleCustomizer sabCustomizer() {
        return this.customizerFor(SAB_DESCRIPTION_NAME, Set.of(SAB_CHAIN), Set.of(SAB_FUNCTION), Set.of(SAB_EXCHANGE), ArrowStyle.InputArrow);
    }

    private IFunctionalChainStyleCustomizer customizerFor(String name, Set<String> chains, Set<String> functions, Set<String> exchanges, ArrowStyle arrow) {
        var customizer = mock(IFunctionalChainStyleCustomizer.class);
        when(customizer.canHandle(any())).thenAnswer(invocation -> name.equals(invocation.<DiagramDescription>getArgument(0).getName()));
        when(customizer.isFunctionalChainNodeDescription(any())).thenAnswer(invocation -> chains.contains(invocation.<NodeDescription>getArgument(0).getName()));
        when(customizer.isFunctionNodeDescription(any())).thenAnswer(invocation -> functions.contains(invocation.<NodeDescription>getArgument(0).getName()));
        when(customizer.isFunctionalExchangeEdgeDescription(any())).thenAnswer(invocation -> exchanges.contains(invocation.<EdgeDescription>getArgument(0).getName()));
        when(customizer.customizeFunctionStyle(any(), any())).thenAnswer(invocation -> {
            RectangularNodeStyle style = invocation.getArgument(0);
            Optional<String> color = invocation.getArgument(1);
            return RectangularNodeStyle.newRectangularNodeStyle(style).borderColor(color.orElse(FunctionalChainStyleConstants.OVERLAP_COLOR)).borderSize(4).borderRadius(8).build();
        });
        when(customizer.customizeFunctionalExchangeStyle(any(), any())).thenAnswer(invocation -> {
            EdgeStyle style = invocation.getArgument(0);
            Optional<String> color = invocation.getArgument(1);
            return EdgeStyle.newEdgeStyle(style).color(color.orElse(FunctionalChainStyleConstants.OVERLAP_COLOR)).size(3).targetArrow(arrow).build();
        });
        return customizer;
    }

    private FunctionalChainStylePostProcessor processorFor(List<IFunctionalChainStyleCustomizer> customizers) {
        var descriptions = mock(IViewDiagramDescriptionSearchService.class);
        when(descriptions.findById(any(), anyString())).thenReturn(Optional.of(this.description));
        var ids = mock(IDiagramIdProvider.class);
        when(ids.getId(any(DiagramElementDescription.class))).thenAnswer(invocation -> ((DiagramElementDescription) invocation.getArgument(0)).getName());
        return new FunctionalChainStylePostProcessor(descriptions, ids, this.objectSearch, this.registry, customizers);
    }

    @Test
    public void representedChainColorsNestedFunctionsAndAllExchangeViews() {
        this.chain("a");
        var nested = Node.newNode(this.node("first", SAB_FUNCTION)).childNodes(List.of(this.node("second", SAB_FUNCTION))).build();
        var duplicate = Node.newNode(this.node("first-copy", SAB_FUNCTION)).targetObjectId("first").build();
        var result = this.process(this.diagram(List.of(this.node("a", SAB_CHAIN), nested, duplicate), List.of(this.edge("one"), this.edge("two"))));
        assertThat(this.background(result.getNodes().getFirst())).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(this.rectangle(result.getNodes().get(1).getChildNodes().getFirst()).getBorderSize()).isEqualTo(4);
        assertThat(this.rectangle(result.getNodes().get(2)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(result.getEdges()).allSatisfy(edge -> {
            assertThat(edge.getStyle().getColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
            assertThat(edge.getStyle().getTargetArrow()).isEqualTo(ArrowStyle.InputArrow);
            assertThat(edge.getSourceId()).isEqualTo("source-port");
        });
        verify(this.objectSearch, times(1)).getObject(this.context, "exchange");
        verify(this.objectSearch, times(1)).getObject(this.context, "first");
    }

    @Test
    public void multipleExchangesOfOneChainDoNotCreateOverlapAndAbsentExchangeViewsStillImplyFunctions() {
        var another = this.creation.createFunctionalExchange(this.first, this.second);
        this.semanticObjects.put("a", this.creation.createFunctionalChain(this.first, List.of(this.exchange, another)));
        var result = this.process(this.diagram(List.of(this.node("a", SAB_CHAIN), this.node("first", SAB_FUNCTION)), List.of()));
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
    }

    @Test
    public void directChainReferencesAcrossResourcesImplyEndpointFunctions() {
        this.chain("a");
        var resource = new ResourceImpl(URI.createURI("emf:///" + UUID.randomUUID()));
        this.capellaModel.getResource().getResourceSet().getResources().add(resource);
        var container = SysmlFactory.eINSTANCE.createPackage();
        resource.getContents().add(container);
        container.getOwnedRelationship().add(this.exchange.getOwningRelationship());
        assertThat(this.exchange.eResource()).isNotEqualTo(this.first.eResource());
        var result = this.process(this.diagram(List.of(this.node("a", SAB_CHAIN), this.node("first", SAB_FUNCTION)), List.of(this.edge("edge"))));
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(result.getEdges().getFirst().getStyle().getColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
    }

    @Test
    public void hiddenChainsContributeAndDistinctChainsSharingAColorStillOverlap() {
        this.chain("a");
        this.chain("b");
        var a = this.node("a", SAB_CHAIN);
        var b = this.node("b", SAB_CHAIN);
        this.previous(this.diagram(List.of(this.colored(a, FunctionalChainStyleConstants.BLUE), this.colored(b, FunctionalChainStyleConstants.BLUE)), List.of()));
        var result = this.process(this.diagram(List.of(Node.newNode(a).state(ViewModifier.Hidden).build(), b, this.node("first", SAB_FUNCTION)), List.of(this.edge("edge"))));
        assertThat(this.rectangle(result.getNodes().get(2)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.OVERLAP_COLOR);
        assertThat(result.getEdges().getFirst().getStyle().getColor()).isEqualTo(FunctionalChainStyleConstants.OVERLAP_COLOR);
    }

    @Test
    public void removalPreservesSurvivorAndNewChainReusesReleasedBlue() {
        this.chain("a");
        this.chain("b");
        this.chain("c");
        var a = this.node("a", SAB_CHAIN);
        var b = this.node("b", SAB_CHAIN);
        var c = this.node("c", SAB_CHAIN);
        this.previous(this.diagram(List.of(this.colored(a, FunctionalChainStyleConstants.BLUE), this.colored(b, FunctionalChainStyleConstants.PURPLE)), List.of()));
        var result = this.process(this.diagram(List.of(b, c), List.of()));
        assertThat(this.background(result.getNodes().getFirst())).isEqualTo(FunctionalChainStyleConstants.PURPLE);
        assertThat(this.background(result.getNodes().get(1))).isEqualTo(FunctionalChainStyleConstants.BLUE);
    }

    @Test
    public void customizationsArePreservedIndependentlyAndDoNotPropagateFromChain() {
        this.chain("a");
        var chain = Node.newNode(this.colored(this.node("a", SAB_CHAIN), FunctionalChainStyleConstants.PURPLE))
                .customizedStyleProperties(Set.of(RectangularNodeAppearanceHandler.BACKGROUND)).build();
        var function = this.node("first", SAB_FUNCTION);
        function = Node.newNode(function).style(RectangularNodeStyle.newRectangularNodeStyle(this.rectangle(function)).borderColor("pink").borderSize(9).borderRadius(19).build())
                .customizedStyleProperties(Set.of(RectangularNodeAppearanceHandler.BORDER_COLOR, RectangularNodeAppearanceHandler.BORDER_SIZE, RectangularNodeAppearanceHandler.BORDER_RADIUS)).build();
        var edge = this.edge("edge");
        edge = Edge.newEdge(edge).style(EdgeStyle.newEdgeStyle(edge.getStyle()).color("pink").size(9).targetArrow(ArrowStyle.None).build())
                .customizedStyleProperties(Set.of(EdgeAppearanceHandler.COLOR, EdgeAppearanceHandler.SIZE, EdgeAppearanceHandler.TARGET_ARROW)).build();
        this.previous(this.diagram(List.of(chain), List.of()));
        var result = this.process(this.diagram(List.of(chain, function, this.node("second", SAB_FUNCTION)), List.of(edge)));
        assertThat(this.background(result.getNodes().getFirst())).isEqualTo(FunctionalChainStyleConstants.PURPLE);
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderSize()).isEqualTo(9);
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderRadius()).isEqualTo(19);
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderColor()).isEqualTo("pink");
        assertThat(this.rectangle(result.getNodes().get(2)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(result.getEdges().getFirst().getStyle().getColor()).isEqualTo("pink");
        assertThat(result.getEdges().getFirst().getStyle().getSize()).isEqualTo(9);
        assertThat(result.getEdges().getFirst().getStyle().getTargetArrow()).isEqualTo(ArrowStyle.None);
        assertThat(result.getNodes().getFirst().getCustomizedStyleProperties()).containsExactly(RectangularNodeAppearanceHandler.BACKGROUND);
    }

    @Test
    public void unrepresentedChainsDoNotContributeAndOtherDiagramsAreRejected() {
        this.chain("a");
        var base = this.diagram(List.of(this.node("first", SAB_FUNCTION)), List.of(this.edge("edge")));
        var result = this.process(base);
        assertThat(this.rectangle(result.getNodes().getFirst()).getBorderSize()).isEqualTo(2);
        assertThat(result.getEdges().getFirst().getStyle().getSize()).isEqualTo(2);
        this.description.setName("LAB - Logical Architecture Blank");
        assertThat(this.processor.canHandle(this.context, new DiagramContext(base))).isFalse();
        assertThat(this.processor.postProcess(this.context, new DiagramContext(base))).isEmpty();
    }

    @Test
    public void secondCustomizerRecognizesItsElementsAndSuppliesItsOwnStyles() {
        this.chain("a");
        this.description.setName("Other diagram");
        this.description.getNodeDescriptions().getFirst().setName("OtherChain");
        this.description.getNodeDescriptions().get(1).setName("OtherFunction");
        var nestedMapping = DiagramFactory.eINSTANCE.createNodeDescription();
        nestedMapping.setName("OtherNestedFunction");
        this.description.getNodeDescriptions().get(1).getChildrenDescriptions().add(nestedMapping);
        this.description.getEdgeDescriptions().getFirst().setName("OtherExchange");
        var customizer = this.customizerFor("Other diagram", Set.of("OtherChain"), Set.of("OtherFunction", "OtherNestedFunction"), Set.of("OtherExchange"), ArrowStyle.None);
        doAnswer(invocation -> RectangularNodeStyle.newRectangularNodeStyle(invocation.<RectangularNodeStyle>getArgument(0))
                .borderColor(invocation.<Optional<String>>getArgument(1).orElseThrow()).borderSize(6).borderRadius(12).build())
                .when(customizer).customizeFunctionStyle(any(), any());
        doAnswer(invocation -> EdgeStyle.newEdgeStyle(invocation.<EdgeStyle>getArgument(0))
                .color(invocation.<Optional<String>>getArgument(1).orElseThrow()).size(5).targetArrow(ArrowStyle.None).build())
                .when(customizer).customizeFunctionalExchangeStyle(any(), any());
        this.processor = this.processorFor(List.of(this.sabCustomizer(), customizer));
        var edge = Edge.newEdge(this.edge("edge")).descriptionId("OtherExchange").build();
        var customized = Edge.newEdge(this.edge("customized")).descriptionId("OtherExchange")
                .style(EdgeStyle.newEdgeStyle(edge.getStyle()).targetArrow(ArrowStyle.InputArrow).build())
                .customizedStyleProperties(Set.of(EdgeAppearanceHandler.TARGET_ARROW)).build();
        var diagram = this.diagram(List.of(this.node("a", "OtherChain"), this.node("first", "OtherFunction"), this.node("second", "OtherNestedFunction"),
                this.node("first", SAB_FUNCTION)), List.of(edge, customized));
        assertThat(this.processor.canHandle(this.context, new DiagramContext(diagram))).isTrue();
        var result = this.process(diagram);
        assertThat(this.background(result.getNodes().getFirst())).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(this.rectangle(result.getNodes().get(1)).getBorderColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(this.rectangle(result.getNodes().get(2)).getBorderSize()).isEqualTo(6);
        assertThat(this.rectangle(result.getNodes().get(2)).getBorderRadius()).isEqualTo(12);
        assertThat(this.rectangle(result.getNodes().get(3)).getBorderSize()).isEqualTo(2);
        assertThat(result.getEdges().getFirst().getStyle().getColor()).isEqualTo(FunctionalChainStyleConstants.BLUE);
        assertThat(result.getEdges().getFirst().getStyle().getSize()).isEqualTo(5);
        assertThat(result.getEdges().getFirst().getStyle().getTargetArrow()).isEqualTo(ArrowStyle.None);
        assertThat(result.getEdges().get(1).getStyle().getTargetArrow()).isEqualTo(ArrowStyle.InputArrow);
    }

    @Test
    public void noRegisteredCustomizersLeavesDiagramUnsupported() {
        this.processor = this.processorFor(List.of());
        var diagramContext = new DiagramContext(this.diagram(List.of(), List.of()));
        assertThat(this.processor.canHandle(this.context, diagramContext)).isFalse();
        assertThat(this.processor.postProcess(this.context, diagramContext)).isEmpty();
    }

    private void chain(String id) {
        this.semanticObjects.put(id, this.creation.createFunctionalChain(this.first, List.of(this.exchange)));
    }

    private void previous(Diagram diagram) {
        var eventProcessor = mock(IRepresentationEventProcessor.class);
        when(eventProcessor.getRepresentation()).thenReturn(diagram);
        when(this.registry.get(this.context.getId(), diagram.getId())).thenReturn(new RepresentationEventProcessorEntry(eventProcessor, Disposables.disposed()));
    }

    private Diagram process(Diagram diagram) {
        return this.processor.postProcess(this.context, new DiagramContext(diagram)).orElseThrow();
    }

    private Diagram diagram(List<Node> nodes, List<Edge> edges) {
        return Diagram.newDiagram("diagram").descriptionId("sab").targetObjectId("root").nodes(nodes).edges(edges).style(DiagramStyle.newDiagramStyle().build()).build();
    }

    private Node node(String id, String mapping) {
        return Node.newNode(id).type(NodeType.NODE_RECTANGLE).targetObjectId(id).targetObjectKind("").targetObjectLabel(id).descriptionId(mapping)
                .borderNode(false).initialBorderNodePosition(BorderNodePosition.EAST).modifiers(Set.of()).state(ViewModifier.Normal).collapsingState(CollapsingState.EXPANDED)
                .outsideLabels(List.of()).style(RectangularNodeStyle.newRectangularNodeStyle().background("#F4E9BD").borderColor("#5E8F2D").borderSize(2)
                        .borderStyle(LineStyle.Solid).childrenLayoutStrategy(new FreeFormLayoutStrategy()).build())
                .borderNodes(List.of()).childNodes(List.of()).labelEditable(false).deletable(true).pinned(false).customizedStyleProperties(Set.of()).decorators(List.of()).build();
    }

    private Node colored(Node node, String color) {
        return Node.newNode(node).style(RectangularNodeStyle.newRectangularNodeStyle(this.rectangle(node)).background(color).build()).build();
    }

    private RectangularNodeStyle rectangle(Node node) {
        return (RectangularNodeStyle) node.getStyle();
    }

    private String background(Node node) {
        return this.rectangle(node).getBackground();
    }

    private Edge edge(String id) {
        return Edge.newEdge(id).type("edge").targetObjectId("exchange").targetObjectKind("").targetObjectLabel("Exchange").descriptionId(SAB_EXCHANGE)
                .sourceId("source-port").targetId("target-port").modifiers(Set.of()).state(ViewModifier.Normal)
                .style(EdgeStyle.newEdgeStyle().color("#325C14").size(2).lineStyle(LineStyle.Solid).sourceArrow(ArrowStyle.None).targetArrow(ArrowStyle.None).edgeType(EdgeType.Manhattan).build())
                .centerLabelEditable(true).deletable(true).customizedStyleProperties(Set.of()).build();
    }
}

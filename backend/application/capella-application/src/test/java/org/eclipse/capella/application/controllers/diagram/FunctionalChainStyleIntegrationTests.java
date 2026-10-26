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
package org.eclipse.capella.application.controllers.diagram;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.capella.AbstractIntegrationTests;
import org.eclipse.capella.diagram.customization.styles.FunctionalChainColorPaletteProvider;
import org.eclipse.capella.diagram.sab.view.SABViewDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.sab.view.nodes.functionalchain.FunctionalChainNodeDescriptionProvider;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.sirius.components.collaborative.api.ChangeDescription;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceService;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramEventProcessorFactory;
import org.eclipse.sirius.components.collaborative.diagrams.RectangularNodeAppearanceHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.collaborative.diagrams.dto.ResetNodeAppearanceInput;
import org.eclipse.sirius.components.collaborative.editingcontext.RepresentationEventProcessorEntry;
import org.eclipse.sirius.components.collaborative.representations.api.IRepresentationEventProcessorRegistry;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.core.api.IPayload;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.components.diagrams.ViewCreationRequest;
import org.eclipse.sirius.components.diagrams.ViewDeletionRequest;
import org.eclipse.sirius.components.diagrams.components.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.emf.diagram.IDiagramIdProvider;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.sirius.web.application.studio.services.ViewBasedRepresentationDescriptionConverter;
import org.eclipse.syson.sysml.ActionUsage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import reactor.core.Disposables;
import reactor.core.publisher.Sinks;

/**
 * Verifies SAB rendering, active-processor recovery, reset and native persistence together.
 *
 * @author Jerome Gout
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class FunctionalChainStyleIntegrationTests extends AbstractIntegrationTests {

    @Autowired
    private IDiagramCreationService diagrams;

    @Autowired
    private DiagramEventProcessorFactory factory;

    @Autowired
    private IRepresentationEventProcessorRegistry registry;

    @Autowired
    private IIdentityService identity;

    @Autowired
    private IDiagramIdProvider descriptionIds;

    @Autowired
    private ViewBasedRepresentationDescriptionConverter converter;

    @Autowired
    private IRepresentationPersistenceService persistence;

    @Autowired
    private IRepresentationSearchService search;

    @Autowired
    private JdbcTemplate jdbc;

    private final CommonCreationService creation = new CommonCreationService();

    private final List<ActionUsage> chains = new ArrayList<>();

    private EditingContext context;

    private Diagram initial;

    @AfterEach
    public void tearDown() {
        if (this.initial != null) {
            this.registry.disposeRepresentation(this.context.getId(), this.initial.getId());
            this.jdbc.update("DELETE FROM representation_metadata WHERE id = ?", this.context.getId() + "#" + this.initial.getId());
        }
        if (this.context != null) {
            this.context.dispose();
        }
    }

    @Test
    public void activeRefreshPreservesSurvivorsAfterRemovalAndReusesReleasedColor() {
        this.prepare(3);
        this.save(this.initial);
        var processor = this.factory.createRepresentationEventProcessor(this.context, this.initial.getId()).orElseThrow();
        this.registry.put(this.context.getId(), this.initial.getId(), new RepresentationEventProcessorEntry(processor, Disposables.disposed()));
        var before = (Diagram) processor.getRepresentation();
        assertThat(this.background(this.node(before, this.chains.get(1)))).isEqualTo("#7B2CBF");
        var removedNode = this.node(before, this.chains.getFirst());
        var withoutFirst = this.diagrams.refresh(this.context, new DiagramContext(before, List.of(),
                List.of(ViewDeletionRequest.newViewDeletionRequest().elementId(removedNode.getId()).build()), List.of())).orElseThrow();
        assertThat(this.background(this.node(withoutFirst, this.chains.get(1)))).isEqualTo("#7B2CBF");
        assertThat(this.background(this.node(withoutFirst, this.chains.get(2)))).isEqualTo("#C65D00");
        assertThat(withoutFirst.getNodes()).noneMatch(node -> node.getTargetObjectId().equals(this.identity.getId(this.chains.getFirst())));
        new CommonDeletionService().delete(this.chains.getFirst());
        processor.refresh(new ChangeDescription(ChangeKind.SEMANTIC_CHANGE, this.context.getId(), new TestInput(UUID.randomUUID())));
        var current = (Diagram) processor.getRepresentation();
        assertThat(this.background(this.node(current, this.chains.get(1)))).isEqualTo("#7B2CBF");
        var exchange = new CommonQueryService().getInvolvedFunctionalExchanges(this.chains.get(1)).getFirst();
        var added = this.creation.createFunctionalChain(this.chains.get(1), List.of(exchange));
        var restored = this.diagrams.refresh(this.context, new DiagramContext(current,
                List.of(this.request(current.getId(), added, FunctionalChainNodeDescriptionProvider.NODE_DESCRIPTION_NAME)), List.of(), List.of())).orElseThrow();
        assertThat(this.background(this.node(restored, this.chains.get(1)))).isEqualTo("#7B2CBF");
        assertThat(this.background(this.node(restored, added))).isEqualTo("#1864D9");
        assertThat(this.search.findById(this.context, current.getId(), Diagram.class)).isPresent();
    }

    @Test
    public void openingAndResetKeepAutomaticStylesCoherentAndPersistThemNatively() {
        this.prepare(1);
        Node chain = this.node(this.initial, this.chains.getFirst());
        var customized = Node.newNode(chain)
                .style(RectangularNodeStyle.newRectangularNodeStyle((RectangularNodeStyle) chain.getStyle()).background("#7B2CBF").build())
                .customizedStyleProperties(new LinkedHashSet<>(Set.of(RectangularNodeAppearanceHandler.BACKGROUND))).build();
        this.initial = Diagram.newDiagram(this.initial).nodes(this.initial.getNodes().stream().map(node -> {
            if (node.getId().equals(chain.getId())) {
                return customized;
            }
            return node;
        }).toList()).build();
        this.save(this.initial);
        var processor = this.factory.createRepresentationEventProcessor(this.context, this.initial.getId()).orElseThrow();
        this.registry.put(this.context.getId(), this.initial.getId(), new RepresentationEventProcessorEntry(processor, Disposables.disposed()));
        var opened = (Diagram) processor.getRepresentation();
        assertThat(this.background(this.node(opened, this.chains.getFirst()))).isEqualTo("#7B2CBF");
        assertThat(opened.getEdges()).hasSize(1);
        assertThat(opened.getEdges().getFirst().getStyle().getColor()).isEqualTo("#1864D9");
        List<ChangeDescription> changes = new ArrayList<>();
        Sinks.Many<ChangeDescription> changeSink = Sinks.many().unicast().onBackpressureBuffer();
        var subscription = changeSink.asFlux().subscribe(changes::add);
        Sinks.One<IPayload> payload = Sinks.one();
        processor.handle(payload, changeSink, new ResetNodeAppearanceInput(UUID.randomUUID(), this.context.getId(), opened.getId(),
                List.of(chain.getId()), List.of(RectangularNodeAppearanceHandler.BACKGROUND)));
        assertThat(changes).hasSize(1);
        processor.refresh(changes.getFirst());
        subscription.dispose();
        var result = (Diagram) processor.getRepresentation();
        var reset = this.node(result, this.chains.getFirst());
        assertThat(reset.getCustomizedStyleProperties()).doesNotContain(RectangularNodeAppearanceHandler.BACKGROUND);
        assertThat(result.getEdges().getFirst().getStyle().getColor()).isEqualTo(this.background(reset));
        var saved = this.search.findById(this.context, result.getId(), Diagram.class).orElseThrow();
        assertThat(this.background(this.node(saved, this.chains.getFirst()))).isEqualTo(this.background(reset));
        this.registry.disposeRepresentation(this.context.getId(), result.getId());
        var reopenedProcessor = this.factory.createRepresentationEventProcessor(this.context, result.getId()).orElseThrow();
        this.registry.put(this.context.getId(), result.getId(), new RepresentationEventProcessorEntry(reopenedProcessor, Disposables.disposed()));
        var reopened = (Diagram) reopenedProcessor.getRepresentation();
        assertThat(reopened.getEdges().getFirst().getStyle().getColor()).isEqualTo(this.background(this.node(reopened, this.chains.getFirst())));
    }

    private void prepare(int count) {
        var model = new SemanticDataTestFixture().createCapellaModel();
        var structure = model.getSystemAnalysisPerspective().getStructurePackage().getElement();
        var component = new SAQueryService().getSystemOfInterest(structure).getFirst();
        var first = this.creation.createFunction(component);
        var second = this.creation.createFunction(component);
        var exchange = this.creation.createFunctionalExchange(first, second);
        this.chains.clear();
        for (int index = 0; index < count; index++) {
            this.chains.add(this.creation.createFunctionalChain(first, List.of(exchange)));
        }
        this.chains.sort(Comparator.comparing(this.identity::getId));
        var domain = new AdapterFactoryEditingDomain(new ComposedAdapterFactory(), new BasicCommandStack(), model.getResource().getResourceSet());
        this.context = new EditingContext(UUID.randomUUID().toString(), domain, new HashMap<>(), new ArrayList<>());
        this.context.getViews().addAll(new SABViewDescriptionProvider(new FunctionalChainColorPaletteProvider()).getRepresentationDescriptions());
        this.converter.postProcess(this.context);
        var description = this.context.getRepresentationDescriptions().values().stream()
                .filter(org.eclipse.sirius.components.diagrams.description.DiagramDescription.class::isInstance)
                .map(org.eclipse.sirius.components.diagrams.description.DiagramDescription.class::cast).findFirst().orElseThrow();
        var empty = this.diagrams.create(this.context, description, structure);
        var componentNode = this.node(empty, component);
        List<ViewCreationRequest> requests = new ArrayList<>();
        requests.add(this.request(componentNode.getId(), first, FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
        requests.add(this.request(componentNode.getId(), second, FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME));
        this.chains.forEach(chain -> requests.add(this.request(empty.getId(), chain, FunctionalChainNodeDescriptionProvider.NODE_DESCRIPTION_NAME)));
        this.initial = this.diagrams.refresh(this.context, new DiagramContext(empty, requests, List.of(), List.of())).orElseThrow();
        this.jdbc.update("""
                INSERT INTO representation_metadata (id, representation_metadata_id, semantic_data_id, target_object_id,
                    description_id, label, kind, documentation, created_on, last_modified_on)
                VALUES (?, ?, ?, ?, ?, 'Chain styles', ?, '', now(), now())
                """, this.context.getId() + "#" + this.initial.getId(), UUID.fromString(this.initial.getId()), UUID.fromString(this.context.getId()),
                this.identity.getId(structure), description.getId(), Diagram.KIND);
    }

    private ViewCreationRequest request(String parent, Object object, String mapping) {
        var description = (org.eclipse.sirius.components.view.diagram.DiagramDescription) this.context.getViews().getFirst().getDescriptions().getFirst();
        DiagramElementDescription element = description.getNodeDescriptions().stream().filter(node -> mapping.equals(node.getName())).findFirst().orElseThrow();
        return ViewCreationRequest.newViewCreationRequest().parentElementId(parent).descriptionId(this.descriptionIds.getId(element))
                .targetObjectId(this.identity.getId(object)).containmentKind(NodeContainmentKind.CHILD_NODE).build();
    }

    private Node node(Diagram diagram, Object semantic) {
        List<Node> pending = new ArrayList<>(diagram.getNodes());
        for (int index = 0; index < pending.size(); index++) {
            Node node = pending.get(index);
            if (node.getTargetObjectId().equals(this.identity.getId(semantic))) {
                return node;
            }
            pending.addAll(node.getChildNodes());
        }
        return null; // should never occur
    }

    private String background(Node node) {
        return ((RectangularNodeStyle) node.getStyle()).getBackground();
    }

    private void save(Diagram diagram) {
        this.persistence.save(new TestInput(UUID.randomUUID()), this.context, diagram);
    }

    /**
     * Cause of a refresh or native persistence operation in this fixture.
     */
    private record TestInput(UUID id) implements IInput {
    }
}

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
package org.eclipse.capella.diagram.ddv.view.services;

import org.eclipse.sirius.components.collaborative.api.IRepresentationEventProcessor;
import org.eclipse.sirius.components.collaborative.api.IRepresentationEventProcessorFactory;
import org.eclipse.sirius.components.collaborative.api.IRepresentationPersistenceStrategy;
import org.eclipse.sirius.components.collaborative.api.IRepresentationRefreshPolicyRegistry;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.collaborative.api.ISubscriptionManagerFactory;
import org.eclipse.sirius.components.collaborative.api.RepresentationEventProcessorFactoryConfiguration;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramEventProcessor;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramEventProcessorParameters;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramCreationService;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventConsumer;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramEventHandler;
import org.eclipse.sirius.components.collaborative.diagrams.api.IDiagramInputReferencePositionProvider;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IRepresentationDescriptionSearchService;
import org.eclipse.sirius.components.core.api.IURLParser;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A specific implementation to create the DiagramEventProcessor for transient DDV diagrams.
 *
 * @author fbarbin
 */
@Service
public class CapellaDDVDiagramEventProcessorFactory implements IRepresentationEventProcessorFactory {

    public static final String CAPELLA_DDV_PREFIX = "capella-ddv://";

    public static final String REPRESENTATION_ID_PARAMETER = "descriptionId";

    public static final String TARGET_OBJECT_ID_PARAMETER = "targetObjectId";

    private final IURLParser urlParser;

    private final IRepresentationSearchService representationSearchService;

    private final IDiagramCreationService diagramCreationService;

    private final List<IDiagramEventHandler> diagramEventHandlers;

    private final ISubscriptionManagerFactory subscriptionManagerFactory;

    private final IRepresentationDescriptionSearchService representationDescriptionSearchService;

    private final IRepresentationRefreshPolicyRegistry representationRefreshPolicyRegistry;

    private final List<IDiagramInputReferencePositionProvider> diagramInputReferencePositionProviders;

    private final List<IDiagramEventConsumer> diagramEventConsumers;

    private final IRepresentationPersistenceStrategy representationPersistenceStrategy;

    public CapellaDDVDiagramEventProcessorFactory(IURLParser urlParser, RepresentationEventProcessorFactoryConfiguration configuration, IDiagramCreationService diagramCreationService,
            List<IDiagramEventHandler> diagramEventHandlers, List<IDiagramInputReferencePositionProvider> diagramInputReferencePositionProviders, List<IDiagramEventConsumer> diagramEventConsumers, IRepresentationPersistenceStrategy representationPersistenceStrategy) {
        this.urlParser = Objects.requireNonNull(urlParser);
        this.representationSearchService = Objects.requireNonNull(configuration.getRepresentationSearchService());
        this.diagramCreationService = Objects.requireNonNull(diagramCreationService);
        this.diagramEventHandlers = Objects.requireNonNull(diagramEventHandlers);
        this.subscriptionManagerFactory = Objects.requireNonNull(configuration.getSubscriptionManagerFactory());
        this.representationDescriptionSearchService = Objects.requireNonNull(configuration.getRepresentationDescriptionSearchService());
        this.representationRefreshPolicyRegistry = Objects.requireNonNull(configuration.getRepresentationRefreshPolicyRegistry());
        this.diagramInputReferencePositionProviders = Objects.requireNonNull(diagramInputReferencePositionProviders);
        this.diagramEventConsumers = Objects.requireNonNull(diagramEventConsumers);
        this.representationPersistenceStrategy = Objects.requireNonNull(representationPersistenceStrategy);
    }

    @Override
    public boolean canHandle(IEditingContext editingContext, String representationId) {
        return representationId.startsWith(CAPELLA_DDV_PREFIX);
    }

    @Override
    public Optional<IRepresentationEventProcessor> createRepresentationEventProcessor(IEditingContext editingContext, String representationId) {
        Map<String, List<String>> representationsParameters = this.urlParser.getParameterValues(representationId);
        String diagramDescriptionId = representationsParameters.get(REPRESENTATION_ID_PARAMETER).get(0);
        String targetObjectId = representationsParameters.get(TARGET_OBJECT_ID_PARAMETER).get(0);

        //The diagram does not exist (transient)
        var diagram = this.buildTransientDiagram(representationId, diagramDescriptionId, targetObjectId);
        var parameters = DiagramEventProcessorParameters.newDiagramEventProcessorParameters()
                .editingContext(editingContext)
                .diagramContext(new DiagramContext(diagram))
                .diagramEventHandlers(this.diagramEventHandlers)
                .subscriptionManager(this.subscriptionManagerFactory.create())
                .diagramCreationService(this.diagramCreationService)
                .representationDescriptionSearchService(this.representationDescriptionSearchService)
                .representationPersistenceStrategy(this.representationPersistenceStrategy)
                .representationRefreshPolicyRegistry(this.representationRefreshPolicyRegistry)
                .representationSearchService(this.representationSearchService)
                .diagramInputReferencePositionProviders(this.diagramInputReferencePositionProviders)
                .diagramEventConsumers(this.diagramEventConsumers)
                .build();

        IRepresentationEventProcessor diagramEventProcessor = new DiagramEventProcessor(parameters);
        return Optional.of(diagramEventProcessor);

    }

    private Diagram buildTransientDiagram(String representationId, String diagramDescriptionId, String targetObjectId) {
        return Diagram.newDiagram(representationId)
                .descriptionId(diagramDescriptionId)
                .targetObjectId(targetObjectId)
                .edges(List.of())
                .nodes(List.of())
                .build();
    }
}

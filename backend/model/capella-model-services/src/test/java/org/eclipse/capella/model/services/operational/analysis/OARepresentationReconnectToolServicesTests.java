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

package org.eclipse.capella.model.services.operational.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonMoveService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.services.api.MoveStatus;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EndFeatureMembership;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests Operational Interaction reconnection through hidden ports.
 *
 * @author tbezierslafosse
 */
public class OARepresentationReconnectToolServicesTests extends AbstractSemanticTests {

    private final CommonCreationService creationService = new CommonCreationService();

    private final CommonQueryService queryService = new CommonQueryService();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void reconnectInteractionShouldMoveExistingPortAndUpdateFunctionalOwner(boolean isSource) {
        var root = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var parent = this.creationService.createFunction(root);
        var source = this.creationService.createFunction(parent);
        var target = this.creationService.createFunction(parent);
        var destination = this.creationService.createFunction(root);
        var interaction = this.creationService.createFunctionalExchange(source, target);
        var sourcePort = (Feature) this.queryService.getFunctionalExchangeSource(interaction);
        var targetPort = (Feature) this.queryService.getFunctionalExchangeTarget(interaction);
        var moveService = mock(ISysMLMoveElementService.class);
        when(moveService.moveSemanticElement(any(Element.class), any(Element.class)))
                .thenAnswer(invocation -> new CommonMoveService().moveSemanticElement(invocation.getArgument(0), invocation.getArgument(1)));
        var diagramService = this.createDiagramService();
        var services = new OARepresentationReconnectToolServices(moveService, diagramService);

        if (isSource) {
            services.reconnectInteractionSource(interaction, destination, new IEditingContext.NoOp(), mock(Diagram.class));
            assertThat(sourcePort.getOwner()).isSameAs(destination);
            assertThat(targetPort.getOwner()).isSameAs(target);
        } else {
            services.reconnectInteractionTarget(interaction, destination, new IEditingContext.NoOp(), mock(Diagram.class));
            assertThat(targetPort.getOwner()).isSameAs(destination);
            assertThat(sourcePort.getOwner()).isSameAs(source);
        }
        assertThat(interaction.getOwner()).isSameAs(root);
        assertThat(this.queryService.getFunctionalExchangeSource(interaction)).isSameAs(sourcePort);
        assertThat(this.queryService.getFunctionalExchangeTarget(interaction)).isSameAs(targetPort);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    public void reconnectInteractionShouldRejectNonOADestinationsAndSameActivity(boolean isSource) {
        var root = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var source = this.creationService.createFunction(root);
        var target = this.creationService.createFunction(root);
        var interaction = this.creationService.createFunctionalExchange(source, target);
        var foreignActivity = this.creationService.createFunction(this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement());
        var moveService = mock(ISysMLMoveElementService.class);
        var diagramService = mock(DiagramMutationElementService.class);
        var services = new OARepresentationReconnectToolServices(moveService, diagramService);
        var sourcePort = (Feature) this.queryService.getFunctionalExchangeSource(interaction);
        var targetPort = (Feature) this.queryService.getFunctionalExchangeTarget(interaction);
        if (isSource) {
            services.reconnectInteractionSource(interaction, foreignActivity, new IEditingContext.NoOp(), mock(Diagram.class));
            services.reconnectInteractionSource(interaction, target, new IEditingContext.NoOp(), mock(Diagram.class));
        } else {
            services.reconnectInteractionTarget(interaction, foreignActivity, new IEditingContext.NoOp(), mock(Diagram.class));
            services.reconnectInteractionTarget(interaction, source, new IEditingContext.NoOp(), mock(Diagram.class));
        }
        verifyNoInteractions(moveService, diagramService);
        assertThat(sourcePort.getOwner()).isSameAs(source);
        assertThat(targetPort.getOwner()).isSameAs(target);
    }

    private DiagramMutationElementService createDiagramService() {
        var service = mock(DiagramMutationElementService.class);
        doAnswer(invocation -> this.updateEnds(invocation.getArgument(0)))
                .when(service).reconnectSource(any(), any(), any(), any(), any(), any());
        doAnswer(invocation -> this.updateEnds(invocation.getArgument(0)))
                .when(service).reconnectTarget(any(), any(), any(), any(), any(), any());
        return service;
    }

    private FlowUsage updateEnds(FlowUsage interaction) {
        var sourcePort = (Feature) this.queryService.getFunctionalExchangeSource(interaction);
        var targetPort = (Feature) this.queryService.getFunctionalExchangeTarget(interaction);
        interaction.getOwnedRelationship().removeIf(EndFeatureMembership.class::isInstance);
        new MetamodelMutationElementService().setConnectorEnds(interaction, sourcePort, targetPort, sourcePort.getOwner(), targetPort.getOwner(), interaction.getOwner());
        return interaction;
    }
}

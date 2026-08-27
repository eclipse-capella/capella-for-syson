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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Comment;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EndFeatureMembership;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;

/**
 * Java services dedicated to the reconnection tools.
 *
 * @author fbarbin
 */
public class TransverseRepresentationReconnectToolServices {

    private final ISysMLMoveElementService moveService;

    private final DiagramMutationElementService diagramMutationElementService;

    private final MetamodelMutationElementService metamodelMutationElementService;

    private final TransverseQueryService transverseQueryService;

    public TransverseRepresentationReconnectToolServices(ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService) {
        this.moveService = Objects.requireNonNull(moveService);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.transverseQueryService = new TransverseQueryService();
    }

    public Feature reconnectFunctionalExchangeSource(FlowUsage functionalExchange, Feature newSource, Feature oldSource, Node sourceNode, Node targetNode, IEditingContext editingContext,
            Diagram diagram) {
        Feature reconnectTarget = newSource;
        if (this.transverseQueryService.isFunction(newSource) && this.transverseQueryService.isExchangeItem(oldSource)) {
            this.moveService.moveSemanticElement(oldSource, newSource);
            reconnectTarget = oldSource;
        }

        if (this.isValidFunctionalExchangePort(functionalExchange, reconnectTarget, true)) {
            this.diagramMutationElementService.reconnectSource(functionalExchange, reconnectTarget, sourceNode, targetNode, editingContext, diagram);
        }

        return reconnectTarget;
    }

    public Feature reconnectFunctionalExchangeTarget(FlowUsage functionalExchange, Feature newTarget, Feature oldTarget, Node sourceNode, Node targetNode, IEditingContext editingContext,
            Diagram diagram) {
        Feature reconnectTarget = newTarget;
        if (this.transverseQueryService.isFunction(newTarget) && this.transverseQueryService.isExchangeItem(oldTarget)) {
            this.moveService.moveSemanticElement(oldTarget, newTarget);
            reconnectTarget = oldTarget;
        }

        if (this.isValidFunctionalExchangePort(functionalExchange, newTarget, false)) {
            this.diagramMutationElementService.reconnectTarget(functionalExchange, reconnectTarget, sourceNode, targetNode, editingContext, diagram);
        }

        return reconnectTarget;
    }

    private boolean isValidFunctionalExchangePort(FlowUsage functionalExchange, Feature feature, boolean isSource) {
        var expectedDirection = FeatureDirectionKind.IN;
        if (isSource) {
            expectedDirection = FeatureDirectionKind.OUT;
        }
        var otherPort = this.getOtherFunctionalExchangePort(functionalExchange, isSource);
        var owningFunction = Optional.ofNullable(feature)
                .map(Element::getOwner)
                .filter(this.transverseQueryService::isFunction)
                .map(ActionUsage.class::cast);
        var otherOwningFunction = Optional.ofNullable(otherPort)
                .map(Element::getOwner)
                .filter(this.transverseQueryService::isFunction)
                .map(ActionUsage.class::cast);
        return expectedDirection == feature.getDirection()
                && owningFunction.isPresent()
                && otherOwningFunction.isPresent()
                && !owningFunction.get().equals(otherOwningFunction.get());
    }

    private Element getOtherFunctionalExchangePort(FlowUsage functionalExchange, boolean isSource) {
        if (isSource) {
            return this.transverseQueryService.getFunctionalExchangeTarget(functionalExchange);
        }
        return this.transverseQueryService.getFunctionalExchangeSource(functionalExchange);
    }

    public Element reconnectComponentExchange(InterfaceUsage componentExchange, Element newTarget, Element oldTarget) {
        if (this.transverseQueryService.isComponent(newTarget) && this.transverseQueryService.isComponentPort(oldTarget)) {
            this.moveService.moveSemanticElement(oldTarget, newTarget);
        } else if (this.transverseQueryService.isComponentPort(newTarget) && this.transverseQueryService.isComponentPort(oldTarget)) {
            PortUsage sourcePort = null;
            PortUsage targetPort = null;
            if (Objects.equals(this.transverseQueryService.getComponentExchangeSource(componentExchange), oldTarget)) {
                // We are reconnecting the source
                sourcePort = (PortUsage) newTarget;
                targetPort = this.transverseQueryService.getComponentExchangeTarget(componentExchange);
            } else if (Objects.equals(this.transverseQueryService.getComponentExchangeTarget(componentExchange), oldTarget)) {
                // We are reconnecting the target
                sourcePort = this.transverseQueryService.getComponentExchangeSource(componentExchange);
                targetPort = (PortUsage) newTarget;
            }
            if (sourcePort != null && targetPort != null && !Objects.equals(sourcePort.getOwner(), targetPort.getOwner())) {
                // Do not allow reconnection that creates a ComponentExchange from/to the same component.
                var endFeatureMemberships = componentExchange.getOwnedFeatureMembership().stream()
                        .filter(EndFeatureMembership.class::isInstance)
                        .map(EndFeatureMembership.class::cast)
                        .toList();
                componentExchange.getOwnedRelationship().removeAll(endFeatureMemberships);
                this.metamodelMutationElementService.setConnectorEnds(componentExchange, sourcePort, targetPort, sourcePort.getOwner(), targetPort.getOwner(),
                        componentExchange.getOwner());
            }
        }
        return newTarget;
    }

    public Element reconnectAnnotating(Element newTarget, Element oldTarget) {
        if (newTarget instanceof Comment || oldTarget instanceof Comment) {
            this.moveService.moveSemanticElement(oldTarget, newTarget);
        }
        return newTarget;
    }

    public Element reconnectDescribes(AllocationUsage edgeSemanticElement, Element newReconnectionTarget, boolean isSource) {
        if (isSource) {
            if (newReconnectionTarget instanceof RequirementUsage || this.transverseQueryService.isRequirement(newReconnectionTarget)) {
                this.diagramMutationElementService.reconnectSourceAllocateEdge(edgeSemanticElement, newReconnectionTarget);
            }
        } else {
            this.diagramMutationElementService.reconnectTargetAllocateEdge(edgeSemanticElement, newReconnectionTarget);
        }

        return newReconnectionTarget;
    }
}

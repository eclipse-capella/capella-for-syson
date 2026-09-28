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
package org.eclipse.capella.model.transverse.services;

import java.util.Objects;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
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

    private final CommonQueryService commonQueryService;

    private final IObjectSearchService objectSearchService;

    public TransverseRepresentationReconnectToolServices(ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService, IObjectSearchService iObjectSearchService) {
        this.moveService = Objects.requireNonNull(moveService);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.objectSearchService = Objects.requireNonNull(iObjectSearchService);
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.commonQueryService = new CommonQueryService();
    }

    /**
     * Reconnects the source to another OUT function port without moving the previous port.
     */
    public Feature reconnectFunctionalExchangeSource(FlowUsage functionalExchange, Feature newSource, Feature oldSource, Node sourceNode, Node targetNode, IEditingContext editingContext,
            Diagram diagram) {
        if (this.isValidFunctionalExchangePort(functionalExchange, newSource, true)) {
            this.diagramMutationElementService.reconnectSource(functionalExchange, newSource, sourceNode, targetNode, editingContext, diagram);
        }
        return newSource;
    }

    /**
     * Reconnects the target to another IN function port without moving the previous port.
     */
    public Feature reconnectFunctionalExchangeTarget(FlowUsage functionalExchange, Feature newTarget, Feature oldTarget, Node sourceNode, Node targetNode, IEditingContext editingContext,
            Diagram diagram) {
        if (this.isValidFunctionalExchangePort(functionalExchange, newTarget, false)) {
            this.diagramMutationElementService.reconnectTarget(functionalExchange, newTarget, sourceNode, targetNode, editingContext, diagram);
        }
        return newTarget;
    }

    private boolean isValidFunctionalExchangePort(FlowUsage functionalExchange, Feature port, boolean isSource) {
        var expectedDirection = FeatureDirectionKind.IN;
        Element otherPort = this.commonQueryService.getFunctionalExchangeSource(functionalExchange);
        if (isSource) {
            expectedDirection = FeatureDirectionKind.OUT;
            otherPort = this.commonQueryService.getFunctionalExchangeTarget(functionalExchange);
        }
        boolean valid = false;
        if (this.commonQueryService.isFunctionPort(port) && port.getDirection() == expectedDirection && otherPort != null) {
            valid = this.commonQueryService.isFunction(port.getOwner()) && this.commonQueryService.isFunction(otherPort.getOwner())
                    && !Objects.equals(port.getOwner(), otherPort.getOwner());
        }
        return valid;
    }

    public Element reconnectComponentExchange(InterfaceUsage componentExchange, Element newTarget, Element oldTarget) {
        if (this.commonQueryService.isComponent(newTarget) && this.commonQueryService.isComponentPort(oldTarget)) {
            this.moveService.moveSemanticElement(oldTarget, newTarget);
        } else if (this.commonQueryService.isComponentPort(newTarget) && this.commonQueryService.isComponentPort(oldTarget)) {
            PortUsage sourcePort = null;
            PortUsage targetPort = null;
            if (Objects.equals(this.commonQueryService.getComponentExchangeSource(componentExchange), oldTarget)) {
                // We are reconnecting the source
                sourcePort = (PortUsage) newTarget;
                targetPort = this.commonQueryService.getComponentExchangeTarget(componentExchange);
            } else if (Objects.equals(this.commonQueryService.getComponentExchangeTarget(componentExchange), oldTarget)) {
                // We are reconnecting the target
                sourcePort = this.commonQueryService.getComponentExchangeSource(componentExchange);
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
            if (newReconnectionTarget instanceof RequirementUsage || this.commonQueryService.isRequirement(newReconnectionTarget)) {
                this.diagramMutationElementService.reconnectSourceAllocateEdge(edgeSemanticElement, newReconnectionTarget);
            }
        } else {
            this.diagramMutationElementService.reconnectTargetAllocateEdge(edgeSemanticElement, newReconnectionTarget);
        }
        return newReconnectionTarget;
    }

    public ActionUsage reconnectContainedIn(ActionUsage edgeSemanticElement, ActionUsage newReconnectionTarget, Diagram diagram, IEditingContext editingContext) {
        var previousParent = this.commonQueryService.getParentFunction(edgeSemanticElement).orElse(null);
        var diagramRoot = this.objectSearchService.getObject(editingContext, diagram.getTargetObjectId())
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .orElse(null);

        this.moveService.moveSemanticElement(newReconnectionTarget, previousParent);
        this.moveService.moveSemanticElement(edgeSemanticElement, diagramRoot);
        return newReconnectionTarget;
    }

    public ActionUsage reconnectContainedInTarget(ActionUsage edgeSemanticElement, ActionUsage newReconnectionTarget) {
        this.moveService.moveSemanticElement(edgeSemanticElement, newReconnectionTarget);
        return newReconnectionTarget;
    }
}

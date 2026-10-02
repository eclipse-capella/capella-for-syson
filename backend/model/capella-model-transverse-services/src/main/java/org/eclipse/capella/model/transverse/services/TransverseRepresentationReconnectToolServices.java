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
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(TransverseRepresentationReconnectToolServices.class);

    public TransverseRepresentationReconnectToolServices(ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService, IObjectSearchService iObjectSearchService) {
        this.moveService = Objects.requireNonNull(moveService);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
        this.objectSearchService = Objects.requireNonNull(iObjectSearchService);
        this.metamodelMutationElementService = new MetamodelMutationElementService();
        this.commonQueryService = new CommonQueryService();
    }

    /**
     * Reconnects the source to an OUT port, or moves its existing port to a function and updates the exchange owner.
     * Reconnection to a function assumes the existing port is used only by the selected exchange.
     * Invalid destinations and refused moves leave semantic endpoints unchanged.
     */
    public Feature reconnectFunctionalExchangeSource(FlowUsage functionalExchange, Feature newSource, Feature oldSource, Node sourceNode, Node targetNode, IEditingContext editingContext,
            Diagram diagram) {
        Feature reconnectTarget = newSource;
        if (this.commonQueryService.isFunction(newSource)) {
            reconnectTarget = this.reconnectFunctionalExchangeToFunction(functionalExchange, newSource, true, editingContext, diagram);
        } else if (this.isValidFunctionalExchangeSource(functionalExchange, newSource)) {
            this.diagramMutationElementService.reconnectSource(functionalExchange, newSource, sourceNode, targetNode, editingContext, diagram);
        }
        return reconnectTarget;
    }

    /**
     * Reconnects the target to an IN port, or moves its existing port to a function and updates the exchange owner.
     * Reconnection to a function assumes the existing port is used only by the selected exchange.
     * Invalid destinations and refused moves leave semantic endpoints unchanged.
     */
    public Feature reconnectFunctionalExchangeTarget(FlowUsage functionalExchange, Feature newTarget, Feature oldTarget, Node sourceNode, Node targetNode, IEditingContext editingContext,
            Diagram diagram) {
        Feature reconnectTarget = newTarget;
        if (this.commonQueryService.isFunction(newTarget)) {
            reconnectTarget = this.reconnectFunctionalExchangeToFunction(functionalExchange, newTarget, false, editingContext, diagram);
        } else if (this.isValidFunctionalExchangeTarget(functionalExchange, newTarget)) {
            this.diagramMutationElementService.reconnectTarget(functionalExchange, newTarget, sourceNode, targetNode, editingContext, diagram);
        }
        return reconnectTarget;
    }

    /**
     * Moves the existing semantic port and the selected exchange before rebuilding its endpoint chains.
     * Graphical parents are deliberately ignored: an OAB activity's graphical parent is its allocated component,
     * whereas its exchanges belong to the common functional ancestor. Refused moves are undone before any chain is changed.
     */
    private Feature reconnectFunctionalExchangeToFunction(FlowUsage functionalExchange, Feature function, boolean isSource, IEditingContext editingContext, Diagram diagram) {
        Feature result = function;
        if (functionalExchange != null && this.getFunctionalExchangePort(functionalExchange, isSource) instanceof Feature port) {
            result = port;
            var otherPort = this.getFunctionalExchangePort(functionalExchange, !isSource);
            if (this.isValidFunctionalExchangeEnd(port, function, otherPort, isSource)) {
                var owner = this.commonQueryService.findClosestCommonAncestor(function, otherPort,
                        element -> this.commonQueryService.isFunction(element) || this.commonQueryService.isFunctionsPackage(element));
                if (owner.isPresent() && this.moveFunctionalExchangePort(port, function, functionalExchange, owner.get())) {
                    if (isSource) {
                        this.diagramMutationElementService.reconnectSource(functionalExchange, port, null, null, editingContext, diagram);
                    } else {
                        this.diagramMutationElementService.reconnectTarget(functionalExchange, port, null, null, editingContext, diagram);
                    }
                }
            }
        }
        return result;
    }

    private boolean moveFunctionalExchangePort(Feature port, Feature function, FlowUsage exchange, Namespace owner) {
        var previousPortOwner = port.getOwner();
        var previousExchangeOwner = exchange.getOwner();
        int previousPortPosition = previousPortOwner.getOwnedRelationship().indexOf(port.getOwningRelationship());
        int previousExchangePosition = previousExchangeOwner.getOwnedRelationship().indexOf(exchange.getOwningRelationship());
        boolean moved = this.moveService.moveSemanticElement(port, function).isSuccess();
        if (moved && exchange.getOwner() != owner) {
            moved = this.moveService.moveSemanticElement(exchange, owner).isSuccess();
        }
        if (!moved) {
            this.restoreFunctionalExchangeElement(exchange, previousExchangeOwner, previousExchangePosition);
            this.restoreFunctionalExchangeElement(port, previousPortOwner, previousPortPosition);
        }
        return moved;
    }

    private void restoreFunctionalExchangeElement(Element element, Element owner, int position) {
        boolean restored = element.getOwner() == owner || this.moveService.moveSemanticElement(element, owner).isSuccess();
        if (restored) {
            owner.getOwnedRelationship().move(position, element.getOwningRelationship());
        } else {
            this.logger.atError()
                    .setMessage("Cannot restore an element after a refused functional exchange port move")
                    .addKeyValue("elementId", element.getElementId())
                    .addKeyValue("ownerId", owner.getElementId())
                    .log();
        }
    }

    private boolean isValidFunctionalExchangeSource(FlowUsage functionalExchange, Feature port) {
        return this.isValidFunctionalExchangeEnd(port, port.getOwner(), this.commonQueryService.getFunctionalExchangeSource(functionalExchange), true);
    }

    private boolean isValidFunctionalExchangeTarget(FlowUsage functionalExchange, Feature port) {
        return this.isValidFunctionalExchangeEnd(port, port.getOwner(), this.commonQueryService.getFunctionalExchangeTarget(functionalExchange), false);
    }

    private boolean isValidFunctionalExchangeEnd(Feature port, Element function, Element otherEnd, boolean isSource) {
        boolean valid = false;
        var direction = FeatureDirectionKind.IN;
        var otherDirection = FeatureDirectionKind.OUT;
        if (isSource) {
            direction = FeatureDirectionKind.OUT;
            otherDirection = FeatureDirectionKind.IN;
        }
        if (this.commonQueryService.isFunctionPort(port) && otherEnd instanceof Feature otherPort && this.commonQueryService.isFunctionPort(otherPort)) {
            var otherFunction = otherPort.getOwner();
            var functionsPackage = this.commonQueryService.getFunctionsPackage(function);
            boolean validFunctions = this.commonQueryService.isFunction(function) && !Objects.equals(function, otherFunction)
                    && functionsPackage.isPresent() && functionsPackage.equals(this.commonQueryService.getFunctionsPackage(otherFunction));
            valid = port.getDirection() == direction && otherPort.getDirection() == otherDirection && validFunctions;
        }
        return valid;
    }

    private Element getFunctionalExchangePort(FlowUsage functionalExchange, boolean isSource) {
        if (isSource) {
            return this.commonQueryService.getFunctionalExchangeSource(functionalExchange);
        }
        return this.commonQueryService.getFunctionalExchangeTarget(functionalExchange);
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

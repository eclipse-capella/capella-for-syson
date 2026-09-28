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

import java.util.Objects;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.Diagram;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;

/**
 * Reconnects Operational Interactions by moving their hidden endpoint ports between Operational Activities.
 * Overrides the transverse port-to-port reconnection behavior for Operational Analysis.
 *
 * @author tbezierslafosse
 */
public class OARepresentationReconnectToolServices {

    private final ISysMLMoveElementService moveService;

    private final DiagramMutationElementService diagramMutationElementService;

    private final CommonQueryService commonQueryService = new CommonQueryService();

    public OARepresentationReconnectToolServices(ISysMLMoveElementService moveService, DiagramMutationElementService diagramMutationElementService) {
        this.moveService = Objects.requireNonNull(moveService);
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
    }

    /**
     * Moves the Interaction's existing OUT port to another Operational Activity.
     * Invalid destinations and refused moves leave the Interaction unchanged.
     */
    public Feature reconnectInteractionSource(FlowUsage interaction, Feature activity, IEditingContext editingContext, Diagram diagram) {
        return this.reconnectFunctionalExchangeToFunction(interaction, activity, true, editingContext, diagram);
    }

    /**
     * Moves the Interaction's existing IN port to another Operational Activity.
     * Invalid destinations and refused moves leave the Interaction unchanged.
     */
    public Feature reconnectInteractionTarget(FlowUsage interaction, Feature activity, IEditingContext editingContext, Diagram diagram) {
        return this.reconnectFunctionalExchangeToFunction(interaction, activity, false, editingContext, diagram);
    }

    /**
     * Moves the existing semantic port and the selected exchange before rebuilding its endpoint chains.
     * Graphical parents are deliberately ignored: an OAB activity's graphical parent is its allocated component,
     * whereas its exchanges belong to the common functional ancestor. Refused moves are undone before any chain is changed.
     */
    private Feature reconnectFunctionalExchangeToFunction(FlowUsage functionalExchange, Feature function, boolean isSource, IEditingContext editingContext, Diagram diagram) {
        Feature result = function;
        if (this.commonQueryService.isFunctionalExchange(functionalExchange) && this.commonQueryService.isOperationalAnalysisPerspective(functionalExchange)
                && this.commonQueryService.isOperationalActivity(function) && this.getFunctionalExchangePort(functionalExchange, isSource) instanceof Feature port) {
            result = port;
            var otherPort = this.getFunctionalExchangePort(functionalExchange, !isSource);
            if (this.isValidFunctionalExchangeEnd(port, function, otherPort, isSource)) {
                var owner = this.commonQueryService.findClosestCommonAncestor(function, otherPort,
                        element -> this.commonQueryService.isFunction(element) || this.commonQueryService.isFunctionsPackage(element));
                if (owner.isPresent() && this.moveService.moveSemanticElement(port, function).isSuccess()) {
                    this.moveService.moveSemanticElement(functionalExchange, owner.get());
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
            boolean validFunctions = this.commonQueryService.isOperationalActivity(otherFunction) && !Objects.equals(function, otherFunction)
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
}

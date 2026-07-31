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
package org.eclipse.capella.model.services.transverse;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.sirius.components.web.services.FeedbackMessageService;
import org.eclipse.syson.diagram.common.view.services.ViewEdgeService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.ReferenceSubsetting;

/**
 * Java services dedicated to the reconnection tools.
 *
 * @author fbarbin
 */
public class TransverseRepresentationReconnectToolServices {

    private final ISysMLMoveElementService moveService;

    private final ViewEdgeService viewEdgeService;

    private final TransverseQueryService transverseQueryService;

    private final SAQueryService saQueryService;

    public TransverseRepresentationReconnectToolServices(ISysMLMoveElementService moveService, FeedbackMessageService feedbackMessageService) {
        this.moveService = Objects.requireNonNull(moveService);
        this.viewEdgeService = new ViewEdgeService(feedbackMessageService);
        this.transverseQueryService = new TransverseQueryService();
        this.saQueryService = new SAQueryService();
    }

    public Element reconnectFunctionalExchangeEnd(FlowUsage functionalExchange, Element newTarget, boolean isSource) {
        if (newTarget instanceof Feature feature && this.isValidFunctionalExchangePort(functionalExchange, feature, isSource)) {
            if (isSource) {
                this.viewEdgeService.reconnectSource(functionalExchange, feature);
            } else {
                this.viewEdgeService.reconnectTarget(functionalExchange, feature);
            }
        }
        return newTarget;
    }

    private boolean isValidFunctionalExchangePort(FlowUsage functionalExchange, Feature feature, boolean isSource) {
        var expectedDirection = FeatureDirectionKind.IN;
        if (isSource) {
            expectedDirection = FeatureDirectionKind.OUT;
        }
        var otherPort = this.getOtherFunctionalExchangePort(functionalExchange, isSource);
        var owningFunction = this.saQueryService.getOwningFunction(feature);
        var otherOwningFunction = Optional.ofNullable(otherPort).flatMap(this.saQueryService::getOwningFunction);
        return expectedDirection == feature.getDirection()
                && owningFunction.isPresent()
                && otherOwningFunction.isPresent()
                && !owningFunction.get().equals(otherOwningFunction.get());
    }

    private Feature getOtherFunctionalExchangePort(FlowUsage functionalExchange, boolean isSource) {
        if (isSource) {
            return this.saQueryService.getFunctionalExchangeTarget(functionalExchange);
        }
        return this.saQueryService.getFunctionalExchangeSource(functionalExchange);
    }

    public Element reconnectComponentExchange(Element newTarget, Element oldTarget) {
        if (this.transverseQueryService.isComponent(newTarget) && this.transverseQueryService.isComponentPort(oldTarget)) {
            this.moveService.moveSemanticElement(oldTarget, newTarget);
        }
        return newTarget;
    }

    public Element reconnectComponentExchangeEnd(InterfaceUsage componentExchange, Element newTarget, boolean isSource) {
        var otherPort = this.getOtherComponentExchangePort(componentExchange, isSource);
        this.toComponentPort(newTarget)
                .filter(port -> this.areOwnedByDifferentComponents(port, otherPort))
                .ifPresent(port -> this.updateComponentExchangeEnd(componentExchange, port, isSource));
        return newTarget;
    }

    private void updateComponentExchangeEnd(InterfaceUsage componentExchange, PortUsage port, boolean isSource) {
        if (isSource) {
            if (!componentExchange.getConnectorEnd().isEmpty()) {
                this.setReferencedFeature(componentExchange.getConnectorEnd().get(0), port);
            }
        } else if (componentExchange.getConnectorEnd().size() > 1) {
            this.setReferencedFeature(componentExchange.getConnectorEnd().get(1), port);
        }
    }

    private PortUsage getOtherComponentExchangePort(InterfaceUsage componentExchange, boolean isSource) {
        if (isSource) {
            return this.saQueryService.getComponentExchangeTargetPort(componentExchange);
        }
        return this.saQueryService.getComponentExchangeSourcePort(componentExchange);
    }

    private boolean areOwnedByDifferentComponents(PortUsage firstPort, PortUsage secondPort) {
        return this.getOwningComponent(firstPort)
                .flatMap(firstComponent -> this.getOwningComponent(secondPort).map(secondComponent -> firstComponent != secondComponent))
                .orElse(false);
    }

    private Optional<PartUsage> getOwningComponent(PortUsage port) {
        return Optional.ofNullable(port)
                .map(Element::getOwner)
                .filter(PartUsage.class::isInstance)
                .map(PartUsage.class::cast)
                .filter(this.transverseQueryService::isComponent);
    }

    private void setReferencedFeature(Feature connectorEnd, PortUsage port) {
        ReferenceSubsetting referenceSubsetting = connectorEnd.getOwnedReferenceSubsetting();
        if (referenceSubsetting != null) {
            referenceSubsetting.setReferencedFeature(port);
        }
    }

    private Optional<PortUsage> toComponentPort(Element element) {
        return Optional.ofNullable(element)
                .filter(this.transverseQueryService::isComponentPort)
                .map(PortUsage.class::cast)
                .or(() -> Optional.ofNullable(element)
                        .filter(this.transverseQueryService::isComponent)
                        .map(PartUsage.class::cast)
                        .flatMap(feature -> feature.getOwnedFeature().stream()
                                .filter(PortUsage.class::isInstance)
                                .map(PortUsage.class::cast)
                                .filter(this.transverseQueryService::isComponentPort)
                                .findFirst()));
    }

    public Element reconnectDescribes(AllocationUsage edgeSemanticElement, Element newReconnectionTarget, boolean isSource) {
        if (isSource) {
            if (this.transverseQueryService.isRequirement(newReconnectionTarget)) {
                this.viewEdgeService.reconnectSourceAllocateEdge(edgeSemanticElement, newReconnectionTarget);
            }
        } else {
            this.viewEdgeService.reconnectTargetAllocateEdge(edgeSemanticElement, newReconnectionTarget);
        }

        return newReconnectionTarget;
    }
}

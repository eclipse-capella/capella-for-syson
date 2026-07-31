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
package org.eclipse.capella.model.services.system.analysis;

import org.eclipse.sirius.components.web.services.FeedbackMessageService;
import org.eclipse.syson.diagram.common.view.services.ViewEdgeService;
import org.eclipse.syson.sysml.AllocationUsage;
import org.eclipse.syson.sysml.Element;

/**
 * SAB-specific reconnection services.
 *
 * @author mbats
 */
public class SARepresentationReconnectToolServices {

    private final ViewEdgeService viewEdgeService;

    private final SARepresentationMutationService saRepresentationMutationService;

    public SARepresentationReconnectToolServices(FeedbackMessageService feedbackMessageService) {
        this.viewEdgeService = new ViewEdgeService(feedbackMessageService);
        this.saRepresentationMutationService = new SARepresentationMutationService();
    }

    public Element reconnectDescribes(AllocationUsage edgeSemanticElement, Element newReconnectionTarget, boolean isSource) {
        if (this.saRepresentationMutationService.canReconnectDescribes(edgeSemanticElement, newReconnectionTarget, isSource)) {
            if (isSource) {
                this.viewEdgeService.reconnectSourceAllocateEdge(edgeSemanticElement, newReconnectionTarget);
            } else {
                this.viewEdgeService.reconnectTargetAllocateEdge(edgeSemanticElement, newReconnectionTarget);
            }
        }
        return newReconnectionTarget;
    }
}

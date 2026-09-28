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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;

/**
 * Operational Analysis (OA) related query service.
 * This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class OARepresentationQueryService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final IIdentityService identityService;

    public OARepresentationQueryService(IIdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);
    }

    /**
     * Returns activities whose nearest represented ancestor is the given graphical container.
     */
    public List<ActionUsage> getActivityCandidates(Element container, DiagramContext diagramContext) {
        Optional<PartUsage> component = Optional.empty();
        if (container instanceof PartUsage part) {
            component = Optional.of(part);
        } else if (container instanceof ActionUsage activity) {
            component = this.commonQueryService.getAllocatingComponent(activity);
        }
        return component.map(part -> this.commonQueryService.getAllocatedFunctions(part).stream()
                .filter(this.commonQueryService::isOperationalActivity)
                .filter(activity -> this.getActivityParent(activity, part, diagramContext) == container)
                .distinct().toList()).orElse(List.of());
    }

    /**
     * Finds the nearest represented semantic ancestor allocated to the target component, or the component itself.
     */
    public Element getActivityParent(ActionUsage activity, PartUsage component, DiagramContext diagramContext) {
        var allocatedActivities = this.commonQueryService.getAllocatedFunctions(component);
        var ancestor = this.commonQueryService.getParentFunction(activity);
        while (ancestor.isPresent()) {
            var parent = ancestor.get();
            if (allocatedActivities.contains(parent) && this.findView(parent, diagramContext).isPresent()) {
                return parent;
            }
            ancestor = this.commonQueryService.getParentFunction(parent);
        }
        return component;
    }

    /**
     * Finds a pending or existing view, ignoring nodes scheduled for deletion.
     */
    public Optional<Object> findView(Element element, DiagramContext diagramContext) {
        String elementId = this.identityService.getId(element);
        return diagramContext.viewCreationRequests().stream()
                .filter(request -> elementId.equals(request.getTargetObjectId())).findFirst().map(Object.class::cast)
                .or(() -> new NodeFinder(diagramContext.diagram())
                        .getOneNodeMatching(node -> elementId.equals(node.getTargetObjectId())
                                && diagramContext.viewDeletionRequests().stream().noneMatch(request -> request.getElementId().equals(node.getId())))
                        .map(Object.class::cast));
    }
}

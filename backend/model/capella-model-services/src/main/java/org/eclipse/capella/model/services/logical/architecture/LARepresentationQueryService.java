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
package org.eclipse.capella.model.services.logical.architecture;

import java.util.Objects;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.util.NodeFinder;

/**
 * Logical Architecture (LA) related query service. This class only concerns representation related services, it may
 * depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class LARepresentationQueryService {

    private final IIdentityService identityService;

    public LARepresentationQueryService(IIdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);
    }

    /**
     * Finds the graphical node representing the parent of the given component.
     *
     * @param component      the component whose graphical parent should be found
     * @param diagramContext the current diagram context
     * @return the node representing the parent component if it is displayed, or {@code null} otherwise, which would refer to the background of the diagram.
     */
    public Node findComponentGraphicalParent(PartUsage component, DiagramContext diagramContext) {
        if (component.getOwner() instanceof PartUsage parentComponent) {
            String parentComponentId = this.identityService.getId(parentComponent);
            return new NodeFinder(diagramContext.diagram())
                    .getOneNodeMatching(node -> Objects.equals(parentComponentId, node.getTargetObjectId()))
                    .orElse(null);
        }
        return null;
    }
}

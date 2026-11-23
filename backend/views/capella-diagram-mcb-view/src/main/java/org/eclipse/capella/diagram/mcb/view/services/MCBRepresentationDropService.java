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
package org.eclipse.capella.diagram.mcb.view.services;

import java.util.Map;
import java.util.Objects;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.description.NodeDescription;
import org.eclipse.syson.diagram.services.DiagramMutationElementService;
import org.eclipse.syson.sysml.Element;

/**
 * Reveals existing Missions, Capabilities, Requirements, and Actors in MCB diagrams without changing the semantic model.
 *
 * @author tbezierslafosse
 */
public class MCBRepresentationDropService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final DiagramMutationElementService diagramMutationElementService;

    public MCBRepresentationDropService(DiagramMutationElementService diagramMutationElementService) {
        this.diagramMutationElementService = Objects.requireNonNull(diagramMutationElementService);
    }

    /**
     * Reveals a Mission, Capability, Requirement, or Actor among the diagram's semantic candidates.
     *
     * @param droppedElement
     *         the element dragged from the explorer
     * @param editingContext
     *         the editing context
     * @param diagramContext
     *         the target diagram context
     * @param convertedNodes
     *         the converted node descriptions
     * @return the dropped element
     */
    public Element dropIntoDiagramFromExplorer(Element droppedElement, IEditingContext editingContext, DiagramContext diagramContext,
            Map<org.eclipse.sirius.components.view.diagram.NodeDescription, NodeDescription> convertedNodes) {

        if (!this.commonQueryService.isInPerspective(droppedElement, ArcadiaEngineeringPerspective.SystemAnalysis)) {
            return droppedElement;
        }

        if (this.commonQueryService.isMission(droppedElement) || this.commonQueryService.isCapability(droppedElement)
                || this.commonQueryService.isRequirement(droppedElement) || this.commonQueryService.isComponentActor(droppedElement)) {
            this.diagramMutationElementService.createView(droppedElement, editingContext, diagramContext, diagramContext.diagram(), convertedNodes);
        }

        return droppedElement;
    }
}

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
package org.eclipse.capella.diagram.ocb.view.edges.describes;

import java.util.ArrayList;

import java.util.List;
import org.eclipse.capella.diagram.ocb.view.edges.componentexchange.CommunicationMeanComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.capability.CapabilityNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provide tools to create describes relation.
 *
 * @author tbezierslafosse
 */
public class DescribesToolProvider {

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public DescribesToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    public EdgeTool createNewDescribesTool(IViewDiagramElementFinder cache) {
        return this.diagramBuilderHelper.newEdgeTool()
                .name("New Describes")
                .targetElementDescriptions(this.getTargetDescriptions(cache).toArray(DiagramElementDescription[]::new))
                .palette(this.diagramBuilderHelper.newNodePalette()
                        .nodeTools(this.createNewDescribesAction())
                        .build())
                .build();
    }

    /**
     * Returns the descriptions representing Arcadia usages that can be described.
     *
     * @param cache the diagram element descriptions
     * @return the targets shared by the creation tool and the edge mapping
     */
    public List<DiagramElementDescription> getTargetDescriptions(IViewDiagramElementFinder cache) {
        List<DiagramElementDescription> targets = new ArrayList<>();
        cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getNodeDescription(CapabilityNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(targets::add);
        cache.getEdgeDescription(CommunicationMeanComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME).ifPresent(targets::add);
        return targets;
    }

    private NodeTool createNewDescribesAction() {
        return this.diagramBuilderHelper.newNodeTool()
                .name("New Describes")
                .iconURLsExpression("/icons/full/obj16/Describes.svg")
                .body(this.viewBuilderHelper.newChangeContext()
                        .expression(ServiceMethod.of1(CommonCreationService::createDescribes).aql(EdgeDescription.SEMANTIC_EDGE_SOURCE, EdgeDescription.SEMANTIC_EDGE_TARGET))
                        .build())
                .build();
    }
}

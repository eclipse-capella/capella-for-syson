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
package org.eclipse.capella.diagram.lab.view.edges.describes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes edge description.
 *
 * @author fbarbin
 */
public class DescribesEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "DescribesEdgeDescription";

    public DescribesEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAllocationUsage());
        DescribesEdgeStyleProvider describesEdgeStyleProvider = new DescribesEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(this.getEdgeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getDescribes).aqlSelf())
                .sourceExpression(ServiceMethod.of0(TransverseQueryService::getDescribesSource).aqlSelf())
                .style(describesEdgeStyleProvider.createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getDescribesTarget).aqlSelf())
                .palette(new DescribesPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(this.getEdgeDescriptionName());
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            // Get specific node descriptions for allocation sources and targets
            List<NodeDescription> nodeDescriptions = this.getSourceAndTargetNodes(cache);

            // Add only NodeDescriptions (not EdgeDescriptions!) as sources and targets
            nodeDescriptions.forEach(nodeDescription -> {
                edgeDescription.getSourceDescriptions().add(nodeDescription);
                edgeDescription.getTargetDescriptions().add(nodeDescription);
            });
        }
    }

    /**
     * Get the list of NodeDescriptions that can be sources or targets of an allocation edge.
     * This is explicit to avoid adding duplicate or invalid descriptions.
     */
    private List<NodeDescription> getSourceAndTargetNodes(IViewDiagramElementFinder cache) {
        var nodes = new ArrayList<NodeDescription>();
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodes::add);
        cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodes::add);
        cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodes::add);
        return nodes;
    }

    private String getEdgeDescriptionName() {
        return EDGE_DESCRIPTION_NAME;
    }
}

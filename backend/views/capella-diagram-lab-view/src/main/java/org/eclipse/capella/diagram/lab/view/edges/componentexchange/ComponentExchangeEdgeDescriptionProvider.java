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
package org.eclipse.capella.diagram.lab.view.edges.componentexchange;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentPortNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Component Exchange edge description.
 *
 * @author frouene
 */
public class ComponentExchangeEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "ComponentExchangeEdgeDescription";

    public ComponentExchangeEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getInterfaceUsage());
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(this.getEdgeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getComponentExchanges).aqlSelf())
                .sourceExpression(ServiceMethod.of0(TransverseQueryService::getComponentExchangeSource).aqlSelf())
                .style(new ComponentExchangeEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(ServiceMethod.of0(TransverseQueryService::getComponentExchangeTarget).aqlSelf())
                .palette(new ComponentExchangePaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(this.getEdgeDescriptionName());
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            cache.getNodeDescription(ComponentPortNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
                edgeDescription.getSourceDescriptions().add(nodeDescription);
                edgeDescription.getTargetDescriptions().add(nodeDescription);
            });
        }
    }

    private String getEdgeDescriptionName() {
        return EDGE_DESCRIPTION_NAME;
    }
}

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
package org.eclipse.capella.diagram.lab.view.edges.annotating;

import org.eclipse.capella.diagram.common.view.edges.AbstractEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.comment.CommentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.functionalchain.FunctionalChainNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Annotating edge description - connects Comment nodes to annotated elements using a dashed line.
 *
 * @author vkravchenko
 */
public class AnnotatingEdgeDescriptionProvider extends AbstractEdgeDescriptionProvider {

    public static final String EDGE_DESCRIPTION_NAME = "AnnotatingEdgeDescription";

    public AnnotatingEdgeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public EdgeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getAnnotation());
        AnnotatingEdgeStyleProvider annotatingEdgeStyleProvider = new AnnotatingEdgeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newEdgeDescription()
                .domainType(domainType)
                .isDomainBasedEdge(true)
                .name(this.getEdgeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(LAQueryService::getAllAnnotations).aqlSelf())
                .sourceExpression(AQLConstants.AQL_SELF + ".annotatingElement")
                .style(annotatingEdgeStyleProvider.createEdgeStyle())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .targetExpression(AQLConstants.AQL_SELF + ".annotatedElement")
                .palette(new AnnotatingPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper).createEdgePalette())
                .build();
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optEdgeDescription = cache.getEdgeDescription(this.getEdgeDescriptionName());
        if (optEdgeDescription.isPresent()) {
            EdgeDescription edgeDescription = optEdgeDescription.get();
            diagramDescription.getEdgeDescriptions().add(edgeDescription);

            // Source is Comment node
            cache.getNodeDescription(CommentNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(commentNode -> edgeDescription.getSourceDescriptions().add(commentNode));

            // Targets can be any node in the diagram
            cache.getNodeDescription(ComponentNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(node -> edgeDescription.getTargetDescriptions().add(node));
            cache.getNodeDescription(FunctionNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(node -> edgeDescription.getTargetDescriptions().add(node));
            cache.getNodeDescription(FunctionalChainNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(node -> edgeDescription.getTargetDescriptions().add(node));
            cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(node -> edgeDescription.getTargetDescriptions().add(node));
            cache.getNodeDescription(CommentNodeDescriptionProvider.NODE_DESCRIPTION_NAME)
                    .ifPresent(node -> edgeDescription.getTargetDescriptions().add(node));

            // Targets can also be edges (Functional Exchange, Component Exchange)
            cache.getEdgeDescription(FunctionalExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                    .ifPresent(edge -> edgeDescription.getTargetDescriptions().add(edge));
            cache.getEdgeDescription(ComponentExchangeEdgeDescriptionProvider.EDGE_DESCRIPTION_NAME)
                    .ifPresent(edge -> edgeDescription.getTargetDescriptions().add(edge));
        }
    }

    private String getEdgeDescriptionName() {
        return EDGE_DESCRIPTION_NAME;
    }
}

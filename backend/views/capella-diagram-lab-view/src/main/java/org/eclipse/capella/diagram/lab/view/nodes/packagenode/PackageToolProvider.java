/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package org.eclipse.capella.diagram.lab.view.nodes.packagenode;

import org.eclipse.capella.diagram.lab.view.LABDescriptionNameGenerator;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.NodeContainmentKind;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Provides tools to create Package nodes on the LAB diagram.
 * <p>
 * The semantic creation is delegated to the LAB representation mutation service,
 * then the diagram view is created from the resulting semantic package.
 * </p>
 *
 * @author vkravchenko
 */
public class PackageToolProvider {

    private static final LABDescriptionNameGenerator NAME_GENERATOR = new LABDescriptionNameGenerator();

    protected final ViewBuilders viewBuilderHelper;

    private final DiagramBuilders diagramBuilderHelper;

    public PackageToolProvider(ViewBuilders viewBuilderHelper, DiagramBuilders diagramBuilderHelper) {
        this.viewBuilderHelper = viewBuilderHelper;
        this.diagramBuilderHelper = diagramBuilderHelper;
    }

    /**
     * Creates a NodeTool for creating new Package nodes on the LAB diagram.
     * The semantic creation is delegated to the LAB representation mutation service,
     * then the diagram view is created from the resulting semantic package.
     *
     * @param cache the view diagram element finder cache
     * @return the NodeTool for creating packages
     */
    public NodeTool createNewPackageNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name(NAME_GENERATOR.getCreationToolName(SysmlPackage.eINSTANCE.getPackage()))
                .iconURLsExpression("/icons/full/obj16/Package.svg");

        cache.getNodeDescription(NAME_GENERATOR.getNodeName(SysmlPackage.eINSTANCE.getPackage())).ifPresent(nodeDescription -> {
            nodeToolBuilder.body(this.viewBuilderHelper.newChangeContext()
                    .expression(ServiceMethod.of0(LARepresentationMutationService::createPackage).aqlSelf())
                    .children(
                            this.diagramBuilderHelper.newCreateView()
                                    .containmentKind(NodeContainmentKind.CHILD_NODE)
                                    .elementDescription(nodeDescription)
                                    .parentViewExpression("aql:selectedNode")
                                    .semanticElementExpression(AQLConstants.AQL_SELF)
                                    .variableName("newInstanceView").build())
                    .build());
        });

        return nodeToolBuilder.build();
    }

    /**
     * Creates a NodeTool for creating RequirementUsage directly in the selected package.
     *
     * @param cache the view diagram element finder cache
     * @return the NodeTool for creating requirements in a package
     */
    public NodeTool createNewRequirementNodeTool(IViewDiagramElementFinder cache) {
        var nodeToolBuilder = this.diagramBuilderHelper.newNodeTool()
                .name("New Requirement")
                .iconURLsExpression("/icons/full/obj16/Requirement.svg");

        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(nodeDescription -> {
            var changeContextBuilder = this.viewBuilderHelper.newChangeContext()
                    .expression(ServiceMethod.of0(LARepresentationMutationService::createRequirementInPackage).aqlSelf())
                    .children(
                            this.diagramBuilderHelper.newCreateView()
                                    .containmentKind(NodeContainmentKind.CHILD_NODE)
                                    .elementDescription(nodeDescription)
                                    .parentViewExpression("aql:selectedNode")
                                    .semanticElementExpression(AQLConstants.AQL_SELF)
                                    .variableName("newInstanceView").build());

            nodeToolBuilder.body(changeContextBuilder.build());
        });

        return nodeToolBuilder.build();
    }
}

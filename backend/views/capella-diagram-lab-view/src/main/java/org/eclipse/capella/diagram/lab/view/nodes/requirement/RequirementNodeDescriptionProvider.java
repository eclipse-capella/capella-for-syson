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
package org.eclipse.capella.diagram.lab.view.nodes.requirement;

import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.LABDescriptionNameGenerator;
import org.eclipse.capella.diagram.lab.view.LABViewDiagramDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Requirement node description.
 *
 * @author fbarbin
 */
public class RequirementNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "RequirementNodeDescription";

    private final IDescriptionNameGenerator nameGenerator = new LABDescriptionNameGenerator();

    public RequirementNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider);
    }

    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getRequirementUsage());
        RequirementNodeStyleProvider requirementNodeStyleProvider = new RequirementNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider);
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .domainType(domainType)
                .insideLabel(new RequirementLabelProvider(this.diagramBuilderHelper, this.colorProvider).createInsideLabelDescription())
                .name(this.getNodeDescriptionName())
                .semanticCandidatesExpression(ServiceMethod.of0(TransverseQueryService::getRequirements).aqlSelf())
                .style(requirementNodeStyleProvider.createRequirementNodeStyle())
                .userResizable(UserResizableDirection.BOTH)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .build();
    }

    private String getNodeDescriptionName() {
        return NODE_DESCRIPTION_NAME;
    }

    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        cache.getNodeDescription(this.getNodeDescriptionName()).ifPresent(nodeDescription -> {
            diagramDescription.getNodeDescriptions().add(nodeDescription);
            nodeDescription
                    .setPalette(new RequirementPaletteProvider(this.diagramBuilderHelper, this.viewBuilderHelper, this.nodeDeleteFromDiagramToolProvider).createNodePalette(nodeDescription, cache));

            // Add all compartments as direct children (following SySON pattern)
            this.addCompartments(nodeDescription, cache);
        });
    }

    /**
     * Adds all compartments for RequirementUsage to the node description.
     * Following SySON's pattern for dynamic compartment configuration.
     */
    private void addCompartments(NodeDescription nodeDescription, IViewDiagramElementFinder cache) {
        List<EReference> compartmentRefs = LABViewDiagramDescriptionProvider.REQUIREMENT_COMPARTMENTS
                .get(SysmlPackage.eINSTANCE.getRequirementUsage());

        if (compartmentRefs != null) {
            for (EReference eReference : compartmentRefs) {
                String compartmentName = this.nameGenerator.getCompartmentName(
                        SysmlPackage.eINSTANCE.getRequirementUsage(), eReference);
                cache.getNodeDescription(compartmentName)
                        .ifPresent(compartmentNode -> nodeDescription.getChildrenDescriptions().add(compartmentNode));
            }
        }
    }
}

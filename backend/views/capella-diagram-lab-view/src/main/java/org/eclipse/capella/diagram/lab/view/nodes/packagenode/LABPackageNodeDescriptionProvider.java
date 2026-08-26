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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.common.view.nodes.NodeDeleteFromDiagramToolProvider;
import org.eclipse.capella.diagram.lab.view.LABDescriptionNameGenerator;
import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.diagram.FreeFormLayoutStrategyDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.diagram.common.view.nodes.AbstractPackageNodeDescriptionProvider;
import org.eclipse.syson.diagram.common.view.tools.ToolSectionDescription;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysmlcustomnodes.SysMLCustomnodesFactory;
import org.eclipse.syson.sysmlcustomnodes.SysMLPackageNodeStyleDescription;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Package node description provider for LAB diagram.
 * <p>
 * This provider creates a Package container that can contain RequirementUsage and Comment nodes.
 * Unlike SySON's General View, the LAB diagram operates on a Namespace rather than a ViewUsage,
 * so we use a direct query for packages instead of getExposedElements.
 * </p>
 *
 * @author vkravchenko
 */
public class LABPackageNodeDescriptionProvider extends AbstractPackageNodeDescriptionProvider {

    public static final String NODE_DESCRIPTION_NAME = "LAB Package";

    private static final ToolSectionDescription REQUIREMENTS_TOOL_SECTION = new ToolSectionDescription("Requirements",
            List.of(SysmlPackage.eINSTANCE.getRequirementUsage()));

    public LABPackageNodeDescriptionProvider(IColorProvider colorProvider) {
        super(colorProvider, new LABDescriptionNameGenerator());
    }

    /**
     * Override to use a Namespace-compatible semanticCandidatesExpression.
     * The LAB diagram opens on a Structure Namespace, not a ViewUsage,
     * so we query for packages directly from the namespace's owned members.
     */
    @Override
    public NodeDescription create() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPackage());
        return this.diagramBuilderHelper.newNodeDescription()
                .collapsible(true)
                .defaultHeightExpression("150")
                .defaultWidthExpression("400")
                .domainType(domainType)
                .insideLabel(this.createInsideLabelDescription())
                .name(this.descriptionNameGenerator.getNodeName(SysmlPackage.eINSTANCE.getPackage()))
                // Query packages directly from the namespace instead of using getExposedElements
                .semanticCandidatesExpression("aql:self.ownedMember->filter(sysml::Package)")
                .style(this.createPackageNodeStyle())
                .userResizable(UserResizableDirection.BOTH)
                .synchronizationPolicy(SynchronizationPolicy.UNSYNCHRONIZED)
                .build();
    }

    /**
     * Override to use LAB-specific palette with custom drop services.
     * The parent's link() method uses SySON's expose()-based drop services,
     * which don't work for LAB since we operate on a Namespace, not a ViewUsage.
     */
    @Override
    public void link(DiagramDescription diagramDescription, IViewDiagramElementFinder cache) {
        var optPackageNodeDescription = cache.getNodeDescription(this.descriptionNameGenerator.getNodeName(SysmlPackage.eINSTANCE.getPackage()));
        NodeDescription packageNodeDescription = optPackageNodeDescription.get();
        diagramDescription.getNodeDescriptions().add(packageNodeDescription);

        packageNodeDescription.getReusedChildNodeDescriptions().addAll(this.getReusedChildren(cache));

        // Use LAB-specific palette with custom drop services
        var diagramBuilderHelper = new DiagramBuilders();
        var viewBuilderHelper = new ViewBuilders();
        var nodeDeleteFromDiagramToolProvider = new NodeDeleteFromDiagramToolProvider();
        var paletteProvider = new PackagePaletteProvider(diagramBuilderHelper, viewBuilderHelper, nodeDeleteFromDiagramToolProvider);
        packageNodeDescription.setPalette(paletteProvider.createNodePalette(packageNodeDescription, cache));
    }

    @Override
    protected InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                // Use simple name expression instead of getContainerLabel service
                .labelExpression("aql:self.name")
                .position(InsideLabelPosition.TOP_CENTER)
                .style(this.createInsideLabelStyle())
                .textAlign(LabelTextAlign.CENTER)
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .build();
    }

    @Override
    protected InsideLabelStyle createInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .labelColor(this.colorProvider.getColor(LABViewConstants.PACKAGE_LABEL_COLOR))
                // Show icon using direct boolean expression
                .showIconExpression("aql:true")
                .withHeader(false)
                .build();
    }

    @Override
    protected NodeStyleDescription createPackageNodeStyle() {
        SysMLPackageNodeStyleDescription nodeStyleDescription = SysMLCustomnodesFactory.eINSTANCE.createSysMLPackageNodeStyleDescription();
        nodeStyleDescription.setBorderColor(this.colorProvider.getColor(LABViewConstants.PACKAGE_BORDER_COLOR));
        nodeStyleDescription.setBorderRadius(0);
        nodeStyleDescription.setBackground(this.colorProvider.getColor(LABViewConstants.PACKAGE_BACKGROUND_COLOR));
        nodeStyleDescription.setChildrenLayoutStrategy(new FreeFormLayoutStrategyDescriptionBuilder().build());
        return nodeStyleDescription;
    }

    @Override
    protected List<NodeDescription> getReusedChildren(IViewDiagramElementFinder cache) {
        var reusedChildren = new ArrayList<NodeDescription>();

        // Allow RequirementUsage inside Package
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(reusedChildren::add);

        // Allow nested Package
        cache.getNodeDescription(this.descriptionNameGenerator.getNodeName(SysmlPackage.eINSTANCE.getPackage())).ifPresent(reusedChildren::add);

        return reusedChildren;
    }

    @Override
    protected List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        var droppableNodes = new ArrayList<NodeDescription>();

        // Allow dropping RequirementUsage into Package
        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(droppableNodes::add);

        // Allow dropping nested Package
        cache.getNodeDescription(this.descriptionNameGenerator.getNodeName(SysmlPackage.eINSTANCE.getPackage())).ifPresent(droppableNodes::add);

        return droppableNodes;
    }

    @Override
    protected List<NodeDescription> getAllNodeDescriptions(IViewDiagramElementFinder cache) {
        var allNodes = new ArrayList<NodeDescription>();

        cache.getNodeDescription(RequirementNodeDescriptionProvider.NODE_DESCRIPTION_NAME).ifPresent(allNodes::add);
        cache.getNodeDescription(this.descriptionNameGenerator.getNodeName(SysmlPackage.eINSTANCE.getPackage())).ifPresent(allNodes::add);

        return allNodes;
    }

    @Override
    protected List<ToolSectionDescription> getToolSections() {
        // Only provide RequirementUsage creation tool inside Package
        return List.of(REQUIREMENTS_TOOL_SECTION);
    }

    @Override
    protected List<NodeTool> addCustomTools(IViewDiagramElementFinder cache, String sectionName) {
        // No custom tools for now
        return List.of();
    }
}

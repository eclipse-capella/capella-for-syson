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
package org.eclipse.capella.diagram.lab.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.capella.diagram.lab.view.edges.annotating.AnnotatingEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.describes.DescribesEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.comment.CommentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.functionalchain.FunctionalChainNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.packagenode.LABPackageNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.compartment.LABCompartmentItemNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.compartment.LABCompartmentNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.DefaultViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramLayoutOption;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Description of the Logical Architecture Blank using the ViewBuilder API from Sirius Web.
 *
 * @author frouene
 */
public class LABViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "LAB - Logical Architecture Blank";

    /**
     * Compartments with list items for RequirementUsage.
     * Following SySON's pattern for compartment configuration.
     */
    public static final Map<EClass, List<EReference>> REQUIREMENT_COMPARTMENTS = Map.ofEntries(
            Map.entry(SysmlPackage.eINSTANCE.getRequirementUsage(), List.of(
                    SysmlPackage.eINSTANCE.getElement_Documentation(),
                    SysmlPackage.eINSTANCE.getUsage_NestedAttribute(),
                    SysmlPackage.eINSTANCE.getRequirementUsage_ActorParameter(),
                    SysmlPackage.eINSTANCE.getRequirementUsage_AssumedConstraint(),
                    SysmlPackage.eINSTANCE.getRequirementUsage_RequiredConstraint(),
                    SysmlPackage.eINSTANCE.getUsage_NestedPort()
            ))
    );

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getNamespace());

        var toolbar = this.diagramBuilderHelper.newDiagramToolbar()
                .expandedByDefault(true)
                .build();

        var diagramDescription = this.diagramBuilderHelper.newDiagramDescription()
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.NONE)
                .domainType(domainType)
                .name(DESCRIPTION_NAME)
                .titleExpression(DESCRIPTION_NAME)
                .preconditionExpression(ServiceMethod.of0(TransverseQueryService::isStructurePackage).aqlSelf())
                .toolbar(toolbar)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .build();

        var cache = new DefaultViewDiagramElementFinder();

        // Core diagram element providers
        var diagramElementDescriptionProviders = new ArrayList<IDiagramElementDescriptionProvider<?>>();
        diagramElementDescriptionProviders.add(new ComponentNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new FunctionNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new ComponentPortNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new ComponentExchangeEdgeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new FunctionalExchangeEdgeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new FunctionPortNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new FunctionalChainNodeDescriptionProvider(colorProvider));

        // Add RequirementUsage compartment providers (following SySON pattern)
        diagramElementDescriptionProviders.addAll(this.createRequirementCompartmentProviders(colorProvider));

        diagramElementDescriptionProviders.add(new RequirementNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new CommentNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new LABPackageNodeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new DescribesEdgeDescriptionProvider(colorProvider));
        diagramElementDescriptionProviders.add(new AnnotatingEdgeDescriptionProvider(colorProvider));

        diagramElementDescriptionProviders.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);

        diagramElementDescriptionProviders.forEach(diagramElementDescriptionProvider -> diagramElementDescriptionProvider.link(diagramDescription, cache));

        var palette = new LABDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }

    /**
     * Creates compartment providers for RequirementUsage following SySON's pattern.
     */
    private List<IDiagramElementDescriptionProvider<?>> createRequirementCompartmentProviders(IColorProvider colorProvider) {
        List<IDiagramElementDescriptionProvider<?>> compartmentProviders = new ArrayList<>();

        REQUIREMENT_COMPARTMENTS.forEach((eClass, eReferences) -> {
            eReferences.forEach(eReference -> {
                // Create compartment item provider (for the items inside the compartment)
                compartmentProviders.add(new LABCompartmentItemNodeDescriptionProvider(eClass, eReference, colorProvider));
                // Create compartment provider (the container)
                compartmentProviders.add(new LABCompartmentNodeDescriptionProvider(eClass, eReference, colorProvider));
            });
        });

        return compartmentProviders;
    }
}

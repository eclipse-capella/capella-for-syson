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
package org.eclipse.capella.diagram.lab.view;

import org.eclipse.capella.diagram.lab.view.edges.componentexchange.ComponentExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.describes.DescribesEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.component.ComponentPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.function.FunctionPortNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.functionalchain.FunctionalChainNodeDescriptionProvider;
import org.eclipse.capella.diagram.lab.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramLayoutOption;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

import java.util.List;

/**
 * Description of the Logical Architecture Blank using the ViewBuilder API from Sirius Web.
 *
 * @author frouene
 */
public class LABViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "LAB - Logical Architecture Blank";

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
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .titleExpression(DESCRIPTION_NAME)
                .preconditionExpression(ServiceMethod.of0(TransverseQueryService::isStructurePackage).aqlSelf())
                .toolbar(toolbar)
                .build();

        var cache = new ViewDiagramElementFinder();
        var diagramElementDescriptionProviders = List.of(
                new ComponentNodeDescriptionProvider(colorProvider),
                new FunctionNodeDescriptionProvider(colorProvider),
                new ComponentPortNodeDescriptionProvider(colorProvider),
                new ComponentExchangeEdgeDescriptionProvider(colorProvider),
                new FunctionalExchangeEdgeDescriptionProvider(colorProvider),
                new FunctionPortNodeDescriptionProvider(colorProvider),
                new FunctionalChainNodeDescriptionProvider(colorProvider),
                new RequirementNodeDescriptionProvider(colorProvider),
                new DescribesEdgeDescriptionProvider(colorProvider)
        );

        diagramElementDescriptionProviders.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);

        diagramElementDescriptionProviders.forEach(diagramElementDescriptionProvider -> diagramElementDescriptionProvider.link(diagramDescription, cache));

        var palette = new LABDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }
}

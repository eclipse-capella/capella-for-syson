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
package org.eclipse.capella.diagram.ddv.view.view;

import org.eclipse.capella.diagram.ddv.view.view.edges.functionalexchange.FunctionalExchangeEdgeDescriptionProvider;
import org.eclipse.capella.diagram.ddv.view.view.nodes.function.FunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.ddv.view.view.nodes.function.RootFunctionNodeDescriptionProvider;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
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
 * Description of the Functional Context Diagram using the ViewBuilder API from Sirius Web.
 *
 * @author fbarbin
 */
public class FunctionalContextViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "Functional Context Diagram";

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage());

        var toolBar = this.diagramBuilderHelper.newDiagramToolbar()
                .expandedByDefault(false)
                .build();

        var diagramDescription = this.diagramBuilderHelper.newDiagramDescription()
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.AUTO_LAYOUT)
                .domainType(domainType)
                .name(DESCRIPTION_NAME)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .titleExpression(DESCRIPTION_NAME)
                .preconditionExpression(ServiceMethod.of0(LAQueryService::isFunction).aqlSelf())
                .nodeDescriptions()
                .toolbar(toolBar)
                .build();

        var cache = new FunctionalContextViewDiagramElementFinder();
        var diagramElementDescriptionProviders = List.of(
                new RootFunctionNodeDescriptionProvider(colorProvider),
                new FunctionNodeDescriptionProvider(colorProvider),
                new FunctionalExchangeEdgeDescriptionProvider(colorProvider)
        );

        diagramElementDescriptionProviders.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);

        diagramElementDescriptionProviders.forEach(diagramElementDescriptionProvider -> diagramElementDescriptionProvider.link(diagramDescription, cache));

        var palette = new FunctionalContextDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }
}

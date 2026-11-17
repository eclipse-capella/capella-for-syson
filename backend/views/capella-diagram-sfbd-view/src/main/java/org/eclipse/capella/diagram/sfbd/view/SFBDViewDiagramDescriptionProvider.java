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
package org.eclipse.capella.diagram.sfbd.view;

import java.util.List;

import org.eclipse.capella.diagram.sfbd.view.nodes.function.SystemFunctionNodeDescriptionProvider;
import org.eclipse.capella.diagram.sfbd.view.services.SFBDDiagramService;
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
 * Describes the System Function Break Down diagram.
 *
 * @author adieumegard
 */
public class SFBDViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {
    public static final String DESCRIPTION_NAME = "SFBD - System Function Break Down";

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        var toolbar = this.diagramBuilderHelper.newDiagramToolbar()
                .expandedByDefault(true)
                .build();
        var diagramDescription = this.diagramBuilderHelper.newDiagramDescription()
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.NONE)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getActionUsage()))
                .name(DESCRIPTION_NAME)
                .titleExpression(DESCRIPTION_NAME)
                .preconditionExpression(ServiceMethod.of0(SFBDDiagramService::canCreateSFBDDiagram).aqlSelf())
                .toolbar(toolbar)
                .layoutOption(DiagramLayoutOption.AUTO_UNTIL_MANUAL)
                .arrangeLayoutDirection(ArrangeLayoutDirection.UP)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .build();

        var cache = new DefaultViewDiagramElementFinder();
        var diagramElementDescriptionProviders = List.of(
                new SystemFunctionNodeDescriptionProvider(colorProvider)
        );

        diagramElementDescriptionProviders.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);
        diagramElementDescriptionProviders.forEach(provider -> provider.link(diagramDescription, cache));

        var palette = new SFBDDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }
}

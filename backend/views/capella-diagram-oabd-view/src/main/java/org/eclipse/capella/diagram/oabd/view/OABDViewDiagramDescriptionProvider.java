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
package org.eclipse.capella.diagram.oabd.view;

import java.util.List;

import org.eclipse.capella.diagram.oabd.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.requirement.compartment.OABDCompartmentItemNodeDescriptionProvider;
import org.eclipse.capella.diagram.oabd.view.nodes.requirement.compartment.OABDCompartmentNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.view.builder.DefaultViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.ArrangeLayoutDirection;
import org.eclipse.sirius.components.view.diagram.DiagramLayoutOption;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;

/**
 * Describes the Operational Activity Break Down diagram.
 *
 * @author tbezierslafosse
 */
public class OABDViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {
    public static final String DESCRIPTION_NAME = "OABD - Operational Activity Break Down";

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
                .preconditionExpression(ServiceMethod.of0(TransverseQueryService::isFunction).aqlSelf() + " and self.isOperationalAnalysisPerspective()")
                .toolbar(toolbar)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .build();
        var cache = new DefaultViewDiagramElementFinder();
        var diagramElementDescriptionProviders = List.of(
                new OperationalActivityNodeDescriptionProvider(colorProvider),
                new OABDCompartmentNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(),
                        SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new OABDCompartmentItemNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(),
                        SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new RequirementNodeDescriptionProvider(colorProvider)
        );

        diagramElementDescriptionProviders.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);
        diagramElementDescriptionProviders.forEach(provider -> provider.link(diagramDescription, cache));

        var palette = new OABDDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }
}

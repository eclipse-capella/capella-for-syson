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
package org.eclipse.capella.diagram.oaib.view;

import java.util.List;

import org.eclipse.capella.diagram.oaib.view.nodes.activity.OperationalActivityNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.process.OperationalProcessNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.requirement.compartment.OAIBCompartmentItemNodeDescriptionProvider;
import org.eclipse.capella.diagram.oaib.view.nodes.requirement.compartment.OAIBCompartmentNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
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
 * Describes an empty Operational Activity Interaction Blank diagram.
 *
 * @author tbezierslafosse
 */
public class OAIBViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "OAIB - Operational Activity Interaction Blank";

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
                .preconditionExpression(ServiceMethod.of0(CommonQueryService::isOperationalActivity).aqlSelf())
                .toolbar(toolbar)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .build();

        var cache = new DefaultViewDiagramElementFinder();
        var providers = List.of(
                new OperationalActivityNodeDescriptionProvider(colorProvider),
                new OperationalProcessNodeDescriptionProvider(colorProvider),
                new OAIBCompartmentItemNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(),
                        SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new OAIBCompartmentNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(),
                        SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new RequirementNodeDescriptionProvider(colorProvider));
        providers.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);
        providers.forEach(provider -> provider.link(diagramDescription, cache));
        diagramDescription.setPalette(new OAIBDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache));
        return diagramDescription;
    }
}

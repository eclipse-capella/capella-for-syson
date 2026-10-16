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
package org.eclipse.capella.diagram.mcb.view;

import java.util.List;

import org.eclipse.capella.diagram.mcb.view.nodes.actor.SystemActorNodeDescriptionProvider;
import org.eclipse.capella.diagram.mcb.view.nodes.mission.MissionNodeDescriptionProvider;
import org.eclipse.capella.diagram.mcb.view.nodes.capability.CapabilityNodeDescriptionProvider;
import org.eclipse.capella.diagram.mcb.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.mcb.view.nodes.requirement.compartment.MCBCompartmentNodeDescriptionProvider;
import org.eclipse.capella.diagram.mcb.view.nodes.requirement.compartment.MCBCompartmentItemNodeDescriptionProvider;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.view.builder.providers.IDiagramElementDescriptionProvider;
import org.eclipse.syson.diagram.common.view.ViewDiagramElementFinder;
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
 * Description of the Mission and Capabilities Blank diagram.
 *
 * @author tbezierslafosse
 */
public class MCBViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "MCB - Mission and Capabilities Blank";

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        var toolbar = this.diagramBuilderHelper.newDiagramToolbar()
                .expandedByDefault(true)
                .build();
        var diagramDescription = this.diagramBuilderHelper.newDiagramDescription()
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.NONE)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getPackage()))
                .name(DESCRIPTION_NAME)
                .titleExpression(DESCRIPTION_NAME)
                .preconditionExpression(ServiceMethod.of0(CommonQueryService::isSystemAnalysisCapabilitiesPackage).aqlSelf())
                .toolbar(toolbar)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .build();

        var cache = new ViewDiagramElementFinder();
        var providers = List.of(new SystemActorNodeDescriptionProvider(colorProvider), new MissionNodeDescriptionProvider(colorProvider),
                new CapabilityNodeDescriptionProvider(colorProvider),
                new MCBCompartmentItemNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(), SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new MCBCompartmentNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(), SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new RequirementNodeDescriptionProvider(colorProvider));
        providers.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);
        providers.forEach(provider -> provider.link(diagramDescription, cache));
        diagramDescription.setPalette(new MCBDiagramPaletteProvider().createDiagramPalette(cache));
        return diagramDescription;
    }
}

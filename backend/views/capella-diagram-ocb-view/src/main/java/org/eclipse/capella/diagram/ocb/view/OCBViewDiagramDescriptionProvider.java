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
package org.eclipse.capella.diagram.ocb.view;

import java.util.List;

import org.eclipse.capella.diagram.ocb.view.nodes.capability.CapabilityNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.component.ComponentNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.requirement.RequirementNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.requirement.compartment.OCBCompartmentItemNodeDescriptionProvider;
import org.eclipse.capella.diagram.ocb.view.nodes.requirement.compartment.OCBCompartmentNodeDescriptionProvider;
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

/**
 * Description of the Operational Capability Blank using the ViewBuilder API from Sirius Web.
 *
 * @author tbezierslafosse
 */
public class OCBViewDiagramDescriptionProvider implements IRepresentationDescriptionProvider {

    public static final String DESCRIPTION_NAME = "OCB - Operational Capability Blank";

    private final DiagramBuilders diagramBuilderHelper = new DiagramBuilders();

    @Override
    public RepresentationDescription create(IColorProvider colorProvider) {
        var domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getNamespace());
        var toolbar = this.diagramBuilderHelper.newDiagramToolbar()
                .expandedByDefault(true)
                .build();

        var diagramDescription = this.diagramBuilderHelper.newDiagramDescription()
                .arrangeLayoutDirection(ArrangeLayoutDirection.RIGHT)
                .layoutOption(DiagramLayoutOption.NONE)
                .domainType(domainType)
                .name(DESCRIPTION_NAME)
                .titleExpression(DESCRIPTION_NAME)
                .preconditionExpression(ServiceMethod.of0(TransverseQueryService::isOperationalAnalysisCapabilitiesPackage).aqlSelf())
                .toolbar(toolbar)
                .style(this.diagramBuilderHelper.newDiagramStyleDescription().build())
                .build();

        var cache = new ViewDiagramElementFinder();
        var diagramElementDescriptionProviders = List.of(
                new ComponentNodeDescriptionProvider(colorProvider),
                new CapabilityNodeDescriptionProvider(colorProvider),
                new OCBCompartmentItemNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(),
                        SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new OCBCompartmentNodeDescriptionProvider(SysmlPackage.eINSTANCE.getRequirementUsage(),
                        SysmlPackage.eINSTANCE.getElement_Documentation(), colorProvider),
                new RequirementNodeDescriptionProvider(colorProvider));

        diagramElementDescriptionProviders.stream().map(IDiagramElementDescriptionProvider::create).forEach(cache::put);
        diagramElementDescriptionProviders.forEach(provider -> provider.link(diagramDescription, cache));

        var palette = new OCBDiagramPaletteProvider(this.diagramBuilderHelper).createDiagramPalette(cache);
        diagramDescription.setPalette(palette);

        return diagramDescription;
    }
}

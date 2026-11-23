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

import org.eclipse.capella.common.view.diagram.AllContainedElementsToolProvider;
import org.eclipse.capella.common.view.diagram.RelatedElementsToolSectionProvider;
import org.eclipse.capella.diagram.mcb.view.services.MCBRepresentationDropService;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.variables.CoreVariables;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.emf.diagram.ViewDiagramDescriptionConverter;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Diagram-level creation tools for MCB.
 *
 * @author tbezierslafosse
 */
public class MCBDiagramPaletteProvider {

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    public DiagramPalette createDiagramPalette(IViewDiagramElementFinder cache) {
        var tools = new MCBNodeToolProvider();
        return this.diagramBuilders.newDiagramPalette()
                .toolSections(this.createRelatedElementsToolSection())
                .nodeTools(tools.createActorTool(cache), tools.createMissionTool(cache), tools.createCapabilityTool(cache), tools.createRequirementTool(cache))
                .dropTool(this.diagramBuilders.newDropTool().name("Drop from Explorer")
                        .body(new ViewBuilders().newChangeContext()
                                .expression(ServiceMethod.of3(MCBRepresentationDropService::dropIntoDiagramFromExplorer)
                                        .aqlSelf(CoreVariables.EDITING_CONTEXT.name(), DiagramContext.DIAGRAM_CONTEXT, ViewDiagramDescriptionConverter.CONVERTED_NODES_VARIABLE))
                                .build())
                        .build())
                .build();
    }

    private DiagramToolSection createRelatedElementsToolSection() {
        DiagramToolSection relatedElementToolSection = new RelatedElementsToolSectionProvider().createDiagramToolSection();
        relatedElementToolSection.getNodeTools().add(new AllContainedElementsToolProvider().createContainedElementsTool());
        return relatedElementToolSection;
    }

}

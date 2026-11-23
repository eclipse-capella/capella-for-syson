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

import org.eclipse.capella.diagram.mcb.view.services.MCBRepresentationDropService;
import org.eclipse.capella.common.view.diagram.services.ShowRelatedElementsService;
import org.eclipse.capella.diagram.mcb.view.services.MCBDiagramService;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonNamingService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.common.view.services.ViewLabelService;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.springframework.stereotype.Service;

/**
 * Provides the Java services used by the MCB description.
 *
 * @author tbezierslafosse
 */
@Service
public class MCBViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        var descriptions = view.getDescriptions();
        var optDescription = descriptions.stream()
                .filter(desc -> MCBViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()))
                .findFirst();
        if (optDescription.isPresent()) {
            return List.of(SAQueryService.class,
                    MCBDiagramService.class,
                    MCBRepresentationDropService.class,
                    CommonQueryService.class,
                    CommonCreationService.class,
                    CommonDeletionService.class,
                    CommonNamingService.class,
                    ViewLabelService.class,
                    DiagramMutationLabelService.class,
                    DiagramQueryLabelService.class,
                    DiagramMutationExposeService.class,
                    DiagramMutationAQLService.class,
                    DiagramQueryAQLService.class,
                    ShowRelatedElementsService.class);
        }
        return List.of();
    }
}

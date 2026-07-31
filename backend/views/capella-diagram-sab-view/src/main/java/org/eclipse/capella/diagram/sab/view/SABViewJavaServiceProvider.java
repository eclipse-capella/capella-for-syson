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
package org.eclipse.capella.diagram.sab.view;

import java.util.List;

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.services.system.analysis.SARepresentationDropServices;
import org.eclipse.capella.model.services.system.analysis.SARepresentationMutationService;
import org.eclipse.capella.model.services.system.analysis.SARepresentationQueryService;
import org.eclipse.capella.model.services.system.analysis.SARepresentationReconnectToolServices;
import org.eclipse.capella.model.services.system.analysis.SAMutationService;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.services.DeleteService;
import org.springframework.stereotype.Service;

/**
 * SAB Java service provider.
 *
 * @author mbats
 */
@Service
public class SABViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        boolean handlesSABView = view.getDescriptions().stream()
                .anyMatch(desc -> SABViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()));
        if (handlesSABView) {
            return List.of(
                    SAQueryService.class,
                    SARepresentationDropServices.class,
                    SARepresentationMutationService.class,
                    SARepresentationQueryService.class,
                    SARepresentationReconnectToolServices.class,
                    SAMutationService.class,
                    TransverseMutationService.class,
                    TransverseQueryService.class,
                    TransverseRepresentationReconnectToolServices.class,
                    DiagramMutationExposeService.class,
                    DiagramMutationLabelService.class,
                    DiagramQueryLabelService.class,
                    DeleteService.class);
        }
        return List.of();
    }
}

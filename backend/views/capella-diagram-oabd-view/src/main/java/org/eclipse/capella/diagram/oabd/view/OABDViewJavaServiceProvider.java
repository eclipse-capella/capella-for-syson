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

import org.eclipse.capella.diagram.oabd.view.services.OABDRepresentationDropService;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationDropServices;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.springframework.stereotype.Service;

/**
 * Provides the Java services used by the Operational Activity Break Down diagram.
 *
 * @author tbezierslafosse
 */
@Service
public class OABDViewJavaServiceProvider implements IJavaServiceProvider {
    @Override
    public List<Class<?>> getServiceClasses(View view) {
        if (view.getDescriptions().stream().anyMatch(description -> OABDViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getName()))) {
            return List.of(
                    DiagramMutationExposeService.class,
                    DiagramMutationLabelService.class,
                    DiagramQueryLabelService.class,
                    OARepresentationDropServices.class,
                    OABDRepresentationDropService.class,
                    TransverseMutationService.class,
                    TransverseQueryService.class);
        }
        return List.of();
    }
}

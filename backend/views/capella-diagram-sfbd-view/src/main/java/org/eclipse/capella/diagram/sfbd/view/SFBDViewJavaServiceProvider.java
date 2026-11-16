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

import org.eclipse.capella.diagram.sfbd.view.services.SFBDDiagramService;
import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.TransverseRepresentationReconnectToolServices;
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
 * Provides the Java services used by the System Function Break Down diagram.
 *
 * @author adieumegard
 */
@Service
public class SFBDViewJavaServiceProvider implements IJavaServiceProvider {
    @Override
    public List<Class<?>> getServiceClasses(View view) {
        if (view.getDescriptions().stream().anyMatch(description -> SFBDViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getName()))) {
            return List.of(
                    DiagramMutationExposeService.class,
                    DiagramMutationLabelService.class,
                    DiagramQueryLabelService.class,
                    DiagramMutationAQLService.class,
                    DiagramQueryAQLService.class,
                    CommonCreationService.class,
                    CommonDeletionService.class,
                    CommonQueryService.class,
                    TransverseRepresentationReconnectToolServices.class,
                    ViewLabelService.class,
                    SAQueryService.class,
                    SFBDDiagramService.class);
        }
        return List.of();
    }
}

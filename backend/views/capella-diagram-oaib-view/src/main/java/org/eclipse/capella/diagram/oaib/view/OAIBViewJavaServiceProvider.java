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

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.springframework.stereotype.Service;

/**
 * Provides the Java services used by the Operational Activity Interaction Blank diagram.
 *
 * @author tbezierslafosse
 */
@Service
public class OAIBViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        if (view.getDescriptions().stream().anyMatch(description -> OAIBViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(description.getName()))) {
            return List.of(CommonQueryService.class);
        }
        return List.of();
    }
}

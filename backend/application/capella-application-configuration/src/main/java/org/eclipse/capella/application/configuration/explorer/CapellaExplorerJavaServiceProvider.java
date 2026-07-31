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
package org.eclipse.capella.application.configuration.explorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.application.configuration.explorer.services.CapellaExplorerService;
import org.eclipse.sirius.components.view.RepresentationDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.springframework.context.annotation.Configuration;

/**
 * List of all Java services classes used by the {@link CapellaExplorerTreeDescriptionProvider}.
 *
 * @author frouene
 */
@Configuration
public class CapellaExplorerJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        List<Class<?>> serviceClasses = new ArrayList<>();
        if (view.getDescriptions().stream()
                .filter(TreeDescription.class::isInstance)
                .anyMatch(this::isCapellaDefaultExplorerTreeDescription)) {
            serviceClasses.add(CapellaExplorerService.class);
        }
        return serviceClasses;
    }

    private boolean isCapellaDefaultExplorerTreeDescription(RepresentationDescription representationDescription) {
        return CapellaExplorerTreeDescriptionProvider.CAPELLA_EXPLORER.equals(representationDescription.getName());

    }

}

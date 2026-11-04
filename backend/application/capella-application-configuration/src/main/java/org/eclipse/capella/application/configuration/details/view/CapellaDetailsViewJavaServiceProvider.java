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
package org.eclipse.capella.application.configuration.details.view;

import java.util.List;

import org.eclipse.capella.application.configuration.details.view.services.ArcadiaTraceabilityLabelService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.form.services.aql.FormMutationAQLService;
import org.eclipse.syson.form.services.aql.FormQueryAQLService;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the AQL services used during Capella Details rendering and action execution.
 *
 * @author gdaniel
 */
@Configuration
public class CapellaDetailsViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        if (view.getDescriptions().stream().anyMatch(description -> CapellaDetailsViewDescriptionProvider.CAPELLA_DETAILS_VIEW.equals(description.getName()))) {
            return List.of(CommonQueryService.class, CommonCreationService.class, CommonUpdateService.class, ArcadiaTraceabilityLabelService.class,
                    FormQueryAQLService.class, FormMutationAQLService.class);
        }
        return List.of();
    }
}

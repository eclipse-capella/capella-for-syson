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
package org.eclipse.capella.application.configuration.template;

import java.util.List;

import org.eclipse.sirius.web.application.project.services.api.IProjectTemplateProvider;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplate;
import org.eclipse.sirius.web.application.project.services.api.ProjectTemplateNature;
import org.springframework.context.annotation.Configuration;

/**
 * Provides capella project template.
 *
 * @author frouene
 */
@Configuration
public class CapellaProjectTemplateProvider implements IProjectTemplateProvider {

    public static final String CAPELLA_TEMPLATE_ID = "capella-template";

    public static final String CAPELLA_NATURE = "siriusWeb://nature?kind=capella";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        return List.of(new ProjectTemplate(CAPELLA_TEMPLATE_ID, "Capella", "/images/logo_capella.png", List.of(new ProjectTemplateNature(CAPELLA_NATURE))));
    }
}

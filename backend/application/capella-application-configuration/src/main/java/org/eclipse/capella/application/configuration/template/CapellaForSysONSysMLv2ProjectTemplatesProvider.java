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
import org.springframework.stereotype.Service;

/**
 * Provides SysMLv2-specific project templates.
 * Override Syson configuration to only keep sysmlv2 template
 *
 * @author frouene
 */
@Service
public class CapellaForSysONSysMLv2ProjectTemplatesProvider implements IProjectTemplateProvider {

    public static final String SYSMLV2_TEMPLATE_ID = "sysmlv2-template";

    @Override
    public List<ProjectTemplate> getProjectTemplates() {
        var sysmlv2Template = new ProjectTemplate(SYSMLV2_TEMPLATE_ID, "SysMLv2", "/images/sysmlv2-logo.png", List.of());
        return List.of(sysmlv2Template);
    }

}

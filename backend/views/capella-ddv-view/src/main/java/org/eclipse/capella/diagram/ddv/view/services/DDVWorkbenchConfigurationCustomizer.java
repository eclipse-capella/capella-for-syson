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
package org.eclipse.capella.diagram.ddv.view.services;

import org.eclipse.sirius.components.collaborative.workbenchconfiguration.api.IWorkbenchConfigurationCustomizer;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.DefaultViewConfiguration;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.IViewConfiguration;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.WorkbenchConfiguration;
import org.eclipse.sirius.components.collaborative.workbenchconfiguration.dto.WorkbenchSidePanelConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The workbench configuration customizer adding the DDV view on all workbench configuration.
 *
 * @author fbarbin
 */
@Service
public class DDVWorkbenchConfigurationCustomizer implements IWorkbenchConfigurationCustomizer {
    @Override
    public WorkbenchConfiguration customize(String editingContextId, WorkbenchConfiguration workbenchConfiguration) {
        if (workbenchConfiguration.workbenchPanels().size() > 1) {
            var leftWorkbenchSidePanelConfiguration = workbenchConfiguration.workbenchPanels().get(0);
            var rightWorkbenchSidePanelConfiguration = workbenchConfiguration.workbenchPanels().get(1);

            List<IViewConfiguration> rightViewConfigurations = new ArrayList<>(rightWorkbenchSidePanelConfiguration.views());
            rightViewConfigurations.add(new DefaultViewConfiguration("ddv-view", false));
            rightWorkbenchSidePanelConfiguration = new WorkbenchSidePanelConfiguration("right", true, List.copyOf(rightViewConfigurations));

            return new WorkbenchConfiguration(
                    workbenchConfiguration.mainPanel(),
                    List.of(
                            leftWorkbenchSidePanelConfiguration,
                            rightWorkbenchSidePanelConfiguration
                    )
            );
        }

        return workbenchConfiguration;
    }
}

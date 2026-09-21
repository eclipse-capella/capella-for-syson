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

package org.eclipse.capella.diagram.customization.filters;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilter;
import org.springframework.stereotype.Component;

/**
 * The Show Activities filter.
 * This filter is active by default.
 *
 * @author Jerome Gout
 */
@Component
public class ShowActivitiesDiagramFilter implements IDiagramFilter {

    public static final String ID = "capella.diagram.filter.showActivities";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getLabel() {
        return "Show Activities";
    }

    @Override
    public String getActiveTooltip() {
        return "Hide Activities in Diagram";
    }

    @Override
    public String getInactiveTooltip() {
        return "Show Activities in Diagram";
    }

    @Override
    public String getURLParam() {
        return "showActivities";
    }

    @Override
    public boolean getInitialState() {
        return true;
    }
}

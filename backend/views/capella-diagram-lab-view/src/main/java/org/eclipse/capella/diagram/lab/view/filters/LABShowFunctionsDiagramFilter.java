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

package org.eclipse.capella.diagram.lab.view.filters;

import org.eclipse.capella.diagram.customization.filters.AbstractDiagramFilter;
import org.eclipse.capella.diagram.customization.services.ElementNodeFilteringService;
import org.springframework.stereotype.Component;

/**
 * The Show Functions filter available to LAB Capella diagram.
 * This filter is active by default.
 *
 * @author Jerome Gout
 */
@Component
public class LABShowFunctionsDiagramFilter extends AbstractDiagramFilter {

    public static final String ID = "capella.lab.diagram.filter.showFunctions";

    public LABShowFunctionsDiagramFilter(ElementNodeFilteringService elementNodeFilteringService) {
        super(ID);
    }

    @Override
    public String getLabel() {
        return "Show Functions";
    }

    @Override
    public String getActiveTooltip() {
        return "Hide Functions in Diagram";
    }

    @Override
    public String getInactiveTooltip() {
        return "Show Functions in Diagram";
    }

    @Override
    public String getURLParam() {
        return "labShowFunctions";
    }

    @Override
    public boolean getInitialState() {
        return true;
    }
}

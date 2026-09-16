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

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.springframework.stereotype.Component;

/**
 * The Show Functions filter available to all Capella diagrams but OCB one.
 * This filter is active by default.
 *
 * @author Jerome Gout
 */
@Component
public class ShowFunctionsDiagramFilter extends AbstractNodeDiagramFilter {

    public static final String ID = "capella.diagram.filter.showFunctions";

    private final TransverseQueryService transverseQueryService;

    ShowFunctionsDiagramFilter(IObjectSearchService objectSearchService) {
        super(ID, objectSearchService);
        this.transverseQueryService = new TransverseQueryService();    }

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
        return "showFunctions";
    }

    @Override
    protected boolean isNodeTargetElement(EObject element) {
        return this.transverseQueryService.isFunction(element);
    }
}

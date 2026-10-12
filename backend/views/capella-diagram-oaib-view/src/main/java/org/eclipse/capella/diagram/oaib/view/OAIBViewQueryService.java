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

import java.util.Objects;

import org.eclipse.capella.model.services.operational.analysis.OAQueryService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.syson.sysml.Feature;

/**
 * Evaluates Interaction tool availability in node and connector palettes.
 *
 * @author tbezierslafosse
 */
public class OAIBViewQueryService {

    private final IObjectSearchService objectSearchService;

    private final OAQueryService oaQueryService = new OAQueryService();

    private final CommonQueryService commonQueryService = new CommonQueryService();

    public OAIBViewQueryService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    /**
     * Checks the source alone in the node palette (null target), or both endpoints in the connector palette.
     */
    public boolean canCreateInteraction(Object receiver, Object target, IEditingContext editingContext) {
        Object source = receiver;
        if (receiver instanceof Node node) {
            source = this.objectSearchService.getObject(editingContext, node.getTargetObjectId()).orElse(null);
        }
        if (source instanceof Feature activity && this.commonQueryService.isOperationalActivity(activity)) {
            return target == null || (target instanceof Feature feature && this.oaQueryService.canCreateInteractionOA(activity, feature));
        }
        return false;
    }
}

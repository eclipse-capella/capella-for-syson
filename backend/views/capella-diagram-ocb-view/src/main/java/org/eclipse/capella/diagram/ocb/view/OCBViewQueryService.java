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
package org.eclipse.capella.diagram.ocb.view;

import java.util.Objects;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;

/**
 * Evaluates OCB tool availability for semantic sources and graphical connector targets.
 *
 * @author tbezierslafosse
 */
public class OCBViewQueryService {

    private final IObjectSearchService objectSearchService;

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    public OCBViewQueryService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    public boolean canCreateInvolvement(Object receiver, IEditingContext editingContext) {
        if (receiver instanceof EObject semanticSource) {
            return this.transverseQueryService.isCapability(semanticSource);
        }
        return this.transverseQueryService.isComponent(this.getSemanticElement(receiver, editingContext));
    }

    public boolean canCreateGeneralization(Object receiver, IEditingContext editingContext) {
        return this.transverseQueryService.isCapability(this.getSemanticElement(receiver, editingContext));
    }

    private EObject getSemanticElement(Object receiver, IEditingContext editingContext) {
        EObject semanticElement = null;
        if (receiver instanceof EObject eObject) {
            semanticElement = eObject;
        } else if (receiver instanceof Node node) {
            semanticElement = this.objectSearchService.getObject(editingContext, node.getTargetObjectId())
                    .filter(EObject.class::isInstance)
                    .map(EObject.class::cast)
                    .orElse(null);
        }
        return semanticElement;
    }
}

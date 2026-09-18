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

package org.eclipse.capella.diagram.customization.services.api;

import java.util.function.Predicate;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.core.api.IEditingContext;

/**
 * Service for filtering node element in diagrams.
 *
 * @author Jerome Gout
 */
public interface IElementNodeFilteringService {

    /**
     * Add {@link org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent} events in the diagram to perform the filter.
     *
     * @param editingContext
     *         The editing context
     * @param diagramContext
     *         The diagram context
     * @param elementPredicate
     *         A predicate used to determine whether the semantic element of a node should be filtered
     * @param state
     *         The activation state of the filter (<code>true</code> nodes are displayed, <code>false</code> nodes are hidden)
     */
    void filter(IEditingContext editingContext, DiagramContext diagramContext, Predicate<EObject> elementPredicate, boolean state);
}

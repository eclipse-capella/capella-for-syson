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

import org.eclipse.capella.diagram.customization.services.ElementNodeFilteringService;
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
     * Initialize the filtering mechanism in a specific diagram.
     *
     * @param editingContext
     *         The editing context from AQL variable
     * @param diagramContext
     *         The digram context from AQL variable.
     * @return the instance of the service itself to allow method chaining.
     */
    ElementNodeFilteringService init(IEditingContext editingContext, DiagramContext diagramContext);

    /**
     * Configure the predicate used to determine the condition to filter a node depending on its semantic element.
     * @param elementPredicate a predicate used to determine whether the given semantic element should be filtered or not.
     * @return the instance of the service itself to allow method chaining.
     */
    ElementNodeFilteringService withElementPredicate(Predicate<EObject> elementPredicate);

    /**
     * Add {@link org.eclipse.sirius.components.diagrams.events.HideDiagramElementEvent} events in the diagram to perform the filter.
     *
     * @param state the activation state of the filter (<code>true</code> nodes are displayed, <code>false</code> nodes are hidden).
     */
    void filter(boolean state);
}

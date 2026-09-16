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

import java.util.List;

import org.eclipse.sirius.components.representations.IRepresentationDescription;

/**
 * Interface for providing diagram filter for a specific representation description.
 *
 * @author Jerome Gout
 */
public interface IDiagramFiltersProvider {

    boolean canHandle(IRepresentationDescription representationDescription);

    List<IDiagramFilter> getDiagramFilters();
}

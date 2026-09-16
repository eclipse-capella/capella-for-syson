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

package org.eclipse.capella.diagram.customization.datafetchers;

import java.util.Objects;

import org.eclipse.capella.diagram.customization.services.api.IDiagramFilterService;
import org.eclipse.sirius.components.annotations.spring.graphql.QueryDataFetcher;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;

import graphql.schema.DataFetchingEnvironment;

/**
 * Data fetcher for Viewer#diagramFilterState query.
 *
 * @author Jerome Gout
 */
@QueryDataFetcher(type = "Viewer", field = "diagramFilterState")
public class ViewerDiagramFilterStateDataFetcher implements IDataFetcherWithFieldCoordinates<Boolean> {

    public static final String INPUT_PARAMETER = "diagramFilterId";

    private final IDiagramFilterService diagramFilterService;

    public ViewerDiagramFilterStateDataFetcher(IDiagramFilterService diagramFilterService) {
        this.diagramFilterService = Objects.requireNonNull(diagramFilterService);
    }

    @Override
    public Boolean get(DataFetchingEnvironment environment) throws Exception {
        String diagramFilterId = environment.getArgument(INPUT_PARAMETER);
        return Boolean.valueOf(this.diagramFilterService.isDiagramFilterActive(diagramFilterId));
    }
}
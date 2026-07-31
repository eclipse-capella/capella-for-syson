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
package org.eclipse.capella.diagram.lab.view.services.datafetchers;

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.services.ShowDiagramFunctionsService;
import org.eclipse.sirius.components.annotations.spring.graphql.QueryDataFetcher;
import org.eclipse.sirius.components.graphql.api.IDataFetcherWithFieldCoordinates;

import graphql.schema.DataFetchingEnvironment;

/**
 * Data fetcher for Viewer#showDiagramFunctions query.
 *
 * @author fbarbin
 */
@QueryDataFetcher(type = "Viewer", field = "showDiagramFunctionsValue")
public class ShowDiagramFunctionsDataFetcher implements IDataFetcherWithFieldCoordinates<Boolean> {

    private final ShowDiagramFunctionsService showDiagramFunctionsService;

    public ShowDiagramFunctionsDataFetcher(ShowDiagramFunctionsService showDiagramFunctionsService) {
        this.showDiagramFunctionsService = Objects.requireNonNull(showDiagramFunctionsService);
    }

    @Override
    public Boolean get(DataFetchingEnvironment environment) throws Exception {
        return Boolean.valueOf(this.showDiagramFunctionsService.isShowFunctions());
    }
}
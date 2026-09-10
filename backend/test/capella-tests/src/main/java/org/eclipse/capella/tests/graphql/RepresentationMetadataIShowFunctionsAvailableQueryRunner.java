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

package org.eclipse.capella.tests.graphql;

import java.util.Map;
import java.util.Objects;

import org.eclipse.sirius.components.graphql.tests.api.GraphQLResult;
import org.eclipse.sirius.components.graphql.tests.api.IGraphQLRequestor;
import org.eclipse.sirius.components.graphql.tests.api.IQueryRunner;
import org.springframework.stereotype.Service;

/**
 * Executes the Capella RepresentationMetadata#isShowFunctionsAvailable query.
 *
 * @author Jerome Gout
 */
@Service
public class RepresentationMetadataIShowFunctionsAvailableQueryRunner implements IQueryRunner {

    private static final String QUERY = """
              query getIsShowFunctionsAvailableQuery($editingContextId: ID!, $representationId: ID!) {
                viewer {
                  editingContext(editingContextId: $editingContextId) {
                    representation(representationId: $representationId) {
                      isShowFunctionsAvailable
                    }
                  }
                }
              }
            """;

    private final IGraphQLRequestor graphQLRequestor;

    public RepresentationMetadataIShowFunctionsAvailableQueryRunner(IGraphQLRequestor graphQLRequestor) {
        this.graphQLRequestor = Objects.requireNonNull(graphQLRequestor);
    }

    @Override
    public GraphQLResult run(Map<String, Object> variables) {
        return this.graphQLRequestor.execute(QUERY, variables);
    }
}

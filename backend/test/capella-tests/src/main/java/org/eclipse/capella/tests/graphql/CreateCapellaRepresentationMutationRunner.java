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

import java.util.Objects;

import org.eclipse.sirius.components.core.api.IInput;
import org.eclipse.sirius.components.graphql.tests.api.GraphQLResult;
import org.eclipse.sirius.components.graphql.tests.api.IGraphQLRequestor;
import org.eclipse.sirius.components.graphql.tests.api.IMutationRunner;
import org.springframework.stereotype.Service;

/**
 * Executes the Capella representation creation mutation.
 *
 * @author tbezierslafosse
 */
@Service
public class CreateCapellaRepresentationMutationRunner implements IMutationRunner<IInput> {

    private static final String MUTATION = """
            mutation createCapellaRepresentation($input: CreateCapellaRepresentationInput!) {
              createCapellaRepresentation(input: $input) {
                __typename
                ... on CreateRepresentationSuccessPayload {
                  representation {
                    id
                    label
                    kind
                  }
                }
                ... on ErrorPayload {
                  message
                }
              }
            }
            """;

    private final IGraphQLRequestor graphQLRequestor;

    public CreateCapellaRepresentationMutationRunner(IGraphQLRequestor graphQLRequestor) {
        this.graphQLRequestor = Objects.requireNonNull(graphQLRequestor);
    }

    @Override
    public GraphQLResult run(IInput input) {
        return this.graphQLRequestor.execute(MUTATION, input);
    }
}

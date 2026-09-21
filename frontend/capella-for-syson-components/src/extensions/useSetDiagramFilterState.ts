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

import { gql, useMutation } from '@apollo/client';

import {
  GQLSetDiagramFilterStateMutationData,
  GQLSetDiagramFilterStateMutationInput,
  GQLSetDiagramFilterStateMutationVariables,
} from './DiagramFilter.types';

const setDiagramFilterStateMutation = gql`
  mutation setDiagramFilterStateMutation($input: SetDiagramFilterStateInput!) {
    setDiagramFilterState(input: $input) {
      __typename
      ... on SetDiagramFilterStateSuccessPayload {
        active
      }
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
    }
  }
`;

export const useSetDiagramFilterState = (editingContextId: string, diagramId: string, filterId: string) => {
  const [setDiagramFilterStateMutationCallback, result] = useMutation<
    GQLSetDiagramFilterStateMutationData,
    GQLSetDiagramFilterStateMutationVariables
  >(setDiagramFilterStateMutation);

  const setDiagramFilterState = (active: boolean) => {
    const input: GQLSetDiagramFilterStateMutationInput = {
      id: crypto.randomUUID(),
      editingContextId,
      representationId: diagramId,
      filterId,
      active,
    };

    return setDiagramFilterStateMutationCallback({ variables: { input } });
  };

  return { setDiagramFilterState, ...result };
};

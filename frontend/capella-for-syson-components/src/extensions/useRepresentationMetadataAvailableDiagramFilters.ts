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

import { gql, useQuery } from '@apollo/client';
import { useMultiToast } from '@eclipse-sirius/sirius-components-core';
import {
  GQLRepresentationMetadataAvailableDiagramFiltersData,
  GQLRepresentationMetadataAvailableDiagramFiltersVariables,
  GQLAvailableFilter,
} from './useRepresentationMetadataAvailableDiagramFilters.types';
import { useEffect } from 'react';

export const getRepresentationMetadataAvailableDiagramFiltersQuery = gql`
  query getRepresentationMetadataAvailableDiagramFiltersQuery($editingContextId: ID!, $representationId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        representation(representationId: $representationId) {
          availableDiagramFilters {
            id
            label
            state
            activeTooltip
            inactiveTooltip
            urlParam
          }
        }
      }
    }
  }
`;

export const useRepresentationMetadataAvailableDiagramFilters = (
  editingContextId: string,
  diagramId: string
): GQLAvailableFilter[] => {
  const { data, error } = useQuery<
    GQLRepresentationMetadataAvailableDiagramFiltersData,
    GQLRepresentationMetadataAvailableDiagramFiltersVariables
  >(getRepresentationMetadataAvailableDiagramFiltersQuery, {
    variables: {
      editingContextId,
      representationId: diagramId,
    },
  });

  const { addErrorMessage } = useMultiToast();
  useEffect(() => {
    if (error) {
      addErrorMessage(error.message);
    }
  }, [error]);

  return data?.viewer.editingContext.representation.availableDiagramFilters ?? [];
};

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
  GQLRepresentationMetadataDiagramFiltersData,
  GQLRepresentationMetadataDiagramFiltersVariables,
  GQLDiagramFilter,
} from './useRepresentationMetadataDiagramFilters.types';
import { useEffect } from 'react';

export const getRepresentationMetadataDiagramFiltersQuery = gql`
  query getRepresentationMetadataDiagramFilters($editingContextId: ID!, $representationId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        representation(representationId: $representationId) {
          diagramFilters {
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

export const useRepresentationMetadataDiagramFilters = (
  editingContextId: string,
  diagramId: string
): GQLDiagramFilter[] => {
  const { data, error } = useQuery<
    GQLRepresentationMetadataDiagramFiltersData,
    GQLRepresentationMetadataDiagramFiltersVariables
  >(getRepresentationMetadataDiagramFiltersQuery, {
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

  return data?.viewer.editingContext.representation.diagramFilters ?? [];
};

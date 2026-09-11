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
  GQLRepresentationMetadataData,
  GQLRepresentationMetadataVariables,
  GQLRepresentationMetadata,
} from './useRepresentationMetadata.types';
import { useEffect } from 'react';

export const getIsShowFunctionsAvailableQuery = gql`
  query getIsShowFunctionsAvailableQuery($editingContextId: ID!, $representationId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        representation(representationId: $representationId) {
          isShowFunctionsAvailable
        }
      }
    }
  }
`;

export const useRepresentationMetadata = (editingContextId: string, diagramId: string): GQLRepresentationMetadata => {
  const { data, error } = useQuery<GQLRepresentationMetadataData, GQLRepresentationMetadataVariables>(
    getIsShowFunctionsAvailableQuery,
    {
      variables: {
        editingContextId,
        representationId: diagramId,
      },
    }
  );

  const { addErrorMessage } = useMultiToast();
  useEffect(() => {
    if (error) {
      addErrorMessage(error.message);
    }
  }, [error]);

  const isShowFunctionsAvailable = data?.viewer.editingContext.representation.isShowFunctionsAvailable;

  return isShowFunctionsAvailable !== undefined ? { isShowFunctionsAvailable } : { isShowFunctionsAvailable: false };
};

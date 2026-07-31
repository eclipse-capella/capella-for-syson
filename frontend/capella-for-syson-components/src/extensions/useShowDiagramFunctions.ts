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
import { useEffect } from 'react';
import { GQLShowDiagramFunctionsQueryData } from './useShowDiagramFunctions.types';

export const showDiagramFunctionsQuery = gql`
  query getShowDiagramFunctions {
    viewer {
      showDiagramFunctionsValue
    }
  }
`;

export const useShowDiagramFunctions = () => {
  const { data, loading, error, refetch } = useQuery<GQLShowDiagramFunctionsQueryData>(showDiagramFunctionsQuery);

  const { addErrorMessage } = useMultiToast();
  useEffect(() => {
    if (error) {
      addErrorMessage(error.message);
    }
  }, [error]);

  const refreshShowDiagramFunctions = () => refetch();

  return { data: data ?? null, loading, refreshShowDiagramFunctions };
};

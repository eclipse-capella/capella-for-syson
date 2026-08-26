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
import { DiagramToolbarActionProps } from '@eclipse-sirius/sirius-components-diagrams';
import Checkbox from '@mui/material/Checkbox';
import Tooltip from '@mui/material/Tooltip';
import { useEffect, useState } from 'react';

import { useMultiToast } from '@eclipse-sirius/sirius-components-core';
import {
  GQLErrorPayload,
  GQLSetShowDiagramFunctionsPayload,
  GQLShowDiagramFunctionsMutationData,
  GQLShowDiagramFunctionsMutationInput,
  GQLShowDiagramFunctionsMutationVariables,
  ShowDiagramFunctionsState,
} from './ShowHideDiagramFunctions.types';
import { useShowDiagramFunctions } from './useShowDiagramFunctions';

const setShowDiagramFunctionsMutation = gql`
  mutation showDiagramFunctions($input: ShowDiagramFunctionsInput!) {
    showDiagramFunctions(input: $input) {
      __typename
      ... on ShowDiagramFunctionsSuccessPayload {
        show
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

const isErrorPayload = (payload: GQLSetShowDiagramFunctionsPayload): payload is GQLErrorPayload =>
  payload.__typename === 'ErrorPayload';

const showTooltip: string = 'Hide Functions in Diagrams';
const hideTooltip: string = 'Show Functions in Diagrams';

export const ShowHideDiagramFunctions = ({ editingContextId, diagramId }: DiagramToolbarActionProps) => {
  const [state, setState] = useState<ShowDiagramFunctionsState>({
    checked: null,
    tooltip: 'Show/Hide Functions in Diagram',
  });

  const { addMessages, addErrorMessage } = useMultiToast();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    updateDiagramFunctionsVisibility(event.target.checked);
  };

  useEffect(() => {
    const timeout = setTimeout(() => {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has('showFunctions') && urlParams.get('showFunctions') === 'false') {
        updateDiagramFunctionsVisibility(false);
      }
    }, 200);

    return () => clearTimeout(timeout);
  }, []);

  const updateDiagramFunctionsVisibility = (show: boolean) => {
    const input: GQLShowDiagramFunctionsMutationInput = {
      id: crypto.randomUUID(),
      editingContextId,
      representationId: diagramId,
      show,
    };
    showDiagramFunctions({ variables: { input } });
    setState((prevState) => {
      const checked: boolean = show;
      return { ...prevState, checked, tooltip: checked ? showTooltip : hideTooltip };
    });
  };

  const { data: queryData, loading: queryLoading } = useShowDiagramFunctions();
  if (!queryLoading && queryData && state.checked === null) {
    const showDiagramFunctions: boolean = queryData?.viewer.showDiagramFunctionsValue;
    if (showDiagramFunctions !== state.checked) {
      setState((prevState) => {
        return {
          ...prevState,
          checked: showDiagramFunctions,
          tooltip: showDiagramFunctions ? showTooltip : hideTooltip,
        };
      });
    }
  }

  const [showDiagramFunctions, { loading, data, error }] = useMutation<
    GQLShowDiagramFunctionsMutationData,
    GQLShowDiagramFunctionsMutationVariables
  >(setShowDiagramFunctionsMutation);

  useEffect(() => {
    if (!loading) {
      if (error) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (data) {
        const { showDiagramFunctions } = data;
        if (showDiagramFunctions.show !== null && showDiagramFunctions.show !== undefined) {
          setState((prevState) => {
            return { ...prevState, checked: showDiagramFunctions.show };
          });
        }
        if (isErrorPayload(showDiagramFunctions)) {
          addMessages(showDiagramFunctions.messages);
        }
      }
    }
  }, [loading, error, data]);

  return (
    <Tooltip title={state.tooltip} placement="left">
      <Checkbox checked={state.checked !== null ? state.checked : true} onChange={handleChange} />
    </Tooltip>
  );
};

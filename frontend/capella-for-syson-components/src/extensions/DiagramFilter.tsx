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
import Checkbox from '@mui/material/Checkbox';
import Tooltip from '@mui/material/Tooltip';
import { useEffect, useState } from 'react';

import { useMultiToast } from '@eclipse-sirius/sirius-components-core';
import {
  GQLErrorPayload,
  GQLSetDiagramFilterStatePayload,
  GQLSetDiagramFilterStateMutationData,
  GQLSetDiagramFilterStateMutationInput,
  GQLSetDiagramFilterStateMutationVariables,
  DiagramFilterState,
} from './DiagramFilter.types';
import { DiagramFilterProps } from './DiagramFilter.types';

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

const isErrorPayload = (payload: GQLSetDiagramFilterStatePayload): payload is GQLErrorPayload =>
  payload.__typename === 'ErrorPayload';

export const DiagramFilter = ({ editingContextId, diagramId, filter }: DiagramFilterProps) => {
  const [state, setState] = useState<DiagramFilterState>({
    checked: filter.state,
    tooltip: filter.state ? filter.activeTooltip : filter.inactiveTooltip,
  });

  const { addMessages, addErrorMessage } = useMultiToast();

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    updateDiagramFunctionsVisibility(event.target.checked);
  };

  useEffect(() => {
    const timeout = setTimeout(() => {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has(filter.urlParam) && urlParams.get(filter.urlParam) === 'false') {
        updateDiagramFunctionsVisibility(false);
      }
    }, 200);

    return () => clearTimeout(timeout);
  }, []);

  const updateDiagramFunctionsVisibility = (show: boolean) => {
    const input: GQLSetDiagramFilterStateMutationInput = {
      id: crypto.randomUUID(),
      editingContextId,
      representationId: diagramId,
      filterId: filter.id,
      active: show,
    };
    setDiagramFilterState({ variables: { input } });
    setState((prevState) => {
      const checked: boolean = show;
      return { ...prevState, checked, tooltip: checked ? filter.activeTooltip : filter.inactiveTooltip };
    });
  };

  const [setDiagramFilterState, { loading, data, error }] = useMutation<
    GQLSetDiagramFilterStateMutationData,
    GQLSetDiagramFilterStateMutationVariables
  >(setDiagramFilterStateMutation);

  useEffect(() => {
    if (!loading) {
      if (error) {
        addErrorMessage('An unexpected error has occurred, please refresh the page');
      }
      if (data) {
        const { setDiagramFilterState } = data;
        if (setDiagramFilterState.active !== null && setDiagramFilterState.active !== undefined) {
          setState((prevState) => {
            return { ...prevState, checked: setDiagramFilterState.active };
          });
          // update local filter state to reflect the change in the UI
          filter.state = setDiagramFilterState.active;
        }
        if (isErrorPayload(setDiagramFilterState)) {
          addMessages(setDiagramFilterState.messages);
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

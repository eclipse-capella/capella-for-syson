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

import Checkbox from '@mui/material/Checkbox';
import Tooltip from '@mui/material/Tooltip';
import { useEffect, useState } from 'react';

import { useMultiToast } from '@eclipse-sirius/sirius-components-core';
import { GQLErrorPayload, GQLSetDiagramFilterStatePayload, DiagramFilterState } from './DiagramFilter.types';
import { DiagramFilterProps } from './DiagramFilter.types';
import { useSetDiagramFilterState } from './useSetDiagramFilterState';

const isErrorPayload = (payload: GQLSetDiagramFilterStatePayload): payload is GQLErrorPayload =>
  payload.__typename === 'ErrorPayload';

export const DiagramFilter = ({ editingContextId, diagramId, filter }: DiagramFilterProps) => {
  const [state, setState] = useState<DiagramFilterState>({
    checked: filter.state,
    tooltip: filter.state ? filter.activeTooltip : filter.inactiveTooltip,
  });

  const { addMessages, addErrorMessage } = useMultiToast();
  const { setDiagramFilterState, loading, data, error } = useSetDiagramFilterState(
    editingContextId,
    diagramId,
    filter.id
  );

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
    setDiagramFilterState(show);
    setState((prevState) => {
      const checked: boolean = show;
      return { ...prevState, checked, tooltip: checked ? filter.activeTooltip : filter.inactiveTooltip };
    });
  };

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

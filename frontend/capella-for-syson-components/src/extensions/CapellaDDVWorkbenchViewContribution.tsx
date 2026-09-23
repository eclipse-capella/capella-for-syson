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

import { gql, useMutation, useQuery } from '@apollo/client';
import {
  SelectionContextProvider,
  useReporting,
  useSelection,
  ViewAccordion,
  ViewAccordionContent,
  ViewAccordionToolbar,
  WorkbenchViewComponentProps,
  WorkbenchViewHandle,
} from '@eclipse-sirius/sirius-components-core';
import { DiagramRepresentation } from '@eclipse-sirius/sirius-components-diagrams';
import OpenInNewIcon from '@mui/icons-material/OpenInNew';
import Box from '@mui/material/Box';
import FormControl from '@mui/material/FormControl';
import IconButton from '@mui/material/IconButton';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import Stack from '@mui/material/Stack';
import { SxProps, Theme } from '@mui/material/styles';
import { forwardRef, useEffect, useImperativeHandle, useState } from 'react';
import {
  CapellaDDVWorkbenchViewContributionState,
  GQLGetRepresentationDescriptionsQueryData,
  GQLGetRepresentationDescriptionsQueryVariables,
  GQLSaveCapellaDDVDiagramMutationData,
  GQLSaveCapellaDDVDiagramMutationVariables,
  GQLSaveCapellaDDVDiagramPayload,
  GQLSaveCapellaDDVDiagramSuccessPayload,
} from './CapellaDDVWorkbenchViewContribution.types';

const ddvDescriptionsLabels: string[] = ['Functional Context Diagram'];

const contentStyle: SxProps<Theme> = (theme) => ({
  display: 'grid',
  gridTemplateColumns: 'minmax(0, 1fr)',
  gridTemplateRows: 'minmax(0, 1fr)',
  height: '100%',
  minHeight: 0,
  overflow: 'hidden',
  padding: theme.spacing(1),
});

const getRepresentationDescriptionsQuery = gql`
  query getRepresentationDescriptions($editingContextId: ID!, $objectId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        representationDescriptions(objectId: $objectId) {
          edges {
            node {
              id
              label
              defaultName
            }
          }
          pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
          }
        }
      }
    }
  }
`;

const setSaveCapellaDDVDiagramMutation = gql`
  mutation saveCapellaDDVDiagram($input: SaveCapellaDDVDiagramInput!) {
    saveCapellaDDVDiagram(input: $input) {
      __typename
      ... on SaveCapellaDDVDiagramSuccessPayload {
        id
        representationId
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

export const CapellaDDVWorkbenchViewContribution = forwardRef<WorkbenchViewHandle, WorkbenchViewComponentProps>(
  ({ id, editingContextId, readOnly }: WorkbenchViewComponentProps, ref) => {
    useImperativeHandle(ref, () => ({
      id,
      getWorkbenchViewConfiguration: () => ({ id: id }),
      applySelection: null,
    }));
    const [state, setState] = useState<CapellaDDVWorkbenchViewContributionState>({
      representationDescriptions: [],
      selectedRepresentationDescriptionId: '',
    });

    const { selection, setSelection } = useSelection();
    const targetObjectId: string = selection?.entries[0]?.id ?? '';
    let toolbar: JSX.Element | null = null;
    let content: JSX.Element | null = null;

    const { data: representationDescriptionsData } = useQuery<
      GQLGetRepresentationDescriptionsQueryData,
      GQLGetRepresentationDescriptionsQueryVariables
    >(getRepresentationDescriptionsQuery, { variables: { editingContextId, objectId: targetObjectId } });

    const [saveCapellaDDVDiagram, saveCapellaDDVDiagramResult] = useMutation<
      GQLSaveCapellaDDVDiagramMutationData,
      GQLSaveCapellaDDVDiagramMutationVariables
    >(setSaveCapellaDDVDiagramMutation);

    const onRepresentationDescriptionChange = (event) => {
      const value = event.target.value;
      setState((prevState) => ({
        ...prevState,
        selectedRepresentationDescriptionId: value,
      }));
    };

    useReporting(saveCapellaDDVDiagramResult, (data: GQLSaveCapellaDDVDiagramMutationData) => ({
      __typename: 'SuccessPayload',
      id: data.saveCapellaDDVDiagram.id,
      messages: data.saveCapellaDDVDiagram.messages,
    }));

    useEffect(() => {
      if (representationDescriptionsData) {
        const descriptions = representationDescriptionsData.viewer.editingContext.representationDescriptions.edges
          .map((edge) => edge.node)
          .filter((description) => ddvDescriptionsLabels.includes(description.label));
        setState((prevState) => {
          let newState = {
            ...prevState,
            representationDescriptions: descriptions,
          };
          // If the current selection is not included in the new descriptions, reset it
          if (descriptions.length > 0) {
            const isCurrentSelectionIncluded = descriptions
              .map((description) => description.id)
              .includes(prevState.selectedRepresentationDescriptionId);
            if (!isCurrentSelectionIncluded) {
              const selected = descriptions[0];
              if (selected) {
                newState.selectedRepresentationDescriptionId = selected.id;
              }
            }
          } else {
            // If no valid descriptions are found, reset the selection
            newState.selectedRepresentationDescriptionId = '';
          }
          return newState;
        });
      }
    }, [representationDescriptionsData]);

    const saveCurrentDiagram = (representationId: string, editingContextId: string): void => {
      saveCapellaDDVDiagram({
        variables: {
          input: {
            id: crypto.randomUUID(),
            editingContextId,
            representationId,
          },
        },
      }).then((result) => {
        if (result.data?.saveCapellaDDVDiagram) {
          if (isSaveCapellaDDVDiagramSuccessPayload(result.data.saveCapellaDDVDiagram)) {
            const newRepresentationId = result.data.saveCapellaDDVDiagram.representationId;
            if (newRepresentationId) {
              setSelection({ entries: [{ id: newRepresentationId }] });
            }
          }
        }
      });
    };

    if (state.selectedRepresentationDescriptionId) {
      const representationId = `capella-ddv://?descriptionId=${encodeURIComponent(
        state.selectedRepresentationDescriptionId
      )}&targetObjectId=${encodeURIComponent(targetObjectId)}`;
      toolbar = (
        <Stack direction="row" spacing={1} alignItems="center" sx={{ width: '100%' }}>
          <FormControl size="small" sx={{ minWidth: 220 }}>
            <InputLabel id="capella-ddv-representation-description-label">Representation description</InputLabel>
            <Select
              labelId="capella-ddv-representation-description-label"
              id="capella-ddv-representation-description"
              value={state.selectedRepresentationDescriptionId}
              label="Representation description"
              onChange={onRepresentationDescriptionChange}
              data-testid="capella-ddv-representation-description-select">
              {state.representationDescriptions.map((representationDescription) => (
                <MenuItem key={representationDescription.id} value={representationDescription.id}>
                  {representationDescription.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Box sx={{ flex: 1 }} />

          <IconButton
            color="primary"
            size="small"
            data-testid="capella-ddv-save-button"
            aria-label="Save diagram"
            onClick={() => saveCurrentDiagram(representationId, editingContextId)}>
            <OpenInNewIcon />
          </IconButton>
        </Stack>
      );
      content = (
        <Box sx={contentStyle} data-testid="capella-ddv-workbench-view">
          <SelectionContextProvider initialSelection={{ entries: [] }}>
            <DiagramRepresentation
              key={`${editingContextId}#${representationId}`}
              editingContextId={editingContextId}
              representationId={representationId}
              readOnly={readOnly}
            />
          </SelectionContextProvider>
        </Box>
      );
    }
    return (
      <ViewAccordion id={id} title="Dynamic Contextual View">
        {toolbar ? <ViewAccordionToolbar>{toolbar}</ViewAccordionToolbar> : null}
        <ViewAccordionContent>{content}</ViewAccordionContent>
      </ViewAccordion>
    );
  }
);

const isSaveCapellaDDVDiagramSuccessPayload = (
  payload: GQLSaveCapellaDDVDiagramPayload
): payload is GQLSaveCapellaDDVDiagramSuccessPayload => {
  return payload.__typename === 'SaveCapellaDDVDiagramSuccessPayload';
};

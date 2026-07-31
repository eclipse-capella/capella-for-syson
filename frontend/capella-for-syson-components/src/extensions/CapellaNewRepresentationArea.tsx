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
import { Toast, useSelection } from '@eclipse-sirius/sirius-components-core';
import Collections from '@mui/icons-material/Collections';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Typography from '@mui/material/Typography';
import { useEffect, useState } from 'react';
import { makeStyles } from 'tss-react/mui';
import {
  GQLCreateRepresentationData,
  GQLCreateRepresentationInput,
  GQLCreateRepresentationVariables,
  GQLErrorPayload,
  CapellaNewRepresentationAreaProps,
  CapellaNewRepresentationAreaState,
} from './CapellaNewRepresentationArea.types';

const useNewRepresentationAreaStyles = makeStyles()((theme) => ({
  subtitles: {
    textOverflow: 'ellipsis " [..]";',
  },
  cardContent: {
    overflowY: 'auto',
    maxHeight: theme.spacing(50),
  },
  item: {
    padding: 0,
  },
}));

const createCapellaRepresentationMutation = gql`
  mutation createCapellaRepresentation($input: CreateCapellaRepresentationInput!) {
    createCapellaRepresentation(input: $input) {
      __typename
      ... on CreateRepresentationSuccessPayload {
        representation {
          id
          label
          kind
          __typename
        }
      }
      ... on ErrorPayload {
        message
      }
    }
  }
`;

const isErrorPayload = (payload): payload is GQLErrorPayload => payload.__typename === 'ErrorPayload';

export const CapellaNewRepresentationArea = ({ editingContextId, readOnly }: CapellaNewRepresentationAreaProps) => {
  const [state, setState] = useState<CapellaNewRepresentationAreaState>({
    message: undefined,
  });
  const { classes } = useNewRepresentationAreaStyles();
  const { setSelection } = useSelection();

  // Representation creation
  const [createRepresentation, { loading, data, error }] = useMutation<
    GQLCreateRepresentationData,
    GQLCreateRepresentationVariables
  >(createCapellaRepresentationMutation);

  useEffect(() => {
    if (!loading) {
      if (error) {
        setState({ message: 'An unexpected error has occurred, please refresh the page' });
      }
      if (data) {
        const { createCapellaRepresentation } = data;
        if (createCapellaRepresentation.representation) {
          const { id } = createCapellaRepresentation.representation;
          setSelection({ entries: [{ id }] });
        }
        if (isErrorPayload(createCapellaRepresentation)) {
          setState({ message: createCapellaRepresentation.message });
        }
      }
    }
  }, [loading, error, data]);

  const onCreateRepresentation = (representationDescriptionId: string) => {
    const input: GQLCreateRepresentationInput = {
      id: crypto.randomUUID(),
      editingContextId,
      representationDescriptionId,
    };
    createRepresentation({ variables: { input } });
  };

  return (
    <>
      <Card>
        <CardContent className={classes.cardContent}>
          <Typography variant="h6">{'Create a new Representation'}</Typography>
          <List dense={true}>
            {readOnly ? null : (
              <>
                <ListItemButton
                  className={classes.item}
                  dense
                  disableGutters
                  key={'lab-representation'}
                  data-testid={'lab-representation'}
                  onClick={() => {
                    onCreateRepresentation('LAB');
                  }}>
                  <ListItemIcon>
                    <Collections fontSize="small" />
                  </ListItemIcon>
                  <ListItemText primary={'LAB'} secondary={'Logical Architecture Blank'} />
                </ListItemButton>
                <ListItemButton
                  className={classes.item}
                  dense
                  disableGutters
                  key={'sab-representation'}
                  data-testid={'sab-representation'}
                  onClick={() => {
                    onCreateRepresentation('SAB');
                  }}>
                  <ListItemIcon>
                    <Collections fontSize="small" />
                  </ListItemIcon>
                  <ListItemText primary={'SAB'} secondary={'System Analysis Blank'} />
                </ListItemButton>
                <ListItemButton
                  className={classes.item}
                  dense
                  disableGutters
                  key={'oab-representation'}
                  data-testid={'oab-representation'}
                  onClick={() => {
                    onCreateRepresentation('OAB');
                  }}>
                  <ListItemIcon>
                    <Collections fontSize="small" />
                  </ListItemIcon>
                  <ListItemText primary={'OAB'} secondary={'Operational Analysis Blank'} />
                </ListItemButton>
              </>
            )}
          </List>
        </CardContent>
      </Card>
      <Toast message={state.message} open={!!state.message} onClose={() => setState({ message: undefined })} />
    </>
  );
};

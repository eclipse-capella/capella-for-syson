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
import { MainAreaComponentProps } from '@eclipse-sirius/sirius-components-core';
import { RepresentationsArea } from '@eclipse-sirius/sirius-web-application';
import { makeStyles } from 'tss-react/mui';
import { CapellaNewRepresentationArea } from './CapellaNewRepresentationArea';

const useOnboardAreaStyles = makeStyles()((theme) => ({
  container: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'stretch',
    padding: theme.spacing(5),
    overflowY: 'auto',
    overflowX: 'auto',
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))',
    gap: theme.spacing(2),
  },
  box: {},
}));

export const CapellaOnboardArea = ({ editingContextId, readOnly }: MainAreaComponentProps) => {
  const { classes } = useOnboardAreaStyles();

  return (
    <div className={classes.container} data-testid="capella-onboard-area">
      <div className={classes.grid}>
        <CapellaNewRepresentationArea editingContextId={editingContextId} readOnly={readOnly} />
        <RepresentationsArea editingContextId={editingContextId} />
      </div>
    </div>
  );
};

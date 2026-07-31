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

import { NavigationBarIconProps } from '@eclipse-sirius/sirius-web-application';
import Link from '@mui/material/Link';
import { emphasize } from '@mui/material/styles';
import Tooltip from '@mui/material/Tooltip';
import { Link as RouterLink } from 'react-router-dom';
import { makeStyles } from 'tss-react/mui';
import { CapellaIcon } from '../core/CapellaIcon';

export const useNavigationBarIconStyles = makeStyles()((theme) => ({
  link: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    '& svg': {
      width: 180,
      height: 45,
    },
  },
  onDarkBackground: {
    '&:hover': {
      backgroundColor: emphasize(theme.palette.secondary.main, 0.08),
    },
  },
}));

export const CapellaNavigationBarIcon = ({}: NavigationBarIconProps) => {
  const { classes } = useNavigationBarIconStyles();
  return (
    <Tooltip title="Back to the homepage">
      <Link component={RouterLink} to="/" className={classes.link} color="inherit">
        <CapellaIcon />
      </Link>
    </Tooltip>
  );
};

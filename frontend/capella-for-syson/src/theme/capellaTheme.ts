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

import { theme } from '@eclipse-sirius/sirius-components-core';
import { Theme, createTheme } from '@mui/material/styles';

export const baseTheme: Theme = createTheme({
  ...theme,
  palette: {
    mode: 'light',
    primary: {
      main: '#7667A3',
      dark: '#453B7A',
      light: '#CDC8DE',
    },
    secondary: {
      main: '#292253',
      dark: '#64669B',
      light: '#2922538a',
    },
    text: {
      primary: '#292253',
      disabled: '#29225326',
    },
    error: {
      main: '#DE1000',
      dark: '#9B0B00',
      light: '#E43F33',
    },
    success: {
      main: '#079459',
      dark: '#079459',
      light: '#E8F3E3',
    },
    warning: {
      main: '#FBB800',
      dark: '#AF8000',
      light: '#FCF2E6',
    },
    info: {
      main: '#E8E9F0',
      dark: '#292253',
      light: '#E8E9F0',
    },
    divider: '#E0E0E0',
    navigation: {
      leftBackground: '#E8E9F0',
      rightBackground: '#E8E9F0',
    },
    navigationBar: {
      border: '#7667A350',
      background: '#7667A306',
    },
    selected: '#7667A3',
    action: {
      hover: '#A1A4C436',
      selected: '#A1A4C426',
    },
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: 'secondary',
      },
    },
    MuiSnackbarContent: {
      styleOverrides: {
        root: {
          backgroundColor: '#64669B',
        },
      },
    },
  },
});

const container = () => {
  return document.fullscreenElement ?? document.body;
};

export const capellaTheme = createTheme(
  {
    components: {
      MuiAvatar: {
        styleOverrides: {
          colorDefault: {
            backgroundColor: baseTheme.palette.primary.main,
          },
        },
      },
      MuiMenu: {
        defaultProps: {
          container,
        },
      },
      MuiTooltip: {
        defaultProps: {
          PopperProps: {
            container,
          },
        },
        styleOverrides: {
          tooltip: {
            backgroundColor: baseTheme.palette.common.black,
          },
        },
      },
    },
  },
  baseTheme
);

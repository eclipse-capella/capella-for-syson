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
import { FooterProps } from '@eclipse-sirius/sirius-web-application';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import { useTranslation } from 'react-i18next';
import { makeStyles } from 'tss-react/mui';

const useFooterStyles = makeStyles()((theme) => ({
  footer: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: theme.spacing(0.5),
    margin: theme.spacing(2),
  },
}));

export const CapellaFooter = ({}: FooterProps) => {
  const { classes } = useFooterStyles();
  const { t } = useTranslation('sirius-web-application', { keyPrefix: 'siriusWebFooter' });

  return (
    <footer className={classes.footer}>
      <Typography variant="caption">
        &copy; {new Date().getFullYear()} Obeo. Capella for SysON — {import.meta.env.VITE_APP_VERSION}. {t('poweredBy')}
      </Typography>
      <Link variant="caption" href="https://www.eclipse.dev/sirius" rel="noopener noreferrer" target="_blank">
        Sirius Web
      </Link>
    </footer>
  );
};

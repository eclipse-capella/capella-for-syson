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

import {
  RepresentationLoadingIndicator,
  useSelection,
  ViewAccordion,
  ViewAccordionContent,
  ViewAccordionToolbar,
  WorkbenchViewComponentProps,
  WorkbenchViewHandle,
} from '@eclipse-sirius/sirius-components-core';
import { FormBasedView, FormContext, GQLForm, Group } from '@eclipse-sirius/sirius-components-forms';
import { useRelatedElementsViewSubscription } from '@eclipse-sirius/sirius-web-application';
import CheckIcon from '@mui/icons-material/Check';
import SyncLockOutlinedIcon from '@mui/icons-material/SyncLockOutlined';
import SyncOutlinedIcon from '@mui/icons-material/SyncOutlined';
import VisibilityOutlinedIcon from '@mui/icons-material/VisibilityOutlined';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import { forwardRef, useEffect, useImperativeHandle, useState } from 'react';
import { useTranslation } from 'react-i18next';

const columnLabels = ['Referencing Elements', 'Current Element', 'Referenced Elements'] as const;

export const CapellaRelatedElementsWorkbenchViewContribution = forwardRef<
  WorkbenchViewHandle,
  WorkbenchViewComponentProps
>(({ id, editingContextId }, ref) => {
  const [objectIds, setObjectIds] = useState<string[]>([]);
  const [form, setForm] = useState<GQLForm | null>(null);
  const [canEdit, setCanEdit] = useState(false);
  const [pinned, setPinned] = useState(false);
  const [visibleColumns, setVisibleColumns] = useState<Set<string>>(() => new Set<string>(columnLabels));
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const { selection } = useSelection();
  const { t } = useTranslation('sirius-web-application', { keyPrefix: 'relatedElementsView' });
  const { t: translateSynchronization } = useTranslation('sirius-web-application', {
    keyPrefix: 'synchronizationButton',
  });

  useImperativeHandle(ref, () => ({
    id,
    getWorkbenchViewConfiguration: () => ({}),
    applySelection: (newSelection) => setObjectIds(newSelection.entries.map((entry) => entry.id)),
  }));

  useEffect(() => {
    if (!pinned) {
      setObjectIds(selection.entries.map((entry) => entry.id));
    }
  }, [selection, pinned]);

  const skip = objectIds.length === 0;
  const { payload, complete, loading } = useRelatedElementsViewSubscription(editingContextId, objectIds, skip);

  useEffect(() => {
    if (payload?.__typename === 'FormRefreshedEventPayload' && 'form' in payload) {
      setForm(payload.form as GQLForm);
    } else if (payload?.__typename === 'FormCapabilitiesRefreshedEventPayload' && 'capabilities' in payload) {
      setCanEdit((payload.capabilities as { canEdit: boolean }).canEdit);
    }
  }, [payload]);

  const toggleColumn = (label: string) => {
    setVisibleColumns((previous) => {
      const next = new Set(previous);
      if (next.has(label)) {
        next.delete(label);
      } else {
        next.add(label);
      }
      return next;
    });
  };

  const renderGroup = (contextId: string, currentForm: GQLForm, readOnly: boolean) => {
    const group = currentForm.pages[0]?.groups[0];
    if (!group) {
      return <></>;
    }
    const visibleGroup = {
      ...group,
      widgets: group.widgets.filter((widget) => visibleColumns.has(widget.label)),
    };
    return <Group editingContextId={contextId} formId={currentForm.id} readOnly={readOnly} group={visibleGroup} />;
  };

  const synchronizationTooltip =
    pinned && !skip ? translateSynchronization('tooltipPinned') : translateSynchronization('tooltipUnpinned');

  const toolbar = (
    <>
      <Tooltip title="Visible columns">
        <IconButton aria-label="Visible columns" onClick={(event) => setMenuAnchor(event.currentTarget)}>
          <VisibilityOutlinedIcon />
        </IconButton>
      </Tooltip>
      <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
        {columnLabels.map((label) => (
          <MenuItem
            key={label}
            role="menuitemcheckbox"
            aria-checked={visibleColumns.has(label)}
            onClick={() => toggleColumn(label)}>
            <ListItemIcon>
              <CheckIcon sx={{ visibility: visibleColumns.has(label) ? 'visible' : 'hidden' }} />
            </ListItemIcon>
            <ListItemText>{label}</ListItemText>
          </MenuItem>
        ))}
      </Menu>
      <Tooltip title={synchronizationTooltip} placement="left">
        <IconButton
          size="small"
          aria-label={synchronizationTooltip}
          color="inherit"
          data-testid="details-toggle-pin"
          onClick={() => setPinned(!pinned)}>
          {pinned && !skip ? <SyncLockOutlinedIcon /> : <SyncOutlinedIcon />}
        </IconButton>
      </Tooltip>
    </>
  );

  let contents = (
    <Box
      sx={{ display: 'grid', gridTemplateColumns: '1fr', gridTemplateRows: '1fr' }}
      data-representation-kind="form-related-elements">
      {(!form || loading) && (
        <Box sx={{ gridRow: 1, gridColumn: 1 }}>
          <RepresentationLoadingIndicator />
        </Box>
      )}
      {form && (
        <Box sx={{ gridRow: 1, gridColumn: 1 }}>
          <FormContext.Provider value={{ payload }}>
            <FormBasedView
              editingContextId={editingContextId}
              form={form}
              initialSelectedPageLabel={null}
              readOnly={!canEdit}
              postProcessor={renderGroup}
            />
          </FormContext.Provider>
        </Box>
      )}
    </Box>
  );
  if (complete || skip) {
    contents = (
      <Box sx={{ p: 1 }}>
        <Typography variant="subtitle2">{t('noObjectSelected')}</Typography>
      </Box>
    );
  }

  return (
    <ViewAccordion id={id} title="Related Elements">
      <ViewAccordionToolbar>{toolbar}</ViewAccordionToolbar>
      <ViewAccordionContent>
        <Box
          sx={{ overflow: 'auto', '& [data-testid="group-Semantic Browser"] > div:first-child': { display: 'none' } }}>
          {contents}
        </Box>
      </ViewAccordionContent>
    </ViewAccordion>
  );
});

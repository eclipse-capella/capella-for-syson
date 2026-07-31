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
import { expect, test } from '@playwright/test';
import { PlaywrightDiagram } from '../../helpers/PlaywrightDiagram';
import { PlaywrightProject } from '../../helpers/PlaywrightProject';
import { PlaywrightWorkbench } from '../../helpers/PlaywrightWorkbench';

test.describe('OAB diagram', () => {
  let projectId: string;

  test.beforeEach(async ({ page, request }) => {
    const project = await new PlaywrightProject(request).createCapellaProject('functional-OAB');
    projectId = project.projectId;
    await page.goto(`/projects/${projectId}/edit`);
  });

  test.afterEach(async ({ request }) => {
    await new PlaywrightProject(request).deleteProject(projectId);
  });

  test('creates the representation and an operational entity from its palette', async ({ page }) => {
    await new PlaywrightWorkbench(page).openRepresentation('oab-representation');
    const nodes = page.locator('.react-flow__nodes > div');
    const initialNodeCount = await nodes.count();
    await new PlaywrightDiagram(page).openPalette();
    await expect(page.getByTestId('Palette')).toBeAttached();
    await page.getByTestId('tool-New Operational Entity').click();
    await expect(nodes).toHaveCount(initialNodeCount + 1);
  });
});

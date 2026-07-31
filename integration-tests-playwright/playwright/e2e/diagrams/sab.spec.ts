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
import { PlaywrightNode } from '../../helpers/PlaywrightNode';
import { PlaywrightProject } from '../../helpers/PlaywrightProject';
import { PlaywrightWorkbench } from '../../helpers/PlaywrightWorkbench';

test.describe('SAB diagram', () => {
  let projectId: string;

  test.beforeEach(async ({ page, request }) => {
    const project = await new PlaywrightProject(request).createCapellaProject('functional-SAB');
    projectId = project.projectId;
    await page.goto(`/projects/${projectId}/edit`);
  });

  test.afterEach(async ({ request }) => {
    await new PlaywrightProject(request).deleteProject(projectId);
  });

  test('creates the representation and a function on the system from its palette', async ({ page }) => {
    await new PlaywrightWorkbench(page).openRepresentation('sab-representation');
    const systemNode = new PlaywrightNode(page, 'system');
    await systemNode.openPalette();
    await expect(page.getByTestId('Palette')).toBeAttached();
    await page.getByTestId('tool-New Function').click();
    await expect(new PlaywrightNode(page, 'Function 6').nodeLocator).toBeAttached();
  });
});

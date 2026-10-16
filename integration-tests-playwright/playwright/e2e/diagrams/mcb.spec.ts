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

test.describe('MCB diagram', () => {
  let projectId: string;

  test.beforeEach(async ({ page, request }) => {
    const project = await new PlaywrightProject(request).createCapellaProject('functional-MCB');
    projectId = project.projectId;
    await page.goto(`/projects/${projectId}/edit`);
  });

  test.afterEach(async ({ request }) => {
    await new PlaywrightProject(request).deleteProject(projectId);
  });

  test('creates and reopens an empty representation', async ({ page }) => {
    await new PlaywrightWorkbench(page).openRepresentation('mcb-representation');

    const canvas = page.getByTestId('rf__wrapper');
    await expect(canvas).toBeVisible();
    await expect(canvas.locator('.react-flow__node')).toHaveCount(0);
    await expect(canvas.locator('.react-flow__edge')).toHaveCount(0);

    await page.reload();

    await expect(canvas).toBeVisible();
    await expect(canvas.locator('.react-flow__node')).toHaveCount(0);
    await expect(canvas.locator('.react-flow__edge')).toHaveCount(0);
  });

  test('creates all four node types and persists them after reopening', async ({ page }) => {
    await new PlaywrightWorkbench(page).openRepresentation('mcb-representation');
    const canvas = page.getByTestId('rf__wrapper');
    const diagram = new PlaywrightDiagram(page);
    const tools = ['New System Actor', 'New Mission', 'New Capability', 'New Requirement'];
    for (const [index, tool] of tools.entries()) {
      await canvas.click({ button: 'right', position: { x: 50 + index * 220, y: 100 } });
      await expect(page.getByTestId('Palette')).toBeVisible();
      await diagram.waitForInitialLayout(() => page.getByTestId(`tool-${tool}`).click());
    }
    await expect(canvas.locator('.react-flow__node-ellipseNode')).toHaveCount(2);
    await expect(page.getByText('M 1', { exact: true })).toBeVisible();
    await expect(page.getByText('C 1', { exact: true })).toBeVisible();
    await expect(canvas.locator('[data-testid^="FreeForm - A "]')).toHaveCount(1);
    await page.reload();
    await expect(canvas).toBeVisible();
    await expect(canvas.locator('.react-flow__node-ellipseNode')).toHaveCount(2);
    await expect(page.getByText('M 1', { exact: true })).toBeVisible();
    await expect(page.getByText('C 1', { exact: true })).toBeVisible();
  });

});

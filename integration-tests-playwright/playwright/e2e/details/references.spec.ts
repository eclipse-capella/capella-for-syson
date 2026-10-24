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
import { PlaywrightExplorer } from '../../helpers/PlaywrightExplorer';
import { PlaywrightProject } from '../../helpers/PlaywrightProject';

test.describe('Arcadia reference widgets', () => {
  let projectId: string;

  test.beforeEach(async ({ page, request }) => {
    const project = await new PlaywrightProject(request).createCapellaProject('functional-references');
    projectId = project.projectId;
    await page.goto(`/projects/${projectId}/edit`);
  });

  test.afterEach(async ({ request }) => {
    await new PlaywrightProject(request).deleteProject(projectId);
  });

  test('adds, removes and clears a realized function from the Details view', async ({ page }) => {
    const explorer = new PlaywrightExplorer(page);
    await explorer.expand('functional-references.sysml');
    await explorer.expand('My Model Name');
    await explorer.expand('Logical Architecture');
    await explorer.expand('Functions');
    await explorer.select('Root Function');
    await page.getByRole('tab', { name: 'Capella', exact: true }).click();

    const reference = page.getByTestId('Realized System Functions');
    const value = reference.getByTestId('reference-value-Root Function');
    await reference.getByRole('combobox').click();
    await page.getByTestId('option-Root Function').click();
    await expect(value).toBeVisible();

    await value.locator('.MuiChip-deleteIcon').click();
    await expect(value).toHaveCount(0);

    await reference.getByRole('combobox').click();
    await page.getByTestId('option-Root Function').click();
    await expect(value).toBeVisible();
    await page.getByTestId('Realized System Functions-clear').click();
    await expect(value).toHaveCount(0);
  });
});

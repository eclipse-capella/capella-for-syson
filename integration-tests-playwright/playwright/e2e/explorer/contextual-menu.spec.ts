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

test.describe('Explorer - Contextual Menu', () => {
  let projectId: string;

  test.beforeEach(async ({ page, request }) => {
    const project = await new PlaywrightProject(request).createCapellaProject('contextual-menu');
    projectId = project.projectId;
    await page.goto(`/projects/${projectId}/edit`);
  });

  test.afterEach(async ({ request }) => {
    await new PlaywrightProject(request).deleteProject(projectId);
  });

  test('when the contextual menu is opened on an element then the menu does not contain empty items', async ({
    page,
  }) => {
    const playwrightExplorer = new PlaywrightExplorer(page);
    await playwrightExplorer.expand('contextual-menu.sysml');
    await playwrightExplorer.openMenu('My Model Name');
    const menuEntries = await page.getByTestId('Palette').locator('nav').first().locator('> *').allTextContents();
    console.log(menuEntries);
    menuEntries.forEach((menuEntry, index) => {
      expect(
        menuEntry.trim().length,
        `Menu entry "${menuEntry}" at index ${index} should not be empty`
      ).toBeGreaterThan(0);
    });
  });
});

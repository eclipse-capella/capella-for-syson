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
import { PlaywrightProject } from '../../helpers/PlaywrightProject';
import { PlaywrightWorkbench } from '../../helpers/PlaywrightWorkbench';
import { PlaywrightExplorer } from '../../helpers/PlaywrightExplorer';
import { PlaywrightNode } from '../../helpers/PlaywrightNode';

test.describe('DDV diagram', () => {
  let projectId: string;

  test.beforeEach(async ({ page, request }) => {
    const project = await new PlaywrightProject(request).createCapellaProject('functional-DDV');
    projectId = project.projectId;
    await page.goto(`/projects/${projectId}/edit`);
  });

  test.afterEach(async ({ request }) => {
    await new PlaywrightProject(request).deleteProject(projectId);
  });

  test('display ddv diagram with related functions', async ({ page }) => {
    const playwrightExplorer = new PlaywrightExplorer(page);
    await playwrightExplorer.expand('functional-DDV.sysml');
    await playwrightExplorer.expand('My Model Name');
    await playwrightExplorer.expand('Logical Architecture');
    await playwrightExplorer.expand('Functions');
    await playwrightExplorer.select('Root Function');
    await new PlaywrightWorkbench(page).openView('viewselector-Related Elements Visual View');

    const functionRootNode = new PlaywrightNode(page, 'Root Function');

    await expect(functionRootNode.nodeLocator).toBeAttached();
  });
});

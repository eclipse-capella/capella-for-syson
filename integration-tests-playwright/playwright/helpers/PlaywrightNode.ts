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
import type { Locator, Page } from '@playwright/test';

export class PlaywrightNode {
  readonly nodeLocator: Locator;

  constructor(page: Page, name: string, type: string = 'FreeForm', index = 0) {
    const nodeStyleLocator = page.locator(`[data-testid="${type} - ${name}"]:not(#hidden-node-container *)`).nth(index);
    this.nodeLocator = nodeStyleLocator.locator('..');
  }

  async openPalette(): Promise<void> {
    await this.nodeLocator.click({
      button: 'right',
      position: { x: 10, y: 10 },
    });
  }
}

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
import { expect, type Page } from '@playwright/test';

export class PlaywrightDiagram {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async openPalette(): Promise<void> {
    await this.page.getByTestId('rf__wrapper').click({
      button: 'right',
      position: { x: 250, y: 250 },
    });
  }

  async waitForInitialLayout(openDiagram: () => Promise<unknown>): Promise<void> {
    const [response] = await Promise.all([
      this.page.waitForResponse((response) => {
        const request = response.request();
        if (request.method() !== 'POST' || !request.url().includes('/api/graphql')) {
          return false;
        }
        const body = request.postDataJSON();
        return body?.operationName === 'layoutDiagram' && body?.variables?.input?.cause === 'refresh';
      }),
      openDiagram(),
    ]);

    expect(response.ok()).toBeTruthy();
    const body = await response.json();
    expect(body.errors).toBeUndefined();
    expect(body.data.layoutDiagram.__typename).toBe('SuccessPayload');
  }
}

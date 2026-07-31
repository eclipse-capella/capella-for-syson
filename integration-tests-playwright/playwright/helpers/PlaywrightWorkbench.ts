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
import type { Page } from '@playwright/test';
import { expect } from '@playwright/test';

export class PlaywrightWorkbench {
  readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async openRepresentation(representationTestId: string): Promise<void> {
    await expect(this.page.getByTestId('capella-onboard-area')).toBeAttached();
    await this.page.getByTestId(representationTestId).click();
    await expect(this.page.getByTestId('rf__wrapper')).toBeAttached();
  }
}

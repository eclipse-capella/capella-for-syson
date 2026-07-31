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
import { render } from '@testing-library/react';
import { expect, test } from 'vitest';
import { CapellaIcon } from '../CapellaIcon';

test('renders the Capella icon', () => {
  const { getByTestId } = render(<CapellaIcon data-testid="capella-icon" />);

  expect(getByTestId('capella-icon').getAttribute('viewBox')).toBe('0 0 2048 500');
});

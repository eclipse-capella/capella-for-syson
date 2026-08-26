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

import { GQLMessage } from '@eclipse-sirius/sirius-components-core';

export interface ShowDiagramFunctionsState {
  checked: boolean | null;
  tooltip: string;
}

export interface GQLShowDiagramFunctionsMutationData {
  showDiagramFunctions: GQLSetShowDiagramFunctionsPayload;
}

export interface GQLSetShowDiagramFunctionsPayload {
  __typename: string;
  show: boolean;
}

export interface GQLErrorPayload extends GQLSetShowDiagramFunctionsPayload {
  messages: GQLMessage[];
}

export interface GQLShowDiagramFunctionsMutationVariables {
  input: GQLShowDiagramFunctionsMutationInput;
}

export interface GQLShowDiagramFunctionsMutationInput {
  id: string;
  editingContextId: string;
  representationId: string;
  show: boolean;
}

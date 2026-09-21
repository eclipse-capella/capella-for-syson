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
import { DiagramToolbarActionProps } from '@eclipse-sirius/sirius-components-diagrams';

import { GQLDiagramFilter } from './useRepresentationMetadataDiagramFilters.types';

export interface DiagramFilterProps extends DiagramToolbarActionProps {
  filter: GQLDiagramFilter;
}

export interface DiagramFilterState {
  checked: boolean | null;
  tooltip: string;
}

export interface GQLSetDiagramFilterStateMutationData {
  setDiagramFilterState: GQLSetDiagramFilterStatePayload;
}

export interface GQLSetDiagramFilterStatePayload {
  __typename: string;
  active: boolean;
}

export interface GQLErrorPayload extends GQLSetDiagramFilterStatePayload {
  messages: GQLMessage[];
}

export interface GQLSetDiagramFilterStateMutationVariables {
  input: GQLSetDiagramFilterStateMutationInput;
}

export interface GQLSetDiagramFilterStateMutationInput {
  id: string;
  editingContextId: string;
  representationId: string;
  filterId: string;
  active: boolean;
}

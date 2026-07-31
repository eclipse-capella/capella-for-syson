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
import { GQLErrorPayload, GQLMessage } from '@eclipse-sirius/sirius-components-core';

export interface CapellaDDVWorkbenchViewContributionState {
  representationDescriptions: GQLRepresentationDescriptionMetadata[];
  selectedRepresentationDescriptionId: string;
}

export interface GQLRepresentationDescriptionMetadata {
  id: string;
  label: string;
  defaultName: string;
}

export interface GQLGetRepresentationDescriptionsQueryVariables {
  editingContextId: string;
  objectId: string;
}

export interface GQLSaveCapellaDDVDiagramMutationData {
  saveCapellaDDVDiagram: GQLSaveCapellaDDVDiagramPayload;
}
export type GQLSaveCapellaDDVDiagramPayload = GQLErrorPayload | GQLSaveCapellaDDVDiagramSuccessPayload;

export interface GQLSaveCapellaDDVDiagramSuccessPayload {
  __typename: 'SaveCapellaDDVDiagramSuccessPayload';
  id: string | null;
  representationId: string;
  messages: GQLMessage[] | null;
}

export interface GQLSaveCapellaDDVDiagramMutationVariables {
  input: GQLSaveCapellaDDVDiagramMutationInput;
}

export interface GQLSaveCapellaDDVDiagramMutationInput {
  id: string;
  editingContextId: string;
  representationId: string;
}

export interface GQLGetRepresentationDescriptionsQueryData {
  viewer: GQLViewer;
}

export interface GQLViewer {
  editingContext: GQLEditingContext;
}

export interface GQLEditingContext {
  representationDescriptions: GQLRepresentationDescriptionConnection;
}

export interface GQLRepresentationDescriptionConnection {
  edges: GQLRepresentationDescriptionEdge[];
  pageInfo: GQLPageInfo;
}
export interface GQLRepresentationDescriptionEdge {
  node: GQLRepresentationDescriptionMetadata;
}

export interface GQLPageInfo {
  hasNextPage: boolean;
  hasPreviousPage: boolean;
  startCursor: string;
  endCursor: string;
}

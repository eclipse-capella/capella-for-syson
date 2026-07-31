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
import { APIRequestContext, expect } from '@playwright/test';

const createProjectQuery = `
mutation createProject($input: CreateProjectInput!) {
  createProject(input: $input) {
    __typename
    ... on CreateProjectSuccessPayload {
      project {
        id
      }
    }
    ... on ErrorPayload {
      message
    }
  }
}
`;

const deleteProjectQuery = `
mutation deleteProject($input: DeleteProjectInput!) {
  deleteProject(input: $input) {
    __typename
  }
}
`;

export class PlaywrightProject {
  readonly request: APIRequestContext;

  constructor(request: APIRequestContext) {
    this.request = request;
  }

  async createCapellaProject(name: string): Promise<{ projectId: string }> {
    const response = await this.request.post('http://localhost:8080/api/graphql', {
      data: {
        query: createProjectQuery,
        variables: {
          input: {
            id: crypto.randomUUID(),
            name,
            templateId: 'capella-template',
            // This is a temporary fix to hardcode the Arcadia library as a dependency of the created project
            // This will most likely have to change once sirius-web#6743 is fixed.
            libraryIds: ['capella-arcadia@0.0.1'],
          },
        },
      },
    });

    expect(response.ok()).toBeTruthy();
    const jsonResponse = await response.json();
    const payload = jsonResponse.data.createProject;
    expect(payload.__typename).toBe('CreateProjectSuccessPayload');
    return { projectId: payload.project.id };
  }

  async deleteProject(projectId: string): Promise<void> {
    const response = await this.request.post('http://localhost:8080/api/graphql', {
      data: {
        query: deleteProjectQuery,
        variables: {
          input: {
            id: crypto.randomUUID(),
            projectId,
          },
        },
      },
    });

    expect(response.ok()).toBeTruthy();
  }
}

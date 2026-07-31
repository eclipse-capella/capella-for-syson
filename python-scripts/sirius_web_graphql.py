##################################################################################
# Copyright (c) 2026 Obeo.
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#
# Contributors:
#   Obeo - initial API and implementation
##################################################################################

# Endpoint and headers
import uuid
import requests

headers = {
    "Content-Type": "application/json"
}


GRAPHQL_ENDPOINT =  "/api/graphql"


# GraphQL query to fetch projects
fetch_projects_query = """
query getProjects($after: String, $before: String, $first: Int, $last: Int) {
        viewer {
          projects(after: $after, before: $before, first: $first, last: $last) {
            edges {
              node {
                id
                name
              }
            }
          }
        }
      }
"""

# GraphQL query to fetch editing context and stereotypes
fetch_editing_context_query = """
query FetchEditingContext($projectId: ID!) {
  viewer {
    project(projectId: $projectId) {
      currentEditingContext {
        id
        stereotypes {
          edges {
            node {
              id
              label
            }
          }
        }
      }
    }
  }
}
"""

getRepresentationDescriptionsQuery = """
  query getRepresentationDescriptions($editingContextId: ID!, $objectId: ID!) {
    viewer {
      editingContext(editingContextId: $editingContextId) {
        representationDescriptions(objectId: $objectId) {
          edges {
            node {
              id
              label
              defaultName
            }
          }
          pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
          }
        }
      }
    }
  }
"""

fetch_representations_query="""
query getRepresentationMetadata($editingContextId: ID!, $first: Int = 50, $after: String) {
  viewer {
    editingContext(editingContextId: $editingContextId) {
      id
      representations(first: $first, after: $after) {
        edges {
          cursor
          node {
            id
            label
            kind
            iconURLs
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
}
"""

# GraphQL mutation to create a project from a template
create_project_from_template_mutation = """
mutation CreateProjectFromTemplate($input: CreateProjectFromTemplateInput!) {
  createProjectFromTemplate(input: $input) {
    __typename
    ... on CreateProjectFromTemplateSuccessPayload {
      project {
        id
      }
    }
    ... on ErrorPayload {
      message
    }
  }
}
"""

create_project_mutation = """
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
"""

create_document_mutation = """
mutation createDocument($input: CreateDocumentInput!) {
    createDocument(input: $input) {
      __typename
      ... on ErrorPayload {
        message
      }
      ... on CreateDocumentSuccessPayload {
        document {
          id
        }
      }
    }
  }
"""

create_root_object_mutation="""
mutation createRootObject($input: CreateRootObjectInput!) {
    createRootObject(input: $input) {
      __typename
      ... on CreateRootObjectSuccessPayload {
        object {
          id
          label
          kind
        }
      }
      ... on ErrorPayload {
        message
      }
    }
  }
"""

# GraphQL mutation to delete a project
delete_project_mutation = """
mutation DeleteProject($input: DeleteProjectInput!) {
  deleteProject(input: $input) {
    __typename
    ... on SuccessPayload {
      id
    }
    ... on ErrorPayload {
      message
    }
  }
}
"""

# GraphQL mutation for creating a representation
createRepresentationMutation = """
mutation createRepresentation($input: CreateRepresentationInput!) {
  createRepresentation(input: $input) {
    __typename
    ... on CreateRepresentationSuccessPayload {
      representation {
        id
        }
      }
    }
  }
"""

# GraphQL mutation for invoking tool on diagram
invokeSingleClickOnDiagramElementToolMutation = """
  mutation invokeSingleClickOnDiagramElementTool($input: InvokeSingleClickOnDiagramElementToolInput!) {
    invokeSingleClickOnDiagramElementTool(input: $input) {
      __typename
      ... on InvokeSingleClickOnDiagramElementToolSuccessPayload {
        newSelection {
          entries {
            id
            kind
          }
        }
        messages {
          body
          level
        }
      }
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
    }
  }
"""

layoutDiagramMutation = """
mutation layoutDiagram($input: LayoutDiagramInput!) {
  layoutDiagram(input: $input) {
    __typename
    ... on SuccessPayload {
      messages {
        body
        level
        __typename
      }
      __typename
    }
    ... on ErrorPayload {
      messages {
        body
        level
        __typename
      }
      __typename
    }
  }
}
"""

evaluate_expression_mutation="""
  mutation evaluateExpression($input: EvaluateExpressionInput!) {
    evaluateExpression(input: $input) {
      __typename
      ... on EvaluateExpressionSuccessPayload {
        result {
          __typename
          ... on ObjectExpressionResult {
            objectValue: value {
              id
              kind
              label
              iconURLs
            }
          }
          ... on ObjectsExpressionResult {
            objectsValue: value {
              id
              kind
              label
              iconURLs
            }
          }
          ... on BooleanExpressionResult {
            booleanValue: value
          }
          ... on IntExpressionResult {
            intValue: value
          }
          ... on StringExpressionResult {
            stringValue: value
          }
        }
      }
      ... on ErrorPayload {
        messages {
          body
          level
        }
      }
    }
  }
"""

# Function to fetch all projects
def fetch_all_projects(url):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    projects = []
    page = 1
    limit = 100  # Adjust the limit as needed

    while True:
        variables = {"page": page, "limit": limit}
        response = requests.post(
            graphql_url,
            json={"query": fetch_projects_query, "variables": variables},
            headers=headers
        )

        if response.status_code != 200:
            print(f"Error: {response.status_code} - {response.text}")
            break

        data = response.json()
        viewer = data.get("data", {}).get("viewer", {})
        edges = viewer.get("projects", {}).get("edges", [])
        page_info = viewer.get("projects", {}).get("pageInfo", {})

        # Extract project details
        for edge in edges:
            project = edge["node"]
            projects.append(project)

        # Check if there are more pages
        if not page_info.get("hasNextPage", False):
            break

        page += 1

    return projects

# Function to fetch all representations
def fetch_all_representations(url, editing_context_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"

    all_reps: list[dict] = []
    after = None

    while True:
        variables = {
            "editingContextId": editing_context_id,
            "first": 50
        }
        if after:
            variables["after"] = after

        try:
            response = requests.post(
                graphql_url,
                json={"query": fetch_representations_query, "variables": variables},
                headers=headers,
                timeout=30
            )
        except requests.RequestException as e:
            print(f"HTTP error while calling GraphQL: {e}")
            break

        if response.status_code != 200:
            print(f"Error: {response.status_code} - {response.text}")
            break

        data = response.json() or {}

        viewer = data.get("data", {}).get("viewer", {}) or {}
        editing_context = viewer.get("editingContext", {}) or {}
        reps = editing_context.get("representations", {}) or {}

        edges = reps.get("edges") or []
        all_reps.extend(edges)

        page_info = reps.get("pageInfo", {}) or {}
        has_next = page_info.get("hasNextPage", False)
        after = page_info.get("endCursor")

        if not has_next:
            break

    print(f"All reps: {all_reps}")
    return all_reps

# Function to delete a project
def delete_project(url, project_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    variables = {
        "input": {
            "id": project_id,
            "projectId": project_id  # Assuming projectId is the same as id
        }
    }

    response = requests.post(
        graphql_url,
        json={"query": delete_project_mutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error deleting project {project_id}: {response.status_code} - {response.text}")
        return False

    data = response.json()
    result = data.get("data", {}).get("deleteProject", {})
    if result.get("__typename") == "SuccessPayload":
        print(f"Project {project_id} deleted successfully.")
        return True
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error deleting project {project_id}: {result.get('message')}")
        return False

# Function to create a project
def create_project(url, project_name):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    project_id = str(uuid.uuid4())  # Generate a unique ID for the project
    variables = {
        "input": {
            "id": project_id,
            "name": project_name
        }
    }

    response = requests.post(
        graphql_url,
        json={"query": create_project_mutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error: {response.status_code} - {response.text}")
        return None

    data = response.json()
    result = data.get("data", {}).get("createProject", {})
    if result.get("__typename") == "CreateProjectSuccessPayload":
        project = result.get("project", {})
        print(f"Project Created: ID = {project['id']}")
        return project
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None

# Function to create a project from a template
def create_project_from_template(url, template_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    project_id = str(uuid.uuid4())  # Generate a unique ID for the project
    variables = {
        "input": {
            "id": project_id,
            "templateId": template_id
        }
    }

    response = requests.post(
        graphql_url,
        json={"query": create_project_from_template_mutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error: {response.status_code} - {response.text}")
        return None

    data = response.json()
    result = data.get("data", {}).get("createProjectFromTemplate", {})
    if result.get("__typename") == "CreateProjectFromTemplateSuccessPayload":
        project = result.get("project", {})
        print(f"Project Created: ID = {project['id']}")
        return project
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None


def create_document(url, editing_context_id, document_name):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"

    variables = {
        "input": {
            "id": str(uuid.uuid4()),# Generate a unique ID for the document
            "editingContextId": editing_context_id,
            "stereotypeId": "empty_sysmlv2", # Create a document with no root element
            "name": document_name
        }
    }

    response = requests.post(
        graphql_url,
        json={"query": create_document_mutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error: {response.status_code} - {response.text}")
        return None

    data = response.json()
    result = data.get("data", {}).get("createDocument", {})
    if result.get("__typename") == "CreateDocumentSuccessPayload":
        document= result['document']
        document_id = document['id']
        print(f"Document Created: ID = {document_id}")
        return document_id
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None


# Function to fetch the editing context
def fetch_editing_context(url, project_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    variables = {"projectId": project_id}
    response = requests.post(
        graphql_url,
        json={"query": fetch_editing_context_query, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error fetching editing context: {response.status_code} - {response.text}")
        return None

    data = response.json()
    viewer = data.get("data", {}).get("viewer", {})
    project = viewer.get("project", {})
    editing_context = project.get("currentEditingContext", {})
    return editing_context

    # Get representation's descriptions
def get_representation_descriptions(url, editing_context_id, object_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    variables = {
        "editingContextId": editing_context_id,
        "objectId": object_id}
    response = requests.post(
        graphql_url,
        json={"query": getRepresentationDescriptionsQuery, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error fetching representations descriptions: {response.status_code} - {response.text}")
        return None

    return response

# Create a representation
def create_representation(url, editing_context_id, object_id, representation_description_id, representation_name):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    # Create a representation
    variables = {
        "input": {
            "id": str(uuid.uuid4()),  # Generate a unique operation ID
            "editingContextId": editing_context_id,
            "objectId": object_id,
            "representationDescriptionId": representation_description_id,
            "representationName": representation_name
        }
    }

    # Send the mutation request to create a representation
    response = requests.post(
        graphql_url,
        json={"query": createRepresentationMutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error creating representation: {response.status_code} - {response.text}")
        return None

    data = response.json()
    result = data.get("data", {}).get("createRepresentation", {})

    if result.get("__typename") == "CreateRepresentationSuccessPayload":
        representation = result.get("representation", {})

        return representation
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None
    else:
        print("Unexpected response:", data)
        return None


def invoke_layout_diagram_tool(url, editing_context_id, representation_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    # Prepare mutation variables
    variables = {
        "input": {
            "id": str(uuid.uuid4()),  # Generate a unique operation ID
            "editingContextId": editing_context_id,
            "representationId": representation_id,
            "diagramLayoutData": "???"
        }
    }

    # Send the mutation request to invoke the tool
    response = requests.post(
        graphql_url,
        json={"query": layoutDiagramMutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error invoking tool: {response.status_code} - {response.text}")
        return None

    return response

def evaluate_expression(url, editing_context_id, expression, selectedObjectIds):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    id = str(uuid.uuid4())  # Generate a unique ID for the evaluation
    variables = {
        "input": {
            "id": id,
            "editingContextId": editing_context_id,
            "expression": expression,
            "selectedObjectIds": selectedObjectIds
        }
    }

    response = requests.post(
        graphql_url,
        json={"query": evaluate_expression_mutation, "variables": variables},
        headers=headers
    )

    if response.status_code != 200:
        print(f"Error: {response.status_code} - {response.text}")
        return None

    data = response.json()
    result = data.get("data", {}).get("evaluateExpression", {})
    if result.get("__typename") == "EvaluateExpressionSuccessPayload":
        res = result.get("result", {})
        if res.get("__typename") == "StringExpressionResult":
            return res.get("stringValue")  # Extract the string value
        elif res.get("__typename") == "ObjectExpressionResult":
            return res.get("objectValue")  # Extract the object value
        elif res.get("__typename") == "ObjectsExpressionResult":
            return res.get("objectsValue")  # Extract the object value
        elif res.get("__typename") == "BooleanExpressionResult":
            return res.get("booleanValue")  # Extract the object value
        elif res.get("__typename") == "IntExpressionResult":
            return res.get("intValue")  # Extract the object value
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None
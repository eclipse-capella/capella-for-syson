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

import requests
from sirius_web_graphql import GRAPHQL_ENDPOINT, create_representation, create_project_from_template, headers, create_root_object_mutation, invokeSingleClickOnDiagramElementToolMutation
import uuid

# General view description ID
GENERAL_VIEW_DESCRIPTION_ID = "siriusComponents://representationDescription?kind=diagramDescription&sourceKind=view&sourceId=8dcd14b0-6259-3193-ad2c-743f394c68e4&sourceElementId=db495705-e917-319b-af55-a32ad63f4089"

SYSMLv2_TEMPLATE_ID = "sysmlv2-template"

# GraphQL mutation for importing SysML v2 content
import_sysml_mutation = """
mutation InsertTextualSysMLv2($input: InsertTextualSysMLv2Input!) {
  insertTextualSysMLv2(input: $input) {
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

def create_root_namespace(url, editing_context_id, document_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    object_id = str(uuid.uuid4())  # Generate a unique ID for the root object
    variables = {
        "input": {
            "id": object_id,
            "editingContextId": editing_context_id,
            "documentId": document_id,
            "domainId": "http://www.eclipse.org/syson/sysml",
            "rootObjectCreationDescriptionId": "SysMLv2EditService-Package"
        }
    }

    response = requests.post(
        graphql_url,
        json={"query":  create_root_object_mutation, "variables": variables},
        headers= headers
    )

    if response.status_code != 200:
        print(f"Error: {response.status_code} - {response.text}")
        return None

    data = response.json()
    result = data.get("data", {}).get("createRootObject", {})
    if result.get("__typename") == "CreateRootObjectSuccessPayload":
        print("Success")
        return object_id
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None

# Function to import SysML v2 content into a project
def import_sysml_to_project(url, file_path, editing_context_id, object_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    try:
        # Read the SysML v2 file
        with open(file_path, "r") as file:
            textual_content = file.read()

        # Generate a unique operation ID
        operation_id = str(uuid.uuid4())

        # Prepare mutation variables
        variables = {
            "input": {
                "id": operation_id,
                "editingContextId": editing_context_id,
                "objectId": object_id,
                "textualContent": textual_content
            }
        }

        # Send the mutation request
        response = requests.post(
            graphql_url,
            json={"query": import_sysml_mutation, "variables": variables},
            headers= headers
        )

        if response.status_code != 200:
            print(f"Error importing SysML v2: {response.status_code} - {response.text}")
            return False

        data = response.json()
        result = data.get("data", {}).get("insertTextualSysMLv2", {})

        if result.get("__typename") == "SuccessPayload":
            return True
        elif result.get("__typename") == "ErrorPayload":
            print(f"Error: {result.get('message')}")
            return False
        else:
            print("Unexpected response:", data)
            return False

    except Exception as e:
        print(f"An error occurred while importing SysML v2: {e}")
        return False

def invoke_add_existing_element_tool(url, editing_context_id, representation_id):
    graphql_url = f"{url}{GRAPHQL_ENDPOINT}"
    # Prepare mutation variables
    variables = {
        "input": {
            "id": str(uuid.uuid4()),  # Generate a unique operation ID
            "editingContextId": editing_context_id,
            "representationId": representation_id,
            "diagramElementId": representation_id,
            "toolId":"4f4840ce-d9d9-3330-a043-1a1c46817c18", # Add existing element (recursive) tool ID
            "startingPositionX":100,
            "startingPositionY":100,
            "variables":[]
        }
    }

    # Send the mutation request to invoke the tool
    response = requests.post(
        graphql_url,
        json={"query": invokeSingleClickOnDiagramElementToolMutation, "variables": variables},
        headers= headers
    )
    # time.sleep(2)

    if response.status_code != 200:
        print(f"Error invoking tool: {response.status_code} - {response.text}")
        return None

    data = response.json()
    # print(data)
    result = data.get("data", {}).get("invokeSingleClickOnDiagramElementTool", {})
    if result.get("__typename") == "InvokeSingleClickOnDiagramElementToolSuccessPayload":
        print(f"SysML v2 content imported successfully.")
        return response
    elif result.get("__typename") == "ErrorPayload":
        print(f"Error: {result.get('message')}")
        return None
    else:
        print("Unexpected response:", data)
        return None

def create_sysmlv2_project(url):
    created_project = create_project_from_template(url, SYSMLv2_TEMPLATE_ID)
    return created_project

def create_general_view(url, editing_context_id, object_id, representation_name):
    representation = create_representation(url,
                editing_context_id,
                object_id,
                GENERAL_VIEW_DESCRIPTION_ID,
                representation_name)

    representation_id = representation.get("id")
    # Proceed to invoke the single-click tool
    invoke_add_existing_element_tool(url, editing_context_id, representation_id)

    # Proceed to invoke the layout tool
    # layout_tool_result = invoke_layout_diagram_tool(editing_context_id, representation_id)
    # if layout_tool_result:
    #     print("Diagram laid out successfully.")
    # else:
    #     print("Failed to layout the diagram.")

    return representation
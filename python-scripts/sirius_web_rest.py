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

# REST API Endpoint
REST_API_ENDPOINT  = f"/api/rest"

def fetch_commits(url, project_id):
    rest_api_url = f"{url}{REST_API_ENDPOINT}"
    commits_url = f"{rest_api_url}/projects/{project_id}/commits"
    response = requests.get(commits_url)
    if response.status_code == 200:
        commits = response.json()
        return commits
    else:
        print(f"Error fetching commits: {response.status_code} - {response.text}")
        return None

#  Retrieves the latest commit for a given project.
def get_last_commit_id(url, project_id):
    commits = fetch_commits(url, project_id)
    if commits:
        last_commit = commits[-1] if commits else None
        if last_commit:
            last_commit_id = last_commit['@id']
            # print(f"Last Commit ID: {last_commit_id}")
        return last_commit_id
    else:
        print("No commits available.")
        return None

# Retrieves the root Namespace ID
def fetch_elements(url, project_id, document_id):
    commit_id = get_last_commit_id(url, project_id)

    rest_api_url = f"{url}{REST_API_ENDPOINT}"

    # API call to fetch elements in the project
    element_get_url = f"{rest_api_url}/projects/{project_id}/commits/{commit_id}/elements" # <3>
    # Send GET request to retrieve elements
    response = requests.get(element_get_url)  # <4>

    if response.status_code == 200:  # <5>
        elements = response.json()
        return elements
    else:
        print(f"Error fetching elements: {response.status_code} - {response.text}")
        return None

def get_object_id(url, project_id, object_name):
    commit_id = get_last_commit_id(url, project_id)

    rest_api_url = f"{url}{REST_API_ENDPOINT}"

    # API call to fetch elements in the project
    element_get_url = f"{rest_api_url}/projects/{project_id}/commits/{commit_id}/elements" # <3>
    # Send GET request to retrieve elements
    response = requests.get(element_get_url)  # <4>

    if response.status_code == 200:  # <5>
        elements = response.json()
        for element in elements:
            if(element['declaredName'] == object_name):
              element_id = element['@id']
              return element_id
    else:
        print(f"Error fetching elements: {response.status_code} - {response.text}")
        return None
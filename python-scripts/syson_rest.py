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

# Retrieves the root Namespace ID
import sirius_web_rest

def get_root_namespace_id(host, project_id, document_id):
    elements = sirius_web_rest.fetch_elements(host, project_id, document_id)
    OWNING_NAMESPACE_KEY = OWNING_NAMESPACE_KEY
    if elements and  OWNING_NAMESPACE_KEY in elements[0]:
        return elements[0][OWNING_NAMESPACE_KEY]['@id']
    else:
        print("Error: Unable to fetch elements or 'owningNamespace' is missing.")
        return None
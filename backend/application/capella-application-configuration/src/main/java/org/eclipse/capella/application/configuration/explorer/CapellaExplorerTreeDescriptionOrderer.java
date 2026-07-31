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
package org.eclipse.capella.application.configuration.explorer;

import java.util.List;

import org.eclipse.sirius.web.application.views.explorer.dto.ExplorerDescriptionMetadata;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerTreeDescriptionOrderer;
import org.springframework.stereotype.Service;

/**
 * Provide IExplorerTreeDescriptionOrderer implementation to reorder Capella explorer on first place.
 *
 * @author frouene
 */
@Service
public class CapellaExplorerTreeDescriptionOrderer implements IExplorerTreeDescriptionOrderer {

    @Override
    public void order(List<ExplorerDescriptionMetadata> explorerDescriptionMetadataList) {
        explorerDescriptionMetadataList.sort((o1, o2) -> {
            int compareResult;
            if (this.isCapellaExplorerDescriptionMetadata(o1) && !this.isCapellaExplorerDescriptionMetadata(o2)) {
                compareResult = -1;
            } else if (!this.isCapellaExplorerDescriptionMetadata(o1) && this.isCapellaExplorerDescriptionMetadata(o2)) {
                compareResult = 1;
            } else {
                compareResult = o1.label().compareTo(o2.label());
            }
            return compareResult;
        });
    }

    private boolean isCapellaExplorerDescriptionMetadata(ExplorerDescriptionMetadata explorerDescriptionMetadata) {
        return CapellaExplorerTreeDescriptionProvider.CAPELLA_EXPLORER.equals(explorerDescriptionMetadata.label());
    }
}

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

package org.eclipse.capella.diagram.sfbd.view.services;

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.syson.sysml.Element;

/**
 * Services of the SFBD diagram.
 *
 * @author adieumegard
 */
public class SFBDDiagramService {

    private final SAQueryService saQueryService;

    public SFBDDiagramService() {
        this.saQueryService = new SAQueryService();
    }

    /**
     * Returns {@code true} if an OABD diagram can be created on {@code self}.
     * <p>
     * OABD diagrams can be created on any function of the <i>operational analysis</i> perspective (i.e. operational activities).
     *
     * @param self
     *         the element to check
     * @return {@code true} if the diagram can be created, {@code false} otherwise
     */
    public boolean canCreateSFBDDiagram(Element self) {
        return this.saQueryService.isSystemFunction(self);
    }
}

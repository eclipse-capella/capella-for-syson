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
package org.eclipse.capella.model.services.system.analysis;

import org.eclipse.capella.model.transverse.services.ArcadiaElementContentAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Replays diagram drop and allocation contracts with the declared type index installed.
 *
 * @author Jerome Gout
 */
public class CachedSARepresentationDropServicesTests extends SARepresentationDropServicesTests {

    private ArcadiaElementContentAdapter adapter;

    @BeforeEach
    public void installTypeIndex() {
        var resourceSet = this.capellaModel.getResource().getResourceSet();
        this.adapter = new ArcadiaElementContentAdapter(resourceSet);
        resourceSet.eAdapters().add(this.adapter);
    }

    @AfterEach
    public void removeTypeIndex() {
        this.capellaModel.getResource().getResourceSet().eAdapters().remove(this.adapter);
    }
}

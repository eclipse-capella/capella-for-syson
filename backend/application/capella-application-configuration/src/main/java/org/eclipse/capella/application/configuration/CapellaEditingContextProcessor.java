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
package org.eclipse.capella.application.configuration;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.springframework.stereotype.Service;

/**
 * {@link IEditingContextProcessor} for Arcadia library based models.
 *
 * @author adieumegard
 */
@Service
public class CapellaEditingContextProcessor implements IEditingContextProcessor {


    public CapellaEditingContextProcessor() {
    }

    @Override
    public void postProcess(IEditingContext editingContext) {
        // Add the Arcadia aware EContentAdapter
        if (editingContext instanceof IEMFEditingContext siriusWebEditingContext) {
            ResourceSet targetResourceSet = siriusWebEditingContext.getDomain().getResourceSet();
            // targetResourceSet.eAdapters().add(ArcadiaElementContentAdapter.getInstance());
        }
    }

}

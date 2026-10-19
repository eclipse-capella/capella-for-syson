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

import org.eclipse.capella.model.transverse.services.ArcadiaElementContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.springframework.stereotype.Service;

/**
 * Installs the declared type index once per EMF editing context.
 *
 * @author Jerome Gout
 */
@Service
public class CapellaEditingContextProcessor implements IEditingContextProcessor {

    @Override
    public void postProcess(IEditingContext editingContext) {
        if (editingContext instanceof IEMFEditingContext emfEditingContext) {
            var resourceSet = emfEditingContext.getDomain().getResourceSet();
            if (EcoreUtil.getAdapter(resourceSet.eAdapters(), ArcadiaElementContentAdapter.class) == null) {
                resourceSet.eAdapters().add(new ArcadiaElementContentAdapter(resourceSet));
            }
        }
    }
}

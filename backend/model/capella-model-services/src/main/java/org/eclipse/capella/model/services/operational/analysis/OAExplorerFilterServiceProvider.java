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
package org.eclipse.capella.model.services.operational.analysis;

import java.util.function.Predicate;

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.api.IExplorerFilterServiceProvider;
import org.eclipse.syson.sysml.Usage;
import org.springframework.stereotype.Service;

/**
 * Hides Operational Activity ports in the model explorer, as they are implicit Interaction endpoints.
 *
 * @author tbezierslafosse
 */
@Service
public class OAExplorerFilterServiceProvider implements IExplorerFilterServiceProvider {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    @Override
    public Predicate<Object> getFilter() {
        return object -> !(object instanceof Usage usage
                && this.commonQueryService.isFunctionPort(usage)
                && this.commonQueryService.isOperationalActivity(usage.getOwningUsage()));
    }
}

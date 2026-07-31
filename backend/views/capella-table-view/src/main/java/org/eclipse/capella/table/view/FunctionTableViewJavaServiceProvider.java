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
package org.eclipse.capella.table.view;

import java.util.List;

import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.core.services.ObjectService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.services.LabelService;
import org.springframework.stereotype.Service;

/**
 * Function table Java service provider.
 *
 * @author ntinsalhi
 */
@Service
public class FunctionTableViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        var descriptions = view.getDescriptions();
        var optDescription = descriptions.stream()
                .filter(desc ->
                        FunctionTableRepresentationDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()))
                .findFirst();
        if (optDescription.isPresent()) {
            return List.of(DeleteService.class,
                    LabelService.class,
                    TransverseQueryService.class,
                    TransverseMutationService.class,
                    ObjectService.class);
        }
        return List.of();
    }
}

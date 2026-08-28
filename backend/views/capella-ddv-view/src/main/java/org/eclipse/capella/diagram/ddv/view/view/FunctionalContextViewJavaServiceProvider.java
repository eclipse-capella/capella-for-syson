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
package org.eclipse.capella.diagram.ddv.view.view;

import java.util.List;

import org.eclipse.capella.model.services.functional.context.DDVQueryService;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.services.DiagramQueryElementService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.eclipse.syson.model.services.aql.ModelQueryAQLService;
import org.eclipse.syson.services.LabelService;
import org.springframework.stereotype.Service;

/**
 * Functional Context Java service provider.
 *
 * @author fbarbin
 */
@Service
public class FunctionalContextViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        var descriptions = view.getDescriptions();
        var optDescription = descriptions.stream()
                .filter(desc -> FunctionalContextViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()))
                .findFirst();
        if (optDescription.isPresent()) {
            return List.of(LabelService.class,
                    LAQueryService.class,
                    TransverseQueryService.class,
                    TransverseMutationService.class,
                    DDVQueryService.class,
                    DiagramQueryElementService.class,
                    DiagramMutationAQLService.class,
                    DiagramQueryAQLService.class,
                    ModelQueryAQLService.class);
        }
        return List.of();
    }
}

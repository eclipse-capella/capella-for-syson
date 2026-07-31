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
package org.eclipse.capella.form.view;

import org.eclipse.capella.form.view.util.CapellaViewFormService;
import org.eclipse.capella.model.services.logical.architecture.LAMutationService;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationQueryService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.sirius.components.core.services.ObjectService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.common.view.services.ViewToolService;
import org.eclipse.syson.services.DeleteService;
import org.eclipse.syson.services.LabelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * List of all Java services classes used by the {@link SystemArchitectureModelPageDescription}.
 *
 * @technical-debt This implementation has been developed in the context of a POC.
 * As such, it focuses on validating functional ideas rather than providing a fully
 * generic, extensible, or optimized solution. Several parts of this service rely
 * on hard-coded concepts, assumptions on SysML/Arcadia structures, and duplicated
 * traversal logic, which may limit reuse and scalability. A future industrialization
 * phase should consider refactoring toward more generic mechanisms, improved separation
 * of concerns, and better configurability.
 *
 * @author ntinsalhi
 */
@Service
public class CapellaFormViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        var descriptions = view.getDescriptions();
        var optDescription = descriptions.stream()
                .filter(desc ->
                        CapellaFormRepresentationDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()))
                .findFirst();
        if (optDescription.isPresent()) {
            return List.of(DeleteService.class,
                    LabelService.class,
                    LAQueryService.class,
                    LAMutationService.class,
                    LARepresentationQueryService.class,
                    LARepresentationMutationService.class,
                    TransverseQueryService.class,
                    ViewToolService.class,
                    CapellaViewFormService.class,
                    ObjectService.class);
        }
        return List.of();
    }
}

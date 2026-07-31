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

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextRepresentationDescriptionProvider;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.forms.description.FormDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.representations.GetOrCreateRandomIdProvider;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.sysml.OccurrenceDefinition;
import org.springframework.stereotype.Service;

/**
 * Registers the capella form representation description.
 *
 * @author ntinsalhi
 * @technical-debt This implementation has been developed in the context of a POC.
 * As such, it focuses on validating functional ideas rather than providing a fully
 * generic, extensible, or optimized solution. Several parts of this service rely
 * on hard-coded concepts, assumptions on SysML/Arcadia structures, and duplicated
 * traversal logic, which may limit reuse and scalability. A future industrialization
 * phase should consider refactoring toward more generic mechanisms, improved separation
 * of concerns, and better configurability.
 */
@Service
public class CapellaFormRepresentationDescriptionProvider implements IEditingContextRepresentationDescriptionProvider {

    public static final String FORM_DESCRIPTION_ID = "capella_form_description";

    public static final String DESCRIPTION_NAME = "System Architecture Model";


    private final IIdentityService identityService;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    public CapellaFormRepresentationDescriptionProvider(IIdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.identityService::getId)
                .orElse(null);
    }

    @Override
    public List<IRepresentationDescription> getRepresentationDescriptions(IEditingContext editingContext) {
        PageDescription systemArchitectureModelPageDescription = new SystemArchitectureModelPageDescription(this.identityService).getSystemArchitectureModelPageDescription();
        var formDescription = FormDescription.newFormDescription(FORM_DESCRIPTION_ID)
                .idProvider(new GetOrCreateRandomIdProvider())
                .label(DESCRIPTION_NAME)
                .labelProvider(variableManager -> DESCRIPTION_NAME)
                .canCreatePredicate(this::canCreate)
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .iconURLsProvider(variableManager -> List.of())
                .pageDescriptions(List.of(systemArchitectureModelPageDescription))
                .build();

        return List.of(formDescription);
    }

    private boolean canCreate(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, Object.class)
                .filter(object -> object instanceof OccurrenceDefinition)
                .isPresent();
    }
}

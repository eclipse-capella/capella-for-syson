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
package org.eclipse.capella.application.configuration.details.view.semanticbrowser;

import org.eclipse.capella.application.configuration.details.view.semanticbrowser.api.ICurrentElementTreeDescriptionProvider;
import org.eclipse.capella.application.configuration.details.view.semanticbrowser.api.IReferencingElementsTreeDescriptionProvider;
import org.eclipse.capella.application.configuration.details.view.semanticbrowser.api.IReferencedElementsTreeDescriptionProvider;

import org.eclipse.sirius.components.collaborative.forms.api.IRelatedElementsDescriptionProvider;
import org.eclipse.sirius.components.collaborative.forms.variables.FormVariableProvider;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.forms.GroupDisplayMode;
import org.eclipse.sirius.components.forms.description.AbstractControlDescription;
import org.eclipse.sirius.components.forms.description.FormDescription;
import org.eclipse.sirius.components.forms.description.GroupDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Provides a tree widget description with referencing, current and referenced trees.
 *
 * @author ntinsalhi
 */
@Primary
@Service
public class SemanticBrowserDescriptionProvider implements IRelatedElementsDescriptionProvider {

    public static final String FORM_DESCRIPTION_ID = "semanticBrowser_form_description";

    public static final String FORM_TITLE = "Semantic Browser";

    private static final String GROUP_DESCRIPTION_ID = "semanticBrowser";

    private static final String PAGE_DESCRIPTION_ID = "semanticBrowser";

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final IReferencingElementsTreeDescriptionProvider incomingTreeDescriptionProvider;

    private final ICurrentElementTreeDescriptionProvider currentTreeDescriptionProvider;

    private final IReferencedElementsTreeDescriptionProvider outgoingTreeDescriptionProvider;

    public SemanticBrowserDescriptionProvider(IIdentityService identityService,
                                              ILabelService labelService,
                                              IReferencingElementsTreeDescriptionProvider incomingTreeDescriptionProvider,
                                              ICurrentElementTreeDescriptionProvider currentTreeDescriptionProvider,
                                              IReferencedElementsTreeDescriptionProvider outgoingTreeDescriptionProvider) {
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
        this.incomingTreeDescriptionProvider = Objects.requireNonNull(incomingTreeDescriptionProvider);
        this.currentTreeDescriptionProvider = Objects.requireNonNull(currentTreeDescriptionProvider);
        this.outgoingTreeDescriptionProvider = Objects.requireNonNull(outgoingTreeDescriptionProvider);
    }

    @Override
    public FormDescription getFormDescription() {
        Function<VariableManager, String> targetObjectIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.identityService::getId)
                .orElse(null);

        List<GroupDescription> groupDescriptions = List.of(this.getGroupDescription());

        return FormDescription.newFormDescription(FORM_DESCRIPTION_ID)
                .label(FORM_TITLE)
                .idProvider(this::getFormId)
                .targetObjectIdProvider(variableManager -> variableManager.get(VariableManager.SELF, Object.class).map(this.identityService::getId).orElse(null))
                .labelProvider(variableManager -> FORM_TITLE)
                .targetObjectIdProvider(targetObjectIdProvider)
                .canCreatePredicate(variableManager -> false)
                .pageDescriptions(List.of(this.getPageDescription(groupDescriptions)))
                .iconURLsProvider(variableManager -> List.of())
                .build();
    }

    private String getFormId(VariableManager variableManager) {
        List<?> selectedObjects = variableManager.get(FormVariableProvider.SELECTION.name(), List.class).orElse(List.of());
        List<String> selectedObjectIds = selectedObjects.stream()
                .map(this.identityService::getId)
                .toList();

        var encodedIds = selectedObjectIds.stream().map(id -> URLEncoder.encode(id, StandardCharsets.UTF_8)).toList();
        return "semanticBrowser://?objectIds=[" + String.join(",", encodedIds) + "]";
    }

    private GroupDescription getGroupDescription() {
        List<AbstractControlDescription> controlDescriptions = new ArrayList<>();
        controlDescriptions.add(this.incomingTreeDescriptionProvider.getTreeDescription());
        controlDescriptions.add(this.currentTreeDescriptionProvider.getTreeDescription());
        controlDescriptions.add(this.outgoingTreeDescriptionProvider.getTreeDescription());

        return GroupDescription.newGroupDescription(GROUP_DESCRIPTION_ID)
                .idProvider(variableManager -> FORM_TITLE)
                .labelProvider(variableManager -> FORM_TITLE)
                .displayModeProvider(variableManager -> GroupDisplayMode.TOGGLEABLE_AREAS)
                .semanticElementsProvider(variableManager -> variableManager.get(VariableManager.SELF, Object.class).stream().toList())
                .controlDescriptions(controlDescriptions)
                .build();
    }

    private PageDescription getPageDescription(List<GroupDescription> groupDescriptions) {
        Function<VariableManager, String> idProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.identityService::getId)
                .orElseGet(() -> UUID.randomUUID().toString());

        Function<VariableManager, String> labelProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.labelService::getStyledLabel)
                .map(Object::toString)
                .orElse("");

        Function<VariableManager, List<?>> semanticElementsProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class).stream().toList();

        return PageDescription.newPageDescription(PAGE_DESCRIPTION_ID)
                .idProvider(idProvider)
                .labelProvider(labelProvider)
                .semanticElementsProvider(semanticElementsProvider)
                .groupDescriptions(groupDescriptions)
                .canCreatePredicate(variableManager -> true)
                .build();
    }
}

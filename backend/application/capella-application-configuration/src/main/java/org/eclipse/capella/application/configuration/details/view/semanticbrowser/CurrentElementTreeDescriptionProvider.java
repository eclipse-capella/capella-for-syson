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
import org.eclipse.capella.application.configuration.label.services.CapellaImagePathsService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.core.CoreImageConstants;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.forms.components.TreeComponent;
import org.eclipse.sirius.components.forms.description.TreeDescription;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.RepresentationMetadata;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Provides the definition of the tree widget for the "Current Element" panel in the Semantic Browser view. It displays two broad
 * top-level categories, "Parent" and "All Related Diagrams", implemented as plain strings.
 *
 * @author ntinsalhi
 */
@Service
public class CurrentElementTreeDescriptionProvider implements ICurrentElementTreeDescriptionProvider {
    private static final String WIDGET_ID = "semanticBrowser/current";

    private static final String TITLE = "Current Element";

    private static final String CATEGORY_PARENT = "Parent";

    private static final String CATEGORY_RELATED_DIAGRAMS = "All Related Diagrams";

    private static final String WIDGET_ICON_URL = "/related-elements/arrow_downward_black_24dp.svg";

    private static final String FOLDER_ICON_URL = "/related-elements/folder_black_24dp.svg";

    private static final String DIAGRAM_ICON_URL = "/diagram-images/diagram.svg";

    private static final String CATEGORY_KIND = "siriusWeb://category";

    private static final String CONTAINMENT_REFERENCE_KIND = "siriusWeb://category/containment-reference";

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final CapellaImagePathsService capellaImagePathsService;

    private final TransverseQueryService transverseQueryService;

    private final SemanticBrowserService semanticBrowserService;

    public CurrentElementTreeDescriptionProvider(IIdentityService identityService,
                                                 ILabelService labelService,
                                                 IRepresentationMetadataSearchService representationMetadataSearchService,
                                                 IRepresentationSearchService representationSearchService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.labelService = Objects.requireNonNull(labelService);
        this.capellaImagePathsService = new CapellaImagePathsService(labelService);
        this.transverseQueryService = new TransverseQueryService();
        this.semanticBrowserService = new SemanticBrowserService(representationMetadataSearchService, identityService, representationSearchService);
    }

    @Override
    public TreeDescription getTreeDescription() {
        return TreeDescription.newTreeDescription(WIDGET_ID)
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this::getTargetObjectIdProvider)
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(variableManager -> "")
                .messageProvider(variableManager -> "")
                .labelProvider(variableManager -> TITLE)
                .iconURLProvider(variableManager -> List.of(WIDGET_ICON_URL))
                .nodeIdProvider(this::getNodeId)
                .nodeLabelProvider(this::getNodeLabel)
                .nodeIconURLProvider(this::getNodeImageURL)
                .nodeEndIconsURLProvider(variableManager -> List.of())
                .nodeKindProvider(this::getNodeKind)
                .nodeSelectableProvider(this::isNodeSelectable)
                .isCheckableProvider(variableManager -> false)
                .checkedValueProvider(variableManager -> false)
                .newCheckedValueHandler((variableManager, newValue) -> new Success())
                .childrenProvider(this::getCurrentChildren)
                .expandedNodeIdsProvider(this.semanticBrowserService::collectExpandedNodeIndexes)
                .build();
    }

    private String getTargetObjectIdProvider(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, Object.class).map(this.identityService::getId).orElse(null);
    }

    private String getNodeId(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = "category/" + self;
        } else if (self instanceof EReference eReference) {
            result = "reference/" + eReference.getName();
        } else if (self instanceof RepresentationMetadata representationMetadata) {
            result = representationMetadata.getRepresentationMetadataId().toString();
        } else if (self != null) {
            result = this.identityService.getId(self);
        }
        return result;
    }

    private String getNodeLabel(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String stringValue) {
            result = stringValue;
        } else if (self != null) {
            result = this.labelService.getStyledLabel(self).toString();
        }
        return result;
    }

    private List<String> getNodeImageURL(VariableManager variableManager) {
        List<String> result = List.of(CoreImageConstants.DEFAULT_SVG);
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = List.of(FOLDER_ICON_URL);
        } else if (self != null) {
            if (self instanceof EObject eObject
                    && this.transverseQueryService.isArcadiaElement(eObject)) {
                result = this.capellaImagePathsService.getImagePaths(self);
            } else {
                result = this.labelService.getImagePaths(self);
            }
        }
        return result;
    }

    private String getNodeKind(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String) {
            result = CATEGORY_KIND;
        } else if (self instanceof RepresentationMetadata representationMetadata) {
            result = representationMetadata.getKind();
        } else if (self instanceof EReference) {
            result = CONTAINMENT_REFERENCE_KIND;
        } else if (self != null) {
            result = this.identityService.getKind(self);
        }
        return result;
    }

    private boolean isNodeSelectable(VariableManager variableManager) {
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        return self instanceof EObject
                || self instanceof RepresentationMetadata;
    }

    private List<?> getCurrentChildren(VariableManager variableManager) {
        var self = variableManager.get(VariableManager.SELF, Object.class);
        var root = variableManager.get(TreeComponent.ROOT_VARIABLE, EObject.class);
        var ancestors = variableManager.get(TreeComponent.ANCESTORS_VARIABLE, List.class);
        IEditingContext editingContext = variableManager.get(IEditingContext.EDITING_CONTEXT, IEditingContext.class).orElse(null);

        if (root.isPresent() && self.isPresent() && ancestors.isPresent()) {
            return this.getCurrentChildren(editingContext, self.get(), root.get(), ancestors.get());
        } else {
            return List.of();
        }
    }

    private List<Object> getCurrentChildren(IEditingContext editingContext, Object self, EObject root, List<?> ancestors) {
        List<Object> result = new ArrayList<>();
        if (ancestors.isEmpty()) {
            result.add(root);
        } else if (root.equals(self)) {
            result.addAll(this.semanticBrowserService.getCurrentCategories(root));
            result.addAll(List.of(CATEGORY_PARENT, CATEGORY_RELATED_DIAGRAMS));
        } else if (self instanceof String category) {
            if (category.equals(CATEGORY_RELATED_DIAGRAMS)) {
                result.addAll(this.semanticBrowserService.getAllRelatedRepresentationMetadata(root, editingContext));
            } else if (category.equals(CATEGORY_PARENT)) {
                result.addAll(this.semanticBrowserService.getParentCategoryElement(root));
            } else {
                result.addAll(this.semanticBrowserService.getCurrentCategoryElements(root, category));
            }
        }
        return result;
    }
}

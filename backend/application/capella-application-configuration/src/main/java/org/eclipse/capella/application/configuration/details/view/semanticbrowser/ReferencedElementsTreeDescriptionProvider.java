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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.capella.application.configuration.details.view.semanticbrowser.api.IReferencedElementsTreeDescriptionProvider;
import org.eclipse.capella.application.configuration.label.services.CapellaImagePathsService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.core.CoreImageConstants;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.forms.components.TreeComponent;
import org.eclipse.sirius.components.forms.description.TreeDescription;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.eclipse.syson.sysml.FlowUsage;
import org.springframework.stereotype.Service;

/**
 * Provides the definition of the tree widget for the "Referenced Elements" panel in the Semantic Browser view.
 *
 * @author ntinsalhi
 */
@Service
public class ReferencedElementsTreeDescriptionProvider implements IReferencedElementsTreeDescriptionProvider {

    private static final String WIDGET_ID = "semanticBrowser/outgoing";

    private static final String TITLE = "Referenced Elements";

    private static final String WIDGET_ICON_URL = "/related-elements/east_black_24dp.svg";

    private static final String FOLDER_ICON_URL = "/related-elements/folder_black_24dp.svg";

    private final IIdentityService identityService;

    private final ILabelService labelService;

    private final CapellaImagePathsService capellaImagePathsService;

    private final SemanticBrowserService semanticBrowserService;

    private final TransverseQueryService transverseQueryService;

    public ReferencedElementsTreeDescriptionProvider(IIdentityService identityService, ILabelService labelService, IRepresentationMetadataSearchService representationMetadataSearchService,
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
                .childrenProvider(this::getOutgoingChildren)
                .expandedNodeIdsProvider(this.semanticBrowserService::collectExpandedNodeIndexes)
                .isCheckableProvider(variableManager -> false)
                .checkedValueProvider(variableManager -> false)
                .newCheckedValueHandler((variableManager, newValue) -> new Success())
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
        } else if (self != null) {
            result = this.identityService.getId(self);
        }
        return result;
    }

    private String getNodeLabel(VariableManager variableManager) {
        String result = null;
        var self = variableManager.get(VariableManager.SELF, Object.class).orElse(null);
        if (self instanceof String string) {
            result = string;
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
        if (self != null) {
            result = this.identityService.getKind(self);
        }
        return result;
    }

    private boolean isNodeSelectable(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, EObject.class).isPresent();
    }

    private List<?> getOutgoingChildren(VariableManager variableManager) {
        var self = variableManager.get(VariableManager.SELF, Object.class);
        var root = variableManager.get(TreeComponent.ROOT_VARIABLE, EObject.class);
        var ancestors = variableManager.get(TreeComponent.ANCESTORS_VARIABLE, List.class);
        if (root.isPresent() && self.isPresent() && ancestors.isPresent()) {
            return this.getOutgoingChildren(self.get(), root.get(), ancestors.get());
        } else {
            return List.of();
        }
    }

    private List<?> getOutgoingChildren(Object self, EObject root, List<?> ancestors) {
        List<Object> result = new ArrayList<>();
        if (ancestors.isEmpty()) {
            if (root.equals(self)) {
                result.addAll(this.semanticBrowserService.getReferencedElementsCategories(root));
            }
        } else if (self instanceof String category) {
            result.addAll(this.semanticBrowserService.getReferencedCategoryElements(root, category));
        } else {
            if (this.transverseQueryService.isFunction(root)
                    && self instanceof FlowUsage flowUsage) {
                result.addAll(this.semanticBrowserService.getFunctionalExchangeCategoryIsReferencing(flowUsage));
            }
        }

        return result;
    }
}

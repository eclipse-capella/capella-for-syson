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
package org.eclipse.capella.application.configuration.explorer.selection;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.capella.diagram.common.view.CapellaViewDescriptionProvider;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.IRepresentationDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.selection.description.SelectionDescription;
import org.eclipse.sirius.components.trees.description.TreeDescription;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.diagram.DialogDescription;
import org.eclipse.sirius.components.view.emf.api.IDialogDescriptionConverter;
import org.eclipse.sirius.components.view.emf.diagram.SelectionDialogDescriptionConverter;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Presents diagram candidates in the Capella hierarchy while preserving their original selection scope.
 *
 * @author tbezierslafosse
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaSelectionDialogDescriptionConverter implements IDialogDescriptionConverter {

    private static final String CANDIDATES = "capellaSelectionCandidates";

    private final SelectionDialogDescriptionConverter defaultConverter;

    private final CapellaSelectionTreeService treeService;

    private final CapellaViewDescriptionProvider viewDescriptionProvider;

    public CapellaSelectionDialogDescriptionConverter(SelectionDialogDescriptionConverter defaultConverter, CapellaSelectionTreeService treeService,
            CapellaViewDescriptionProvider viewDescriptionProvider) {
        this.defaultConverter = Objects.requireNonNull(defaultConverter);
        this.treeService = Objects.requireNonNull(treeService);
        this.viewDescriptionProvider = Objects.requireNonNull(viewDescriptionProvider);
    }

    @Override
    public boolean canConvert(DialogDescription dialogDescription) {
        return this.defaultConverter.canConvert(dialogDescription)
                && EMFUtils.getFirstAncestor(View.class, dialogDescription, null)
                        .map(this.viewDescriptionProvider::isCapellaView)
                        .orElse(false);
    }

    @Override
    public List<IRepresentationDescription> convert(DialogDescription dialogDescription, AQLInterpreter interpreter) {
        return this.defaultConverter.convert(dialogDescription, interpreter).stream()
                .map(this::adapt)
                .toList();
    }

    private IRepresentationDescription adapt(IRepresentationDescription description) {
        var result = description;
        if (description instanceof TreeDescription tree) {
            result = this.adaptTree(tree);
        } else if (description instanceof SelectionDescription selection) {
            // Selection dialog adaptation
            result = SelectionDescription.newSelectionDescription(selection.getId())
                    .label(selection.getLabel())
                    .idProvider(selection.getIdProvider())
                    .labelProvider(selection.getLabelProvider())
                    .targetObjectIdProvider(selection.getTargetObjectIdProvider())
                    .canCreatePredicate(selection.getCanCreatePredicate())
                    .iconURLsProvider(selection.getIconURLsProvider())
                    .multiple(selection.isMultiple())
                    .optional(selection.isOptional())
                    .treeDescription(this.adaptTree(selection.getTreeDescription()))
                    .build();
        }
        return result;
    }

    private TreeDescription adaptTree(TreeDescription original) {
        return TreeDescription.newTreeDescription(this.treeService.adapt(original))
                .elementsProvider(variables -> {
                    // defines the roots in the tree
                    var candidates = original.getElementsProvider().apply(variables);
                    variables.put(CANDIDATES, new HashSet<>(candidates));
                    return this.treeService.getRoots(variables);
                })
                .selectableProvider(variables -> {
                    // checks if a line can be selected in the tree
                    Object candidates = variables.getVariables().get(CANDIDATES);
                    return candidates instanceof Set<?> elements && elements.contains(variables.getVariables().get(VariableManager.SELF))
                            && original.getSelectableProvider().apply(variables);
                })
                .build();
    }
}

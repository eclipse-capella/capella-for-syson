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
package org.eclipse.capella.table.view.providers;

import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.tables.api.IRowContextMenuEntryExecutor;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.tables.Line;
import org.eclipse.sirius.components.tables.Table;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.sysml.ActionUsage;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * Executor for adding a new row in the Function Table using the context menu.
 * Creates a new {@link ActionUsage} function.
 *
 * @author ntinsalhi
 */
@Service
public class AddSameLevelFunctionContextMenuEntryExecutor implements IRowContextMenuEntryExecutor {

    private final IObjectSearchService objectSearchService;

    private final LARepresentationMutationService laRepresentationMutationService;

    public AddSameLevelFunctionContextMenuEntryExecutor(IObjectSearchService objectSearchService,
            IFeedbackMessageService feedbackMessageService,
            IReadOnlyObjectPredicate readOnlyService,
            ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService) {

        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.laRepresentationMutationService = new LARepresentationMutationService(feedbackMessageService, readOnlyService, objectSearchService,
                showDiagramsInheritedMembersService);
    }

    @Override
    public boolean canExecute(IEditingContext editingContext, TableDescription tableDescription, Table table, Line row, String rowMenuContextEntryId) {
        return FunctionTableRowContextMenuProvider.ADD_FUNCTION_ID.equals(rowMenuContextEntryId);
    }

    @Override
    public IStatus execute(IEditingContext editingContext, TableDescription tableDescription, Table table, Line row, String rowMenuContextEntryId) {

        this.objectSearchService.getObject(editingContext, row.getTargetObjectId())
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .ifPresent(selectedFunction -> this.laRepresentationMutationService
                        .createNewSameLevelFunction(selectedFunction, editingContext));

        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
    }
}

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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.table.view.FunctionTableRepresentationDescriptionProvider;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.collaborative.tables.api.IEditCellHandler;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.tables.Column;
import org.eclipse.sirius.components.tables.ICell;
import org.eclipse.sirius.components.tables.Line;
import org.eclipse.sirius.components.tables.descriptions.TableDescription;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.springframework.stereotype.Service;

/**
 * Provides edit table cell executor for the arcadia function concepts.
 *
 * @author ntinsalhi
 */
@Service
public class FunctionTableEditCellHandler implements IEditCellHandler {

    private final IObjectSearchService objectSearchService;

    private final TransverseMutationService transverseMutationService;

    public FunctionTableEditCellHandler(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.transverseMutationService = new TransverseMutationService();
    }

    @Override
    public boolean canHandle(TableDescription tableDescription) {
        return tableDescription.getId().equals(FunctionTableRepresentationDescriptionProvider.TABLE_DESCRIPTION_ID);
    }

    @Override
    public IStatus handle(IEditingContext editingContext,
                          TableDescription tableDescription,
                          ICell cell, Line line,
                          Column column, Object newValue) {

        AtomicReference<IStatus> status = new AtomicReference<>(new Success());

        var optEObject = this.objectSearchService
                .getObject(editingContext, line.getTargetObjectId())
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast);

        optEObject.ifPresent(eObject -> {
            var columnId = column.getTargetObjectId();

            if (EcoreUtil.getURI(SysmlPackage.eINSTANCE.getLiteralString()).toString().equals(columnId)) {
                status.set(this.setFunctionDescription(eObject, (String) newValue));

            } else if (EcoreUtil.getURI(SysmlPackage.eINSTANCE.getOwningMembership()).toString().equals(columnId)) {
                status.set(this.setFunctionStatusKind(eObject, newValue, editingContext));
            }
        });

        return status.get();
    }

    private IStatus setFunctionDescription(Usage eObject, String newValue) {
        this.transverseMutationService.setElementDescription(eObject, newValue);

        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
    }

    private IStatus setFunctionStatusKind(ActionUsage function, Object newValue, IEditingContext editingContext) {
        this.transverseMutationService.setStatusKind(function, newValue, editingContext);
        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
    }
}

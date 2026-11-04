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
package org.eclipse.capella.application.configuration.details.view.referencewidget;

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Provide the allocated function reference widget content.
 *
 * @author fbarbin
 */
@Service
public class AllocatedFunctionReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {


    public static final String NAME = "AllocatedFunctionWidget";

    private static final String ERROR_MSG = "Something went wrong while deleting the allocated function";

    private final CommonQueryService commonQueryService;

    private final CommonDeletionService commonDeletionService;

    private final CommonUpdateService commonUpdateService;

    public AllocatedFunctionReferenceWidgetProvider() {
        this.commonQueryService = new CommonQueryService();
        this.commonDeletionService = new CommonDeletionService();
        this.commonUpdateService = new CommonUpdateService();
    }

    @Override
    public boolean canHandle(ReferenceWidgetDescription referenceDescription) {
        return (ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + NAME).equals(referenceDescription.getName());
    }

    @Override
    public boolean isMany() {
        return true;
    }

    @Override
    public List<?> getReferenceOptions(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof EObject eObject) {
            return this.commonQueryService.getFunctions(eObject);
        }
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof PartUsage partUsage) {
            return this.commonQueryService.getAllocatedFunctions(partUsage);
        }
        return List.of();
    }

    @Override
    public IStatus handleAdd(ReferenceWidgetDescription referenceDescription, EObject owner, List<Object> newValues) {
        if (owner instanceof PartUsage partUsage && newValues.stream().allMatch(ActionUsage.class::isInstance)) {
            var currentValues = this.commonQueryService.getAllocatedFunctions(partUsage);
            // The semantic service creates one allocation at a time; retain existing allocations.
            newValues.stream().map(ActionUsage.class::cast).distinct()
                    .filter(function -> !currentValues.contains(function))
                    .forEach(function -> this.commonUpdateService.setPerformAction(partUsage, function));
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure("The selected values cannot be added to this reference.");
    }

    @Override
    public IStatus handleSet(ReferenceWidgetDescription referenceDescription, EObject owner, Object newValue) {
        return new Failure("Setting this reference is not supported.");
    }

    @Override
    public IStatus handleRemove(ReferenceWidgetDescription referenceDescription, EObject owner, Object value) {
        if (owner instanceof PartUsage partUsage && value instanceof ActionUsage actionUsage) {
            this.commonDeletionService.deletePerformedActionUsage(partUsage, actionUsage);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

    @Override
    public EClass getType() {
        return SysmlPackage.eINSTANCE.getActionUsage();
    }

    @Override
    public IStatus handleClear(ReferenceWidgetDescription referenceDescription, EObject owner) {
        if (owner instanceof PartUsage partUsage) {
            this.commonQueryService.getPerformedActions(partUsage, performAction -> this.commonQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION).test(performAction))
                    .forEach(actionUsage -> this.commonDeletionService.deletePerformedActionUsage(partUsage, actionUsage));
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

}

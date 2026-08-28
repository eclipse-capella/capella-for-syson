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

import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_FUNCTION;
import static org.eclipse.capella.model.transverse.services.TransverseQueryService.ARCADIA_PREFIX;

import java.util.List;
import java.util.Map;

import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetComponent;
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

    private final TransverseQueryService transverseQueryService;

    private final TransverseMutationService transverseMutationService;

    public AllocatedFunctionReferenceWidgetProvider() {
        this.transverseQueryService = new TransverseQueryService();
        this.transverseMutationService = new TransverseMutationService();
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
            return this.transverseQueryService.getFunctions(eObject);
        }
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof PartUsage partUsage) {
            return this.transverseQueryService.getAllocatedFunctions(partUsage);
        }
        return List.of();
    }

    @Override
    public IStatus handleItemRemoved(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object owner = variableManager.getVariables().get(VariableManager.SELF);
        if (owner instanceof PartUsage partUsage) {
            variableManager.get(ReferenceWidgetComponent.ITEM_VARIABLE, ActionUsage.class)
                    .ifPresent(actionUsage -> this.transverseMutationService.deletePerformedActionUsage(partUsage, actionUsage));
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

    @Override
    public EClass getType() {
        return SysmlPackage.eINSTANCE.getActionUsage();
    }

    @Override
    public IStatus handleClearReference(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object owner = variableManager.getVariables().get(VariableManager.SELF);
        if (owner instanceof PartUsage partUsage) {
            this.transverseQueryService.getPerformedActions(partUsage, performAction -> this.transverseQueryService.isTypedWith(ARCADIA_PREFIX + ARCADIA_FUNCTION).test(performAction))
                    .forEach(actionUsage -> this.transverseMutationService.deletePerformedActionUsage(partUsage, actionUsage));
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

}

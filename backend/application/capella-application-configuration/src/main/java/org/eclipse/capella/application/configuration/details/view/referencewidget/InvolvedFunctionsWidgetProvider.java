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

import java.util.List;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Provide the involved functions reference widget content.
 *
 * @author fbarbin
 */
@Service
public class InvolvedFunctionsWidgetProvider implements ICapellaReferenceWidgetProvider {

    public static final String WIDGET_NAME = "InvolvedFunctionsWidget";

    public static final String FEATURE_NAME = "involvedFunctions";

    private static final String ERROR_MSG = "The delete action on a this derived feature is not supported";

    private final TransverseQueryService transverseQueryService;


    public InvolvedFunctionsWidgetProvider() {
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public boolean canHandle(ReferenceWidgetDescription referenceDescription) {
        return (ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + WIDGET_NAME).equals(referenceDescription.getName());
    }

    @Override
    public boolean isMany() {
        return true;
    }

    @Override
    public List<?> getReferenceOptions(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        // Cannot be edited
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof ActionUsage actionUsage) {
            return this.transverseQueryService.getInvolvedFunctions(actionUsage);
        }
        return List.of();
    }

    @Override
    public IStatus handleItemRemoved(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        // Cannot be edited
        return new Failure(ERROR_MSG);
    }

    @Override
    public EClass getType() {
        return SysmlPackage.eINSTANCE.getActionUsage();
    }

    @Override
    public IStatus handleClearReference(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        // Cannot be edited
        return new Failure(ERROR_MSG);
    }

}

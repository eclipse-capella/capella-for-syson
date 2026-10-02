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

import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Usage;
import org.springframework.stereotype.Service;

/**
 * Provide the isRealizedBy reference widget content.
 *
 * @author Jerome Gout
 */
@Service
public class IsRealizedByReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {

    public static final String WIDGET_NAME = "IsRealizedByWidget";

    private static final String ERROR_MSG = "The isRealizedBy reference is read-only";

    private final CommonQueryService commonQueryService = new CommonQueryService();

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
        if (object instanceof Usage usage) {
            return this.commonQueryService.getIsRealizedBy(usage);
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
        return SysmlPackage.eINSTANCE.getOccurrenceUsage();
    }

    @Override
    public IStatus handleClearReference(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        // Cannot be edited
        return new Failure(ERROR_MSG);
    }

}

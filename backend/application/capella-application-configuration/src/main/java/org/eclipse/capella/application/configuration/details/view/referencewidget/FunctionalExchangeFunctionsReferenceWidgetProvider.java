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
import java.util.Optional;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Provide the source and target reference widget content.
 *
 * @author fbarbin
 */
@Service
public class FunctionalExchangeFunctionsReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {


    public static final String WIDGET_NAME = "SourceAndTargetFunctionWidget";

    public static final String SOURCE_FEATURE = "source";

    public static final String TARGET_FEATURE = "target";

    private static final String ERROR_MSG = "Something went wrong while removing the function";

    private final TransverseQueryService transverseQueryService;

    public FunctionalExchangeFunctionsReferenceWidgetProvider() {
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public boolean canHandle(ReferenceWidgetDescription referenceDescription) {
        return (ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + WIDGET_NAME).equals(referenceDescription.getName());
    }

    @Override
    public boolean isMany() {
        return false;
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
        var returnValue = List.of();
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof FlowUsage flowUsage) {
            if (SOURCE_FEATURE.equals(referenceDescription.getReferenceNameExpression())) {
                returnValue = List.of(flowUsage.getSourceFeature());
            } else if (TARGET_FEATURE.equals(referenceDescription.getReferenceNameExpression())) {
                Optional<Feature> optionalTarget = flowUsage.getTargetFeature().stream().findFirst();
                if (optionalTarget.isPresent()) {
                    returnValue = List.of(optionalTarget.get());
                }
            }
        }
        return returnValue;
    }

    @Override
    public IStatus handleItemRemoved(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        // Not implemented yet : It is not clear what we should do on source or target feature. Indeed, they should
        // reference a function parameter typed by an ExchangeItem.
        return new Failure(ERROR_MSG);
    }

    @Override
    public EClass getType() {
        return SysmlPackage.eINSTANCE.getActionUsage();
    }

    @Override
    public IStatus handleClearReference(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        // Not implemented yet : It is not clear what we should do on source or target feature. Indeed, they should
        // reference a function parameter typed by an ExchangeItem.
        return new Failure(ERROR_MSG);
    }

}

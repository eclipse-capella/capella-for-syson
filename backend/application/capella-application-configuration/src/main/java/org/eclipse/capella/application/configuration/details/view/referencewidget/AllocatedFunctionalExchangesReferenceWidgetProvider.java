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

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Provide the allocated functional exchanges reference widget content.
 *
 * @author fbarbin
 */
@Service
public class AllocatedFunctionalExchangesReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {


    public static final String WIDGET_NAME = "AllocatedFunctionalExchangesWidget";

    public static final String FEATURE_NAME = "allocatedFunctionalExchanges";

    private static final String ERROR_MSG = "Something went wrong while deleting the allocated functional exchange";

    private final CommonUpdateService commonUpdateService;

    private final CommonQueryService commonQueryService;

    public AllocatedFunctionalExchangesReferenceWidgetProvider() {
        this.commonUpdateService = new CommonUpdateService();
        this.commonQueryService = new CommonQueryService();
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
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof EObject eObject) {
            return this.commonQueryService.getFunctionalExchanges(eObject);
        }
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof InterfaceUsage interfaceUsage) {
            return this.commonQueryService.getFeatureReferenceValue(interfaceUsage, FEATURE_NAME);
        }
        return List.of();
    }

    @Override
    public IStatus handleAdd(ReferenceWidgetDescription referenceDescription, EObject owner, List<Object> newValues) {
        if (owner instanceof InterfaceUsage interfaceUsage && newValues.stream().allMatch(Feature.class::isInstance)) {
            var currentValues = this.commonQueryService.getFeatureReferenceValue(interfaceUsage, FEATURE_NAME);
            // Keep the setter's single-value ordering; multiple selections require the complete reference value.
            var values = newValues;
            if (newValues.size() > 1) {
                values = Stream.concat(currentValues.stream(), newValues.stream()).distinct().toList();
            }
            if (newValues.stream().anyMatch(value -> !currentValues.contains(value))) {
                this.commonUpdateService.setArcadiaReferenceFeature(interfaceUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE, FEATURE_NAME, values, SysmlPackage.eINSTANCE.getFlowUsage().getName());
            }
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
        if (owner instanceof InterfaceUsage interfaceUsage && value instanceof Feature feature) {
            this.commonUpdateService.deleteFeaturesFromReference(interfaceUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE, FEATURE_NAME, SysmlPackage.eINSTANCE.getFlowUsage(), List.of(feature));
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
        if (owner instanceof InterfaceUsage interfaceUsage) {
            this.commonUpdateService.deleteReference(interfaceUsage, FEATURE_NAME);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

}

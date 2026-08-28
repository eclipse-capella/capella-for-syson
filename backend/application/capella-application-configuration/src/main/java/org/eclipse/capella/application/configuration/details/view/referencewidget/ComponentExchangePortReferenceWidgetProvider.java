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
import java.util.Map;
import java.util.Optional;

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
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Provide the component exchange ports reference widget content.
 *
 * @author fbarbin
 */
@Service
public class ComponentExchangePortReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {

    public static final String WIDGET_NAME = "ComponentExchangePortWidget";

    public static final String SOURCE_PORT_FEATURE = "source";

    public static final String TARGET_PORT_FEATURE = "target";

    private static final String ERROR_MSG = "Something went wrong while deleting the exchange item payload";

    private final TransverseQueryService transverseQueryService;

    private final TransverseMutationService transverseMutationService;

    public ComponentExchangePortReferenceWidgetProvider() {
        this.transverseQueryService = new TransverseQueryService();
        this.transverseMutationService = new TransverseMutationService();

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
            return this.transverseQueryService.getComponentPorts(eObject);
        }
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        List<?> values = List.of();
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof InterfaceUsage interfaceUsage) {
            if (SOURCE_PORT_FEATURE.equals(referenceDescription.getReferenceNameExpression())) {
                values = List.of(this.transverseQueryService.getSource(interfaceUsage));
            } else if (TARGET_PORT_FEATURE.equals(referenceDescription.getReferenceNameExpression())) {
                values = this.transverseQueryService.getTarget(interfaceUsage);
            }
        }
        return values;
    }

    @Override
    public IStatus handleItemRemoved(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object owner = variableManager.getVariables().get(VariableManager.SELF);
        if (owner instanceof InterfaceUsage interfaceUsage) {
            variableManager.get(ReferenceWidgetComponent.ITEM_VARIABLE, Feature.class)
                    .ifPresent(feature -> this.transverseMutationService.deleteFeaturesFromReference(interfaceUsage, "", referenceDescription.getReferenceNameExpression(), null, List.of(feature)));
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

    @Override
    public EClass getType() {
        return SysmlPackage.eINSTANCE.getItemUsage();
    }

    @Override
    public IStatus handleClearReference(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object owner = variableManager.getVariables().get(VariableManager.SELF);
        if (owner instanceof FlowUsage flowUsage) {
            Optional.ofNullable(flowUsage.getPayloadFeature()).ifPresent(this.transverseMutationService::delete);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

}

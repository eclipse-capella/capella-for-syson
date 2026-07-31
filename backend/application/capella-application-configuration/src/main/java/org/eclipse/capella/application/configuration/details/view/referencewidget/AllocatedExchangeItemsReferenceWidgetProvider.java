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

import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
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
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;

/**
 * Provide the allocated exchange items reference widget content.
 *
 * @author fbarbin
 */
@Service
public class AllocatedExchangeItemsReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {


    public static final String WIDGET_NAME = "AllocatedExchangeItemsWidget";

    public static final String FEATURE_NAME = "allocatedExchangeItems";

    private static final String ERROR_MSG = "Something went wrong while deleting the allocated exchange item";

    private final LAQueryService lAQueryService;


    private final TransverseMutationService transverseMutationService;

    private final TransverseQueryService transverseQueryService;

    public AllocatedExchangeItemsReferenceWidgetProvider() {
        this.lAQueryService = new LAQueryService();
        this.transverseMutationService = new TransverseMutationService();
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
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof EObject eObject) {
            var allExchangeItems = new ArrayList<>(this.lAQueryService.getExchangeItems(eObject));
            allExchangeItems.removeIf(this.lAQueryService::isFunctionPort);
            return allExchangeItems;
        }
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof InterfaceUsage interfaceUsage) {
            return this.transverseQueryService.getFeatureReferenceValue(interfaceUsage, FEATURE_NAME);
        }
        return List.of();
    }

    @Override
    public IStatus handleItemRemoved(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object owner = variableManager.getVariables().get(VariableManager.SELF);
        if (owner instanceof InterfaceUsage interfaceUsage) {
            variableManager.get(ReferenceWidgetComponent.ITEM_VARIABLE, Feature.class)
                    .ifPresent(feature -> this.transverseMutationService.deleteFeaturesFromReference(interfaceUsage, ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE, FEATURE_NAME, SysmlPackage.eINSTANCE.getItemUsage(), List.of(feature)));
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
        if (owner instanceof InterfaceUsage interfaceUsage) {
            this.transverseMutationService.deleteReference(interfaceUsage, FEATURE_NAME);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

}

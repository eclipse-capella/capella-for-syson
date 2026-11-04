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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.PayloadFeature;
import org.eclipse.syson.sysml.SysmlPackage;
import org.springframework.stereotype.Service;

/**
 * Provide the functional exchange exchangedItem reference widget content.
 *
 * @author fbarbin
 */
@Service
public class ExchangedItemPayloadReferenceWidgetProvider implements ICapellaReferenceWidgetProvider {

    public static final String WIDGET_NAME = "FunctionalExchangeExchangeItemWidget";

    public static final String PAYLOAD_FEATURE = "exchangedItems";

    private static final String ERROR_MSG = "Something went wrong while deleting the exchange item payload";

    private final CommonQueryService commonQueryService;

    private final CommonDeletionService commonDeletionService;

    private final CommonUpdateService commonUpdateService;

    public ExchangedItemPayloadReferenceWidgetProvider() {
        this.commonQueryService = new CommonQueryService();
        this.commonDeletionService = new CommonDeletionService();
        this.commonUpdateService = new CommonUpdateService();
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
            var allExchangeItems = new ArrayList<>(this.commonQueryService.getExchangeItems(eObject));
            allExchangeItems.removeIf(this.commonQueryService::isFunctionPort);
            return allExchangeItems;
        }
        return List.of();
    }

    @Override
    public List<?> getReferenceValue(ReferenceWidgetDescription referenceDescription, AQLInterpreter interpreter, VariableManager variableManager) {
        Object object = variableManager.getVariables().get(VariableManager.SELF);
        if (object instanceof FlowUsage flowUsage) {
            return this.getPayloadValues(flowUsage);
        }
        return List.of();
    }

    @Override
    public IStatus handleAdd(ReferenceWidgetDescription referenceDescription, EObject owner, List<Object> newValues) {
        if (owner instanceof FlowUsage flowUsage && newValues.stream().allMatch(ItemUsage.class::isInstance)) {
            var currentValues = this.getPayloadValues(flowUsage);
            // Keep the setter's single-value ordering; multiple selections require the complete reference value.
            var values = newValues;
            if (newValues.size() > 1) {
                values = Stream.concat(currentValues.stream(), newValues.stream()).distinct().toList();
            }
            if (newValues.stream().anyMatch(value -> !currentValues.contains(value))) {
                this.commonUpdateService.setFunctionalExchangePayload(flowUsage, values);
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
        if (owner instanceof FlowUsage flowUsage && value instanceof ItemUsage itemUsage) {
            flowUsage.getPayloadFeature().getOwnedSpecialization().stream()
                    .filter(FeatureTyping.class::isInstance)
                    .map(FeatureTyping.class::cast)
                    .filter(typing -> itemUsage.equals(typing.getType()))
                    .forEach(this.commonDeletionService::delete);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

    @Override
    public EClass getType() {
        return SysmlPackage.eINSTANCE.getItemUsage();
    }

    private List<?> getPayloadValues(FlowUsage flowUsage) {
        return Optional.ofNullable(flowUsage.getPayloadFeature()).stream().map(PayloadFeature::getType).flatMap(List::stream)
                .filter(this.commonQueryService::isExchangeItem).toList();
    }

    @Override
    public IStatus handleClear(ReferenceWidgetDescription referenceDescription, EObject owner) {
        if (owner instanceof FlowUsage flowUsage) {
            Optional.ofNullable(flowUsage.getPayloadFeature()).ifPresent(this.commonDeletionService::delete);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }
        return new Failure(ERROR_MSG);
    }

}

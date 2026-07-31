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
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.interpreter.Result;
import org.eclipse.sirius.components.interpreter.StringValueProvider;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.MessageLevel;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.Operation;
import org.eclipse.sirius.components.view.emf.operations.api.IOperationExecutor;
import org.eclipse.sirius.components.view.emf.operations.api.OperationExecutionStatus;
import org.eclipse.sirius.components.view.emf.widget.reference.ReferenceWidgetBehaviorConverter;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription.Builder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * A Specific Capella ReferenceWidgetBehaviorConverter to implement Capella reference widgets specific behavior.
 *
 * @author fbarbin
 */
@Service
@Primary
public class CapellaReferenceWidgetBehaviorConverter extends ReferenceWidgetBehaviorConverter {

    private final IOperationExecutor operationExecutor;

    private final IFeedbackMessageService feedbackMessageService;

    private final List<ICapellaReferenceWidgetProvider> capellaReferenceWidgetProviders;

    public CapellaReferenceWidgetBehaviorConverter(IOperationExecutor operationExecutor, IFeedbackMessageService feedbackMessageService,
            List<ICapellaReferenceWidgetProvider> capellaReferenceWidgetProviders) {
        super(operationExecutor, feedbackMessageService);
        this.operationExecutor = Objects.requireNonNull(operationExecutor);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.capellaReferenceWidgetProviders = Objects.requireNonNull(capellaReferenceWidgetProviders);
    }

    @Override
    public void convert(Builder referenceWidgetDescriptionBuilder, ReferenceWidgetDescription viewReferenceWidgetDescription, AQLInterpreter interpreter) {
        if (viewReferenceWidgetDescription.getName().startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX)) {

            referenceWidgetDescriptionBuilder.clearHandlerProvider(variableManager -> this.handleClearReference(interpreter, variableManager, viewReferenceWidgetDescription))
                    .itemRemoveHandlerProvider(variableManager -> this.handleItemRemove(interpreter, variableManager, viewReferenceWidgetDescription))
                    .moveHandlerProvider(variableManager -> this.handleMoveReferenceValue(interpreter, variableManager, viewReferenceWidgetDescription));

            if (viewReferenceWidgetDescription.getHelpExpression() != null && !viewReferenceWidgetDescription.getHelpExpression().isBlank()) {
                referenceWidgetDescriptionBuilder.helpTextProvider(new StringValueProvider(interpreter, Optional.ofNullable(viewReferenceWidgetDescription.getHelpExpression()).orElse("")));
            }
            if (!viewReferenceWidgetDescription.getBody().isEmpty()) {
                referenceWidgetDescriptionBuilder.setHandlerProvider(variableManager -> this.newValueHandler(interpreter, variableManager, viewReferenceWidgetDescription.getBody()));
                referenceWidgetDescriptionBuilder.addHandlerProvider(variableManager -> this.newValueHandler(interpreter, variableManager, viewReferenceWidgetDescription.getBody()));
            }
        }

        else {
            super.convert(referenceWidgetDescriptionBuilder, viewReferenceWidgetDescription, interpreter);
        }

    }

    private EObject getReferenceOwner(AQLInterpreter interpreter, VariableManager variableManager, String referenceOwnerExpression) {
        String safeValueExpression = Optional.ofNullable(referenceOwnerExpression).orElse("");
        EObject referenceOwner = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (!safeValueExpression.isBlank()) {
            Result result = interpreter.evaluateExpression(variableManager.getVariables(), safeValueExpression);
            referenceOwner = result.asObject().filter(EObject.class::isInstance).map(EObject.class::cast).orElse(referenceOwner);
        }
        return referenceOwner;
    }

    private IStatus handleClearReference(AQLInterpreter interpreter, VariableManager variableManager, ReferenceWidgetDescription referenceDescription) {
        EObject owner = this.getReferenceOwner(interpreter, variableManager, referenceDescription.getReferenceOwnerExpression());
        VariableManager childVariableManager = variableManager.createChild();
        childVariableManager.put(VariableManager.SELF, owner);
        return this.capellaReferenceWidgetProviders.stream()
                .filter(provider -> provider.canHandle(referenceDescription))
                .findFirst()
                .map(provider -> provider.handleClearReference(referenceDescription, interpreter, childVariableManager))
                .orElse(new Failure("Something went wrong while deleting the element."));
    }

    private IStatus handleItemRemove(AQLInterpreter interpreter, VariableManager variableManager, ReferenceWidgetDescription referenceDescription) {
        EObject owner = this.getReferenceOwner(interpreter, variableManager, referenceDescription.getReferenceOwnerExpression());
        VariableManager childVariableManager = variableManager.createChild();
        childVariableManager.put(VariableManager.SELF, owner);
        return this.capellaReferenceWidgetProviders.stream()
                .filter(provider -> provider.canHandle(referenceDescription))
                .findFirst()
                .map(provider -> provider.handleItemRemoved(referenceDescription, interpreter, childVariableManager))
                .orElse(new Failure("Something went wrong while deleting the element."));

    }

    private IStatus handleMoveReferenceValue(AQLInterpreter interpreter, VariableManager variableManager, ReferenceWidgetDescription referenceDescription) {
        // @technical-debt
        // Not implemented yet
        return new Failure("The move reference value is not yet implemented in Capella.");
    }

    private IStatus newValueHandler(AQLInterpreter interpreter, VariableManager variableManager, List<Operation> operations) {
        var result = this.operationExecutor.execute(interpreter, variableManager, operations);
        if (result.status() == OperationExecutionStatus.FAILURE) {
            List<Message> errorMessages = new ArrayList<>();
            errorMessages.add(new Message("Something went wrong while setting the reference value.", MessageLevel.ERROR));
            errorMessages.addAll(this.feedbackMessageService.getFeedbackMessages());
            return new Failure(errorMessages);
        }
        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of(), this.feedbackMessageService.getFeedbackMessages());
    }


}

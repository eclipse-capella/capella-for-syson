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

import java.util.Optional;

import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.interpreter.StringValueProvider;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.view.emf.widget.reference.ReferenceWidgetBehaviorConverter;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription.Builder;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Preserves Capella help text and unsupported reordering; mutations are handled by CapellaReferenceWidgetHandler.
 *
 * @author fbarbin
 */
@Service
@Primary
public class CapellaReferenceWidgetBehaviorConverter extends ReferenceWidgetBehaviorConverter {

    public CapellaReferenceWidgetBehaviorConverter(IFeedbackMessageService feedbackMessageService) {
        super(feedbackMessageService);
    }

    @Override
    public void convert(Builder referenceWidgetDescriptionBuilder, ReferenceWidgetDescription viewReferenceWidgetDescription, AQLInterpreter interpreter) {
        if (viewReferenceWidgetDescription.getName().startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX)) {
            referenceWidgetDescriptionBuilder.moveHandlerProvider(variableManager -> new Failure("The move reference value is not yet implemented in Capella."));
            if (viewReferenceWidgetDescription.getHelpExpression() != null && !viewReferenceWidgetDescription.getHelpExpression().isBlank()) {
                referenceWidgetDescriptionBuilder.helpTextProvider(new StringValueProvider(interpreter, Optional.ofNullable(viewReferenceWidgetDescription.getHelpExpression()).orElse("")));
            }
        } else {
            super.convert(referenceWidgetDescriptionBuilder, viewReferenceWidgetDescription, interpreter);
        }
    }
}

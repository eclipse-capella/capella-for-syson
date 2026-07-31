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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.capella.application.configuration.label.services.CapellaImagePathsService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.interpreter.Result;
import org.eclipse.sirius.components.interpreter.StringValueProvider;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.emf.widget.reference.ReferenceWidgetPropertiesConverter;
import org.eclipse.sirius.components.view.emf.widget.reference.ReferenceWidgetStyleProvider;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescriptionStyle;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetComponent;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription.Builder;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetStyle;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * A specific Capella ReferenceWidgetPropertiesConverter to handle Arcadia references.
 *
 * @author fbarbin
 */
@Service
@Primary
public class CapellaReferenceWidgetPropertiesConverter extends ReferenceWidgetPropertiesConverter {

    private final IIdentityService identityService;

    private final IReadOnlyObjectPredicate readOnlyObjectPredicate;

    private final ILabelService labelService;

    private final IEMFKindService emfKindService;

    private final List<ICapellaReferenceWidgetProvider> capellaReferenceWidgetProviders;

    private final CapellaImagePathsService capellaImagePathsService;

    public CapellaReferenceWidgetPropertiesConverter(IIdentityService identityService, IReadOnlyObjectPredicate readOnlyObjectPredicate, ILabelService labelService,
            IEMFKindService emfKindService, List<ICapellaReferenceWidgetProvider> capellaReferenceWidgetProviders,
            CapellaImagePathsService capellaImagePathsService) {
        super(identityService, readOnlyObjectPredicate, labelService, emfKindService);
        this.identityService = Objects.requireNonNull(identityService);
        this.readOnlyObjectPredicate = Objects.requireNonNull(readOnlyObjectPredicate);
        this.labelService = Objects.requireNonNull(labelService);
        this.emfKindService = Objects.requireNonNull(emfKindService);
        this.capellaReferenceWidgetProviders = Objects.requireNonNull(capellaReferenceWidgetProviders);
        this.capellaImagePathsService = Objects.requireNonNull(capellaImagePathsService);
    }

    @Override
    public void convert(Builder referenceWidgetDescriptionBuilder, ReferenceWidgetDescription viewReferenceWidgetDescription, AQLInterpreter interpreter) {
        if (viewReferenceWidgetDescription.getName().startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX)) {

            Function<VariableManager, String> semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                    .map(this.identityService::getId)
                    .orElse(null);

            referenceWidgetDescriptionBuilder
                    .idProvider(new WidgetIdProvider())
                    .targetObjectIdProvider(semanticTargetIdProvider)
                    .labelProvider(new StringValueProvider(interpreter, viewReferenceWidgetDescription.getLabelExpression()))
                    .iconURLProvider(variableManager -> List.of())
                    .isReadOnlyProvider(this.getReadOnlyValueProvider(interpreter, viewReferenceWidgetDescription.getIsEnabledExpression()))
                    .itemsProvider(variableManager -> this.getReferenceValue(interpreter, viewReferenceWidgetDescription, variableManager))
                    .optionsProvider(variableManager -> this.getReferenceOptions(interpreter, viewReferenceWidgetDescription, variableManager))
                    .itemIdProvider(this::getItemId)
                    .itemKindProvider(this::getItemKind)
                    .itemLabelProvider(this::getItemLabel)
                    .itemIconURLProvider(this::getItemIconURL)
                    .ownerKindProvider(this::getOwnerKind)
                    .referenceKindProvider(variableManager -> this.getReferenceKind(interpreter, variableManager, viewReferenceWidgetDescription))
                    .isContainmentProvider(variableManager -> this.isContainment(interpreter, variableManager, viewReferenceWidgetDescription))
                    .isManyProvider(variableManager -> this.isMany(interpreter, variableManager, viewReferenceWidgetDescription))
                    .ownerIdProvider(variableManager -> this.getOwnerId(interpreter, viewReferenceWidgetDescription, variableManager))
                    .styleProvider(this.getStyleProvider(viewReferenceWidgetDescription, interpreter))
                    .diagnosticsProvider(variableManager -> List.of())
                    .kindProvider(object -> "")
                    .messageProvider(object -> "");
        } else {
            super.convert(referenceWidgetDescriptionBuilder, viewReferenceWidgetDescription, interpreter);
        }
    }

    private Function<VariableManager, Boolean> getReadOnlyValueProvider(AQLInterpreter interpreter, String expression) {
        return variableManager -> {
            boolean isReadOnly = variableManager.get(VariableManager.SELF, Object.class)
                    .filter(this.readOnlyObjectPredicate)
                    .isPresent();

            if (!isReadOnly && expression != null && !expression.isBlank()) {
                Result result = interpreter.evaluateExpression(variableManager.getVariables(), expression);
                isReadOnly = result.asBoolean()
                        .map(value -> !value)
                        .orElse(Boolean.FALSE);
            }
            return isReadOnly;
        };
    }

    private List<?> getReferenceValue(AQLInterpreter interpreter, ReferenceWidgetDescription referenceDescription, VariableManager variableManager) {
        EObject owner = this.getReferenceOwner(interpreter, variableManager, referenceDescription.getReferenceOwnerExpression());
        VariableManager childVariableManager = variableManager.createChild();
        childVariableManager.put(VariableManager.SELF, owner);
        return this.capellaReferenceWidgetProviders.stream()
                .filter(provider -> provider.canHandle(referenceDescription))
                .findFirst()
                .map(provider -> provider.getReferenceValue(referenceDescription, interpreter, childVariableManager))
                .orElse(List.of());

    }

    private List<?> getReferenceOptions(AQLInterpreter interpreter, ReferenceWidgetDescription referenceDescription, VariableManager variableManager) {
        EObject owner = this.getReferenceOwner(interpreter, variableManager, referenceDescription.getReferenceOwnerExpression());
        VariableManager childVariableManager = variableManager.createChild();
        childVariableManager.put(VariableManager.SELF, owner);
        return this.capellaReferenceWidgetProviders.stream()
                .filter(provider -> provider.canHandle(referenceDescription))
                .findFirst()
                .map(provider -> provider.getReferenceOptions(referenceDescription, interpreter, childVariableManager))
                .orElse(List.of());
    }

    private EObject getReferenceOwner(AQLInterpreter interpreter, VariableManager variableManager, String referenceOwnerExpression) {
        String safeValueExpression = Optional.ofNullable(referenceOwnerExpression).orElse("");
        EObject referenceOwner = variableManager.get(VariableManager.SELF, EObject.class).orElse(null);
        if (!safeValueExpression.isBlank()) {
            Result result = interpreter.evaluateExpression(variableManager.getVariables(), safeValueExpression);
            referenceOwner = result.asObject()
                    .filter(EObject.class::isInstance)
                    .map(EObject.class::cast)
                    .orElse(referenceOwner);
        }
        return referenceOwner;
    }

    private Optional<Object> getItem(VariableManager variableManager) {
        return variableManager.get(ReferenceWidgetComponent.ITEM_VARIABLE, Object.class);
    }

    private String getItemId(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.identityService::getId).orElse("");
    }

    private String getItemKind(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.identityService::getKind).orElse("");
    }

    private String getItemLabel(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.labelService::getStyledLabel).map(Object::toString).orElse("");
    }

    private List<String> getItemIconURL(VariableManager variableManager) {
        return this.getItem(variableManager).map(this.capellaImagePathsService::getImagePaths).orElse(List.of());
    }

    private String getOwnerKind(VariableManager variableManager) {
        return variableManager.get(VariableManager.SELF, EObject.class)
                .map(self -> this.emfKindService.getKind(self.eClass()))
                .orElse("");
    }

    private String getReferenceKind(AQLInterpreter interpreter, VariableManager variableManager, ReferenceWidgetDescription referenceDescription) {
        return this.capellaReferenceWidgetProviders.stream()
                .filter(provider -> provider.canHandle(referenceDescription))
                .findFirst()
                .map(ICapellaReferenceWidgetProvider::getType)
                .map(this.emfKindService::getKind)
                .orElse("");
    }

    private boolean isContainment(AQLInterpreter interpreter, VariableManager variableManager, ReferenceWidgetDescription referenceDescription) {
        return false;
    }

    private boolean isMany(AQLInterpreter interpreter, VariableManager variableManager, ReferenceWidgetDescription referenceDescription) {
        return this.capellaReferenceWidgetProviders.stream()
                .filter(provider -> provider.canHandle(referenceDescription))
                .findFirst()
                .map(ICapellaReferenceWidgetProvider::isMany)
                .orElse(false);
    }

    private String getOwnerId(AQLInterpreter interpreter, ReferenceWidgetDescription referenceDescription, VariableManager variableManager) {
        EObject owner = this.getReferenceOwner(interpreter, variableManager, referenceDescription.getReferenceOwnerExpression());
        return this.identityService.getId(owner);
    }

    private Function<VariableManager, ReferenceWidgetStyle> getStyleProvider(ReferenceWidgetDescription viewReferenceWidgetDescription, AQLInterpreter interpreter) {
        return variableManager -> {
            var effectiveStyle = viewReferenceWidgetDescription.getConditionalStyles().stream()
                    .filter(style -> interpreter.evaluateExpression(variableManager.getVariables(), style.getCondition()).asBoolean().orElse(Boolean.FALSE))
                    .map(ReferenceWidgetDescriptionStyle.class::cast)
                    .findFirst()
                    .orElseGet(viewReferenceWidgetDescription::getStyle);
            if (effectiveStyle == null) {
                return null;
            }
            return new ReferenceWidgetStyleProvider(effectiveStyle).apply(variableManager);
        };
    }
}

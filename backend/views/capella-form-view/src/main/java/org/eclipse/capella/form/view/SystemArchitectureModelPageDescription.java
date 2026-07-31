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
package org.eclipse.capella.form.view;

import org.eclipse.capella.form.view.util.CapellaViewFormService;
import org.eclipse.capella.form.view.util.WidgetDescriptionBuilderHelper;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.forms.ContainerBorderStyle;
import org.eclipse.sirius.components.forms.ContainerBorderLineStyle;
import org.eclipse.sirius.components.forms.FlexDirection;
import org.eclipse.sirius.components.forms.GroupDisplayMode;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.forms.description.AbstractWidgetDescription;
import org.eclipse.sirius.components.forms.description.FlexboxContainerDescription;
import org.eclipse.sirius.components.forms.description.GroupDescription;
import org.eclipse.sirius.components.forms.description.LabelDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Used to provide the page description for the information displayed in the form.
 *
 * @technical-debt This implementation has been developed in the context of a POC.
 * As such, it focuses on validating functional ideas rather than providing a fully
 * generic, extensible, or optimized solution. Several parts of this service rely
 * on hard-coded concepts, assumptions on SysML/Arcadia structures, and duplicated
 * traversal logic, which may limit reuse and scalability. A future industrialization
 * phase should consider refactoring toward more generic mechanisms, improved separation
 * of concerns, and better configurability.
 *
 * @author ntinsalhi
 */
@Service
public class SystemArchitectureModelPageDescription {

    public static final String PIE_CHART_WIDGET_ID = "concepts_pie_chart_widget";

    public static final String PIE_CHART_LAYER_WIDGET_ID = "layer_pie_chart_widget";

    public static final String BAR_CHART_WIDGET_ID = "bar_chart_widget";

    public static final String MODEL_SYSTEM_ARCHITECTURE_LABEL_ID = "model_system_architecture_label";

    public static final String MODEL_QUALITATIVE_ANALYSIS_GROUP_ID = "model_qualitative_analysis_group";

    public static final String PIE_CHARTS_ID = "pie_charts";

    public static final String BAR_CHARTS_ID = "bar_charts";

    public static final String SYSTEM_ARCHITECTURE_PAGE_ID = "system_architecture_page";

    public static final List<String> ARCHITECTURE_LAYERS = List.of("Operational", "System", "Logical", "Physical");

    private final IIdentityService identityService;

    private final CapellaViewFormService capellaViewFormService;

    private final WidgetDescriptionBuilderHelper widgetDescriptionBuilderHelper;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    public SystemArchitectureModelPageDescription(IIdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);
        this.capellaViewFormService = new CapellaViewFormService();
        this.widgetDescriptionBuilderHelper = new WidgetDescriptionBuilderHelper(this.identityService);
        this.semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.identityService::getId)
                .orElse(null);
    }

    PageDescription getSystemArchitectureModelPageDescription() {

        GroupDescription getModelSystemArchitectureLabelGroup = GroupDescription.newGroupDescription(MODEL_SYSTEM_ARCHITECTURE_LABEL_ID)
                .idProvider(variableManager -> MODEL_SYSTEM_ARCHITECTURE_LABEL_ID)
                .labelProvider(variableManager -> "")
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .controlDescriptions(List.of(this.getModelSystemArchitectureLabel()))
                .displayModeProvider(variableManager -> GroupDisplayMode.LIST)
                .build();

        GroupDescription modelQualitativeAnalysisGroup = GroupDescription.newGroupDescription(MODEL_QUALITATIVE_ANALYSIS_GROUP_ID)
                .idProvider(variableManager -> MODEL_QUALITATIVE_ANALYSIS_GROUP_ID)
                .labelProvider(variableManager -> "")
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .controlDescriptions(List.of(this.getModelQualitativeAnalysisContainer()))
                .displayModeProvider(variableManager -> GroupDisplayMode.LIST)
                .build();

        GroupDescription pieChartsGroup = GroupDescription.newGroupDescription(PIE_CHARTS_ID)
                .idProvider(variableManager -> PIE_CHARTS_ID)
                .labelProvider(variableManager -> "")
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .controlDescriptions(List.of(this.getPieChartsContainer()))
                .displayModeProvider(variableManager -> GroupDisplayMode.LIST)
                .build();

        GroupDescription barChartGroup = GroupDescription.newGroupDescription(BAR_CHARTS_ID)
                .idProvider(variableManager -> BAR_CHARTS_ID)
                .labelProvider(variableManager -> "")
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .controlDescriptions(List.of(this.getBarChartContainer()))
                .displayModeProvider(variableManager -> GroupDisplayMode.LIST)
                .build();

        return PageDescription.newPageDescription(SYSTEM_ARCHITECTURE_PAGE_ID)
                .idProvider(variableManager -> SYSTEM_ARCHITECTURE_PAGE_ID)
                .labelProvider(variableManager -> "")
                .semanticElementsProvider(variableManager -> Collections.singletonList(variableManager.getVariables().get(VariableManager.SELF)))
                .groupDescriptions(List.of(getModelSystemArchitectureLabelGroup, modelQualitativeAnalysisGroup, pieChartsGroup, barChartGroup))
                .canCreatePredicate(variableManager -> true)
                .build();
    }

    private AbstractWidgetDescription getModelSystemArchitectureLabel() {

        LabelDescription titleLabelDescription = widgetDescriptionBuilderHelper.buildLabelDescription(
                "Model Analysis",
                44);
        LabelDescription subTitleLabelDescription = widgetDescriptionBuilderHelper.buildColoredLabelDescription(
                "Overview of the model's quality and progression",
                14,
                "#797592");

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.column)
                .children(List.of(titleLabelDescription, subTitleLabelDescription))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(0)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();
    }

    private AbstractWidgetDescription getPieChartsContainer() {
        AbstractWidgetDescription conceptsRepartitionPieChart = this.widgetDescriptionBuilderHelper.buildPieChartFlexBox(
                PIE_CHART_WIDGET_ID,
                "Concepts Repartition in Model",
                this.capellaViewFormService::getConceptsRepartitionPieChartKeys,
                this.capellaViewFormService::getConceptsRepartitionPieChartValues
        );

        AbstractWidgetDescription conceptsRepartitionPerLayerPieChart = this.widgetDescriptionBuilderHelper.buildPieChartFlexBox(
                PIE_CHART_LAYER_WIDGET_ID,
                "Concepts Repartition Per Layer",
                variableManager -> ARCHITECTURE_LAYERS,
                this.capellaViewFormService::getConceptsRepartitionPerLayerPieChartValues
        );

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.row)
                .children(List.of(conceptsRepartitionPieChart, conceptsRepartitionPerLayerPieChart))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(0)
                        .color("#D3D3D3")
                        .radius(6)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();
    }

    private AbstractWidgetDescription getBarChartContainer() {
        LabelDescription labelDescription = widgetDescriptionBuilderHelper.buildLabelDescription("Review status per Architecture", 16);
        FlexboxContainerDescription upperFlexBox = getUpperStatusBarChartsContainerDescription();
        FlexboxContainerDescription lowerFlexBox = getLowerStatusBarChartsContainerDescription();

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.column)
                .children(List.of(labelDescription, upperFlexBox, lowerFlexBox))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(2)
                        .color("#D3D3D3")
                        .radius(6)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();
    }

    private FlexboxContainerDescription getUpperStatusBarChartsContainerDescription() {
        AbstractWidgetDescription openBarChart = widgetDescriptionBuilderHelper.buildArchitectureBarChartFlexBox(
                BAR_CHART_WIDGET_ID + "_open",
                "Open",
                "#1E90FF",
                variableManager -> ARCHITECTURE_LAYERS,
                variableManager ->
                        this.capellaViewFormService.getConceptsRepartitionPerStatusLayerPieChartValues(variableManager, "open")
        );

        AbstractWidgetDescription tbdBarChart = widgetDescriptionBuilderHelper.buildArchitectureBarChartFlexBox(
                BAR_CHART_WIDGET_ID + "_tbd",
                "To Be Determined",
                "#A0A0A0",
                variableManager -> ARCHITECTURE_LAYERS,
                variableManager ->
                        this.capellaViewFormService.getConceptsRepartitionPerStatusLayerPieChartValues(variableManager, "tbd")
        );

        AbstractWidgetDescription tbrBarChart = widgetDescriptionBuilderHelper.buildArchitectureBarChartFlexBox(
                BAR_CHART_WIDGET_ID + "_tbr",
                "To Be Resolved",
                "#FF8C00",
                variableManager -> ARCHITECTURE_LAYERS,
                variableManager ->
                        this.capellaViewFormService.getConceptsRepartitionPerStatusLayerPieChartValues(variableManager, "tbr")
        );

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.row)
                .children(List.of(openBarChart, tbdBarChart, tbrBarChart))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(0)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();
    }

    private FlexboxContainerDescription getLowerStatusBarChartsContainerDescription() {
        AbstractWidgetDescription tbcBarChart = widgetDescriptionBuilderHelper.buildArchitectureBarChartFlexBox(
                BAR_CHART_WIDGET_ID + "_tbc",
                "to Be Confirmed",
                "#FFD700",
                variableManager -> ARCHITECTURE_LAYERS,
                variableManager ->
                        this.capellaViewFormService.getConceptsRepartitionPerStatusLayerPieChartValues(variableManager, "tbc")
        );

        AbstractWidgetDescription doneBarChart = widgetDescriptionBuilderHelper.buildArchitectureBarChartFlexBox(
                BAR_CHART_WIDGET_ID + "_done",
                "Done",
                "#008000",
                variableManager -> ARCHITECTURE_LAYERS,
                variableManager ->
                        this.capellaViewFormService.getConceptsRepartitionPerStatusLayerPieChartValues(variableManager, "done")
        );

        AbstractWidgetDescription closedBarChart = widgetDescriptionBuilderHelper.buildArchitectureBarChartFlexBox(
                BAR_CHART_WIDGET_ID + "_closed",
                "Closed",
                "#003366",
                variableManager -> ARCHITECTURE_LAYERS,
                variableManager ->
                        this.capellaViewFormService.getConceptsRepartitionPerStatusLayerPieChartValues(variableManager, "closed")
        );

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.row)
                .children(List.of(tbcBarChart, doneBarChart, closedBarChart))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(0)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();
    }

    private AbstractWidgetDescription getModelQualitativeAnalysisContainer() {
        LabelDescription labelDescription = widgetDescriptionBuilderHelper.buildLabelDescription("Model Qualitative Analysis", 16);
        FlexboxContainerDescription upperFlexBox = FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.row)
                .children(List.of(this.getNonAllocatedFunctionsFlexBox(), this.getPortsWithExchangesFlexBox(), this.getNonAllocatedFunctionalExchangesFlexBox(), this.getValidatedComponentsFlexBox()))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(0)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.column)
                .children(List.of(labelDescription, upperFlexBox))
                .borderStyleProvider(variableManager -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(2)
                        .color("#D3D3D3")
                        .radius(6)
                        .build())
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(object -> "")
                .messageProvider(object -> "")
                .build();
    }

    private AbstractWidgetDescription getNonAllocatedFunctionsFlexBox() {
        return widgetDescriptionBuilderHelper.buildModelAnalysisFlexBox(
                "Unallocated Functions",
                16,
                "Functions not allocated to a component",
                this.capellaViewFormService::getNonAllocatedFunctionWidgetValue,
                this.capellaViewFormService::getNonAllocatedFunctionsColor
        );
    }

    private AbstractWidgetDescription getPortsWithExchangesFlexBox() {
        return widgetDescriptionBuilderHelper.buildModelAnalysisFlexBox(
                "Ports Without Exchange",
                16,
                "Functional or component ports not connected",
                this.capellaViewFormService::getPortsWithNoExchangeWidgetValue,
                this.capellaViewFormService::getPortsWithNoExchangeColor
        );
    }

    private AbstractWidgetDescription getNonAllocatedFunctionalExchangesFlexBox() {
        return widgetDescriptionBuilderHelper.buildModelAnalysisFlexBox(
                "Unallocated Functional Exchanges",
                16,
                "Functional exchanges without component exchange allocation",
                this.capellaViewFormService::getNonAllocatedFunctionalExchangesWidgetValue,
                this.capellaViewFormService::getNonAllocatedFunctionalExchanges
        );
    }

    private AbstractWidgetDescription getValidatedComponentsFlexBox() {
        return widgetDescriptionBuilderHelper.buildModelAnalysisFlexBox(
                "Validated Components",
                16,
                "Components with full review",
                this.capellaViewFormService::getValidatedComponents,
                this.capellaViewFormService::getValidatedComponentsColor
        );
    }
}

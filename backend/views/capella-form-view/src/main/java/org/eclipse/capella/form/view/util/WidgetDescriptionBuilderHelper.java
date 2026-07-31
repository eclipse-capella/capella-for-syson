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
package org.eclipse.capella.form.view.util;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.eclipse.sirius.components.charts.barchart.components.BarChartStyle;
import org.eclipse.sirius.components.charts.barchart.descriptions.BarChartDescription;
import org.eclipse.sirius.components.charts.piechart.PieChartDescription;
import org.eclipse.sirius.components.charts.piechart.components.PieChartStyle;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.forms.ContainerBorderLineStyle;
import org.eclipse.sirius.components.forms.ContainerBorderStyle;
import org.eclipse.sirius.components.forms.FlexDirection;
import org.eclipse.sirius.components.forms.LabelWidgetStyle;
import org.eclipse.sirius.components.forms.WidgetIdProvider;
import org.eclipse.sirius.components.forms.description.AbstractWidgetDescription;
import org.eclipse.sirius.components.forms.description.ChartWidgetDescription;
import org.eclipse.sirius.components.forms.description.FlexboxContainerDescription;
import org.eclipse.sirius.components.forms.description.LabelDescription;
import org.eclipse.sirius.components.representations.VariableManager;

/**
 * Helper for building widget descriptions.
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
public class WidgetDescriptionBuilderHelper {

    private final IIdentityService identityService;

    private final Function<VariableManager, String> semanticTargetIdProvider;

    public WidgetDescriptionBuilderHelper(IIdentityService identityService) {
        this.identityService = Objects.requireNonNull(identityService);

        this.semanticTargetIdProvider = variableManager -> variableManager.get(VariableManager.SELF, Object.class)
                .map(this.identityService::getId)
                .orElse(null);
    }

    public LabelDescription buildLabelDescription(String value, int size) {
        LabelWidgetStyle labelWidgetStyle = LabelWidgetStyle.newLabelWidgetStyle()
                .fontSize(size)
                .italic(false)
                .bold(true)
                .underline(false)
                .strikeThrough(false)
                .build();

        return LabelDescription.newLabelDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .valueProvider(variableManager -> value)
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(diagnostic -> "")
                .messageProvider(diagnostic -> "")
                .styleProvider(variableManager -> labelWidgetStyle)
                .build();
    }

    public LabelDescription buildColoredLabelDescription(String value, int size, String color) {
        LabelWidgetStyle labelWidgetStyle = LabelWidgetStyle.newLabelWidgetStyle()
                .fontSize(size)
                .italic(false)
                .bold(true)
                .underline(false)
                .strikeThrough(false)
                .color(color)
                .build();

        return LabelDescription.newLabelDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .valueProvider(variableManager -> value)
                .diagnosticsProvider(variableManager -> List.of())
                .kindProvider(diagnostic -> "")
                .messageProvider(diagnostic -> "")
                .styleProvider(variableManager -> labelWidgetStyle)
                .build();
    }

    public AbstractWidgetDescription buildModelAnalysisFlexBox(String title, int titleFontSize, String subTitle, Function<VariableManager, ComponentProgress> valueProvider, Function<VariableManager, String> colorProvider) {
        LabelDescription titleLabelDescription = buildLabelDescription(title, titleFontSize);
        LabelDescription subTitleLabelDescription = buildColoredLabelDescription(subTitle, 10, "#797592");
        LabelDescription trendLabelDescription = buildColoredLabelDescription("Trend: Stable", 12, "#797592");

        LabelWidgetStyle.Builder valueStyle = LabelWidgetStyle.newLabelWidgetStyle()
                .fontSize(45)
                .italic(false)
                .bold(true)
                .underline(false)
                .strikeThrough(false);

        LabelDescription valueLabel = LabelDescription.newLabelDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(semanticTargetIdProvider)
                .labelProvider(vm -> "")
                .valueProvider(vm -> valueProvider.apply(vm).toString())
                .diagnosticsProvider(vm -> List.of())
                .kindProvider(diagnostic -> "")
                .messageProvider(diagnostic -> "")
                .styleProvider(vm -> valueStyle.color(colorProvider.apply(vm)).build())
                .build();

        ContainerBorderStyle.Builder containerBorderStyle = ContainerBorderStyle.newContainerBorderStyle()
                .lineStyle(ContainerBorderLineStyle.Solid)
                .size(2)
                .radius(6);

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(semanticTargetIdProvider)
                .labelProvider(vm -> "")
                .flexDirection(FlexDirection.column)
                .children(List.of(titleLabelDescription, subTitleLabelDescription, valueLabel, trendLabelDescription))
                .borderStyleProvider(vm -> containerBorderStyle.color(colorProvider.apply(vm)).build())
                .diagnosticsProvider(vm -> List.of())
                .kindProvider(obj -> "")
                .messageProvider(obj -> "")
                .build();
    }

    public AbstractWidgetDescription buildArchitectureBarChartFlexBox(String barChartId, String status, String color, Function<VariableManager, List<String>> keysProvider, Function<VariableManager, List<Number>> valuesProvider) {
        LabelDescription titleLabel = buildLabelDescription(status, 20);

        BarChartDescription barChartDescription = BarChartDescription.newBarChartDescription(barChartId)
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .label("bar chart - " + status)
                .labelProvider(variableManager -> "")
                .yAxisLabelProvider(variableManager -> "")
                .styleProvider(variableManager -> BarChartStyle
                        .newBarChartStyle()
                        .barsColor(color)
                        .fontSize(8)
                        .build())

                .keysProvider(keysProvider)
                .valuesProvider(valuesProvider)
                .width(250)
                .height(250)
                .build();

        ChartWidgetDescription chartWidgetDescription = ChartWidgetDescription.newChartWidgetDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .kindProvider(object -> "")
                .labelProvider(variableManager -> "")
                .iconURLProvider(variableManager -> List.of())
                .chartDescription(barChartDescription)
                .diagnosticsProvider(variableManager -> List.of())
                .messageProvider(object -> "")
                .build();

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(semanticTargetIdProvider)
                .labelProvider(vm -> "")
                .flexDirection(FlexDirection.column)
                .children(List.of(titleLabel, chartWidgetDescription))
                .borderStyleProvider(vm -> ContainerBorderStyle.newContainerBorderStyle()
                        .lineStyle(ContainerBorderLineStyle.Solid)
                        .size(0)
                        .color("#D3D3D3")
                        .radius(6)
                        .build())
                .diagnosticsProvider(vm -> List.of())
                .kindProvider(obj -> "")
                .messageProvider(obj -> "")
                .build();
    }

    public AbstractWidgetDescription buildPieChartFlexBox(String pieChartId, String title, Function<VariableManager, List<String>> keysProvider, Function<VariableManager, List<Number>> valuesProvider) {
        LabelDescription labelDescription = this.buildLabelDescription(title, 16);
        PieChartDescription pieChartDescription = PieChartDescription.newPieChartDescription(pieChartId)
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .label("Pie Chart")
                .styleProvider(variableManager -> PieChartStyle
                        .newPieChartStyle()
                        .fontSize(12)
                        .bold(true)
                        .strikeThrough(false)
                        .build())
                .keysProvider(keysProvider)
                .valuesProvider(valuesProvider)
                .build();

        ChartWidgetDescription chartWidgetDescription = ChartWidgetDescription.newChartWidgetDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .kindProvider(object -> "")
                .labelProvider(variableManager -> "")
                .iconURLProvider(variableManager -> List.of())
                .chartDescription(pieChartDescription)
                .diagnosticsProvider(variableManager -> List.of())
                .messageProvider(object -> "")
                .build();

        return FlexboxContainerDescription.newFlexboxContainerDescription(UUID.randomUUID().toString())
                .idProvider(new WidgetIdProvider())
                .targetObjectIdProvider(this.semanticTargetIdProvider)
                .labelProvider(variableManager -> "")
                .flexDirection(FlexDirection.column)
                .children(List.of(labelDescription, chartWidgetDescription))
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
}

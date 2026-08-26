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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view.nodes.component;

import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalInsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;

import java.util.Objects;

/**
 * Provide Label for Component nodes.
 *
 * @author frouene
 */
public class ComponentLabelProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public ComponentLabelProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("aql:self.name")
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .position(InsideLabelPosition.TOP_CENTER)
                .style(this.createInsideLabelStyle())
                .conditionalStyles(this.createHumanConditionalInsideLabelStyle(), this.createActorConditionalInsideLabelStyle())
                .textAlign(LabelTextAlign.CENTER)
                .build();
    }

    public InsideLabelStyle createInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .fontSize(12)
                .labelColor(this.colorProvider.getColor(LABViewConstants.COMPONENT_LABEL_COLOR))
                .showIconExpression("aql:true")
                .labelIcon("/icons/full/obj16/LogicalComponent.svg")
                .withHeader(true)
                .build();
    }

    public ConditionalInsideLabelStyle createHumanConditionalInsideLabelStyle() {
        return this.diagramBuilderHelper.newConditionalInsideLabelStyle()
                .style(this.createHumanActorInsideLabelStyle())
                .condition(ServiceMethod.of0(TransverseQueryService::isComponentHumanActor).aqlSelf())
                .build();
    }

    public InsideLabelStyle createHumanActorInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .fontSize(12)
                .labelColor(this.colorProvider.getColor(LABViewConstants.ACTOR_LABEL_COLOR))
                .showIconExpression("aql:true")
                .labelIcon("/icons/full/obj16/LogicalComponentHuman.svg")
                .withHeader(true)
                .build();
    }

    public ConditionalInsideLabelStyle createActorConditionalInsideLabelStyle() {
        return this.diagramBuilderHelper.newConditionalInsideLabelStyle()
                .style(this.createActorInsideLabelStyle())
                .condition(ServiceMethod.of0(TransverseQueryService::isComponentActor).aqlSelf())
                .build();
    }

    public InsideLabelStyle createActorInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .fontSize(12)
                .labelColor(this.colorProvider.getColor(LABViewConstants.ACTOR_LABEL_COLOR))
                .showIconExpression("aql:true")
                .labelIcon("/icons/full/obj16/LogicalActor.svg")
                .withHeader(true)
                .build();
    }
}

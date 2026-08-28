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
package org.eclipse.capella.diagram.lab.view.nodes.component;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.ConditionalNodeStyle;

import java.util.Objects;

/**
 * Provide Conditional Style for Component nodes.
 *
 * @author frouene
 */
public class ComponentConditionalStyleProvider {

    private final DiagramBuilders diagramBuilderHelper;

    private final IColorProvider colorProvider;

    public ComponentConditionalStyleProvider(DiagramBuilders diagramBuilderHelper, IColorProvider colorProvider) {
        this.diagramBuilderHelper = Objects.requireNonNull(diagramBuilderHelper);
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public ConditionalNodeStyle createActorConditionalStyle() {
        return this.diagramBuilderHelper.newConditionalNodeStyle()
                .condition(ServiceMethod.of0(TransverseQueryService::isComponentActor).aqlSelf())
                .style(new ComponentNodeStyleProvider(this.diagramBuilderHelper, this.colorProvider).createActorNodeStyle())
                .build();
    }
}

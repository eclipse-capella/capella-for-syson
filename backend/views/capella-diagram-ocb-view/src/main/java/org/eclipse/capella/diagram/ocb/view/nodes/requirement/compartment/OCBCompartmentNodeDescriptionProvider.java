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
package org.eclipse.capella.diagram.ocb.view.nodes.requirement.compartment;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.INodeToolProvider;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.diagram.common.view.nodes.AbstractCompartmentNodeDescriptionProvider;
import org.eclipse.syson.util.DescriptionNameGenerator;

/**
 * Provides the documentation compartment for requirements in the OCB diagram.
 *
 * @author tbezierslafosse
 */
public class OCBCompartmentNodeDescriptionProvider extends AbstractCompartmentNodeDescriptionProvider {

    public OCBCompartmentNodeDescriptionProvider(EClass eClass, EReference eReference, IColorProvider colorProvider) {
        super(eClass, eReference, colorProvider, new DescriptionNameGenerator("OCB"));
    }

    @Override
    protected InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression(this.getCompartmentLabel())
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .position(InsideLabelPosition.TOP_CENTER)
                .textAlign(LabelTextAlign.CENTER)
                .style(this.createInsideLabelStyle())
                .build();
    }

    @Override
    protected List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        return List.of();
    }

    @Override
    protected List<INodeToolProvider> getItemCreationToolProviders() {
        return List.of();
    }

    @Override
    protected List<NodeTool> getToolsWithoutSection(IViewDiagramElementFinder cache) {
        return List.of();
    }

    @Override
    protected String isHiddenByDefaultExpression() {
        return "aql:false";
    }
}

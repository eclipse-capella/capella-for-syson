/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package org.eclipse.capella.diagram.lab.view.nodes.requirement.compartment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.lab.view.LABDescriptionNameGenerator;
import org.eclipse.capella.diagram.lab.view.services.LABDiagramService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.builder.IViewDiagramElementFinder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.INodeToolProvider;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.syson.diagram.common.view.nodes.AbstractCompartmentNodeDescriptionProvider;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.ServiceMethod;

/**
 * Compartment node description provider for RequirementUsage compartments in LAB diagram.
 * <p>
 * This provider creates compartments for RequirementUsage elements using the LAB naming convention.
 * Compartment creation tools are disabled for now - can be enabled later when the backend services are ready.
 * </p>
 *
 * @author vkravchenko
 */
public class LABCompartmentNodeDescriptionProvider extends AbstractCompartmentNodeDescriptionProvider {

    public LABCompartmentNodeDescriptionProvider(EClass eClass, EReference eReference, IColorProvider colorProvider) {
        super(eClass, eReference, colorProvider, new LABDescriptionNameGenerator());
    }

    @Override
    protected String getCustomCompartmentLabel() {
        String customLabel = super.getCustomCompartmentLabel();
        if (this.eReference == SysmlPackage.eINSTANCE.getRequirementUsage_AssumedConstraint()
                || this.eReference == SysmlPackage.eINSTANCE.getRequirementDefinition_AssumedConstraint()) {
            customLabel = "assume constraints";
        } else if (this.eReference == SysmlPackage.eINSTANCE.getRequirementUsage_RequiredConstraint()
                || this.eReference == SysmlPackage.eINSTANCE.getRequirementDefinition_RequiredConstraint()) {
            customLabel = "require constraints";
        }
        return customLabel;
    }

    @Override
    protected List<NodeDescription> getDroppableNodes(IViewDiagramElementFinder cache) {
        // For now, return empty list - can be extended to support drag-and-drop
        return new ArrayList<>();
    }

    @Override
    protected List<INodeToolProvider> getItemCreationToolProviders() {
        // Disable creation tools for now - services not yet implemented
        return new ArrayList<>();
    }

    @Override
    protected List<NodeTool> getToolsWithoutSection(IViewDiagramElementFinder cache) {
        // Disable "New Documentation" tool for now - services not yet implemented
        return List.of();
    }

    @Override
    protected String isHiddenByDefaultExpression() {
        // Use LABDiagramService to determine visibility: only documentation compartment is visible by default
        return ServiceMethod.of1(LABDiagramService::isHiddenByDefault).aqlSelf("'" + this.eReference.getName() + "'");
    }
}

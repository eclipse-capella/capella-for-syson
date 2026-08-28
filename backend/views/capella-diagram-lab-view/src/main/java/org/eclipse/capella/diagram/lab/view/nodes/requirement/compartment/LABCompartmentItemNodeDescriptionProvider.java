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

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABDescriptionNameGenerator;
import org.eclipse.capella.model.transverse.services.TransverseMutationService;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.HeaderSeparatorDisplayMode;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.InsideLabelStyle;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.LabelTextAlign;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodePalette;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.sirius.components.view.diagram.SynchronizationPolicy;
import org.eclipse.sirius.components.view.diagram.UserResizableDirection;
import org.eclipse.syson.diagram.common.view.nodes.AbstractNodeDescriptionProvider;
import org.eclipse.syson.diagram.common.view.services.ViewLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.eclipse.syson.diagram.services.utils.CompartmentItemPreconditionSwitch;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.IDescriptionNameGenerator;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.eclipse.syson.util.ViewConstants;

/**
 * Compartment item node description provider for RequirementUsage compartments in LAB diagram.
 * <p>
 * This provider creates compartment items (the individual items inside compartments) 
 * for RequirementUsage elements using the LAB naming convention.
 * </p>
 *
 * @author vkravchenko
 */
public class LABCompartmentItemNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    protected final EClass eClass;

    protected final EReference eReference;

    private final IDescriptionNameGenerator descriptionNameGenerator;

    public LABCompartmentItemNodeDescriptionProvider(EClass eClass, EReference eReference, IColorProvider colorProvider) {
        super(colorProvider);
        this.eClass = Objects.requireNonNull(eClass);
        this.eReference = Objects.requireNonNull(eReference);
        this.descriptionNameGenerator = new LABDescriptionNameGenerator();
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .defaultHeightExpression(ViewConstants.DEFAULT_COMPARTMENT_NODE_ITEM_HEIGHT)
                .defaultWidthExpression(ViewConstants.DEFAULT_NODE_WIDTH)
                .domainType(this.getDomainType())
                .insideLabel(this.createInsideLabelDescription())
                .name(this.getName())
                .preconditionExpression(new CompartmentItemPreconditionSwitch(this.eReference).doSwitch(this.eClass))
                .semanticCandidatesExpression(this.getSemanticCandidateExpression())
                .style(this.createCompartmentItemNodeStyle())
                .userResizable(UserResizableDirection.NONE)
                .palette(this.createCompartmentItemNodePalette())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .build();
    }

    protected String getDomainType() {
        return SysMLMetamodelHelper.buildQualifiedName(this.getEReference().getEType());
    }

    protected String getName() {
        return this.getDescriptionNameGenerator().getCompartmentItemName(this.getEClass(), this.getEReference());
    }

    protected String getSemanticCandidateExpression() {
        return AQLConstants.AQL_SELF + "." + this.getEReference().getName();
    }

    protected InsideLabelDescription createInsideLabelDescription() {
        // For Documentation items, show the body text directly
        // For other items, fall back to name
        String labelExpression = this.getLabelExpression();

        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression(labelExpression)
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .position(InsideLabelPosition.TOP_LEFT)
                .style(this.createInsideLabelStyle())
                .textAlign(LabelTextAlign.LEFT)
                .build();
    }

    protected InsideLabelStyle createInsideLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .labelColor(this.colorProvider.getColor(ViewConstants.DEFAULT_LABEL_COLOR))
                .showIconExpression(ServiceMethod.of0(ViewLabelService::showIcon).aqlSelf())
                .withHeader(false)
                .build();
    }

    protected EClass getEClass() {
        return this.eClass;
    }

    public EReference getEReference() {
        return this.eReference;
    }

    private NodeStyleDescription createCompartmentItemNodeStyle() {
        return this.diagramBuilderHelper.newIconLabelNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(ViewConstants.DEFAULT_BORDER_COLOR))
                .borderRadius(0)
                .background(this.colorProvider.getColor(ViewConstants.DEFAULT_BACKGROUND_COLOR))
                .build();
    }

    private NodePalette createCompartmentItemNodePalette() {
        var callDeleteService = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf());

        var deleteTool = this.diagramBuilderHelper.newDeleteTool()
                .name("Delete from Model")
                .body(callDeleteService.build());

        var callEditService = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of1(DiagramMutationAQLService::directEditListItem).aqlSelf("newLabel"));

        var editTool = this.diagramBuilderHelper.newLabelEditTool()
                .name("Edit")
                .initialDirectEditLabelExpression(ServiceMethod.of0(DiagramQueryAQLService::getInitialDirectEditListItemLabel).aqlSelf())
                .body(callEditService.build());

        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .labelEditTool(editTool.build())
                .quickAccessTools(this.getDuplicateElementAndNodeTool())
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }

    public IDescriptionNameGenerator getDescriptionNameGenerator() {
        return this.descriptionNameGenerator;
    }

    private String getLabelExpression() {
        if (SysmlPackage.eINSTANCE.getElement_Documentation().equals(this.getEReference())) {
            // For Documentation compartment, show the body text
            return "aql:self.body";
        }
        // For other compartments (attributes, constraints, etc.), show name with optional value
        return "aql:if self.name <> null and self.name.size() > 0 then self.name else 'unnamed' endif";
    }
}

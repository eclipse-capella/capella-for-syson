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

import java.util.Objects;

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
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.DescriptionNameGenerator;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.eclipse.syson.util.ViewConstants;

/**
 * Provides documentation items inside the requirement compartment in OCB.
 *
 * @author tbezierslafosse
 */
public class OCBCompartmentItemNodeDescriptionProvider extends AbstractNodeDescriptionProvider {

    private final EClass eClass;
    private final EReference eReference;
    private final DescriptionNameGenerator nameGenerator = new DescriptionNameGenerator("OCB");

    public OCBCompartmentItemNodeDescriptionProvider(EClass eClass, EReference eReference, IColorProvider colorProvider) {
        super(colorProvider);
        this.eClass = Objects.requireNonNull(eClass);
        this.eReference = Objects.requireNonNull(eReference);
    }

    @Override
    public NodeDescription create() {
        return this.diagramBuilderHelper.newNodeDescription()
                .defaultHeightExpression(ViewConstants.DEFAULT_COMPARTMENT_NODE_ITEM_HEIGHT)
                .defaultWidthExpression(ViewConstants.DEFAULT_NODE_WIDTH)
                .domainType(SysMLMetamodelHelper.buildQualifiedName(this.eReference.getEType()))
                .insideLabel(this.createInsideLabelDescription())
                .name(this.nameGenerator.getCompartmentItemName(this.eClass, this.eReference))
                .preconditionExpression(new CompartmentItemPreconditionSwitch(this.eReference).doSwitch(this.eClass))
                .semanticCandidatesExpression(AQLConstants.AQL_SELF + "." + this.eReference.getName())
                .style(this.createStyle())
                .userResizable(UserResizableDirection.NONE)
                .palette(this.createPalette())
                .synchronizationPolicy(SynchronizationPolicy.SYNCHRONIZED)
                .build();
    }

    private InsideLabelDescription createInsideLabelDescription() {
        return this.diagramBuilderHelper.newInsideLabelDescription()
                .labelExpression("aql:self.body")
                .overflowStrategy(LabelOverflowStrategy.WRAP)
                .position(InsideLabelPosition.TOP_LEFT)
                .style(this.createLabelStyle())
                .textAlign(LabelTextAlign.LEFT)
                .build();
    }

    private InsideLabelStyle createLabelStyle() {
        return this.diagramBuilderHelper.newInsideLabelStyle()
                .borderSize(0)
                .headerSeparatorDisplayMode(HeaderSeparatorDisplayMode.NEVER)
                .labelColor(this.colorProvider.getColor(ViewConstants.DEFAULT_LABEL_COLOR))
                .showIconExpression(ServiceMethod.of0(ViewLabelService::showIcon).aqlSelf())
                .withHeader(false)
                .build();
    }

    private NodeStyleDescription createStyle() {
        return this.diagramBuilderHelper.newIconLabelNodeStyleDescription()
                .borderColor(this.colorProvider.getColor(ViewConstants.DEFAULT_BORDER_COLOR))
                .borderRadius(0)
                .background(this.colorProvider.getColor(ViewConstants.DEFAULT_BACKGROUND_COLOR))
                .build();
    }

    private NodePalette createPalette() {
        var deleteBody = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of0(TransverseMutationService::delete).aqlSelf());
        var deleteTool = this.diagramBuilderHelper.newDeleteTool().name("Delete from Model").body(deleteBody.build());
        var editBody = this.viewBuilderHelper.newChangeContext()
                .expression(ServiceMethod.of1(DiagramMutationAQLService::directEditListItem).aqlSelf("newLabel"));
        var editTool = this.diagramBuilderHelper.newLabelEditTool()
                .name("Edit")
                .initialDirectEditLabelExpression(ServiceMethod.of0(DiagramQueryAQLService::getInitialDirectEditListItemLabel).aqlSelf())
                .body(editBody.build());
        return this.diagramBuilderHelper.newNodePalette()
                .deleteTool(deleteTool.build())
                .labelEditTool(editTool.build())
                .quickAccessTools(this.getDuplicateElementAndNodeTool())
                .toolSections(this.diagramDefaultToolsFactory.createDefaultHideRevealNodeToolSection())
                .build();
    }
}

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
package org.eclipse.capella.application.configuration.explorer;

import java.util.UUID;

import org.eclipse.capella.application.configuration.explorer.services.CapellaExplorerLabelService;
import org.eclipse.capella.application.configuration.explorer.services.CapellaExplorerService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.builder.generated.tree.TreeBuilders;
import org.eclipse.sirius.components.view.builder.generated.tree.TreeDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilders;
import org.eclipse.sirius.components.view.tree.TreeDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelDescription;
import org.eclipse.sirius.components.view.tree.TreeItemLabelFragmentDescription;
import org.eclipse.sirius.emfjson.resource.JsonResource;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;

/**
 * Description of the Capella explorer tree using the ViewBuilder API from Sirius Web.
 *
 * @author frouene
 */
public class CapellaExplorerTreeDescriptionProvider {

    public static final String CAPELLA_EXPLORER = "Capella Explorer";

    public View createView() {

        var sysonDefaultTreeView = new ViewBuilders()
                .newView()
                .descriptions(this.build())
                .build();

        UUID resourceId = UUID.nameUUIDFromBytes(CAPELLA_EXPLORER.getBytes());
        String resourcePath = resourceId.toString();
        JsonResource resource = new JSONResourceFactory().createResourceFromPath(resourcePath);
        resource.eAdapters().add(new ResourceMetadataAdapter(CAPELLA_EXPLORER));
        resource.getContents().add(sysonDefaultTreeView);

        return sysonDefaultTreeView;
    }

    private TreeDescription build() {
        TreeDescription description = new TreeDescriptionBuilder()
                .name(CAPELLA_EXPLORER)
                .childrenExpression(ServiceMethod.of4(CapellaExplorerService::getChildren).aqlSelf(EditingContext.EDITING_CONTEXT, "existingRepresentations", "expanded", "activeFilterIds"))
                .deletableExpression(ServiceMethod.of0(CapellaExplorerService::isDeletable).aqlSelf())
                .editableExpression(ServiceMethod.of0(CapellaExplorerService::isEditable).aqlSelf())
                .elementsExpression(ServiceMethod.of1(CapellaExplorerService::getElements).aql(EditingContext.EDITING_CONTEXT, "activeFilterIds"))
                .hasChildrenExpression(ServiceMethod.of4(CapellaExplorerService::hasChildren).aqlSelf(EditingContext.EDITING_CONTEXT, "existingRepresentations", "expanded", "activeFilterIds"))
                .treeItemIconExpression(ServiceMethod.of0(CapellaExplorerService::getImageURL).aqlSelf())
                .kindExpression(ServiceMethod.of0(CapellaExplorerService::getKind).aqlSelf())
                .parentExpression(ServiceMethod.of2(CapellaExplorerService::getParent).aqlSelf("id", EditingContext.EDITING_CONTEXT))
                // This predicate will NOT be used while creating the explorer, but we don't want to see the description
                // of the explorer in the list of representations that can be created. Thus, we will return false all
                // the time.
                .preconditionExpression("aql:false")
                .selectableExpression(ServiceMethod.of0(CapellaExplorerService::isSelectable).aqlSelf())
                .titleExpression(CAPELLA_EXPLORER)
                .treeItemIdExpression(ServiceMethod.of0(CapellaExplorerService::getTreeItemId).aqlSelf())
                .treeItemObjectExpression(ServiceMethod.of1(CapellaExplorerService::getTreeItemObject).aql("id", EditingContext.EDITING_CONTEXT))
                .treeItemLabelDescriptions(this.createDefaultStyle())
                .build();
        return description;
    }

    private TreeItemLabelDescription createDefaultStyle() {
        return new TreeBuilders()
                .newTreeItemLabelDescription()
                .name("Default style")
                .preconditionExpression("aql:true")
                .children(this.getDefaultLabelFragmentDescription())
                .build();
    }

    private TreeItemLabelFragmentDescription getDefaultLabelFragmentDescription() {
        return new TreeBuilders().newTreeItemLabelFragmentDescription()
                .labelExpression(ServiceMethod.of0(CapellaExplorerLabelService::getLabel).aqlSelf())
                .build();
    }

}

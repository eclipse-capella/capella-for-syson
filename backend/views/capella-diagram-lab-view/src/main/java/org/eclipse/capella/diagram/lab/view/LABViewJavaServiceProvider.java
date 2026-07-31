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
package org.eclipse.capella.diagram.lab.view;

import java.util.List;

import org.eclipse.capella.diagram.lab.view.services.LABDiagramService;
import org.eclipse.capella.model.services.logical.architecture.LAMutationService;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationDropServices;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationMutationService;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationQueryService;
import org.eclipse.capella.model.services.logical.architecture.LARepresentationReconnectToolServices;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationMutationService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.diagram.common.view.services.ViewLabelService;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryElementService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.diagram.services.aql.DiagramMutationAQLService;
import org.eclipse.syson.diagram.services.aql.DiagramQueryAQLService;
import org.eclipse.syson.model.services.aql.ModelMutationAQLService;
import org.eclipse.syson.model.services.aql.ModelQueryAQLService;
import org.eclipse.syson.services.LabelService;
import org.eclipse.syson.tree.services.aql.TreeQueryAQLService;
import org.springframework.stereotype.Service;

/**
 * LAB Java service provider.
 *
 * @author frouene
 */
@Service
public class LABViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        var descriptions = view.getDescriptions();
        var optDescription = descriptions.stream()
                .filter(desc -> LABViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()))
                .findFirst();
        if (optDescription.isPresent()) {
            return List.of(LabelService.class,
                    LAQueryService.class,
                    LARepresentationDropServices.class,
                    LARepresentationReconnectToolServices.class,
                    LAMutationService.class,
                    LARepresentationQueryService.class,
                    LARepresentationMutationService.class,
                    TransverseMutationService.class,
                    TransverseQueryService.class,
                    TransverseRepresentationReconnectToolServices.class,
                    TransverseRepresentationQueryService.class,
                    TransverseRepresentationMutationService.class,
                    TransverseRepresentationReconnectToolServices.class,
                    DiagramMutationAQLService.class,
                    DiagramQueryAQLService.class,
                    ModelMutationAQLService.class,
                    ModelQueryAQLService.class,
                    TreeQueryAQLService.class,
                    DiagramMutationLabelService.class,
                    DiagramQueryElementService.class,
                    DiagramQueryLabelService.class,
                    DiagramMutationExposeService.class,
                    ViewLabelService.class,
                    LABDiagramService.class);
        }
        return List.of();
    }
}

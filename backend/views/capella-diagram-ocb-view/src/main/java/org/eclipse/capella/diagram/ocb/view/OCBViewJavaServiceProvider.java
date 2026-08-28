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
package org.eclipse.capella.diagram.ocb.view;

import java.util.List;

import org.eclipse.capella.model.services.operational.analysis.OAMutationService;
import org.eclipse.capella.model.services.operational.analysis.OAQueryService;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationDropServices;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationMutationService;
import org.eclipse.capella.model.services.operational.analysis.OARepresentationQueryService;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationMutationService;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
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
 * OCB Java service provider.
 *
 * @author tbezierslafosse
 */
@Service
public class OCBViewJavaServiceProvider implements IJavaServiceProvider {

    @Override
    public List<Class<?>> getServiceClasses(View view) {
        var descriptions = view.getDescriptions();
        var optDescription = descriptions.stream()
                .filter(desc -> OCBViewDiagramDescriptionProvider.DESCRIPTION_NAME.equals(desc.getName()))
                .findFirst();
        if (optDescription.isPresent()) {
            return List.of(LabelService.class,
                    OAQueryService.class,
                    OARepresentationDropServices.class,
                    OAMutationService.class,
                    OARepresentationQueryService.class,
                    OARepresentationMutationService.class,
                    TransverseMutationService.class,
                    TransverseQueryService.class,
                    TransverseRepresentationMutationService.class,
                    DiagramMutationAQLService.class,
                    DiagramQueryAQLService.class,
                    DiagramMutationLabelService.class,
                    DiagramQueryElementService.class,
                    DiagramQueryLabelService.class,
                    DiagramMutationExposeService.class,
                    ModelMutationAQLService.class,
                    ModelQueryAQLService.class,
                    TreeQueryAQLService.class);
        }
        return List.of();
    }
}

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
package org.eclipse.capella.diagram.sab.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.capella.model.services.system.analysis.SAQueryService;
import org.eclipse.capella.model.services.system.analysis.SARepresentationDropServices;
import org.eclipse.capella.model.services.system.analysis.SARepresentationMutationService;
import org.eclipse.capella.model.services.system.analysis.SARepresentationQueryService;
import org.eclipse.capella.model.services.system.analysis.SARepresentationReconnectToolServices;
import org.eclipse.capella.model.services.system.analysis.SAMutationService;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.capella.model.services.transverse.TransverseRepresentationReconnectToolServices;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.syson.diagram.services.DiagramMutationExposeService;
import org.eclipse.syson.diagram.services.DiagramMutationLabelService;
import org.eclipse.syson.diagram.services.DiagramQueryLabelService;
import org.eclipse.syson.services.DeleteService;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SAB Java service provider.
 *
 * @author mbats
 */
public class SABViewJavaServiceProviderTests {

    @Test
    public void getServiceClassesShouldReturnNoServiceForUnrelatedView() {
        var services = new SABViewJavaServiceProvider().getServiceClasses(ViewFactory.eINSTANCE.createView());

        assertTrue(services.isEmpty());
    }

    @Test
    public void getServiceClassesShouldReturnSABServicesForSABView() {
        var view = ViewFactory.eINSTANCE.createView();
        view.getDescriptions().add(new SABViewDiagramDescriptionProvider().create(new SABViewDescriptionProvider().getColorProvider(view)));

        var services = new SABViewJavaServiceProvider().getServiceClasses(view);

        assertTrue(services.contains(SAQueryService.class));
        assertTrue(services.contains(SARepresentationDropServices.class));
        assertTrue(services.contains(SARepresentationMutationService.class));
        assertTrue(services.contains(SARepresentationQueryService.class));
        assertTrue(services.contains(SARepresentationReconnectToolServices.class));
        assertTrue(services.contains(SAMutationService.class));
        assertTrue(services.contains(TransverseMutationService.class));
        assertTrue(services.contains(TransverseQueryService.class));
        assertTrue(services.contains(TransverseRepresentationReconnectToolServices.class));
        assertTrue(services.contains(DiagramMutationExposeService.class));
        assertTrue(services.contains(DiagramMutationLabelService.class));
        assertTrue(services.contains(DiagramQueryLabelService.class));
        assertTrue(services.contains(DeleteService.class));
    }
}

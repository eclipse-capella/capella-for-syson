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
package org.eclipse.capella.model.services.system.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.model.transverse.services.ArcadiaElementContentAdapter;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.junit.jupiter.api.Test;

/**
 * Verifies System Analysis roles and direct containment when type queries use the index.
 *
 * @author Jerome Gout
 */
public class CachedSAQueryServiceTests extends AbstractSemanticTests {

    @Test
    public void getSystemOfInterestWhenIndexInstalledShouldPreserveActorsAndDirectComponents() {
        var creationService = new CommonCreationService();
        var queryService = new SAQueryService();
        var structure = this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement();
        var component = creationService.createComponent(structure);
        var nested = creationService.createComponent(component);
        var actor = creationService.createActor(structure);
        var expectedSystem = queryService.getSystemOfInterest(structure);
        var expectedActors = queryService.getSystemActors(structure);
        var expectedComponents = queryService.getSystemComponents(structure);
        var resourceSet = structure.eResource().getResourceSet();
        var adapter = new ArcadiaElementContentAdapter(resourceSet);
        resourceSet.eAdapters().add(adapter);
        try {
            assertThat(queryService.getSystemOfInterest(structure)).containsExactlyElementsOf(expectedSystem).hasSize(1);
            assertThat(queryService.getSystemActors(structure)).containsExactlyInAnyOrderElementsOf(expectedActors).contains(actor).doesNotContain(component, nested);
            assertThat(queryService.getSystemComponents(structure)).containsExactlyInAnyOrderElementsOf(expectedComponents)
                    .contains(component).doesNotContain(actor, nested).doesNotContainAnyElementsOf(expectedSystem);
        } finally {
            resourceSet.eAdapters().remove(adapter);
        }
    }
}

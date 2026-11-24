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

package org.eclipse.capella.diagram.mcb.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.diagram.mcb.view.services.MCBSemanticCandidateService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.diagrams.IDiagramTests;
import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.junit.jupiter.api.Test;

/**
 * Tests the semantic candidates of MCB diagrams.
 *
 * @author tbezierslafosse
 */
public class MCBSemanticCandidateServiceTests extends AbstractSemanticTests implements IDiagramTests {

    private final MCBSemanticCandidateService semanticCandidateService = new MCBSemanticCandidateService();

    @Test
    public void getActorCandidatesShouldRespectTheActorHierarchyAndPerspective() {
        var creationService = new CommonCreationService();
        var perspective = this.capellaModel.getSystemAnalysisPerspective();
        var actor = creationService.createActor(perspective.getStructurePackage().getElement());
        var child = creationService.createActor(actor);
        var grandchild = creationService.createActor(child);
        creationService.createComponent(actor);
        creationService.createActor(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());

        assertThat(this.semanticCandidateService.getActorCandidates(perspective.getCapabilitiesPackage().getElement())).containsExactly(actor, child, grandchild);
        assertThat(this.semanticCandidateService.getActorCandidates(actor)).containsExactly(child, grandchild);
        assertThat(this.semanticCandidateService.getActorCandidates(child)).containsExactly(grandchild);
        assertThat(this.semanticCandidateService.getActorCandidates(grandchild)).isEmpty();
    }

    @Test
    public void getActorCandidatesWhenParentIsHumanActorShouldReturnNoChildren() {
        var creationService = new CommonCreationService();
        var actor = creationService.createActor(this.capellaModel.getSystemAnalysisPerspective().getStructurePackage().getElement());
        creationService.createActor(actor);
        new CommonUpdateService().setBooleanAttribute(actor, CommonQueryService.ARCADIA_PREFIX + CommonQueryService.ARCADIA_COMPONENT,
                CommonQueryService.ARCADIA_IS_HUMAN, true);

        assertThat(this.semanticCandidateService.getActorCandidates(actor)).isEmpty();
    }

}

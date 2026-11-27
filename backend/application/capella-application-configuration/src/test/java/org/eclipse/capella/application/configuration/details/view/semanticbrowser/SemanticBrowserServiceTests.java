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
package org.eclipse.capella.application.configuration.details.view.semanticbrowser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests traceability categories in the semantic browser.
 *
 * @author Jerome Gout
 */
public class SemanticBrowserServiceTests {

    private static final SemanticDataTestFixture FIXTURE = new SemanticDataTestFixture();

    private static final String REALIZED = "Realized System Functions";

    private static final String REALIZING = "Realizing Logical Functions";

    private final SemanticBrowserService service = new SemanticBrowserService(
            mock(IRepresentationMetadataSearchService.class), mock(IIdentityService.class), mock(IRepresentationSearchService.class));

    private final CommonUpdateService updateService = new CommonUpdateService();

    private CapellaModel capellaModel;

    @BeforeEach
    public void beforeEach() {
        this.capellaModel = FIXTURE.createCapellaModel();
    }

    @Test
    public void traceabilityCategoriesShouldReflectAddedAndRemovedFunctions() {
        var firstRealizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var secondRealizer = new CommonCreationService().createFunction(firstRealizer);
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();

        assertThat(this.service.getReferencedElementsCategories(firstRealizer)).doesNotContain(REALIZED);
        assertThat(this.service.getReferencingElementsCategories(realized)).doesNotContain(REALIZING);

        this.updateService.setRealized(firstRealizer, realized);
        this.updateService.setRealized(secondRealizer, realized);

        assertThat(this.service.getReferencedElementsCategories(firstRealizer)).contains(REALIZED);
        assertThat(this.service.getReferencedCategoryElements(firstRealizer, REALIZED)).isEqualTo(List.of(realized));
        assertThat(this.service.getReferencingElementsCategories(realized)).contains(REALIZING);
        assertThat(Set.copyOf(this.service.getReferencingCategoryElements(realized, REALIZING))).isEqualTo(Set.of(firstRealizer, secondRealizer));

        this.updateService.removeRealized(firstRealizer, realized);

        assertThat(this.service.getReferencedElementsCategories(firstRealizer)).doesNotContain(REALIZED);
        assertThat(this.service.getReferencingCategoryElements(realized, REALIZING)).isEqualTo(List.of(secondRealizer));

        this.updateService.removeRealized(secondRealizer, realized);

        assertThat(this.service.getReferencingElementsCategories(realized)).doesNotContain(REALIZING);
        assertThat(this.service.getReferencingCategoryElements(realized, REALIZING)).isEmpty();
    }

    @Test
    public void traceabilityCategoriesShouldIncludeFunctionPorts() {
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var realizerPort = creationService.createFunctionPort(logicalFunction, FeatureDirectionKind.OUT);
        var realizedPort = creationService.createFunctionPort(systemFunction, FeatureDirectionKind.OUT);

        this.updateService.setRealized(realizerPort, realizedPort);

        String realizedCategory = "Realized Function Output Ports";
        String realizingCategory = "Realizing Function Output Ports";
        assertThat(this.service.getReferencedElementsCategories(realizerPort)).contains(realizedCategory);
        assertThat(this.service.getReferencedCategoryElements(realizerPort, realizedCategory)).isEqualTo(List.of(realizedPort));
        assertThat(this.service.getReferencingElementsCategories(realizedPort)).contains(realizingCategory);
        assertThat(this.service.getReferencingCategoryElements(realizedPort, realizingCategory)).isEqualTo(List.of(realizerPort));
    }

    @Test
    public void traceabilityCategoriesWhenRealizationIsRejectedShouldBeAbsent() {
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var port = new CommonCreationService().createFunctionPort(systemFunction, FeatureDirectionKind.OUT);
        this.updateService.setRealized(port, operationalFunction);

        assertThat(new CommonQueryService().getRealized(port)).isEmpty();
        assertThat(this.service.getReferencedElementsCategories(port)).isEmpty();
        assertThat(this.service.getReferencedCategoryElements(port, "")).isEmpty();
        assertThat(this.service.getReferencedCategoryElements(port, "Realized")).isEmpty();
    }
}

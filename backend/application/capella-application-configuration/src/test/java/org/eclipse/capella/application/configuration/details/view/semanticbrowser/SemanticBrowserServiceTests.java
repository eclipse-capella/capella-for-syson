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
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_ELEMENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_IS_REALIZED_BY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_REALIZES;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.sirius.components.collaborative.api.IRepresentationSearchService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.web.domain.boundedcontexts.representationdata.services.api.IRepresentationMetadataSearchService;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.SysmlPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests traceability categories in the semantic browser.
 *
 * @author Jerome Gout
 */
public class SemanticBrowserServiceTests {

    private static final SemanticDataTestFixture FIXTURE = new SemanticDataTestFixture();

    private static final String REALIZES = "Realized System Functions";

    private static final String IS_REALIZED_BY = "Realizing Logical Functions";

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

        assertThat(this.service.getReferencedElementsCategories(firstRealizer)).doesNotContain(REALIZES);
        assertThat(this.service.getReferencingElementsCategories(realized)).doesNotContain(IS_REALIZED_BY);

        this.updateService.setRealizes(firstRealizer, realized);
        this.updateService.setRealizes(secondRealizer, realized);

        assertThat(this.service.getReferencedElementsCategories(firstRealizer)).contains(REALIZES);
        assertThat(this.service.getReferencedCategoryElements(firstRealizer, REALIZES)).isEqualTo(List.of(realized));
        assertThat(this.service.getReferencingElementsCategories(realized)).contains(IS_REALIZED_BY);
        assertThat(Set.copyOf(this.service.getReferencingCategoryElements(realized, IS_REALIZED_BY))).isEqualTo(Set.of(firstRealizer, secondRealizer));

        this.updateService.removeRealizes(firstRealizer, realized);

        assertThat(this.service.getReferencedElementsCategories(firstRealizer)).doesNotContain(REALIZES);
        assertThat(this.service.getReferencingCategoryElements(realized, IS_REALIZED_BY)).isEqualTo(List.of(secondRealizer));

        this.updateService.removeRealizes(secondRealizer, realized);

        assertThat(this.service.getReferencingElementsCategories(realized)).doesNotContain(IS_REALIZED_BY);
        assertThat(this.service.getReferencingCategoryElements(realized, IS_REALIZED_BY)).isEmpty();
    }

    @Test
    public void traceabilityCategoriesShouldIncludeFunctionPorts() {
        var logicalFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creationService = new CommonCreationService();
        var realizerPort = creationService.createFunctionPort(logicalFunction, FeatureDirectionKind.OUT);
        var realizedPort = creationService.createFunctionPort(systemFunction, FeatureDirectionKind.OUT);

        this.updateService.setFeatureReferenceValues(realizerPort, ARCADIA_PREFIX + ARCADIA_ELEMENT, ARCADIA_REALIZES,
                List.of(realizedPort), SysmlPackage.eINSTANCE.getOccurrenceUsage());
        this.updateService.setFeatureReferenceValues(realizedPort, ARCADIA_PREFIX + ARCADIA_ELEMENT, ARCADIA_IS_REALIZED_BY,
                List.of(realizerPort), SysmlPackage.eINSTANCE.getOccurrenceUsage());

        String realizesCategory = "Realized Function Output Ports";
        String isRealizedByCategory = "Realizing Function Output Ports";
        assertThat(this.service.getReferencedElementsCategories(realizerPort)).contains(realizesCategory);
        assertThat(this.service.getReferencedCategoryElements(realizerPort, realizesCategory)).isEqualTo(List.of(realizedPort));
        assertThat(this.service.getReferencingElementsCategories(realizedPort)).contains(isRealizedByCategory);
        assertThat(this.service.getReferencingCategoryElements(realizedPort, isRealizedByCategory)).isEqualTo(List.of(realizerPort));
    }

    @Test
    public void traceabilityCategoriesWhenRealizedNameIsUnavailableShouldBeAbsent() {
        var systemFunction = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var operationalFunction = this.capellaModel.getOperationalAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var port = new CommonCreationService().createFunctionPort(systemFunction, FeatureDirectionKind.OUT);
        this.updateService.setFeatureReferenceValues(port, ARCADIA_PREFIX + ARCADIA_ELEMENT, ARCADIA_REALIZES,
                List.of(operationalFunction), SysmlPackage.eINSTANCE.getOccurrenceUsage());

        assertThat(this.service.getReferencedElementsCategories(port)).isEmpty();
        assertThat(this.service.getReferencedCategoryElements(port, "")).isEmpty();
        assertThat(this.service.getReferencedCategoryElements(port, "Realizes")).isEmpty();
    }
}

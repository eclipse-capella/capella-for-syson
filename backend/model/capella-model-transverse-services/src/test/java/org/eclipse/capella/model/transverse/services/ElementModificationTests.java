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

package org.eclipse.capella.model.transverse.services;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.MetadataUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests the modification of semantic elements.
 *
 * @author gdaniel
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class ElementModificationTests extends AbstractSemanticTests {

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    @Test
    public void setStatusKindShouldSetTheStatusKindOfTheElement() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.transverseMutationService.setStatusKind(rootFunction, "open");

        assertThat(this.transverseQueryService.getStatusStringValue(rootFunction)).isEqualTo("open");
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.transverseQueryService::isStatusInfo)
                .hasSize(1);
        assertThat(this.transverseQueryService.getStatus(rootFunction))
                .extracting(Element::getDeclaredName)
                .isEqualTo("open");

    }

    @Test
    public void setStatusKindThenUnsetStatusShouldSetTheStatusThenUnsetIt() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.transverseMutationService.setStatusKind(rootFunction, "open");
        assertThat(this.transverseQueryService.getStatusStringValue(rootFunction)).isEqualTo("open");

        this.transverseMutationService.unSetUsageStatusKind(rootFunction);
        assertThat(this.transverseQueryService.getStatusStringValue(rootFunction)).isEmpty();
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.transverseQueryService::isStatusInfo)
                .isEmpty();
        assertThat(this.transverseQueryService.getStatus(rootFunction)).isNull();
    }

    @Test
    public void setStatusKindTwiceShouldSetTheLatestStatus() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.transverseMutationService.setStatusKind(rootFunction, "open");
        assertThat(this.transverseQueryService.getStatusStringValue(rootFunction)).isEqualTo("open");
        this.transverseMutationService.setStatusKind(rootFunction, "tbd");

        assertThat(this.transverseQueryService.getStatusStringValue(rootFunction)).isEqualTo("tbd");
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.transverseQueryService::isStatusInfo)
                .hasSize(1);
        assertThat(this.transverseQueryService.getStatus(rootFunction))
                .extracting(Element::getDeclaredName)
                .isEqualTo("tbd");
    }

    @Test
    public void setStatusKindWithInvalidStatusShouldNotSetTheStatus() {
        ActionUsage rootFunction = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        this.transverseMutationService.setStatusKind(rootFunction, "test");
        assertThat(this.transverseQueryService.getStatusStringValue(rootFunction)).isEmpty();
        assertThat(rootFunction.getOwnedElement())
                .filteredOn(MetadataUsage.class::isInstance)
                .map(MetadataUsage.class::cast)
                .filteredOn(this.transverseQueryService::isStatusInfo)
                .isEmpty();
        assertThat(this.transverseQueryService.getStatus(rootFunction)).isNull();
    }
}

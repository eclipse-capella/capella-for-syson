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
package org.eclipse.capella.model.services.logical.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.capella.model.services.transverse.AbstractSemanticTests;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartUsage;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LARepresentationMutationService}.
 *
 * @author fbarbin
 */
public class LARepresentationMutationServiceTests extends AbstractSemanticTests {

    private final TransverseQueryService transverseQueryService = new TransverseQueryService();

    private final TransverseMutationService transverseMutationService = new TransverseMutationService();

    @Test
    public void createFunctionShouldCreateInSelectedComponentOrParentFunction() {
        PartUsage component = this.transverseMutationService.createComponent(this.getLogicalArchitectureStructurePackage());
        ActionUsage parentFunction = this.transverseMutationService.createFunction(this.getRootFunction());
        ActionUsage createdInComponent = this.transverseMutationService.createFunction(component);
        ActionUsage createdInFunction = this.transverseMutationService.createFunction(parentFunction);

        assertThat(this.getRootFunction().getNestedAction()).contains(createdInComponent);
        assertThat(this.transverseQueryService.getAllocatedFunctions(component)).contains(createdInComponent);

        assertThat(parentFunction.getNestedAction()).contains(createdInFunction);
        assertThat(createdInFunction.getOwningUsage()).isSameAs(parentFunction);
    }

    @Test
    public void createFunctionPortShouldSetDirectionAndDefaultName() {
        ActionUsage function = this.transverseMutationService.createFunction(this.getRootFunction());

        Feature inPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.IN);
        Feature outPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.OUT);
        Feature inOutPort = this.transverseMutationService.createFunctionPort(function, FeatureDirectionKind.INOUT);

        assertThat(inPort.getDirection()).isEqualTo(FeatureDirectionKind.IN);
        assertThat(outPort.getDirection()).isEqualTo(FeatureDirectionKind.OUT);
        assertThat(inOutPort.getDirection()).isEqualTo(FeatureDirectionKind.INOUT);

        assertThat(inPort.getDeclaredName()).startsWith("FIP ");
        assertThat(outPort.getDeclaredName()).startsWith("FOP ");
        assertThat(inOutPort.getDeclaredName()).startsWith("FP ");
    }

    @Test
    public void createFunctionalChainShouldStoreExchangesInGivenOrder() {
        var function1 = this.transverseMutationService.createFunction(this.getRootFunction());
        var function2 = this.transverseMutationService.createFunction(this.getRootFunction());

        var function3 = this.transverseMutationService.createFunction(this.getRootFunction());
        var function4 = this.transverseMutationService.createFunction(this.getRootFunction());

        var flowUsage1 = this.transverseMutationService.createFunctionalExchange(function1, function2);
        var flowUsage2 = this.transverseMutationService.createFunctionalExchange(function3, function4);

        ActionUsage createdFunctionalChain = this.transverseMutationService.createFunctionalChain(this.getLogicalArchitectureFunctionsPackage(), List.of(flowUsage1, flowUsage2));

        assertThat(this.transverseQueryService.getFeatureReferenceValue(createdFunctionalChain, TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES))
                .containsExactly(flowUsage1, flowUsage2);
    }

    private Package getLogicalArchitectureStructurePackage() {
        return this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement();
    }

    private Package getLogicalArchitectureFunctionsPackage() {
        return this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getElement();
    }

    private ActionUsage getRootFunction() {
        return this.getLogicalArchitectureFunctionsPackage().getOwnedElement().stream()
                .filter(ActionUsage.class::isInstance)
                .map(ActionUsage.class::cast)
                .filter(this.transverseQueryService::isFunction)
                .findFirst()
                .orElseThrow();
    }

}

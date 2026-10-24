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
package org.eclipse.capella.diagram.common.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.variables.DiagramVariables;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.syson.diagram.common.view.services.SysONDiagramElementPaletteVariableManagerProvider;
import org.junit.jupiter.api.Test;

/**
 * Checks that Capella augments the SysON palette variables without losing connector context.
 *
 * @author tbezierslafosse
 */
public class CapellaPaletteVariableManagerProviderTests {

    @Test
    public void getVariableManagerShouldPreserveSysONVariablesAndDefineAnAbsentSemanticTarget() {
        var delegate = mock(SysONDiagramElementPaletteVariableManagerProvider.class);
        var editingContext = mock(IEditingContext.class);
        var diagramContext = mock(DiagramContext.class);
        var node = new Object();
        var semanticElement = new Object();
        var variables = new VariableManager();
        variables.put(DiagramVariables.EDGE_TARGET.name(), semanticElement);
        when(delegate.getVariableManager(editingContext, diagramContext, node, semanticElement)).thenReturn(Optional.of(variables));
        var provider = new CapellaPaletteVariableManagerProvider(delegate);

        assertThat(provider.getVariableManager(editingContext, diagramContext, node, semanticElement)).containsSame(variables);
        assertThat(variables.getVariables()).containsEntry(DiagramVariables.EDGE_TARGET.name(), semanticElement)
                .containsKey(EdgeDescription.SEMANTIC_EDGE_TARGET);
        assertThat(variables.getVariables().get(EdgeDescription.SEMANTIC_EDGE_TARGET)).isNull();

        variables.put(EdgeDescription.SEMANTIC_EDGE_TARGET, semanticElement);
        provider.getVariableManager(editingContext, diagramContext, node, semanticElement);
        assertThat(variables.getVariables()).containsEntry(EdgeDescription.SEMANTIC_EDGE_TARGET, semanticElement);

        when(delegate.getVariableManager(editingContext, diagramContext, node, semanticElement)).thenReturn(Optional.empty());
        assertThat(provider.getVariableManager(editingContext, diagramContext, node, semanticElement)).isEmpty();
    }
}

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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.diagrams.description.EdgeDescription;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.emf.diagram.tools.api.IDiagramElementPaletteVariableManagerProvider;
import org.eclipse.syson.diagram.common.view.services.SysONDiagramElementPaletteVariableManagerProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Defines an explicit absent connector target in node palettes so AQL preconditions can also evaluate selected targets.
 * Delegates SysON variables and preserves any existing target value.
 *
 * @author tbezierslafosse
 */
@Service
@Primary
public class CapellaPaletteVariableManagerProvider implements IDiagramElementPaletteVariableManagerProvider {

    private final SysONDiagramElementPaletteVariableManagerProvider delegate;

    public CapellaPaletteVariableManagerProvider(SysONDiagramElementPaletteVariableManagerProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Optional<VariableManager> getVariableManager(IEditingContext editingContext, DiagramContext diagramContext, Object diagramElement, Object semanticElement) {
        var result = this.delegate.getVariableManager(editingContext, diagramContext, diagramElement, semanticElement);
        result.ifPresent(variableManager -> {
            if (!variableManager.getVariables().containsKey(EdgeDescription.SEMANTIC_EDGE_TARGET)) {
                variableManager.put(EdgeDescription.SEMANTIC_EDGE_TARGET, null);
            }
        });
        return result;
    }
}

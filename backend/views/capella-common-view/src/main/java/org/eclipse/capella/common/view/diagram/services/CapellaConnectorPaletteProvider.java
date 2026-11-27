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
package org.eclipse.capella.common.view.diagram.services;

import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;

import org.eclipse.sirius.components.collaborative.diagrams.DiagramContext;
import org.eclipse.sirius.components.collaborative.diagrams.api.ConnectorPaletteResult;
import org.eclipse.sirius.components.collaborative.diagrams.api.DefaultConnectorToolResult;
import org.eclipse.sirius.components.collaborative.diagrams.api.IConnectorPaletteProvider;
import org.eclipse.sirius.components.collaborative.diagrams.api.IConnectorToolsProviderResult;
import org.eclipse.sirius.components.collaborative.diagrams.dto.SingleClickOnTwoDiagramElementsTool;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.diagrams.description.DiagramDescription;
import org.eclipse.sirius.components.palette.dto.IPaletteEntry;
import org.eclipse.sirius.components.palette.dto.ITool;
import org.eclipse.sirius.components.palette.dto.Palette;
import org.eclipse.sirius.components.palette.dto.ToolSection;
import org.eclipse.sirius.components.view.emf.diagram.ViewConnectorPaletteProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * Returns the connectors available for the given context.
 * <p>
 * This class delegates to Sirius Web's implementation, and processes the result to replace palettes with a single element with {@link DefaultConnectorToolResult}, ensuring a single tool is directly
 * executed by the frontend instead of opening a palette containing it.
 *
 * @technical-debt this class should be removed or adapted once Sirius Web #7213 is fixed.
 *
 * @author gdaniel
 */
@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CapellaConnectorPaletteProvider implements IConnectorPaletteProvider {

    private final ViewConnectorPaletteProvider delegate;

    public CapellaConnectorPaletteProvider(ViewConnectorPaletteProvider delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean canHandle(DiagramDescription diagramDescription) {
        return this.delegate.canHandle(diagramDescription);
    }

    @Override
    public IConnectorToolsProviderResult getConnectorTools(IEditingContext editingContext, DiagramContext diagramContext, DiagramDescription diagramDescription,
            Object sourceDiagramElement, Object targetDiagramElement, Object sourceElementDescription, Object targetElementDescription) {
        IConnectorToolsProviderResult result = this.delegate.getConnectorTools(editingContext, diagramContext, diagramDescription, sourceDiagramElement, targetDiagramElement, sourceElementDescription,
                targetElementDescription);
        if (result instanceof ConnectorPaletteResult(Palette palette)) {
            // Use a TreeSet to ensure we don't create a palette if the unique tool is visible in both the palette content and the quick access tools.
            TreeSet<ITool> tools = new TreeSet<>(Comparator.comparing(ITool::id));
            tools.addAll(palette.quickAccessTools());
            for (IPaletteEntry entry : palette.paletteEntries()) {
                if (entry instanceof ITool tool) {
                    tools.add(tool);
                } else if (entry instanceof ToolSection section) {
                    tools.addAll(section.tools());
                }
            }
            if (tools.size() == 1 && tools.getFirst() instanceof SingleClickOnTwoDiagramElementsTool tool) {
                result = new DefaultConnectorToolResult(tool);
            }
        }
        return result;
    }
}

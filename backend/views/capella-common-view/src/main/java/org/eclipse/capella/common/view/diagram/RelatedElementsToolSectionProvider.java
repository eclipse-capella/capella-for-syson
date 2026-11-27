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

package org.eclipse.capella.common.view.diagram;

import org.eclipse.sirius.components.view.builder.generated.diagram.DiagramBuilders;
import org.eclipse.sirius.components.view.diagram.DiagramToolSection;
import org.eclipse.sirius.components.view.diagram.NodeToolSection;

/**
 * Provides the related elements tool section shared by Capella for SysON diagrams.
 *
 * @author Jerome Gout
 */
public class RelatedElementsToolSectionProvider {

    private static final String RELATED_ELEMENTS_TOOL_SECTION_LABEL = "Related Elements";

    private final DiagramBuilders diagramBuilders = new DiagramBuilders();

    public DiagramToolSection createDiagramToolSection() {
        return this.diagramBuilders.newDiagramToolSection()
                .name(RELATED_ELEMENTS_TOOL_SECTION_LABEL)
                .build();
    }

    public NodeToolSection createNodeToolSection() {
        return this.diagramBuilders.newNodeToolSection()
                .name(RELATED_ELEMENTS_TOOL_SECTION_LABEL)
                .build();
    }
}

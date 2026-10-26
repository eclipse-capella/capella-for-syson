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
package org.eclipse.capella.diagram.customization.styles.api;

import java.util.Optional;

import org.eclipse.sirius.components.diagrams.EdgeStyle;
import org.eclipse.sirius.components.diagrams.RectangularNodeStyle;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.EdgeDescription;
import org.eclipse.sirius.components.view.diagram.NodeDescription;

/**
 * Interface to define diagram specific functional chain style customization.
 *
 * @author Jerome Gout
 */
public interface IFunctionalChainStyleCustomizer {

    boolean canHandle(DiagramDescription description);

    boolean isFunctionalChainNodeDescription(NodeDescription description);

    boolean isFunctionNodeDescription(NodeDescription description);

    boolean isFunctionalExchangeEdgeDescription(EdgeDescription description);

    /**
     * Returns function style according to the chain color.
     * An empty chain color denotes overlap; other style properties are retained by the post-processor.
     */
    RectangularNodeStyle customizeFunctionStyle(RectangularNodeStyle style, Optional<String> chainColor);

    /**
     * Returns function exchange style according to the chain color.
     * An empty chain color denotes overlap; other style properties are retained by the post-processor.
     */
    EdgeStyle customizeFunctionalExchangeStyle(EdgeStyle style, Optional<String> chainColor);
}


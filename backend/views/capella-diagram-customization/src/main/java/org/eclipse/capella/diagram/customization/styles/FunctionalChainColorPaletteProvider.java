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
package org.eclipse.capella.diagram.customization.styles;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.capella.diagram.customization.styles.api.IFunctionalChainColorPaletteProvider;
import org.eclipse.sirius.components.view.FixedColor;
import org.eclipse.sirius.components.view.ViewFactory;
import org.springframework.stereotype.Service;

/**
 * Creates diagram palette entries from the shared Functional Chain colors.
 *
 * @author Jerome Gout
 */
@Service
public class FunctionalChainColorPaletteProvider implements IFunctionalChainColorPaletteProvider {

    @Override
    public List<FixedColor> createColors() {
        List<FixedColor> colors = new ArrayList<>();
        for (int index = 0; index < FunctionalChainStyleConstants.COLORS.size(); index++) {
            var color = ViewFactory.eINSTANCE.createFixedColor();
            color.setName(FunctionalChainStyleConstants.FUNCTIONAL_CHAIN_COLOR_PREFIX + "_" + index);
            color.setValue(FunctionalChainStyleConstants.COLORS.get(index));
            colors.add(color);
        }
        return colors;
    }
}


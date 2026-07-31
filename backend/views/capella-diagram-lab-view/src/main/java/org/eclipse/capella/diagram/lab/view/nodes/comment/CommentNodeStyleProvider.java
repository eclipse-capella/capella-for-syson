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
 *     DB Netz AG - implementation
 *******************************************************************************/
package org.eclipse.capella.diagram.lab.view.nodes.comment;

import java.util.Objects;

import org.eclipse.capella.diagram.lab.view.LABViewConstants;
import org.eclipse.sirius.components.view.builder.generated.diagram.FreeFormLayoutStrategyDescriptionBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.diagram.NodeStyleDescription;
import org.eclipse.syson.sysmlcustomnodes.SysMLCustomnodesFactory;
import org.eclipse.syson.sysmlcustomnodes.SysMLNoteNodeStyleDescription;

/**
 * Provide Style for Comment nodes using post-it style.
 *
 * @author vkravchenko
 */
public class CommentNodeStyleProvider {

    private final IColorProvider colorProvider;

    public CommentNodeStyleProvider(IColorProvider colorProvider) {
        this.colorProvider = Objects.requireNonNull(colorProvider);
    }

    public NodeStyleDescription createCommentNodeStyle() {
        SysMLNoteNodeStyleDescription noteStyle = SysMLCustomnodesFactory.eINSTANCE.createSysMLNoteNodeStyleDescription();
        noteStyle.setBorderColor(this.colorProvider.getColor(LABViewConstants.COMMENT_BORDER_COLOR));
        noteStyle.setBorderRadius(0);
        noteStyle.setBorderSize(1);
        noteStyle.setBackground(this.colorProvider.getColor(LABViewConstants.COMMENT_BACKGROUND_COLOR));
        noteStyle.setChildrenLayoutStrategy(new FreeFormLayoutStrategyDescriptionBuilder().build());
        return noteStyle;
    }
}

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
package org.eclipse.capella.application.configuration.explorer.services;

import java.util.List;
import java.util.Objects;

import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerFragment;
import org.eclipse.capella.application.configuration.explorer.services.api.ICapellaExplorerLabelService;
import org.eclipse.capella.application.configuration.label.services.CapellaImagePathsService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.sirius.components.core.api.labels.StyledString;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerLabelServiceDelegate;
import org.eclipse.syson.sysml.Element;
import org.springframework.stereotype.Service;

/**
 * Used to provide the behavior of the SysON Explorer view for {@link Element}.
 *
 * @author arichard
 */
@Service
public class CapellaExplorerLabelService implements IExplorerLabelServiceDelegate, ICapellaExplorerLabelService {

    private final IReadOnlyObjectPredicate readOnlyObjectPredicate;

    private final CapellaImagePathsService capellaImagePathsService;

    private final ILabelService labelService;

    public CapellaExplorerLabelService(IReadOnlyObjectPredicate readOnlyObjectPredicate, ILabelService labelService, CapellaImagePathsService capellaImagePathsService) {
        this.readOnlyObjectPredicate = Objects.requireNonNull(readOnlyObjectPredicate);
        this.labelService = Objects.requireNonNull(labelService);
        this.capellaImagePathsService = Objects.requireNonNull(capellaImagePathsService);
    }

    @Override
    public boolean canHandle(Object object) {
        return object instanceof Element;
    }

    @Override
    public boolean isEditable(Object self) {
        boolean editable = self instanceof Element && !this.readOnlyObjectPredicate.test(self);
        return editable;
    }

    @Override
    public void editLabel(Object self, String newValue) {
        if (self instanceof Element element && !this.readOnlyObjectPredicate.test(self)) {
            element.setDeclaredName(newValue);
        }
    }

    @Override
    public String getLabel(Object self) {
        String label = "";
        if (self instanceof ICapellaExplorerFragment fragment) {
            label = fragment.getLabel();
        } else {
            StyledString styledLabel = this.labelService.getStyledLabel(self);
            if (styledLabel != null) {
                label = styledLabel.toString();
            }
        }
        return label;
    }

    @Override
    public List<String> getImageURL(Object self) {
        List<String> result = List.of();
        if (self instanceof ICapellaExplorerFragment fragment) {
            result = fragment.getIconURL();
        } else {
            result = this.capellaImagePathsService.getImagePaths(self);
        }
        return result;
    }
}

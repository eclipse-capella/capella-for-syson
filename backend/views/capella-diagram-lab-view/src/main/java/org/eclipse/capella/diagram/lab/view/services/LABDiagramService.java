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
package org.eclipse.capella.diagram.lab.view.services;

import java.util.Objects;

/**
 * A Service class specific to the LAB Diagram. This class is intended to be used for services specific to the view
 * without relation with the model or the business layer.
 *
 * @author fbarbin
 */
public class LABDiagramService {

    private final ShowDiagramFunctionsService showDiagramFunctionsService;

    public LABDiagramService(ShowDiagramFunctionsService diagramFunctionsService) {
        this.showDiagramFunctionsService = Objects.requireNonNull(diagramFunctionsService);
    }

    public boolean isFunctionHidden(Object self) {
        return !this.showDiagramFunctionsService.isShowFunctions();
    }
}

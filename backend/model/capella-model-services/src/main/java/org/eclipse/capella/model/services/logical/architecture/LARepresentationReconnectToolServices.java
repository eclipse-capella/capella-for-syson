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

import org.eclipse.sirius.components.web.services.FeedbackMessageService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.Element;

import java.util.Objects;

/**
 * Java services dedicated to the reconnection tools.
 *
 * @author fbarbin
 */
public class LARepresentationReconnectToolServices {

    private final LAQueryService laQueryService;

    private final ISysMLMoveElementService moveService;

    public LARepresentationReconnectToolServices(ISysMLMoveElementService moveService, FeedbackMessageService feedbackMessageService) {
        this.laQueryService = new LAQueryService();
        this.moveService = Objects.requireNonNull(moveService);
    }

    public Element reconnectFunctionalExchange(Element newTarget, Element oldTarget) {
        if (this.laQueryService.isFunction(newTarget) && this.laQueryService.isExchangeItem(oldTarget)) {
            this.moveService.moveSemanticElement(oldTarget, newTarget);
        }
        return newTarget;
    }
}

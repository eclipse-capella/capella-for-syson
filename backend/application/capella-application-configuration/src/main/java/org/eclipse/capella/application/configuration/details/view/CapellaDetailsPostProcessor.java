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
package org.eclipse.capella.application.configuration.details.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.sirius.components.collaborative.forms.api.IFormPostProcessor;
import org.eclipse.sirius.components.forms.Form;
import org.eclipse.sirius.components.forms.Page;
import org.eclipse.sirius.components.representations.VariableManager;
import org.springframework.stereotype.Service;

/**
 * This service is in charge of sorting pages in the Details view.
 *
 * @author frouene
 */
@Service
public class CapellaDetailsPostProcessor implements IFormPostProcessor {

    private static final String CAPELLA_PAGE_LABEL = "Capella";

    private Optional<Page> getPage(Form form, String pageLabel) {
        return form.getPages().stream().filter(p -> pageLabel.equals(p.getLabel())).findFirst();
    }

    private List<Page> getExtraPages(Form form) {
        return form.getPages().stream().filter(p -> !CAPELLA_PAGE_LABEL.equals(p.getLabel())).toList();
    }

    @Override
    public Form postProcess(Form form, VariableManager variableManager) {
        List<Page> newPages = new ArrayList<>();
        this.getPage(form, CAPELLA_PAGE_LABEL).ifPresent(newPages::add);
        newPages.addAll(this.getExtraPages(form));
        return Form.newForm(form).pages(newPages).build();
    }

}

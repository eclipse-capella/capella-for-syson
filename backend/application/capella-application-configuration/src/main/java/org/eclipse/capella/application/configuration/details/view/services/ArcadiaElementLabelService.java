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
package org.eclipse.capella.application.configuration.details.view.services;

import java.util.Optional;

import org.eclipse.capella.model.transverse.services.ArcadiaElementNameService;
import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.Usage;

/**
 * Provides traceability widget labels and display conditions.
 *
 * @author Jerome Gout
 */
public class ArcadiaElementLabelService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final ArcadiaElementNameService arcadiaElementNameService = new ArcadiaElementNameService();

    public boolean hasRealizesWidget(Element element) {
        return this.getRealizesWidgetLabel(element).isPresent();
    }

    /**
     * Returns the string value expected by the widget label expression.
     */
    public String getRealizesWidgetLabelValue(Element element) {
        return this.getRealizesWidgetLabel(element).orElse("");
    }

    private Optional<String> getRealizesWidgetLabel(Element element) {
        Optional<String> result = Optional.empty();
        if (element instanceof Usage usage) {
            result = this.commonQueryService.getArcadiaPerspective(usage)
                    .flatMap(this::getRealizedPerspective)
                    .flatMap(perspective -> this.arcadiaElementNameService.getElementName(usage, perspective))
                    .map(name -> this.getRealizedElementLabel(usage, name));
        }
        return result;
    }

    private Optional<ArcadiaEngineeringPerspective> getRealizedPerspective(ArcadiaEngineeringPerspective perspective) {
        int previousIndex = perspective.ordinal() - 1;
        if (previousIndex >= 0) {
            return Optional.of(ArcadiaEngineeringPerspective.values()[previousIndex]);
        }
        return Optional.empty();
    }

    private String getRealizedElementLabel(Usage usage, String name) {
        String result = "Realized " + this.getPluralName(name);
        if ("Function Port".equals(name)) {
            if (FeatureDirectionKind.IN == usage.getDirection()) {
                result = "Realized Function Input Ports";
            } else if (FeatureDirectionKind.OUT == usage.getDirection()) {
                result = "Realized Function Output Ports";
            }
        }
        return result;
    }

    private String getPluralName(String name) {
        String result = name + "s";
        if (name.endsWith("y")) {
            result = name.substring(0, name.length() - 1) + "ies";
        } else if (name.endsWith("s")) {
            result = name + "es";
        }
        return result;
    }
}

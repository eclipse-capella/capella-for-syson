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
package org.eclipse.capella.model.services.operational.analysis;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.OccurrenceUsage;

/**
 * Operational Analysis (OA) related mutation service.
 * It is important to note that this service must retain its empty constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class OAMutationService {

    private final CommonCreationService commonCreationService;

    private final CommonQueryService commonQueryService;

    public OAMutationService() {
        this.commonCreationService = new CommonCreationService();
        this.commonQueryService = new CommonQueryService();
    }

    /**
     * Creates an operational Interaction using the common Functional Exchange semantics.
     *
     * @param source
     *            the source activity or output port
     * @param target
     *            the target activity or input port
     * @return the named Interaction, or {@code null} when the exchange cannot be created
     */
    public FlowUsage createInteractionOA(Feature source, Feature target) {
        var interaction = this.commonCreationService.createFunctionalExchange(source, target);
        if (interaction != null) {
            long existingElementsCount = this.commonQueryService.existingElementsCount(interaction);
            interaction.setDeclaredName("Interaction " + existingElementsCount);
        }
        return interaction;
    }

    public InterfaceUsage createCommunicationMeanComponentExchangeOA(Feature source, Feature target) {
        var componentExchange = this.commonCreationService.createComponentExchange(source, target);
        if (componentExchange != null) {
            long existingElementsCount = this.commonQueryService.existingElementsCount(componentExchange);
            componentExchange.setDeclaredName("CommunicationMean " + existingElementsCount);
        }
        return componentExchange;
    }

    public OccurrenceUsage createOperationalCapabilityOA(Element parent) {
        var capability = this.commonCreationService.createOperationalCapability(parent);
        if (capability != null) {
            long existingElementsCount = this.commonQueryService.existingElementsCount(capability);
            capability.setDeclaredName("OC " + existingElementsCount);
        }
        return capability;
    }

    public ActionUsage createOperationalActivityOA(Element parent) {
        ActionUsage activity = null;
        if (this.commonQueryService.isOperationalActivity(parent)
                || this.commonQueryService.isComponent(parent) && this.commonQueryService.isOperationalAnalysisPerspective(parent)) {
            activity = this.commonCreationService.createFunction(parent);
            if (activity != null) {
                activity.setDeclaredName("OA " + this.commonQueryService.existingElementsCount(activity));
            }
        }
        return activity;
    }

}

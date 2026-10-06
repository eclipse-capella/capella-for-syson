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

import static org.eclipse.capella.model.transverse.services.CommonNamingService.COMMUNICATION_MEAN_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.INTERACTION_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.OPERATIONAL_ACTIVITY_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.OPERATIONAL_CAPABILITY_DEFAULT_DECLAREDNAME_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonNamingService.OPERATIONAL_PROCESS_DEFAULT_DECLAREDNAME_PREFIX;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonDeletionService;
import org.eclipse.capella.model.transverse.services.CommonNamingService;
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

    private final CommonDeletionService commonDeletionService;

    private final CommonNamingService commonNamingService;

    public OAMutationService() {
        this.commonCreationService = new CommonCreationService();
        this.commonQueryService = new CommonQueryService();
        this.commonDeletionService = new CommonDeletionService();
        this.commonNamingService = new CommonNamingService();
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
            this.commonNamingService.setElementDefaultDeclaredName(interaction, INTERACTION_DEFAULT_DECLAREDNAME_PREFIX);
        }
        return interaction;
    }

    /**
     * Deletes an Operational Interaction and its unused hidden ports from the OAB edge palette.
     * Ports still used by another exchange are preserved.
     *
     * @param interaction
     *            the Operational Interaction to delete
     * @return the deleted Interaction, or {@code null} when it is not an Operational Interaction
     */
    public FlowUsage deleteInteractionOA(FlowUsage interaction) {
        var ports = Stream.of(this.commonQueryService.getFunctionalExchangeSource(interaction), this.commonQueryService.getFunctionalExchangeTarget(interaction))
                .distinct()
                .toList();
        this.commonDeletionService.delete(interaction);
        for (Element port : ports) {
            this.commonDeletionService.delete(port);
        }
        return interaction;
    }

    public InterfaceUsage createCommunicationMeanComponentExchangeOA(Feature source, Feature target) {
        var componentExchange = this.commonCreationService.createComponentExchange(source, target);
        if (componentExchange != null) {
            this.commonNamingService.setElementDefaultDeclaredName(componentExchange, COMMUNICATION_MEAN_DEFAULT_DECLAREDNAME_PREFIX);
        }
        return componentExchange;
    }

    public OccurrenceUsage createOperationalCapabilityOA(Element parent) {
        var capability = this.commonCreationService.createOperationalCapability(parent);
        if (capability != null) {
            this.commonNamingService.setElementDefaultDeclaredName(capability, OPERATIONAL_CAPABILITY_DEFAULT_DECLAREDNAME_PREFIX);
        }
        return capability;
    }

    public ActionUsage createOperationalActivityOA(Element parent) {
        ActionUsage activity = null;
        if (this.commonQueryService.isOperationalActivity(parent)
                || this.commonQueryService.isComponent(parent) && this.commonQueryService.isOperationalAnalysisPerspective(parent)) {
            activity = this.commonCreationService.createFunction(parent);
            if (activity != null) {
                this.commonNamingService.setElementDefaultDeclaredName(activity, OPERATIONAL_ACTIVITY_DEFAULT_DECLAREDNAME_PREFIX);
            }
        }
        return activity;
    }

    public ActionUsage createOperationalProcessOA(Element parent, List<Object> selectedObjects) {
        ActionUsage process = this.commonCreationService.createFunctionalChain(parent, selectedObjects);
        this.commonNamingService.setElementDefaultDeclaredName(process, OPERATIONAL_PROCESS_DEFAULT_DECLAREDNAME_PREFIX);
        return process;
    }
}

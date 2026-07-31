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
package org.eclipse.capella.model.services.transverse;

import org.eclipse.capella.model.services.logical.architecture.LibraryServices;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.syson.diagram.common.view.services.ShowDiagramsInheritedMembersService;
import org.eclipse.syson.diagram.common.view.services.ViewCreateService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.InterfaceUsage;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.PortUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.springframework.stereotype.Service;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_EXCHANGE_ITEM;

/**
 * Transverse mutation service.
 * This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
@Service
public class TransverseRepresentationMutationService {

    private final ViewCreateService viewCreateService;

    private final TransverseMutationService transverseMutationService;

    private final LibraryServices libraryServices;

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final MetamodelMutationElementService metamodelMutationElementService;



    public TransverseRepresentationMutationService(IReadOnlyObjectPredicate readOnlyService,
            IObjectSearchService objectSearchService,
            ShowDiagramsInheritedMembersService showDiagramsInheritedMembersService, IFeedbackMessageService feedbackMessageService) {
        this.viewCreateService = new ViewCreateService(objectSearchService, readOnlyService, showDiagramsInheritedMembersService, feedbackMessageService);
        this.transverseMutationService = new TransverseMutationService();
        this.libraryServices = new LibraryServices();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.transverseQueryService = new TransverseQueryService();
        this.metamodelMutationElementService = new MetamodelMutationElementService();

    }

    public ItemUsage createNewExchangeItem(Element parent) {
        ItemUsage itemUsage = SysmlFactory.eINSTANCE.createItemUsage();
        this.metamodelMutationElementService.addChildInParent(parent, itemUsage);
        this.libraryServices.typeWithExchangeItem(itemUsage);
        this.elementInitializerSwitch.doSwitch(itemUsage);
        itemUsage.setDeclaredName(ARCADIA_EXCHANGE_ITEM + this.transverseQueryService.existingElementsCount(itemUsage));

        return itemUsage;
    }

    public InterfaceUsage createComponentExchange(Feature source, Feature target) {
        PortUsage sourcePort = this.getOrCreatePort(source, FeatureDirectionKind.OUT);
        PortUsage targetPort = this.getOrCreatePort(target, FeatureDirectionKind.IN);

        if (sourcePort != null && targetPort != null) {
            InterfaceUsage componentExchange = this.viewCreateService.createInterfaceUsage(sourcePort, targetPort);
            this.elementInitializerSwitch.doSwitch(componentExchange);
            this.libraryServices.typeWithArcadiaComponentExchange(componentExchange);
            long existingElementsCount = this.transverseQueryService.existingElementsCount(componentExchange);
            componentExchange.setDeclaredName(ARCADIA_COMPONENT_EXCHANGE + " " + existingElementsCount);
            return componentExchange;

        }
        return null;
    }

    public PortUsage createComponentPort(Feature parent) {
        PortUsage portUsage = this.transverseMutationService.createPortUsage(parent);
        parent.unsetDirection();
        this.libraryServices.typeWithArcadiaComponentPort(portUsage);
        portUsage.setDeclaredName("CP " + this.transverseQueryService.existingElementsCount(portUsage));
        return portUsage;
    }

    private PortUsage getOrCreatePort(Feature port, FeatureDirectionKind direction) {
        PortUsage portUsage = null;
        if (port instanceof PortUsage) {
            portUsage = (PortUsage) port;
        } else if (port instanceof PartUsage) {
            portUsage = this.createComponentPort(port);
            portUsage.setDirection(direction);
        }
        return portUsage;
    }

    public Element createRequirementDescribes(Element source, Element target) {
        // We use the SysON Service here
        return this.viewCreateService.createAllocateEdge(source, target, null, null, null);
    }
}

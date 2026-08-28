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
package org.eclipse.capella.application.configuration.explorer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.sirius.components.collaborative.api.ChangeKind;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.IStatus;
import org.eclipse.sirius.components.representations.Message;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.trees.Tree;
import org.eclipse.sirius.web.application.views.explorer.services.api.IExplorerDropTreeItemExecutor;
import org.eclipse.sirius.web.domain.services.api.IMessageService;
import org.eclipse.syson.services.api.ISysMLMoveElementService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.RequirementUsage;
import org.springframework.stereotype.Service;

/**
 * Executes drop operations in the Capella explorer tree, including validation
 * and semantic moves of model elements.
 *
 * @author ntinsalhi
 */
@Service
public class CapellaExplorerDropTreeItemExecutor implements IExplorerDropTreeItemExecutor {

    private final IObjectSearchService objectSearchService;

    private final IMessageService messageService;

    private final ISysMLMoveElementService moveService;

    private final TransverseQueryService transverseQueryService;

    public CapellaExplorerDropTreeItemExecutor(IObjectSearchService objectSearchService,
                                               IMessageService messageService,
                                               ISysMLMoveElementService moveService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
        this.messageService = Objects.requireNonNull(messageService);
        this.moveService = Objects.requireNonNull(moveService);
        this.transverseQueryService = new TransverseQueryService();
    }

    @Override
    public IStatus drop(IEditingContext editingContext, Tree tree, List<String> droppedElementIds, String targetElementId, int index) {
        List<Element> objectsToMove = this.getObjectsToMove(editingContext, droppedElementIds);
        Optional<Element> targetElement = this.getTargetElement(editingContext, targetElementId);

        return targetElement.map(target -> this.moveObjects(objectsToMove, target))
                .orElse(new Failure(this.messageService.unavailableFeature()));
    }

    private List<Element> getObjectsToMove(IEditingContext editingContext, List<String> droppedElementIds) {
        return droppedElementIds.stream()
                .map(droppedEltId -> this.objectSearchService.getObject(editingContext, droppedEltId))
                .filter(Objects::nonNull)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .toList();
    }

    private Optional<Element> getTargetElement(IEditingContext editingContext,
                                            String targetElementId) {
        return this.objectSearchService.getObject(editingContext, targetElementId)
                .filter(Element.class::isInstance)
                .map(Element.class::cast);
    }

    private IStatus moveObjects(List<Element> objectsToMove, Element targetElement) {
        return objectsToMove.stream()
                .map(droppedElement -> {
                    if (this.isValidTreeItemDrop(droppedElement, targetElement)) {
                        this.moveService.moveSemanticElement(droppedElement, targetElement);
                        return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
                    }

                    return new Failure(this.messageService.invalidDroppedObject());
                })
                .reduce((accStatus, currentStatus) -> {
                    var messages = new ArrayList<Message>();
                    if (accStatus instanceof Success accSuccess) {
                        messages.addAll(accSuccess.getMessages());
                    }
                    if (currentStatus instanceof Failure currentFailure) {
                        messages.addAll(currentFailure.getMessages());
                    }
                    return new Success(messages);
                })
                .orElse(new Failure(this.messageService.invalidDroppedObject()));
    }

    /**
     * Check if the drop is valid for any of the supported element types.
     */
    private boolean isValidTreeItemDrop(Element droppedElement, Element targetElement) {
        return this.isFunctionsTreeItemDrop(droppedElement, targetElement)
                || this.isComponentTreeItemDrop(droppedElement, targetElement)
                || this.isPortTreeItemDrop(droppedElement, targetElement)
                || this.isPackageOrRequirementTreeItemDrop(droppedElement, targetElement);
    }

    private boolean isPortTreeItemDrop(Element droppedElement, Element targetElement) {
        return this.isComponentPortTreeItemDrop(droppedElement, targetElement)
                || this.isFunctionPortTreeItemDrop(droppedElement, targetElement);
    }

    private boolean isPackageOrRequirementTreeItemDrop(Element droppedElement, Element targetElement) {
        return this.isPackageTreeItemDrop(droppedElement, targetElement)
                || this.isRequirementTreeItemDrop(droppedElement, targetElement);
    }

    private IStatus functionTreeItemDrop(Element droppedElement,
                                         Element targetElement) {

        if (this.transverseQueryService.isFunction(droppedElement)
                && (this.transverseQueryService.isFunction(targetElement)
                || this.transverseQueryService.isFunctionsPackage(targetElement))) {
            this.moveService.moveSemanticElement(droppedElement, targetElement);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }

        return new Failure(this.messageService.invalidDroppedObject());
    }

    private boolean isFunctionsTreeItemDrop(Element droppedElement,
                                            Element targetElement) {
        return this.transverseQueryService.isFunction(droppedElement)
                && (this.transverseQueryService.isFunction(targetElement)
                || this.transverseQueryService.isFunctionsPackage(targetElement));
    }

    private IStatus componentTreeItemDrop(Element droppedElement, Element targetElement) {
        if (this.transverseQueryService.isComponent(droppedElement)) {
            this.moveService.moveSemanticElement(droppedElement, targetElement);
            return new Success(ChangeKind.SEMANTIC_CHANGE, Map.of());
        }

        return new Failure(this.messageService.invalidDroppedObject());
    }

    private boolean isComponentTreeItemDrop(Element droppedElement,
                                            Element targetElement) {
        return this.transverseQueryService.isComponent(droppedElement)
                && (this.transverseQueryService.isComponent(targetElement)
                || this.transverseQueryService.isStructurePackage(targetElement));
    }

    private boolean isComponentPortTreeItemDrop(Element droppedElement,
                                                Element targetElement) {
        return this.transverseQueryService.isComponentPort(droppedElement)
                && !this.transverseQueryService.isStructurePackage(targetElement)
                && this.transverseQueryService.isComponent(targetElement);
    }

    private boolean isFunctionPortTreeItemDrop(Element droppedElement,
                                                Element targetElement) {
        return this.transverseQueryService.isExchangeItem(droppedElement)
                && !this.transverseQueryService.isFunctionsPackage(targetElement)
                && this.transverseQueryService.isFunction(targetElement);
    }

    /**
     * Checks if a Package can be dropped into a target element.
     * Packages can be dropped into:
     * - Requirements package (to organize requirements with sub-packages)
     * - Another user Package (for nesting)
     */
    private boolean isPackageTreeItemDrop(Element droppedElement, Element targetElement) {
        return this.transverseQueryService.isUserPackage(droppedElement)
                && (this.transverseQueryService.isRequirementsPackage(targetElement)
                    || this.transverseQueryService.isUserPackage(targetElement));
    }

    /**
     * Checks if a RequirementUsage can be dropped into a target element.
     * Requirements can be dropped into:
     * - Requirements package (top-level or nested)
     * - User Package (for organization)
     */
    private boolean isRequirementTreeItemDrop(Element droppedElement, Element targetElement) {
        return droppedElement instanceof RequirementUsage
                && (this.transverseQueryService.isRequirementsPackage(targetElement)
                    || this.transverseQueryService.isUserPackage(targetElement));
    }
}

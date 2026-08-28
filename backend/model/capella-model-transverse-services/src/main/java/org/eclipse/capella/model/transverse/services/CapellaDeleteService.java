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

package org.eclipse.capella.model.transverse.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.util.SysONEcoreUtil;

/**
 * Deletion-related Java services.
 * <p>
 * This service applies Capella-specific logic on top of SysON's {@link org.eclipse.syson.services.DeleteService}. See {@link CapellaRelatedElementsSwitch} for more information.
 * </p>
 *
 * @author gdaniel
 */
public class CapellaDeleteService {

    /**
     * Deletes the provided {@code element} and cleans up the related elements that needs to.
     *
     * @param element
     *         the element to delete
     * @return the deleted element
     */
    public Element deleteFromModel(Element element) {
        Set<EObject> elementsToDelete = new LinkedHashSet<>();
        Set<EObject> relatedElements = new HashSet<>();
        if (element.eContainer() instanceof Membership membership) {
            elementsToDelete.add(membership);
            this.collectRelatedElements(membership, relatedElements);
        } else {
            elementsToDelete.add(element);
        }
        this.collectRelatedElements(element, relatedElements);
        element.eAllContents().forEachRemaining(eObject -> this.collectRelatedElements(eObject, relatedElements));

        elementsToDelete.addAll(relatedElements);
        SysONEcoreUtil.deleteAll(elementsToDelete, true);
        return element;
    }

    private void collectRelatedElements(EObject eObject, Set<EObject> relatedElements) {
        var referenceAdapter = ECrossReferenceAdapter.getCrossReferenceAdapter(eObject);
        if (referenceAdapter != null) {
            Collection<EStructuralFeature.Setting> inverseReferences = referenceAdapter.getInverseReferences(eObject);
            for (EStructuralFeature.Setting setting : inverseReferences) {
                EObject relatedElement = setting.getEObject();
                Set<EObject> collectedElements = new CapellaRelatedElementsSwitch(setting.getEStructuralFeature()).doSwitch(relatedElement);
                for (EObject collectedElement : collectedElements) {
                    boolean notAlreadyContained = relatedElements.add(collectedElement);
                    if (notAlreadyContained) {
                        this.collectRelatedElements(collectedElement, relatedElements);
                    }
                }
            }
        }
    }
}

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

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureMembership;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.impl.SysmlPackageImpl;

/**
 * @author adieumegard
 */
public class ArcadiaElementContentAdapter extends EContentAdapter {

    private final Map<String, List<Element>> cache = new LinkedHashMap<>();

    private final Map<Element, String> reverseCache = new LinkedHashMap<>();

    private final UtilService utilService;

    private static ArcadiaElementContentAdapter INSTANCE;

    private ArcadiaElementContentAdapter() {
        this.utilService = new UtilService();
    }

    public static ArcadiaElementContentAdapter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ArcadiaElementContentAdapter();
        }
        return INSTANCE;
    }

    public Map<String, List<Element>> getCache() {
        return this.cache;
    }

    public Map<Element, String> getReverseCache() {
        return this.reverseCache;
    }

    @Override
    public void notifyChanged(Notification notification) {
        if (notification.getEventType() == Notification.SET && notification.getNotifier() instanceof FeatureTyping ft
                && notification.getFeatureID(FeatureTyping.class) == SysmlPackageImpl.FEATURE_TYPING__TYPE) {
            Element newTypeValue = (Element) notification.getNewValue();
            Element oldTypeValue = (Element) notification.getOldValue();

            Element owningElement = this.utilService.getOwningElement(ft);
            if (!(owningElement.eContainer() instanceof FeatureMembership)) {
                // Remove
                List<Element> value;
                if (oldTypeValue != null) {
                    String oldQualifiedName = oldTypeValue.getQualifiedName();
                    if (this.cache.containsKey(oldQualifiedName)) {
                        value = this.cache.get(oldQualifiedName);
                        value.remove(owningElement);
                        this.cache.put(oldQualifiedName, value);
                    }
                }

                String newQualifiedName = newTypeValue.getQualifiedName();
                if (newQualifiedName.startsWith(ARCADIA_PREFIX)) {
                    this.setCachesValuesForElement(owningElement, newQualifiedName);
                }
            }
        }
        super.notifyChanged(notification);
    }

    @Override
    protected void removeAdapter(Notifier notifier) {
        if (notifier instanceof FeatureTyping ft) {
            Element owningElement = this.utilService.getOwningElement(ft);
            Element oldTypeValue = ft.getType();
            if (oldTypeValue != null) {
                List<Element> value;
                String oldQualifiedName = oldTypeValue.getQualifiedName();
                if (this.cache.containsKey(oldQualifiedName)) {
                    value = this.cache.get(oldQualifiedName);
                    value.remove(owningElement);
                    this.cache.put(oldQualifiedName, value);
                }
                this.reverseCache.remove(owningElement);
            }
        }
        super.removeAdapter(notifier);
    }

    /**
     * @param owningElement
     * @param newQualifiedName
     */
    private void setCachesValuesForElement(Element owningElement, String newQualifiedName) {
        List<Element> value;
        // Add
        if (this.cache.containsKey(newQualifiedName)) {
            value = this.cache.get(newQualifiedName);
        } else {
            value = new ArrayList<>();
        }
        value.add(owningElement);
        this.cache.put(newQualifiedName, value);

        // Update reverse cache
        this.reverseCache.put(owningElement, newQualifiedName);
    }

    @Override
    public boolean isAdapterForType(Object type) {
        return ArcadiaElementContentAdapter.class.equals(type);
    }

    /**
     * Initialize adapter caches
     *
     * @param targetResourceSet
     *            The Capella project Resource set
     */
    public void init(ResourceSet targetResourceSet) {
        this.cache.clear();
        this.reverseCache.clear();
        var userResources = targetResourceSet.getResources().stream()
                .filter(r -> r.getURI().toString().startsWith(IEMFEditingContext.RESOURCE_SCHEME))
                .toList();
        for (Resource resource : userResources) {
            var contents = resource.getContents();
            for (EObject root : contents) {
                TreeIterator<EObject> iter = root.eAllContents();
                while(iter.hasNext()) {
                    var elem = iter.next();
                    if (elem instanceof Feature feature && !(feature.eContainer() instanceof FeatureMembership)) {
                        Optional<String> optType = feature.getType().stream().map(Element::getQualifiedName).filter(name -> name != null && name.startsWith(ARCADIA_PREFIX)).findFirst();
                        if (optType.isPresent()) {
                            this.setCachesValuesForElement(feature, optType.get());
                        }
                    }
                }
            }
        }
    }

}

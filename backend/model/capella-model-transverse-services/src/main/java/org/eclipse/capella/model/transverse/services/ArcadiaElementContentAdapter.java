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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Maintains both directions of the declared business type index for one resource set.
 * Each feature has at most one business type. Access follows the editing context's execution model.
 *
 * @author Jerome Gout
 */
public class ArcadiaElementContentAdapter extends EContentAdapter {

    private final Map<String, Set<Feature>> cache = new LinkedHashMap<>();

    private final Map<Feature, String> reverseCache = new LinkedHashMap<>();

    private final TypeQueryService typeQueryService = new TypeQueryService();

    private final ResourceSet resourceSet;

    public ArcadiaElementContentAdapter(ResourceSet resourceSet) {
        this.resourceSet = Objects.requireNonNull(resourceSet);
    }

    @Override
    public boolean isAdapterForType(Object type) {
        return ArcadiaElementContentAdapter.class.equals(type);
    }

    public Optional<String> getType(Feature feature) {
        return Optional.ofNullable(this.reverseCache.get(feature));
    }

    public List<Feature> getElements(Resource resource, EClass elementClass, String qualifiedName) {
        return this.cache.getOrDefault(qualifiedName, Set.of()).stream()
                .filter(feature -> feature.eResource() == resource)
                .filter(elementClass::isInstance)
                .toList();
    }

    @Override
    public void setTarget(Notifier target) {
        if (target instanceof Feature feature) {
            this.updateType(feature);
        }
        super.setTarget(target);
    }

    @Override
    public void unsetTarget(Notifier target) {
        super.unsetTarget(target);
        if (target instanceof Feature feature) {
            this.removeType(feature);
        }
    }

    @Override
    protected boolean resolve() {
        return false;
    }

    @Override
    public void notifyChanged(Notification notification) {
        super.notifyChanged(notification);
        if (!notification.isTouch()) {
            if (notification.getNotifier() instanceof Feature feature) {
                this.updateType(feature);
            } else if (notification.getNotifier() instanceof FeatureTyping typing && typing.eContainer() instanceof Feature owner) {
                this.updateType(owner);
            }
            if (this.affectsTypeNames(notification)) {
                // Refresh adapted features without traversing or resolving the resource set.
                List.copyOf(this.reverseCache.keySet()).forEach(this::updateType);
            }
        }
    }

    private boolean affectsTypeNames(Notification notification) {
        boolean renamed = notification.getNotifier() instanceof Namespace namespace
                && notification.getFeature() == SysmlPackage.eINSTANCE.getElement_DeclaredName()
                && this.affectsDeclaredTypeNames(namespace);
        boolean namespaceMoved = this.isContainmentChange(notification)
                && (this.containsNamespace(notification.getNewValue()) || this.containsNamespace(notification.getOldValue()));
        return renamed || this.isResourceLoadStateChanged(notification) || namespaceMoved;
    }

    private boolean isResourceLoadStateChanged(Notification notification) {
        return notification.getNotifier() instanceof Resource resource
                && notification.getFeatureID(Resource.class) == Resource.RESOURCE__IS_LOADED
                && (Boolean.FALSE.equals(notification.getNewValue()) || this.containsNamespace(resource.getContents()));
    }

    private boolean isContainmentChange(Notification notification) {
        boolean result;
        if (notification.getNotifier() instanceof Resource) {
            result = notification.getFeatureID(Resource.class) == Resource.RESOURCE__CONTENTS;
        } else if (notification.getNotifier() instanceof ResourceSet) {
            result = notification.getFeatureID(ResourceSet.class) == ResourceSet.RESOURCE_SET__RESOURCES;
        } else {
            result = notification.getFeature() instanceof EReference reference && reference.isContainment();
        }
        return result;
    }

    private boolean affectsDeclaredTypeNames(Namespace namespace) {
        return !(namespace instanceof Feature) || this.containsNamespace(namespace.getOwnedRelationship());
    }

    private boolean containsNamespace(Object value) {
        boolean result = value instanceof Namespace namespace && this.affectsDeclaredTypeNames(namespace);
        if (value instanceof Resource resource) {
            result = this.containsNamespace(resource.getContents());
        } else if (value instanceof Membership membership) {
            result = this.containsNamespace(membership.getOwnedRelatedElement());
        } else if (value instanceof InternalEList<?> list) {
            var contents = list.basicIterator();
            while (!result && contents.hasNext()) {
                result = this.containsNamespace(contents.next());
            }
        } else if (value instanceof Collection<?> collection) {
            result = collection.stream().anyMatch(this::containsNamespace);
        }
        return result;
    }

    private void updateType(Feature feature) {
        if (feature.eResource() != null && feature.eResource().getResourceSet() == this.resourceSet) {
            String qualifiedName = this.typeQueryService.getDeclaredType(feature).orElse(null);
            String previousType = this.reverseCache.put(feature, qualifiedName);
            if (!Objects.equals(previousType, qualifiedName)) {
                if (previousType != null) {
                    this.removeAssociation(feature, previousType);
                }
                if (qualifiedName != null) {
                    this.cache.computeIfAbsent(qualifiedName, key -> new LinkedHashSet<>()).add(feature);
                }
            }
        }
    }

    private void removeType(Feature feature) {
        var previousType = this.reverseCache.remove(feature);
        if (previousType != null) {
            this.removeAssociation(feature, previousType);
        }
    }

    private void removeAssociation(Feature feature, String previousType) {
        var features = this.cache.get(previousType);
        features.remove(feature);
        if (features.isEmpty()) {
            this.cache.remove(previousType);
        }
    }
}

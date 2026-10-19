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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Queries the single explicitly declared business type of Capella features.
 * Prefers Arcadia types and SysML::ExchangeItem over other declared types, regardless of declaration order.
 * Does not infer types through subsetting, redefinition or feature chaining.
 * Resolves references only against resources already present in the resource set, without demand loading.
 *
 * @author Jerome Gout
 */
public class TypeQueryService {

    private final UtilService utilService = new UtilService();

    public Optional<String> getType(Feature feature) {
        var adapter = this.getAdapter(feature);
        if (adapter.isPresent()) {
            return adapter.get().getType(feature);
        }
        return this.getDeclaredType(feature);
    }

    public List<Feature> getElements(EObject context, EClass elementClass, String qualifiedName) {
        var adapter = this.getAdapter(context);
        if (adapter.isPresent()) {
            return adapter.get().getElements(context.eResource(), elementClass, qualifiedName);
        }
        return this.utilService.getAllReachable(context, elementClass).stream()
                .filter(element -> element.eResource() == context.eResource())
                .filter(Feature.class::isInstance)
                .map(Feature.class::cast)
                .filter(feature -> this.getDeclaredType(feature).filter(qualifiedName::equals).isPresent())
                .toList();
    }

    public Optional<String> getDeclaredType(Feature feature) {
        var declaredTypes = feature.getOwnedRelationship().stream()
                .filter(FeatureTyping.class::isInstance)
                .map(FeatureTyping.class::cast)
                .filter(typing -> !typing.isIsImplied() && this.getLoadedReference(typing, SysmlPackage.eINSTANCE.getFeatureTyping_TypedFeature()) == feature)
                .map(typing -> this.getLoadedReference(typing, SysmlPackage.eINSTANCE.getFeatureTyping_Type()))
                .filter(Element.class::isInstance)
                .map(Element.class::cast)
                .map(Element::getQualifiedName)
                .filter(Objects::nonNull)
                .toList();
        return declaredTypes.stream()
                .filter(this::isBusinessType)
                .findFirst()
                .or(() -> declaredTypes.stream().findFirst());
    }

    private boolean isBusinessType(String name) {
        return name.startsWith(CommonQueryService.ARCADIA_PREFIX) || "SysML::ExchangeItem".equals(name);
    }

    private EObject getLoadedReference(EObject owner, EReference reference) {
        var value = (EObject) owner.eGet(reference, false);
        if (value instanceof InternalEObject proxy && proxy.eIsProxy()) {
            value = null;
            var resource = owner.eResource();
            if (resource != null && resource.getResourceSet() != null) {
                value = resource.getResourceSet().getEObject(proxy.eProxyURI().resolve(resource.getURI()), false);
            }
        }
        return value;
    }

    private Optional<ArcadiaElementContentAdapter> getAdapter(EObject element) {
        if (element.eResource() != null && element.eResource().getResourceSet() != null) {
            return Optional.ofNullable(EcoreUtil.getAdapter(element.eResource().getResourceSet().eAdapters(), ArcadiaElementContentAdapter.class))
                    .map(ArcadiaElementContentAdapter.class::cast);
        }
        return Optional.empty();
    }
}

/*******************************************************************************
 * Copyright (c) 2025, 2026 Obeo.
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
package org.eclipse.capella.model.services.logical.architecture;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.syson.sysml.Annotation;
import org.eclipse.syson.sysml.Comment;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Logical Architecture (LA) related query service. It is important to note that this service must retain its empty
 * constructor and should not have constructors with parameters.
 *
 * @author frouene
 */
public class LAQueryService {

    private final TransverseQueryService transverseQueryService;

    public LAQueryService() {
        this.transverseQueryService = new TransverseQueryService();

    }

    public List<Comment> getComments(EObject eObject) {
        var allComments = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getComment());
        return allComments.stream()
                .filter(Comment.class::isInstance)
                .map(Comment.class::cast)
                .toList();
    }

    /**
     * Get all Annotations owned by a Comment.
     */
    public List<Annotation> getAnnotations(Comment comment) {
        return comment.getOwnedRelationship().stream()
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .toList();
    }

    /**
     * Get all Annotations in the model (for displaying annotation edges).
     */
    public List<Annotation> getAllAnnotations(EObject eObject) {
        var allAnnotations = this.transverseQueryService.getAllReachableInResource(eObject, SysmlPackage.eINSTANCE.getAnnotation());
        return allAnnotations.stream()
                .filter(Annotation.class::isInstance)
                .map(Annotation.class::cast)
                .toList();
    }

    private Predicate<? super Feature> isTypedWith(String qualifiedName) {
        return element -> element.getType().stream().anyMatch(t -> t != null && qualifiedName != null && qualifiedName.equals(t.getQualifiedName()));
    }
}

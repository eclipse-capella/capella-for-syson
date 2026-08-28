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
package org.eclipse.capella.model.services.logical.architecture;

import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.syson.sysml.Annotation;
import org.eclipse.syson.sysml.Comment;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.RequirementUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.metamodel.services.ElementInitializerSwitch;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;


/**
 * Logical Architecture (LA) related mutation service. This class only concerns representation related services, it may depend on other beans or the editingContext.
 *
 * @author frouene
 */
public class LARepresentationMutationService {

    private static final String WHITE_SPACE = " ";

    private final ElementInitializerSwitch elementInitializerSwitch;

    private final TransverseQueryService transverseQueryService;

    private final MetamodelMutationElementService metamodelMutationElementService;

    public LARepresentationMutationService() {
        this.transverseQueryService = new TransverseQueryService();
        this.elementInitializerSwitch = new ElementInitializerSwitch();
        this.metamodelMutationElementService = new MetamodelMutationElementService();
    }

    /**
     * Creates a RequirementUsage directly inside the provided package/container.
     *
     * @param parent
     *         the selected package/container element
     * @return the newly created requirement
     */
    public RequirementUsage createRequirementInPackage(Element parent) {
        String name = "Requirement";

        RequirementUsage requirementUsage = SysmlFactory.eINSTANCE.createRequirementUsage();
        this.metamodelMutationElementService.addChildInParent(parent, requirementUsage);
        this.elementInitializerSwitch.doSwitch(requirementUsage);

        long existingElementsCount = this.transverseQueryService.existingElementsCount(requirementUsage);
        requirementUsage.setDeclaredName(name + WHITE_SPACE + existingElementsCount);
        return requirementUsage;
    }

    /**
     * Creates a new SysML Package as a child of the given parent.
     *
     * @param parent
     *         the parent element
     * @return the newly created Package
     */
    public Package createPackage(Element parent) {
        String name = "Package";

        Package pkg = SysmlFactory.eINSTANCE.createPackage();
        this.metamodelMutationElementService.addChildInParent(parent, pkg);
        this.elementInitializerSwitch.doSwitch(pkg);

        long existingElementsCount = this.transverseQueryService.existingElementsCount(pkg);
        pkg.setDeclaredName(name + WHITE_SPACE + existingElementsCount);
        return pkg;
    }

    public Comment createComment(Element parent) {
        Comment comment = SysmlFactory.eINSTANCE.createComment();
        comment.setBody("New comment");
        this.metamodelMutationElementService.addChildInParent(parent, comment);
        this.elementInitializerSwitch.doSwitch(comment);
        return comment;
    }

    /**
     * Creates an Annotation linking a Comment to an Element it annotates.
     * @param comment the Comment (source)
     * @param target the Element being annotated (target)
     * @return the created Annotation
     */
    public Annotation createCommentLink(Comment comment, Element target) {
        Annotation annotation = SysmlFactory.eINSTANCE.createAnnotation();
        annotation.setAnnotatedElement(target);
        comment.getOwnedRelationship().add(annotation);
        return annotation;
    }
}

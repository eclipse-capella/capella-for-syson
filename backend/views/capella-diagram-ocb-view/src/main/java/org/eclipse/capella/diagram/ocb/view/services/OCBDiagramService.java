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
package org.eclipse.capella.diagram.ocb.view.services;

import java.util.Objects;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;

/**
 * Evaluates OCB tool availability for semantic sources and graphical connector targets.
 *
 * @author tbezierslafosse
 */
public class OCBDiagramService {

    private final IObjectSearchService objectSearchService;

    private final CommonQueryService commonQueryService = new CommonQueryService();

    public OCBDiagramService(IObjectSearchService objectSearchService) {
        this.objectSearchService = Objects.requireNonNull(objectSearchService);
    }

    /**
     * Returns {@code true} if an OCB diagram can be created on {@code self}.
     * <p>
     * OCB diagrams can be created on the capabilities package or its sub-packages in the <i>operational analysis</i> perspective.
     *
     * @param self
     *         the element to check
     * @return {@code true} if the diagram can be created, {@code false} otherwise
     */
    public boolean canCreateOCBDiagram(Element self) {
        return this.commonQueryService.isInPerspective(self, ArcadiaEngineeringPerspective.OperationalAnalysis)
                && (this.commonQueryService.isCapabilitiesPackage(self)
                || this.isCapabilitiesSubPackage(self));
    }

    /**
     * Checks that Involvement starts from a Capability and targets an Entity or Actor when an endpoint is selected.
     * Like Generalization, an explicit {@code null} target denotes source-side availability in the node palette.
     *
     * @param receiver the semantic source or its graphical node
     * @param semanticTarget the selected semantic target, or {@code null} in the node palette
     * @param editingContext the editing context used to resolve graphical receivers
     * @return whether the tool is available in the current palette context
     */
    public boolean canCreateInvolvement(Object receiver, Object semanticTarget, IEditingContext editingContext) {
        if (!this.commonQueryService.isCapability(this.getSemanticElement(receiver, editingContext))) {
            return false;
        }
        return semanticTarget == null || (semanticTarget instanceof EObject target && this.commonQueryService.isComponent(target));
    }

    /**
     * Checks the Generalization source and, when selected, its semantic target.
     * A {@code null} target denotes the node palette before an endpoint is selected, not a valid connector endpoint.
     * The OCB palette variable provider supplies that explicit {@code null} because an absent AQL variable is an error.
     *
     * @param receiver the semantic source or its graphical node
     * @param semanticTarget the selected semantic target, or {@code null} in the node palette
     * @param editingContext the editing context used to resolve graphical receivers
     * @return whether the tool is available in the current palette context
     */
    public boolean canCreateGeneralization(Object receiver, Object semanticTarget, IEditingContext editingContext) {
        var semanticSource = this.getSemanticElement(receiver, editingContext);
        if (!this.commonQueryService.isCapability(semanticSource)) {
            return false;
        }
        return semanticTarget == null || (semanticSource != semanticTarget && semanticTarget instanceof EObject target
                && this.commonQueryService.isCapability(target));
    }

    private EObject getSemanticElement(Object receiver, IEditingContext editingContext) {
        EObject semanticElement = null;
        if (receiver instanceof EObject eObject) {
            semanticElement = eObject;
        } else if (receiver instanceof Node node) {
            semanticElement = this.objectSearchService.getObject(editingContext, node.getTargetObjectId())
                    .filter(EObject.class::isInstance)
                    .map(EObject.class::cast)
                    .orElse(null);
        }
        return semanticElement;
    }

    private boolean isCapabilitiesSubPackage(Element self) {
        return self instanceof Package
                && EMFUtils.getFirstAncestor(Package.class, self, this.commonQueryService::isCapabilitiesPackage).isPresent();
    }
}

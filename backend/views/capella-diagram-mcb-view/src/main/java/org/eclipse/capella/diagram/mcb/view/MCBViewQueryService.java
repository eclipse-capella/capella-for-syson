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
package org.eclipse.capella.diagram.mcb.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.syson.services.UtilService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;

/**
 * Read-only semantic candidates and parent paths used by MCB nodes.
 *
 * @author tbezierslafosse
 */
public class MCBViewQueryService {

    private final CommonQueryService commonQueryService = new CommonQueryService();

    private final UtilService utilService = new UtilService();

    public List<Element> getMissionCandidates(EObject context) {
        return this.commonQueryService.getMissions(context).stream().filter(this::isInSystemAnalysis).map(Element.class::cast).toList();
    }

    public List<Element> getCapabilityCandidates(EObject context) {
        return this.utilService.getAllReachable(context, SysmlPackage.eINSTANCE.getOccurrenceUsage()).stream()
                .filter(this.commonQueryService::isCapability).filter(Element.class::isInstance).map(Element.class::cast)
                .filter(this::isInSystemAnalysis).toList();
    }

    public List<Element> getRequirementCandidates(EObject context) {
        return this.utilService.getAllReachable(context, SysmlPackage.eINSTANCE.getRequirementUsage()).stream()
                .filter(Element.class::isInstance).map(Element.class::cast).filter(this::isInSystemAnalysis)
                .filter(element -> this.commonQueryService.getRequirementsPackage(element).filter(parent -> EcoreUtil.isAncestor(parent, element)).isPresent())
                .toList();
    }

    public List<Element> getActorCandidates(EObject context) {
        if (context instanceof PartUsage actor) {
            return actor.getOwnedElement().stream().filter(PartUsage.class::isInstance).map(PartUsage.class::cast)
                    .filter(child -> !this.getActorPath(child).isEmpty()).map(Element.class::cast).toList();
        }
        return this.utilService.getAllReachable(context, SysmlPackage.eINSTANCE.getPartUsage()).stream()
                .filter(PartUsage.class::isInstance).map(PartUsage.class::cast)
                .filter(actor -> !(actor.getOwner() instanceof PartUsage)).filter(actor -> !this.getActorPath(actor).isEmpty())
                .map(Element.class::cast).toList();
    }

    public boolean canCreateActor(Element context) {
        return this.commonQueryService.isSystemAnalysisCapabilitiesPackage(context) || this.commonQueryService.canContainSystemActor(context);
    }

    /**
     * Returns the full valid actor path, from the top-level actor to the requested actor.
     */
    public List<PartUsage> getActorPath(PartUsage actor) {
        var path = new ArrayList<PartUsage>();
        PartUsage current = actor;
        while (this.commonQueryService.isSystemAnalysisActor(current)) {
            path.add(current);
            if (current.getOwner() instanceof PartUsage parent && this.commonQueryService.canContainSystemActor(parent)) {
                current = parent;
            } else if (this.commonQueryService.canContainSystemActor(current.getOwner())) {
                Collections.reverse(path);
                return path;
            } else {
                break;
            }
        }
        return List.of();
    }

    public boolean isReachableNode(Element element, Element context) {
        if (element instanceof PartUsage actor) {
            return !this.getActorPath(actor).isEmpty() && this.utilService.getAllReachable(context, SysmlPackage.eINSTANCE.getPartUsage()).contains(actor);
        }
        return this.getMissionCandidates(context).contains(element) || this.getCapabilityCandidates(context).contains(element)
                || this.getRequirementCandidates(context).contains(element);
    }

    private boolean isInSystemAnalysis(Element element) {
        return this.commonQueryService.getArcadiaPerspective(element).filter(ArcadiaEngineeringPerspective.SystemAnalysis::equals).isPresent();
    }
}

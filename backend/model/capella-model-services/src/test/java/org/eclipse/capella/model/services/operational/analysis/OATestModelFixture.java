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
package org.eclipse.capella.model.services.operational.analysis;

import org.eclipse.capella.model.transverse.services.ArcadiaEngineeringPerspective;
import org.eclipse.capella.model.transverse.services.TransverseQueryService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.InterfaceDefinition;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.RequirementDefinition;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.util.SysONEContentAdapter;

/**
 * Shared SysML model fixture for Operational Analysis service tests.
 *
 * @author fbarbin
 */
class OATestModelFixture {

    Package createRootPackage() {
        ResourceSet resourceSet = new ResourceSetImpl();
        Resource resource = new ResourceImpl(URI.createURI("model.sysml"));
        resourceSet.getResources().add(resource);

        Package root = SysmlFactory.eINSTANCE.createPackage();
        root.eAdapters().add(new SysONEContentAdapter());
        root.setDeclaredName("Arcadia");
        resource.getContents().add(root);

        return root;
    }

    OperationalAnalysisPackages createOperationalAnalysisPackages(Package root) {
        Package operationalAnalysisPackage = this.createPackage(root, ArcadiaEngineeringPerspective.OperationalAnalysis.getLabel());
        Package structurePackage = this.createPackage(operationalAnalysisPackage, TransverseQueryService.STRUCTURE_PACKAGE);
        Package requirementsPackage = this.createPackage(operationalAnalysisPackage, TransverseQueryService.REQUIREMENTS_PACKAGE);
        return new OperationalAnalysisPackages(operationalAnalysisPackage, structurePackage, requirementsPackage);
    }

    Package createPackage(Element owner, String name) {
        Package packageElement = this.addOwnedElement(owner, SysmlFactory.eINSTANCE.createPackage());
        packageElement.setDeclaredName(name);
        return packageElement;
    }

    PartUsage createPartUsage(Element owner, String name) {
        PartUsage partUsage = this.addOwnedElement(owner, SysmlFactory.eINSTANCE.createPartUsage());
        partUsage.setDeclaredName(name);
        return partUsage;
    }

    PartUsage createArcadiaTypedComponent(Element owner, String name) {
        PartUsage component = this.createPartUsage(owner, name);
        this.setType(component, this.getOrCreatePartDefinition(this.getRoot(owner), TransverseQueryService.ARCADIA_COMPONENT));
        return component;
    }

    void createArcadiaComponentAttributes(Package root) {
        PartDefinition componentDefinition = this.getOrCreatePartDefinition(root, TransverseQueryService.ARCADIA_COMPONENT);
        AttributeUsage isActor = this.addOwnedElement(componentDefinition, SysmlFactory.eINSTANCE.createAttributeUsage());
        isActor.setDeclaredName(TransverseQueryService.ARCADIA_IS_ACTOR);

        AttributeUsage isHuman = this.addOwnedElement(componentDefinition, SysmlFactory.eINSTANCE.createAttributeUsage());
        isHuman.setDeclaredName(TransverseQueryService.ARCADIA_IS_HUMAN);
    }

    void createArcadiaComponentExchangeDefinition(Package root) {
        this.getOrCreateInterfaceDefinition(root, TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE);
    }

    void createArcadiaRequirementDefinition(Package root) {
        this.getOrCreateRequirementDefinition(root, TransverseQueryService.ARCADIA_REQUIREMENT);
    }

    private InterfaceDefinition getOrCreateInterfaceDefinition(Package parent, String name) {
        return parent.getOwnedElement().stream()
                .filter(InterfaceDefinition.class::isInstance)
                .map(InterfaceDefinition.class::cast)
                .filter(definition -> name.equals(definition.getDeclaredName()))
                .findFirst()
                .orElseGet(() -> {
                    InterfaceDefinition interfaceDefinition = this.addOwnedElement(parent, SysmlFactory.eINSTANCE.createInterfaceDefinition());
                    interfaceDefinition.setDeclaredName(name);
                    return interfaceDefinition;
                });
    }

    private RequirementDefinition getOrCreateRequirementDefinition(Package parent, String name) {
        return parent.getOwnedElement().stream()
                .filter(RequirementDefinition.class::isInstance)
                .map(RequirementDefinition.class::cast)
                .filter(definition -> name.equals(definition.getDeclaredName()))
                .findFirst()
                .orElseGet(() -> {
                    RequirementDefinition requirementDefinition = this.addOwnedElement(parent, SysmlFactory.eINSTANCE.createRequirementDefinition());
                    requirementDefinition.setDeclaredName(name);
                    return requirementDefinition;
                });
    }

    private PartDefinition getOrCreatePartDefinition(Package parent, String name) {
        return parent.getOwnedElement().stream()
                .filter(PartDefinition.class::isInstance)
                .map(PartDefinition.class::cast)
                .filter(definition -> name.equals(definition.getDeclaredName()))
                .findFirst()
                .orElseGet(() -> {
                    PartDefinition partDefinition = this.addOwnedElement(parent, SysmlFactory.eINSTANCE.createPartDefinition());
                    partDefinition.setDeclaredName(name);
                    return partDefinition;
                });
    }

    private void setType(Feature feature, Type type) {
        FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        featureTyping.setTypedFeature(feature);
        featureTyping.setType(type);
        feature.getOwnedRelationship().add(featureTyping);
    }

    private <T extends Element> T addOwnedElement(Element owner, T ownedElement) {
        Membership membership = SysmlFactory.eINSTANCE.createOwningMembership();
        if (owner instanceof Type && ownedElement instanceof Feature) {
            membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        }
        owner.getOwnedRelationship().add(membership);
        membership.getOwnedRelatedElement().add(ownedElement);
        membership.setMemberElement(ownedElement);
        return ownedElement;
    }

    private Package getRoot(Element element) {
        Element current = element;
        while (current.eContainer() instanceof Element parent) {
            current = parent;
        }
        return (Package) current;
    }

    record OperationalAnalysisPackages(Package operationalAnalysisPackage, Package structurePackage, Package requirementsPackage) {
    }
}

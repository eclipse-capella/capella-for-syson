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
package org.eclipse.capella.model.services.logical.architecture;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreAdapterFactory;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.syson.sysml.ActionDefinition;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.AttributeUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.EndFeatureMembership;
import org.eclipse.syson.sysml.EnumerationDefinition;
import org.eclipse.syson.sysml.EnumerationUsage;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureReferenceExpression;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.FeatureValue;
import org.eclipse.syson.sysml.FlowEnd;
import org.eclipse.syson.sysml.FlowUsage;
import org.eclipse.syson.sysml.ItemDefinition;
import org.eclipse.syson.sysml.ItemUsage;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.MetadataDefinition;
import org.eclipse.syson.sysml.Package;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.Redefinition;
import org.eclipse.syson.sysml.ReferenceUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.Type;
import org.eclipse.syson.sysml.VariantMembership;
import org.eclipse.syson.sysml.metamodel.services.MetamodelMutationElementService;
import org.eclipse.syson.util.SysONEContentAdapter;

/**
 * Shared SysML model fixture for Logical Architecture service tests.
 *
 * @author fbarbin
 */
class LATestModelFixture {

    private final MetamodelMutationElementService metamodelMutationElementService = new MetamodelMutationElementService();

    Package createRootPackage() {
        return this.createRootPackage("Arcadia");
    }

    Package createRootPackage(String rootPackageName) {
        ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.eAdapters().add(new ECrossReferenceAdapter());
        Resource resource = new ResourceImpl(URI.createURI("model.sysml"));
        resourceSet.getResources().add(resource);

        Package root = SysmlFactory.eINSTANCE.createPackage();
        root.eAdapters().add(new SysONEContentAdapter());
        root.setDeclaredName(rootPackageName);
        resource.getContents().add(root);
        return root;
    }

    Package createPackage(Element parent, String name) {
        Package packageElement = this.addOwnedElement(parent, SysmlFactory.eINSTANCE.createPackage());
        packageElement.setDeclaredName(name);
        return packageElement;
    }

    ActionUsage createActionUsage(Element owner, String name) {
        ActionUsage actionUsage = this.addOwnedElement(owner, SysmlFactory.eINSTANCE.createActionUsage());
        actionUsage.setDeclaredName(name);
        return actionUsage;
    }

    PartUsage createPartUsage(Element owner, String name) {
        PartUsage partUsage = this.addOwnedElement(owner, SysmlFactory.eINSTANCE.createPartUsage());
        partUsage.setDeclaredName(name);
        return partUsage;
    }

    FlowUsage createFlowUsage(Element owner, String name) {
        FlowUsage flowUsage = this.addOwnedElement(owner, SysmlFactory.eINSTANCE.createFlowUsage());
        flowUsage.setDeclaredName(name);
        return flowUsage;
    }

    ItemUsage createItemUsage(Element owner, String name) {
        ItemUsage itemUsage = this.addOwnedElement(owner, SysmlFactory.eINSTANCE.createItemUsage());
        itemUsage.setDeclaredName(name);
        return itemUsage;
    }

    ActionUsage createArcadiaTypedFunction(Element owner, String name) {
        return this.createArcadiaTypedActionUsage(owner, name, "Function");
    }

    ActionUsage createArcadiaTypedFunctionalChain(Element owner, String name) {
        return this.createArcadiaTypedActionUsage(owner, name, "FunctionalChain");
    }

    ActionUsage createArcadiaTypedFunctionalExchangeUsage(Element owner, String name) {
        return this.createArcadiaTypedActionUsage(owner, name, "FunctionalExchange");
    }

    PartUsage createArcadiaTypedComponent(Element owner, String name) {
        return this.createArcadiaTypedPartUsage(owner, name, "Component");
    }

    ItemUsage createArcadiaTypedExchangeItem(Element owner, String name) {
        ItemUsage itemUsage = this.createItemUsage(owner, name);
        this.setType(itemUsage, this.getOrCreateItemDefinition(this.getRoot(owner), "ExchangeItem"));
        return itemUsage;
    }

    void createArcadiaComponentAttribute(Package root, String attributeName) {
        PartDefinition componentDefinition = this.getOrCreatePartDefinition(root, "Component");
        AttributeUsage attributeUsage = this.addOwnedElement(componentDefinition, SysmlFactory.eINSTANCE.createAttributeUsage());
        attributeUsage.setDeclaredName(attributeName);
    }

    FlowUsage createArcadiaTypedFunctionalExchange(Package owner, String name, ActionUsage source, ActionUsage target) {
        ItemUsage sourcePort = this.createArcadiaTypedExchangeItem(source, name + " Source Port");
        ItemUsage targetPort = this.createArcadiaTypedExchangeItem(target, name + " Target Port");
        return this.createArcadiaTypedFunctionalExchange(owner, name, sourcePort, targetPort);
    }

    FlowUsage createArcadiaTypedFunctionalExchange(Package owner, String name, ItemUsage source, ItemUsage target) {
        FlowUsage flowUsage = this.metamodelMutationElementService.createFlowUsage(source, target, source.getOwner(), target.getOwner(), owner);
        this.setType(flowUsage, this.getOrCreateActionDefinition(this.getRoot(owner), "FunctionalExchange"));
        flowUsage.setDeclaredName(name);
        return flowUsage;
    }

    FlowUsage createLibraryFlowUsageReference(Package root, String parentName, String referenceName) {
        Package referenceContainer = this.createPackage(root, parentName);
        return this.createFlowUsage(referenceContainer, referenceName);
    }

    void createStatusKindEnumeration(Package root, List<String> literals) {
        EnumerationDefinition statusKindDefinition = this.addOwnedElement(root, SysmlFactory.eINSTANCE.createEnumerationDefinition());
        statusKindDefinition.setDeclaredName(TransverseQueryService.STATUS_KIND);

        for (String literal : literals) {
            EnumerationUsage enumerationUsage = SysmlFactory.eINSTANCE.createEnumerationUsage();
            enumerationUsage.setDeclaredName(literal);
            VariantMembership variantMembership = SysmlFactory.eINSTANCE.createVariantMembership();
            statusKindDefinition.getOwnedRelationship().add(variantMembership);
            variantMembership.getOwnedRelatedElement().add(enumerationUsage);
            variantMembership.setMemberElement(enumerationUsage);
        }
    }

    void createModelingMetadataLibrary(Package modelingMetadataRoot) {
        MetadataDefinition statusInfo = this.addOwnedElement(modelingMetadataRoot, SysmlFactory.eINSTANCE.createMetadataDefinition());
        statusInfo.setDeclaredName("StatusInfo");

        AttributeUsage status = this.addOwnedElement(statusInfo, SysmlFactory.eINSTANCE.createAttributeUsage());
        status.setDeclaredName(TransverseQueryService.STATUS);
    }

    void setInvolvedFunctionalExchanges(ActionUsage functionalChain, FlowUsage... functionalExchanges) {
        ReferenceUsage referenceUsage = this.addOwnedElement(functionalChain, SysmlFactory.eINSTANCE.createReferenceUsage());
        referenceUsage.setDeclaredName("involvedFunctionalExchanges");

        for (FlowUsage functionalExchange : functionalExchanges) {
            FeatureReferenceExpression featureReferenceExpression = SysmlFactory.eINSTANCE.createFeatureReferenceExpression();
            Membership membership = SysmlFactory.eINSTANCE.createMembership();
            featureReferenceExpression.getOwnedRelationship().add(membership);
            membership.setMemberElement(functionalExchange);

            FeatureValue featureValue = SysmlFactory.eINSTANCE.createFeatureValue();
            featureValue.getOwnedRelatedElement().add(featureReferenceExpression);
            referenceUsage.getOwnedRelationship().add(featureValue);
        }
    }

    List<ItemUsage> getPayloadFeatureTypedItems(FlowUsage flowUsage) {
        return flowUsage.getPayloadFeature().getOwnedRelationship().stream()
                .filter(FeatureTyping.class::isInstance)
                .map(FeatureTyping.class::cast)
                .map(FeatureTyping::getType)
                .filter(ItemUsage.class::isInstance)
                .map(ItemUsage.class::cast)
                .toList();
    }

    EditingContext createEditingContext(Resource resource, String contextIdSeed) {
        ComposedAdapterFactory composedAdapterFactory = new ComposedAdapterFactory();
        composedAdapterFactory.addAdapterFactory(new EcoreAdapterFactory());
        composedAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

        EPackage.Registry ePackageRegistry = new EPackageRegistryImpl();
        ePackageRegistry.put(EcorePackage.eINSTANCE.getNsURI(), EcorePackage.eINSTANCE);
        ePackageRegistry.put(SysmlPackage.eINSTANCE.getNsURI(), SysmlPackage.eINSTANCE);

        AdapterFactoryEditingDomain editingDomain = new AdapterFactoryEditingDomain(composedAdapterFactory, new BasicCommandStack());
        ResourceSet resourceSet = editingDomain.getResourceSet();
        resourceSet.setPackageRegistry(ePackageRegistry);
        resourceSet.eAdapters().add(new ECrossReferenceAdapter());
        resourceSet.getResources().add(resource);

        return new EditingContext(
                UUID.nameUUIDFromBytes(contextIdSeed.getBytes(StandardCharsets.UTF_8)).toString(),
                editingDomain,
                Map.of(),
                List.of());
    }

    void attachCrossReferenceAdapter(Element root) {
        root.eAdapters().add(new ECrossReferenceAdapter());
    }

    private ActionUsage createArcadiaTypedActionUsage(Element owner, String name, String typeName) {
        ActionUsage actionUsage = this.createActionUsage(owner, name);
        this.setType(actionUsage, this.getOrCreateActionDefinition(this.getRoot(owner), typeName));
        return actionUsage;
    }

    private PartUsage createArcadiaTypedPartUsage(Element owner, String name, String typeName) {
        PartUsage partUsage = this.createPartUsage(owner, name);
        this.setType(partUsage, this.getOrCreatePartDefinition(this.getRoot(owner), typeName));
        return partUsage;
    }

    private ActionDefinition getOrCreateActionDefinition(Package parent, String name) {
        return parent.getOwnedElement().stream()
                .filter(ActionDefinition.class::isInstance)
                .map(ActionDefinition.class::cast)
                .filter(definition -> name.equals(definition.getDeclaredName()))
                .findFirst()
                .orElseGet(() -> {
                    ActionDefinition actionDefinition = this.addOwnedElement(parent, SysmlFactory.eINSTANCE.createActionDefinition());
                    actionDefinition.setDeclaredName(name);
                    return actionDefinition;
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

    private ItemDefinition getOrCreateItemDefinition(Package parent, String name) {
        return parent.getOwnedElement().stream()
                .filter(ItemDefinition.class::isInstance)
                .map(ItemDefinition.class::cast)
                .filter(definition -> name.equals(definition.getDeclaredName()))
                .findFirst()
                .orElseGet(() -> {
                    ItemDefinition itemDefinition = this.addOwnedElement(parent, SysmlFactory.eINSTANCE.createItemDefinition());
                    itemDefinition.setDeclaredName(name);
                    return itemDefinition;
                });
    }

    private void setType(Feature feature, Type type) {
        FeatureTyping featureTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        featureTyping.setTypedFeature(feature);
        featureTyping.setType(type);
        feature.getOwnedRelationship().add(featureTyping);
    }

    private <T extends Element> T addOwnedElement(Element owner, T ownedElement) {
        Membership membership;
        if (owner instanceof Type && ownedElement instanceof Feature) {
            membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        } else {
            membership = SysmlFactory.eINSTANCE.createOwningMembership();
        }
        owner.getOwnedRelationship().add(membership);
        membership.getOwnedRelatedElement().add(ownedElement);
        membership.setMemberElement(ownedElement);
        return ownedElement;
    }

    private void updateRedefiningFeatureOwner(EndFeatureMembership flowConnectionEnd) {
        flowConnectionEnd.getOwnedRelatedElement().stream()
                .filter(FlowEnd.class::isInstance)
                .map(FlowEnd.class::cast)
                .findFirst()
                .ifPresent(flowEnd -> {
                    Feature redefiningFeature = this.addOwnedElement(flowEnd, SysmlFactory.eINSTANCE.createFeature());
                    flowEnd.getOwnedRelationship().stream()
                            .filter(EndFeatureMembership.class::isInstance)
                            .map(EndFeatureMembership.class::cast)
                            .flatMap(endFeatureMembership -> endFeatureMembership.getOwnedRelatedElement().stream())
                            .filter(Feature.class::isInstance)
                            .map(Feature.class::cast)
                            .flatMap(feature -> feature.getOwnedRelationship().stream())
                            .filter(Redefinition.class::isInstance)
                            .map(Redefinition.class::cast)
                            .forEach(redefinition -> redefinition.setRedefiningFeature(redefiningFeature));
                });
    }

    private Package getRoot(Element element) {
        Element current = element;
        while (current.eContainer() instanceof Element parent) {
            current = parent;
        }
        return (Package) current;
    }
}

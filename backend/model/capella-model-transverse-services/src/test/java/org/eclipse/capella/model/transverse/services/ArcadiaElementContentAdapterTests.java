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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.impl.AdapterImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.syson.sysml.Feature;
import org.eclipse.syson.sysml.FeatureTyping;
import org.eclipse.syson.sysml.Membership;
import org.eclipse.syson.sysml.Namespace;
import org.eclipse.syson.sysml.PartDefinition;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlFactory;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.SysONEContentAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests declared typing notifications and the lifecycle of the per-resource-set index.
 *
 * @author Jerome Gout
 */
public class ArcadiaElementContentAdapterTests {

    private static final String COMPONENT_TYPE = "Arcadia::Component";

    private static final String OTHER_TYPE = "Other";

    private static final String ALTERNATE_TYPE = "Arcadia::Alternate";

    private final CommonQueryService queryService = new CommonQueryService();

    private final TypeQueryService typeQueryService = new TypeQueryService();

    private ResourceSetImpl resourceSet;

    private Resource resource;

    private PartDefinition componentType;

    private ArcadiaElementContentAdapter adapter;

    @BeforeEach
    public void beforeEach() {
        this.resourceSet = new ResourceSetImpl();
        this.resource = new ResourceImpl(URI.createURI("memory:/model"));
        this.resourceSet.getResources().add(this.resource);
        var library = SysmlFactory.eINSTANCE.createPackage();
        library.setDeclaredName("Arcadia");
        this.resource.getContents().add(library);
        this.componentType = SysmlFactory.eINSTANCE.createPartDefinition();
        this.componentType.setDeclaredName("Component");
        this.addMember(library, this.componentType);
        this.adapter = new ArcadiaElementContentAdapter(this.resourceSet);
    }

    @Test
    public void addingAndRemovingRootsShouldNotRecomputeExistingTypes() {
        var existing = spy(SysmlFactory.eINSTANCE.createPartUsage());
        existing.getOwnedRelationship().add(this.newTyping(existing));
        this.resource.getContents().add(existing);
        this.resourceSet.eAdapters().add(this.adapter);
        clearInvocations(existing);
        var added = new ArrayList<PartUsage>();

        for (int i = 0; i < 1000; i++) {
            added.add(this.addComponent(this.resource));
        }
        assertThat(this.queryService.getComponents(existing)).hasSize(1001).containsAll(added);
        this.resource.getContents().removeAll(added);

        assertThat(this.queryService.getComponents(existing)).containsExactly(existing);
        verify(existing, never()).getOwnedRelationship();
    }

    @Test
    public void resourceMetadataAndEmptyResourcesShouldNotRecomputeExistingTypes() {
        var existing = spy(SysmlFactory.eINSTANCE.createPartUsage());
        existing.getOwnedRelationship().add(this.newTyping(existing));
        this.resource.getContents().add(existing);
        this.resourceSet.eAdapters().add(this.adapter);
        clearInvocations(existing);

        this.resource.setModified(true);
        this.resource.setModified(false);
        this.resource.setTimeStamp(42);
        var emptyResource = new ResourceImpl(URI.createURI("memory:/empty"));
        this.resourceSet.getResources().add(emptyResource);
        this.resourceSet.getResources().remove(emptyResource);

        assertThat(this.queryService.getComponents(existing)).containsExactly(existing);
        verify(existing, never()).getOwnedRelationship();
    }

    @Test
    public void installationShouldIndexExistingRootsAndNestedFeatures() {
        var parent = this.addComponent(this.resource);
        var child = this.addComponent(this.resource);
        this.addMember(parent, child);

        this.resourceSet.eAdapters().add(this.adapter);

        assertThat(this.queryService.getComponents(parent)).containsExactly(parent, child);
        assertThat(this.adapter.getType(child)).contains(COMPONENT_TYPE);
    }

    @Test
    public void addingPreparedTypingShouldUpdateBothIndexes() {
        this.resourceSet.eAdapters().add(this.adapter);
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        this.resource.getContents().add(component);
        component.getOwnedRelationship().add(this.newTyping(component));

        assertThat(this.adapter.getType(component)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.getComponents(component)).containsExactly(component);
    }

    @Test
    public void settingTypingAfterAttachmentShouldUpdateBothIndexes() {
        this.resourceSet.eAdapters().add(this.adapter);
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        this.resource.getContents().add(component);
        var typing = SysmlFactory.eINSTANCE.createFeatureTyping();
        component.getOwnedRelationship().add(typing);
        typing.setType(this.componentType);
        assertThat(this.adapter.getType(component)).isEmpty();

        typing.setTypedFeature(component);

        assertThat(this.queryService.getComponents(component)).containsExactly(component);
    }

    @Test
    public void changingOrClearingTypeShouldRemovePreviousAssociations() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        var otherType = SysmlFactory.eINSTANCE.createPartDefinition();
        otherType.setDeclaredName(OTHER_TYPE);
        this.resource.getContents().add(otherType);

        typing.setType(otherType);

        assertThat(this.queryService.getComponents(component)).isEmpty();
        assertThat(this.adapter.getType(component)).contains(OTHER_TYPE);
        typing.setType(null);
        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), OTHER_TYPE)).isEmpty();
    }

    @Test
    public void removingTypingShouldRemoveBothAssociations() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);

        component.getOwnedRelationship().clear();

        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.queryService.getComponents(component)).isEmpty();
    }

    @Test
    public void removingAndReattachingSubtreeShouldRestoreAssociations() {
        var component = this.addComponent(this.resource);
        var child = this.addComponent(this.resource);
        this.addMember(component, child);
        this.resourceSet.eAdapters().add(this.adapter);

        this.resource.getContents().remove(component);

        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.adapter.getType(child)).isEmpty();
        this.resource.getContents().add(component);
        assertThat(this.queryService.getComponents(component)).containsExactly(component, child);
    }

    @Test
    public void addedResourcesAndMovesShouldRespectQueryScope() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        var otherResource = new ResourceImpl(URI.createURI("memory:/other"));
        var otherComponent = this.addComponent(otherResource);

        this.resourceSet.getResources().add(otherResource);

        assertThat(this.queryService.getComponents(component)).containsExactly(component);
        assertThat(this.queryService.getComponents(otherComponent)).containsExactly(otherComponent);
        otherResource.getContents().add(component);
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), COMPONENT_TYPE)).isEmpty();
        assertThat(this.queryService.getComponents(otherComponent)).containsExactly(otherComponent, component);
        this.resourceSet.getResources().remove(otherResource);
        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.adapter.getType(otherComponent)).isEmpty();
    }

    @Test
    public void separateResourceSetsShouldKeepIndependentIndexes() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        var otherSet = new ResourceSetImpl();
        var otherResource = new ResourceImpl(URI.createURI("memory:/model"));
        otherSet.getResources().add(otherResource);
        var otherComponent = this.addComponent(otherResource);
        var otherAdapter = new ArcadiaElementContentAdapter(otherSet);
        otherSet.eAdapters().add(otherAdapter);

        assertThat(this.queryService.getComponents(component)).containsExactly(component);
        assertThat(this.queryService.getComponents(otherComponent)).containsExactly(otherComponent);
        this.resourceSet.eAdapters().remove(this.adapter);
        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(otherAdapter.getType(otherComponent)).contains(COMPONENT_TYPE);
    }

    @Test
    public void renamingTypeOrNamespaceShouldRefreshQualifiedNames() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);

        this.componentType.setDeclaredName("Renamed");

        assertThat(this.queryService.getComponents(component)).isEmpty();
        assertThat(this.adapter.getType(component)).contains("Arcadia::Renamed");
        ((Namespace) this.componentType.getOwner()).setDeclaredName("OtherLibrary");
        assertThat(this.adapter.getType(component)).contains("OtherLibrary::Renamed");
    }

    @Test
    public void movingDefinitionShouldRefreshQualifiedNames() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        var otherLibrary = SysmlFactory.eINSTANCE.createPackage();
        otherLibrary.setDeclaredName("OtherLibrary");
        this.resource.getContents().add(otherLibrary);

        this.addMember(otherLibrary, this.componentType);

        assertThat(this.adapter.getType(component)).contains("OtherLibrary::Component");
        assertThat(this.queryService.getComponents(component)).isEmpty();
    }

    @Test
    public void unloadingResourceShouldRemoveBothAssociations() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);

        this.resource.unload();

        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), COMPONENT_TYPE)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void renamingUsageContainingDefinitionShouldRefreshQualifiedNames(int nestingDepth) {
        var container = this.newDefinitionContainer(nestingDepth);
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        var previousType = this.componentType.getQualifiedName();
        assertThat(this.adapter.getType(component)).contains(previousType);

        container.setDeclaredName("Renamed");

        var renamedType = this.componentType.getQualifiedName();
        assertThat(renamedType).isNotEqualTo(previousType).startsWith("Renamed::");
        assertThat(this.adapter.getType(component)).contains(renamedType);
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), previousType)).isEmpty();
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), renamedType)).containsExactly(component);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1 })
    public void movingUsageContainingDefinitionShouldRefreshQualifiedNames(int nestingDepth) {
        var container = this.newDefinitionContainer(nestingDepth);
        var component = this.addComponent(this.resource);
        var destination = SysmlFactory.eINSTANCE.createPackage();
        destination.setDeclaredName("Destination");
        this.resource.getContents().add(destination);
        this.resourceSet.eAdapters().add(this.adapter);
        var previousType = this.componentType.getQualifiedName();

        this.addMember(destination, container);

        var movedType = this.componentType.getQualifiedName();
        assertThat(movedType).isNotEqualTo(previousType).startsWith("Destination::Container::");
        assertThat(this.adapter.getType(component)).contains(movedType);
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), previousType)).isEmpty();
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), movedType)).containsExactly(component);
    }

    @Test
    public void renamingUsageWithoutLocalDefinitionsShouldNotRecomputeOtherTypes() {
        var existing = spy(SysmlFactory.eINSTANCE.createPartUsage());
        existing.getOwnedRelationship().add(this.newTyping(existing));
        this.resource.getContents().add(existing);
        var renamed = this.addComponent(this.resource);
        this.addMember(renamed, this.addComponent(this.resource));
        this.resourceSet.eAdapters().add(this.adapter);
        clearInvocations(existing);

        renamed.setDeclaredName("Renamed");

        assertThat(this.adapter.getType(existing)).contains(COMPONENT_TYPE);
        verify(existing, never()).getOwnedRelationship();
    }

    @Test
    public void renamingInitiallyUnnamedDefinitionShouldIndexPreviouslyUntypedFeatures() {
        this.componentType.setDeclaredName(null);
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        assertThat(this.adapter.getType(component)).isEmpty();

        this.componentType.setDeclaredName("Component");

        assertThat(this.adapter.getType(component)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.getComponents(component)).containsExactly(component);
    }

    @Test
    public void impliedOrIndirectTypingShouldNotBeIndexed() {
        var component = this.addComponent(this.resource);
        var indirect = SysmlFactory.eINSTANCE.createPartUsage();
        this.resource.getContents().add(indirect);
        var subsetting = SysmlFactory.eINSTANCE.createSubsetting();
        subsetting.setSubsettedFeature(component);
        subsetting.setSubsettingFeature(indirect);
        indirect.getOwnedRelationship().add(subsetting);
        this.resourceSet.eAdapters().add(this.adapter);

        assertThat(this.adapter.getType(indirect)).isEmpty();
        ((FeatureTyping) component.getOwnedRelationship().getFirst()).setIsImplied(true);
        assertThat(this.adapter.getType(component)).isEmpty();
    }

    @Test
    public void typingOtherThanArcadiaShouldRemainAvailable() {
        var component = this.addComponent(this.resource);
        var library = (Namespace) this.componentType.getOwner();
        library.setDeclaredName("ModelingMetadata");
        this.componentType.setDeclaredName("StatusInfo");
        this.resourceSet.eAdapters().add(this.adapter);

        assertThat(this.typeQueryService.getType(component)).contains("ModelingMetadata::StatusInfo");
        assertThat(this.queryService.isTypedWith("ModelingMetadata::StatusInfo").test(component)).isTrue();
        assertThat(this.queryService.getArcadiaType(component)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({ "Arcadia, Component, 0", "Arcadia, Component, 1", "SysML, ExchangeItem, 0", "SysML, ExchangeItem, 1" })
    public void metadataShouldNotHideBusinessTypeWithOrWithoutIndex(String libraryName, String typeName, int metadataPosition) {
        ((Namespace) this.componentType.getOwner()).setDeclaredName(libraryName);
        this.componentType.setDeclaredName(typeName);
        var expectedType = this.componentType.getQualifiedName();
        var context = (Namespace) this.componentType.getOwner();
        var component = this.addComponent(this.resource);
        this.addMember(context, component);
        var metadataType = SysmlFactory.eINSTANCE.createPartDefinition();
        metadataType.setDeclaredName("Metadata");
        this.resource.getContents().add(metadataType);
        var metadataTyping = SysmlFactory.eINSTANCE.createFeatureTyping();
        metadataTyping.setType(metadataType);
        metadataTyping.setTypedFeature(component);
        component.getOwnedRelationship().add(metadataPosition, metadataTyping);
        this.resourceSet.eAdapters().add(new SysONEContentAdapter());

        assertThat(this.typeQueryService.getType(component)).contains(expectedType);
        assertThat(this.queryService.checkType(component, expectedType)).isTrue();
        assertThat(this.queryService.isTypedWith(expectedType).test(component)).isTrue();
        assertThat(this.typeQueryService.getElements(context, SysmlPackage.eINSTANCE.getPartUsage(), expectedType)).containsExactly(component);

        this.resourceSet.eAdapters().add(this.adapter);

        assertThat(this.adapter.getType(component)).contains(expectedType);
        assertThat(this.queryService.checkType(component, expectedType)).isTrue();
        assertThat(this.queryService.isTypedWith(expectedType).test(component)).isTrue();
        assertThat(this.typeQueryService.getElements(context, SysmlPackage.eINSTANCE.getPartUsage(), expectedType)).containsExactly(component);
        component.getOwnedRelationship().remove(metadataTyping);
        assertThat(this.adapter.getType(component)).contains(expectedType);
    }

    @Test
    public void bulkChangesAndRepeatedNotificationsShouldKeepUniqueAssociations() {
        this.resourceSet.eAdapters().add(this.adapter);
        var first = SysmlFactory.eINSTANCE.createPartUsage();
        var second = SysmlFactory.eINSTANCE.createPartUsage();
        first.getOwnedRelationship().add(this.newTyping(first));
        second.getOwnedRelationship().add(this.newTyping(second));

        this.resource.getContents().addAll(List.of(first, second));
        ((FeatureTyping) first.getOwnedRelationship().getFirst()).setType(this.componentType);

        assertThat(this.queryService.getComponents(first)).containsExactly(first, second);
        this.resource.getContents().removeAll(List.of(first, second));
        assertThat(this.adapter.getType(first)).isEmpty();
        assertThat(this.adapter.getType(second)).isEmpty();
    }

    @Test
    public void detachedFeaturesShouldUseExplicitTypingWithoutInstallingIndex() {
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        component.getOwnedRelationship().add(this.newTyping(component));

        assertThat(this.queryService.getArcadiaType(component)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.checkType(component, COMPONENT_TYPE)).isTrue();
        assertThat(this.resourceSet.eAdapters()).isEmpty();
    }

    @Test
    public void changingBusinessTypesShouldKeepBothIndexesConsistent() {
        var component = this.addComponent(this.resource);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        var alternate = SysmlFactory.eINSTANCE.createPartDefinition();
        alternate.setDeclaredName("Alternate");
        this.addMember((Namespace) this.componentType.getOwner(), alternate);
        var external = SysmlFactory.eINSTANCE.createPartDefinition();
        external.setDeclaredName(OTHER_TYPE);
        this.resource.getContents().add(external);
        this.resourceSet.eAdapters().add(this.adapter);
        var typeNames = List.of(COMPONENT_TYPE, ALTERNATE_TYPE, OTHER_TYPE);

        for (var type : List.of(alternate, external, this.componentType)) {
            typing.setType(type);
            assertThat(this.adapter.getType(component)).contains(type.getQualifiedName());
            this.assertMatchesRebuiltIndex(List.of(component), List.of(this.resource), typeNames);
        }
        typing.setType(null);
        assertThat(this.adapter.getType(component)).isEmpty();
        this.assertMatchesRebuiltIndex(List.of(component), List.of(this.resource), typeNames);
    }

    @Test
    public void unsettingTypeShouldClearIndexesUsingActualSysMLNotification() {
        var component = this.addComponent(this.resource);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        this.resourceSet.eAdapters().add(this.adapter);
        var notifications = this.recordNotifications(typing);

        typing.eUnset(SysmlPackage.eINSTANCE.getFeatureTyping_Type());

        // This non-unsettable SysML reference emits SET to null, rather than Notification.UNSET.
        assertThat(notifications).extracting(Notification::getEventType).containsExactly(Notification.SET);
        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.queryService.getComponents(component)).isEmpty();
    }

    @Test
    public void unsettingTypedFeatureShouldClearIndexesAndAllowRestoration() {
        var component = this.addComponent(this.resource);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        this.resourceSet.eAdapters().add(this.adapter);
        var notifications = this.recordNotifications(typing);

        typing.eUnset(SysmlPackage.eINSTANCE.getFeatureTyping_TypedFeature());

        assertThat(notifications).extracting(Notification::getEventType).containsExactly(Notification.SET);
        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.queryService.getComponents(component)).isEmpty();
        typing.setTypedFeature(component);
        assertThat(this.adapter.getType(component)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.getComponents(component)).containsExactly(component);
    }

    @Test
    public void transferringTypingAfterRetargetingShouldRemoveOldAssociations() {
        var first = this.addComponent(this.resource);
        var second = SysmlFactory.eINSTANCE.createPartUsage();
        this.resource.getContents().add(second);
        var typing = (FeatureTyping) first.getOwnedRelationship().getFirst();
        this.resourceSet.eAdapters().add(this.adapter);

        typing.setTypedFeature(second);

        assertThat(this.adapter.getType(first)).isEmpty();
        assertThat(this.adapter.getType(second)).isEmpty();
        assertThat(this.queryService.getComponents(first)).isEmpty();
        second.getOwnedRelationship().add(typing);
        assertThat(this.adapter.getType(first)).isEmpty();
        assertThat(this.adapter.getType(second)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.getComponents(first)).containsExactly(second);
        this.assertMatchesRebuiltIndex(List.of(first, second), List.of(this.resource), List.of(COMPONENT_TYPE));
    }

    @Test
    public void transferringTypingBeforeRetargetingShouldWaitForMatchingOwner() {
        var first = this.addComponent(this.resource);
        var second = SysmlFactory.eINSTANCE.createPartUsage();
        this.resource.getContents().add(second);
        var typing = (FeatureTyping) first.getOwnedRelationship().getFirst();
        this.resourceSet.eAdapters().add(this.adapter);

        second.getOwnedRelationship().add(typing);

        assertThat(this.adapter.getType(first)).isEmpty();
        assertThat(this.adapter.getType(second)).isEmpty();
        assertThat(this.queryService.getComponents(first)).isEmpty();
        typing.setTypedFeature(second);
        assertThat(this.queryService.getComponents(first)).containsExactly(second);
        this.assertMatchesRebuiltIndex(List.of(first, second), List.of(this.resource), List.of(COMPONENT_TYPE));
    }

    @Test
    public void resolvingTypeProxyShouldUpdateIndexesWithoutDuplicates() {
        var component = this.addComponent(this.resource);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        this.resourceSet.eAdapters().add(this.adapter);
        var notifications = this.recordNotifications(typing);
        var proxy = SysmlFactory.eINSTANCE.createPartDefinition();
        ((InternalEObject) proxy).eSetProxyURI(EcoreUtil.getURI(this.componentType));

        typing.setType(proxy);

        assertThat(this.adapter.getType(component)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.getComponents(component)).containsExactly(component);
        assertThat(notifications).extracting(Notification::getEventType).doesNotContain(Notification.RESOLVE);
        assertThat(typing.getType()).isSameAs(this.componentType);
        assertThat(notifications).extracting(Notification::getEventType).contains(Notification.RESOLVE);
    }

    @Test
    public void resolvingTypedFeatureProxyShouldRestoreMatchingOwner() {
        var component = this.addComponent(this.resource);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        this.resourceSet.eAdapters().add(this.adapter);
        var notifications = this.recordNotifications(typing);
        var proxy = SysmlFactory.eINSTANCE.createPartUsage();
        ((InternalEObject) proxy).eSetProxyURI(EcoreUtil.getURI(component));

        typing.setTypedFeature(proxy);

        assertThat(this.queryService.getComponents(component)).containsExactly(component);
        assertThat(notifications).extracting(Notification::getEventType).doesNotContain(Notification.RESOLVE);
        assertThat(typing.getTypedFeature()).isSameAs(component);
        assertThat(notifications).extracting(Notification::getEventType).contains(Notification.RESOLVE);
    }

    @Test
    public void addingPreviouslyUnavailableTypeResourceShouldResolveCachedTyping() {
        var component = this.addComponent(this.resource);
        var typing = (FeatureTyping) component.getOwnedRelationship().getFirst();
        var libraryResource = new ResourceImpl(URI.createURI("memory:/late-library"));
        var lateType = SysmlFactory.eINSTANCE.createPartDefinition();
        lateType.setDeclaredName("LateType");
        libraryResource.getContents().add(lateType);
        var proxy = SysmlFactory.eINSTANCE.createPartDefinition();
        ((InternalEObject) proxy).eSetProxyURI(EcoreUtil.getURI(lateType));
        typing.setType(proxy);
        this.resourceSet.eAdapters().add(this.adapter);
        assertThat(this.adapter.getType(component)).isEmpty();
        var notifications = this.recordNotifications(typing);

        this.resourceSet.getResources().add(libraryResource);

        assertThat(notifications).extracting(Notification::getEventType).doesNotContain(Notification.RESOLVE);
        assertThat(this.adapter.getType(component)).contains("LateType");
        assertThat(this.adapter.getElements(this.resource, SysmlPackage.eINSTANCE.getPartUsage(), "LateType")).containsExactly(component);
        assertThat(this.queryService.getComponents(component)).isEmpty();
    }

    @Test
    public void unloadedTypeResourceShouldStayUnloadedUntilExplicitReload(@TempDir Path directory) throws IOException {
        var libraryResource = new XMIResourceImpl(URI.createFileURI(directory.resolve("library.xmi").toString()));
        this.resourceSet.getPackageRegistry().put(SysmlPackage.eNS_URI, SysmlPackage.eINSTANCE);
        this.resourceSet.getResources().add(libraryResource);
        libraryResource.getContents().add(this.componentType.getOwner());
        libraryResource.save(null);
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        assertThat(this.adapter.getType(component)).contains(COMPONENT_TYPE);

        libraryResource.unload();

        assertThat(libraryResource.isLoaded()).isFalse();
        component.setDeclaredName("Changed");
        assertThat(libraryResource.isLoaded()).isFalse();
        assertThat(libraryResource.getContents()).isEmpty();
        assertThat(this.adapter.getType(component)).isEmpty();
        assertThat(this.queryService.getComponents(component)).isEmpty();

        libraryResource.load(null);

        assertThat(libraryResource.isLoaded()).isTrue();
        assertThat(this.adapter.getType(component)).contains(COMPONENT_TYPE);
        assertThat(this.queryService.getComponents(component)).containsExactly(component);
    }

    @Test
    public void installationShouldNotDemandLoadUnavailableTypeResource() {
        var guardedResourceSet = new ResourceSetImpl() {
            @Override
            public Resource getResource(URI uri, boolean loadOnDemand) {
                assertThat(loadOnDemand).isFalse();
                return super.getResource(uri, loadOnDemand);
            }
        };
        var guardedResource = new ResourceImpl(URI.createURI("memory:/guarded-type"));
        guardedResourceSet.getResources().add(guardedResource);
        var component = this.addComponent(guardedResource);
        var proxy = SysmlFactory.eINSTANCE.createPartDefinition();
        ((InternalEObject) proxy).eSetProxyURI(URI.createURI("memory:/unavailable-library#/"));
        ((FeatureTyping) component.getOwnedRelationship().getFirst()).setType(proxy);
        var guardedAdapter = new ArcadiaElementContentAdapter(guardedResourceSet);

        guardedResourceSet.eAdapters().add(guardedAdapter);

        assertThat(guardedAdapter.getType(component)).isEmpty();
        assertThat(proxy.eIsProxy()).isTrue();
        assertThat(guardedResourceSet.getResources()).containsExactly(guardedResource);
    }

    @Test
    public void installationShouldNotDemandLoadUnrelatedContainmentProxies() {
        var guardedResourceSet = new ResourceSetImpl() {
            @Override
            public Resource getResource(URI uri, boolean loadOnDemand) {
                assertThat(loadOnDemand).isFalse();
                return super.getResource(uri, loadOnDemand);
            }
        };
        var guardedResource = new ResourceImpl(URI.createURI("memory:/guarded"));
        guardedResourceSet.getResources().add(guardedResource);
        var libraryProxy = SysmlFactory.eINSTANCE.createPackage();
        ((InternalEObject) libraryProxy).eSetProxyURI(URI.createURI("memory:/unrelated-library#/"));
        guardedResource.getContents().add(libraryProxy);

        guardedResourceSet.eAdapters().add(new ArcadiaElementContentAdapter(guardedResourceSet));

        assertThat(libraryProxy.eIsProxy()).isTrue();
        assertThat(guardedResourceSet.getResources()).containsExactly(guardedResource);
    }

    @Test
    public void mutationSequenceShouldMatchRebuiltIndexAfterEveryChange() {
        var first = this.addComponent(this.resource);
        var second = this.addComponent(this.resource);
        var typing = (FeatureTyping) first.getOwnedRelationship().getFirst();
        var otherResource = new ResourceImpl(URI.createURI("memory:/sequence"));
        this.resourceSet.getResources().add(otherResource);
        this.resourceSet.eAdapters().add(this.adapter);
        var features = List.<Feature>of(first, second);
        var resources = List.of(this.resource, otherResource);
        var typeNames = List.of(COMPONENT_TYPE);
        var mutations = List.<Runnable>of(
                () -> this.resource.getContents().move(0, second),
                () -> typing.setIsImplied(true),
                () -> typing.setIsImplied(false),
                () -> otherResource.getContents().add(first),
                () -> first.getOwnedRelationship().remove(typing),
                () -> second.getOwnedRelationship().clear(),
                () -> second.getOwnedRelationship().add(typing),
                () -> typing.setTypedFeature(second),
                () -> this.resource.getContents().remove(second),
                () -> otherResource.getContents().add(second));

        for (var mutation : mutations) {
            mutation.run();
            this.assertMatchesRebuiltIndex(features, resources, typeNames);
        }
    }

    @Test
    public void differentConstructionOrdersShouldProduceSameAssociations() {
        var component = this.addComponent(this.resource);
        this.resourceSet.eAdapters().add(this.adapter);
        var expectedType = this.adapter.getType(component);
        var expectedElements = this.queryService.getComponents(component);
        this.resource.getContents().remove(component);
        component.getOwnedRelationship().clear();

        this.resource.getContents().add(component);
        var typing = SysmlFactory.eINSTANCE.createFeatureTyping();
        component.getOwnedRelationship().add(typing);
        typing.setTypedFeature(component);
        typing.setType(this.componentType);

        assertThat(this.adapter.getType(component)).isEqualTo(expectedType);
        assertThat(this.queryService.getComponents(component)).containsExactlyInAnyOrderElementsOf(expectedElements);
        this.assertMatchesRebuiltIndex(List.of(component), List.of(this.resource), List.of(COMPONENT_TYPE));
    }

    @Test
    public void copiedSubtreeShouldIndexCopiedFeaturesAndKeepExternalTypeReferences() {
        var parent = this.addComponent(this.resource);
        var child = this.addComponent(this.resource);
        this.addMember(parent, child);
        this.resourceSet.eAdapters().add(this.adapter);

        var copy = EcoreUtil.copy(parent);
        var copiedChild = (PartUsage) copy.getOwnedRelationship().stream().filter(Membership.class::isInstance)
                .map(Membership.class::cast).findFirst().orElseThrow().getOwnedRelatedElement().getFirst();
        this.resource.getContents().add(copy);

        assertThat(copy).isNotSameAs(parent);
        assertThat(copiedChild).isNotSameAs(child);
        var copiedTyping = (FeatureTyping) copiedChild.getOwnedRelationship().getFirst();
        assertThat(copiedTyping.getTypedFeature()).isSameAs(copiedChild);
        assertThat(copiedTyping.getType()).isSameAs(this.componentType);
        assertThat(this.queryService.getComponents(parent)).containsExactlyInAnyOrder(parent, child, copy, copiedChild);
        this.resource.getContents().remove(copy);
        assertThat(this.adapter.getType(copy)).isEmpty();
        assertThat(this.adapter.getType(copiedChild)).isEmpty();
        this.assertMatchesRebuiltIndex(List.of(parent, child, copy, copiedChild), List.of(this.resource), List.of(COMPONENT_TYPE));
    }

    @Test
    public void movingNestedSubtreeBetweenContainersAndResourcesShouldKeepBothIndexesConsistent() {
        var parent = this.addComponent(this.resource);
        var child = this.addComponent(this.resource);
        var sibling = this.addComponent(this.resource);
        this.addMember(parent, child);
        this.addMember(parent, sibling);
        var otherParent = this.addComponent(this.resource);
        var otherResource = new ResourceImpl(URI.createURI("memory:/nested-move"));
        this.resourceSet.getResources().add(otherResource);
        this.resourceSet.eAdapters().add(this.adapter);
        var features = List.<Feature>of(parent, child, sibling, otherParent);
        var resources = List.of(this.resource, otherResource);

        this.addMember(otherParent, child);
        assertThat(child.getOwner()).isSameAs(otherParent);
        this.assertMatchesRebuiltIndex(features, resources, List.of(COMPONENT_TYPE));
        otherResource.getContents().add(otherParent);
        assertThat(this.queryService.getComponents(parent)).containsExactlyInAnyOrder(parent, sibling);
        assertThat(this.queryService.getComponents(child)).containsExactlyInAnyOrder(otherParent, child);
        this.assertMatchesRebuiltIndex(features, resources, List.of(COMPONENT_TYPE));
        this.addMember(parent, child);
        parent.getOwnedRelationship().move(0, parent.getOwnedRelationship().size() - 1);
        this.assertMatchesRebuiltIndex(features, resources, List.of(COMPONENT_TYPE));
    }

    @Test
    public void indexedRecognitionAndRetypingShouldNeverComputeSysMLTypes() {
        var component = spy(SysmlFactory.eINSTANCE.createPartUsage());
        doThrow(new AssertionError("Feature.getType() must not be used by declared type queries")).when(component).getType();
        component.getOwnedRelationship().add(this.newTyping(component));
        this.resource.getContents().add(component);

        this.resourceSet.eAdapters().add(this.adapter);

        assertThat(this.queryService.getComponents(component)).containsExactly(component);
        assertThat(this.queryService.checkType(component, COMPONENT_TYPE)).isTrue();
        assertThat(this.queryService.isTypedWith(COMPONENT_TYPE).test(component)).isTrue();
        assertThat(this.queryService.getArcadiaType(component)).contains(COMPONENT_TYPE);
        ((FeatureTyping) component.getOwnedRelationship().getFirst()).setType(null);
        assertThat(this.queryService.getComponents(component)).isEmpty();
        assertThat(this.queryService.checkType(component, COMPONENT_TYPE)).isFalse();
        assertThat(this.queryService.getArcadiaType(component)).isEmpty();
    }

    private List<Notification> recordNotifications(FeatureTyping typing) {
        var notifications = new ArrayList<Notification>();
        typing.eAdapters().add(new AdapterImpl() {
            @Override
            public void notifyChanged(Notification notification) {
                notifications.add(notification);
            }
        });
        return notifications;
    }

    private void assertMatchesRebuiltIndex(List<Feature> features, List<Resource> resources, List<String> typeNames) {
        var rebuilt = new ArcadiaElementContentAdapter(this.resourceSet);
        this.resourceSet.eAdapters().add(rebuilt);
        try {
            for (var feature : features) {
                assertThat(this.adapter.getType(feature)).isEqualTo(rebuilt.getType(feature));
            }
            for (var targetResource : resources) {
                for (var typeName : typeNames) {
                    assertThat(this.adapter.getElements(targetResource, SysmlPackage.eINSTANCE.getFeature(), typeName))
                            .containsExactlyInAnyOrderElementsOf(rebuilt.getElements(targetResource, SysmlPackage.eINSTANCE.getFeature(), typeName));
                }
            }
        } finally {
            this.resourceSet.eAdapters().remove(rebuilt);
        }
    }

    private PartUsage newDefinitionContainer(int nestingDepth) {
        var container = SysmlFactory.eINSTANCE.createPartUsage();
        container.setDeclaredName("Container");
        this.resource.getContents().add(container);
        var owner = container;
        for (int i = 0; i < nestingDepth; i++) {
            var nested = SysmlFactory.eINSTANCE.createPartUsage();
            nested.setDeclaredName("Nested");
            this.addMember(owner, nested);
            owner = nested;
        }
        var membership = SysmlFactory.eINSTANCE.createOwningMembership();
        membership.getOwnedRelatedElement().add(this.componentType);
        owner.getOwnedRelationship().add(membership);
        return container;
    }

    private PartUsage addComponent(Resource targetResource) {
        var component = SysmlFactory.eINSTANCE.createPartUsage();
        component.getOwnedRelationship().add(this.newTyping(component));
        targetResource.getContents().add(component);
        return component;
    }

    private FeatureTyping newTyping(Feature feature) {
        var typing = SysmlFactory.eINSTANCE.createFeatureTyping();
        typing.setType(this.componentType);
        typing.setTypedFeature(feature);
        return typing;
    }

    private void addMember(Namespace owner, org.eclipse.syson.sysml.Element member) {
        var membership = SysmlFactory.eINSTANCE.createFeatureMembership();
        membership.getOwnedRelatedElement().add(member);
        owner.getOwnedRelationship().add(membership);
    }
}

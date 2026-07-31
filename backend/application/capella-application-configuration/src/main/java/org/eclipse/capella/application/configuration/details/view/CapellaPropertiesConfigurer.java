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
package org.eclipse.capella.application.configuration.details.view;

import org.eclipse.capella.application.configuration.details.view.referencewidget.AllocatedExchangeItemsReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.AllocatedFunctionPortsReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.AllocatedFunctionReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.AllocatedFunctionalExchangesReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.ComponentExchangePortReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.ExchangedItemPayloadReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.FunctionalExchangeFunctionsReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.ICapellaReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.InvolvedFunctionalExchangesReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.InvolvedFunctionsWidgetProvider;
import org.eclipse.capella.model.services.logical.architecture.LAMutationService;
import org.eclipse.capella.model.services.logical.architecture.LAQueryService;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.model.services.transverse.TransverseQueryService;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistryConfigurer;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.sirius.components.emf.services.IDAdapter;
import org.eclipse.sirius.components.emf.services.api.IEMFEditingContext;
import org.eclipse.sirius.components.forms.components.SelectComponent;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.emf.ViewConverterResult;
import org.eclipse.sirius.components.view.emf.form.ViewFormDescriptionConverter;
import org.eclipse.sirius.components.view.form.CheckboxDescription;
import org.eclipse.sirius.components.view.form.FormDescription;
import org.eclipse.sirius.components.view.form.FormElementDescription;
import org.eclipse.sirius.components.view.form.FormElementIf;
import org.eclipse.sirius.components.view.form.FormFactory;
import org.eclipse.sirius.components.view.form.GroupDescription;
import org.eclipse.sirius.components.view.form.GroupDisplayMode;
import org.eclipse.sirius.components.view.form.PageDescription;
import org.eclipse.sirius.components.view.form.RadioDescription;
import org.eclipse.sirius.components.view.form.RichTextDescription;
import org.eclipse.sirius.components.view.form.TextfieldDescription;
import org.eclipse.sirius.components.view.form.WidgetDescription;
import org.eclipse.sirius.components.view.widget.reference.ReferenceFactory;
import org.eclipse.sirius.components.view.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetComponent;
import org.eclipse.syson.application.services.DetailsViewService;
import org.eclipse.syson.form.services.api.IDetailsViewHelpTextProvider;
import org.eclipse.syson.sysml.ActionUsage;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.PartUsage;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.sysml.metamodel.services.MetamodelQueryElementService;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_EXCHANGE;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_COMPONENT_PORT;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_FUNCTIONAL_CHAIN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_IS_HUMAN;
import static org.eclipse.capella.model.services.transverse.TransverseQueryService.ARCADIA_PREFIX;

/**
 * Provides custom Details view for Capella elements.
 *
 * @author frouene
 */
@Configuration
public class CapellaPropertiesConfigurer implements IPropertiesDescriptionRegistryConfigurer {

    public static final String CAPELLA_DETAILS_VIEW = "Capella Details View";

    private final ViewFormDescriptionConverter converter;

    private final List<ComposedAdapterFactory.Descriptor> composedAdapterFactoryDescriptors;

    private final IFeedbackMessageService feedbackMessageService;

    private final IReadOnlyObjectPredicate readOnlyObjectPredicate;

    private final List<IDetailsViewHelpTextProvider> detailsViewHelpTextProviders;

    public CapellaPropertiesConfigurer(ViewFormDescriptionConverter converter, List<ComposedAdapterFactory.Descriptor> composedAdapterFactoryDescriptors,
            IFeedbackMessageService feedbackMessageService, IReadOnlyObjectPredicate readOnlyObjectPredicate, List<IDetailsViewHelpTextProvider> detailsViewHelpTextProviders) {
        this.converter = Objects.requireNonNull(converter);
        this.composedAdapterFactoryDescriptors = Objects.requireNonNull(composedAdapterFactoryDescriptors);
        this.feedbackMessageService = Objects.requireNonNull(feedbackMessageService);
        this.readOnlyObjectPredicate = Objects.requireNonNull(readOnlyObjectPredicate);
        this.detailsViewHelpTextProviders = Objects.requireNonNull(detailsViewHelpTextProviders);
    }

    @Override
    public void addPropertiesDescriptions(IPropertiesDescriptionRegistry registry) {
        // Build the actual FormDescription that will be used in Detail view.
        FormDescription viewFormDescription = this.createDetailsView();

        // The FormDescription must be part of View inside a proper EMF Resource to be correctly handled
        URI uri = URI.createURI(IEMFEditingContext.RESOURCE_SCHEME + ":///" + UUID.nameUUIDFromBytes(CapellaPropertiesConfigurer.class.getCanonicalName().getBytes()));
        Resource resource = new XMIResourceImpl(uri);
        View view = org.eclipse.sirius.components.view.ViewFactory.eINSTANCE.createView();

        view.eAllContents().forEachRemaining(eObject -> eObject.eAdapters().add(new IDAdapter(UUID.nameUUIDFromBytes(EcoreUtil.getURI(eObject).toString().getBytes()))));

        resource.getContents().add(view);
        view.getDescriptions().add(viewFormDescription);

        // Convert the View-based FormDescription and register the result into the system
        AQLInterpreter interpreter = new AQLInterpreter(List.of(LAQueryService.class, TransverseQueryService.class, TransverseMutationService.class, LAMutationService.class),
                List.of(new DetailsViewService(this.composedAdapterFactoryDescriptors, this.feedbackMessageService, this.readOnlyObjectPredicate, new MetamodelQueryElementService(), this.detailsViewHelpTextProviders)),
                List.of(SysmlPackage.eINSTANCE));
        ViewConverterResult viewConverterResult = this.converter.convert(viewFormDescription, List.of(), interpreter);
        if (viewConverterResult.representationDescription() instanceof org.eclipse.sirius.components.forms.description.FormDescription formDescription) {
            formDescription.getPageDescriptions().forEach(registry::add);
        }
    }

    private FormDescription createDetailsView() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getElement());
        FormDescription form = FormFactory.eINSTANCE.createFormDescription();
        form.setName(CAPELLA_DETAILS_VIEW);
        form.setDomainType(domainType);
        form.setTitleExpression(CAPELLA_DETAILS_VIEW);

        PageDescription pageCore = FormFactory.eINSTANCE.createPageDescription();
        pageCore.setName("Capella-DetailsView-Core");
        pageCore.setDomainType(domainType);
        pageCore.setPreconditionExpression(ServiceMethod.of0(TransverseQueryService::isArcadiaElement).aqlSelf());
        pageCore.setLabelExpression("Capella");
        pageCore.getGroups().add(this.createCorePropertiesGroup());

        form.getPages().add(pageCore);

        return form;
    }

    private GroupDescription createCorePropertiesGroup() {
        GroupDescription group = FormFactory.eINSTANCE.createGroupDescription();
        group.setDisplayMode(GroupDisplayMode.LIST);
        group.setLabelExpression("aql:'Properties'");
        group.setSemanticCandidatesExpression(AQLConstants.AQL_SELF);

        group.getChildren().addAll(this.createArcadiaElementWidgets());
        group.getChildren().add(this.createComponentWidget());
        group.getChildren().add(this.createFunctionalChainWidget());
        group.getChildren().add(this.createFunctionalExchangeWidget());
        group.getChildren().add(this.createComponentExchangeWidget());
        group.getChildren().add(this.createComponentPortWidget());
        group.getChildren().add(this.createExchangeItemWidget());

        return group;
    }

    private FormElementDescription createComponentPortWidget() {
        FormElementIf componentPortWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentPortWidgetIf.setName("ComponentPortWidgetIf");
        componentPortWidgetIf.setPredicateExpression(ServiceMethod.of0(TransverseQueryService::isComponentPort).aqlSelf());

        componentPortWidgetIf.getChildren().addAll(this.createComponentPortWidgets());
        return componentPortWidgetIf;
    }

    private Collection<? extends FormElementDescription> createComponentPortWidgets() {

        RadioDescription radioDescription = FormFactory.eINSTANCE.createRadioDescription();
        radioDescription.setName("ComponentPortDirectionWidget");
        radioDescription.setLabelExpression("Direction");
        radioDescription.setValueExpression(ServiceMethod.<DetailsViewService, Element, String> of1(DetailsViewService::getEnumValue).aqlSelf("'direction'"));

        radioDescription.setCandidatesExpression(ServiceMethod.<DetailsViewService, Element, String> of1(DetailsViewService::getEnumCandidates).aqlSelf("'direction'"));
        radioDescription.setCandidateLabelExpression(AQLConstants.AQL + SelectComponent.CANDIDATE_VARIABLE);

        ChangeContext setNewDirectionValueChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        setNewDirectionValueChangeContext.setExpression(ServiceMethod.of1(TransverseMutationService::setFeatureDirection).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        radioDescription.getBody().add(setNewDirectionValueChangeContext);

        ReferenceWidgetDescription refAllocatedPortsWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refAllocatedPortsWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedFunctionPortsReferenceWidgetProvider.WIDGET_NAME);
        refAllocatedPortsWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refAllocatedPortsWidget.setReferenceNameExpression(AllocatedFunctionPortsReferenceWidgetProvider.FEATURE_NAME);
        refAllocatedPortsWidget.setLabelExpression("Allocated Function Ports");

        ChangeContext setInvolvedFunctionalExchangesOperation = ViewFactory.eINSTANCE.createChangeContext();
        setInvolvedFunctionalExchangesOperation.setExpression(ServiceMethod.of4(LAMutationService::setArcadiaReferenceFeature)
                .aqlSelf(AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT_PORT), AQLUtils.aqlString(AllocatedFunctionPortsReferenceWidgetProvider.FEATURE_NAME),
                        ReferenceWidgetComponent.NEW_VALUE, AQLUtils.aqlString(SysmlPackage.eINSTANCE.getItemUsage().getName())));
        refAllocatedPortsWidget.getBody().add(setInvolvedFunctionalExchangesOperation);

        return List.of(radioDescription, refAllocatedPortsWidget);
    }

    private List<WidgetDescription> createArcadiaElementWidgets() {
        TextfieldDescription textfieldName = FormFactory.eINSTANCE.createTextfieldDescription();
        textfieldName.setName("ArcadiaElementNameWidget");
        textfieldName.setLabelExpression("Name");
        textfieldName.setValueExpression(ServiceMethod.of0(LAQueryService::getArcadiaElementName).aqlSelf());
        textfieldName.setValueExpression(ServiceMethod.of0(LAQueryService::getArcadiaElementName).aqlSelf());

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(
                ServiceMethod.<DetailsViewService, Element, String, Object> of2(DetailsViewService::setNewValue).aqlSelf("'declaredName'", ViewFormDescriptionConverter.NEW_VALUE));
        textfieldName.getBody().add(setNewValueOperation);

        RichTextDescription richTextDescription = FormFactory.eINSTANCE.createRichTextDescription();
        richTextDescription.setName("ArcadiaElementDescriptionWidget");
        richTextDescription.setLabelExpression("Description");
        richTextDescription.setIsEnabledExpression("true");
        richTextDescription.setValueExpression(ServiceMethod.of0(TransverseQueryService::getArcadiaElementDescription).aqlSelf());
        ChangeContext setNewDescriptionOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewDescriptionOperation.setExpression(ServiceMethod.of1(TransverseMutationService::setElementDescription).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        richTextDescription.getBody().add(setNewDescriptionOperation);

        var statusSelectDescription = FormFactory.eINSTANCE.createSelectDescription();
        statusSelectDescription.setName("ArcadiaElementStatusWidget");
        statusSelectDescription.setLabelExpression("Status");
        statusSelectDescription.setValueExpression(ServiceMethod.of0(LAQueryService::getStatusStringValue).aqlSelf());
        statusSelectDescription.setCandidateLabelExpression(AQLConstants.AQL + SelectComponent.CANDIDATE_VARIABLE);
        statusSelectDescription.setCandidatesExpression(ServiceMethod.of0(LAQueryService::getStatusKindEnumLiterals).aql(IEditingContext.EDITING_CONTEXT));
        var setNewStatusOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewStatusOperation.setExpression(ServiceMethod.of2(LAMutationService::setStatusKind).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE, IEditingContext.EDITING_CONTEXT));
        statusSelectDescription.getBody().add(setNewStatusOperation);
        return List.of(textfieldName, richTextDescription, statusSelectDescription);
    }

    private FormElementIf createComponentWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("ComponentWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(TransverseQueryService::isComponent).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createComponentWidgets());
        return componentWidgetIf;
    }

    private FormElementIf createFunctionalChainWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("FunctionalChainWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(LAQueryService::isFunctionalChain).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createFunctionalChainWidgets());
        return componentWidgetIf;
    }

    private FormElementIf createFunctionalExchangeWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("FunctionalExchangeWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(LAQueryService::isFunctionalExchange).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createFunctionalExchangeWidgets());
        return componentWidgetIf;
    }

    private FormElementIf createComponentExchangeWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("ComponentExchangeWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(TransverseQueryService::isComponentExchange).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createComponentExchangeWidgets());
        return componentWidgetIf;
    }

    private Collection<? extends FormElementDescription> createFunctionalChainWidgets() {
        // @technical-debt
        // This reference widget depends on InvolvedFunctionalExchangesReferenceWidgetProvider to work properly. We
        // implemented
        // CapellaReferenceWidgetPropertiesConverter and CapellaReferenceWidgetBehaviorConverter to be able to create
        // a custom capella reference widget.
        ReferenceWidgetDescription refInvolvedFunctionalExchangesWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refInvolvedFunctionalExchangesWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + InvolvedFunctionalExchangesReferenceWidgetProvider.WIDGET_NAME);
        refInvolvedFunctionalExchangesWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refInvolvedFunctionalExchangesWidget.setReferenceNameExpression(ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);
        refInvolvedFunctionalExchangesWidget.setLabelExpression("Involved functional exchanges");

        ChangeContext setInvolvedFunctionalExchangesOperation = ViewFactory.eINSTANCE.createChangeContext();
        setInvolvedFunctionalExchangesOperation.setExpression(ServiceMethod.of4(LAMutationService::setArcadiaReferenceFeature)
                .aqlSelf(AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_FUNCTIONAL_CHAIN), AQLUtils.aqlString(InvolvedFunctionalExchangesReferenceWidgetProvider.FEATURE_NAME),
                        ReferenceWidgetComponent.NEW_VALUE, AQLUtils.aqlString(SysmlPackage.eINSTANCE.getFlowUsage().getName())));
        refInvolvedFunctionalExchangesWidget.getBody().add(setInvolvedFunctionalExchangesOperation);

        ChangeContext involvedFunctionChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        involvedFunctionChangeContext.setExpression(AQLConstants.AQL_SELF);
        ReferenceWidgetDescription refInvolvedFunctionWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refInvolvedFunctionWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + InvolvedFunctionsWidgetProvider.WIDGET_NAME);
        refInvolvedFunctionWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refInvolvedFunctionWidget.setIsEnabledExpression("false");
        refInvolvedFunctionWidget.setReferenceNameExpression("involvedFunctions");
        refInvolvedFunctionWidget.setLabelExpression("Involved functions");
        refInvolvedFunctionWidget.getBody().add(involvedFunctionChangeContext);

        return List.of(refInvolvedFunctionalExchangesWidget, refInvolvedFunctionWidget);
    }

    private Collection<? extends FormElementDescription> createFunctionalExchangeWidgets() {
        // @technical-debt

        // Exchange Item PayLoad Widget

        ReferenceWidgetDescription payloadWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        payloadWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + ExchangedItemPayloadReferenceWidgetProvider.WIDGET_NAME);
        payloadWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        payloadWidget.setReferenceNameExpression(ExchangedItemPayloadReferenceWidgetProvider.PAYLOAD_FEATURE);
        payloadWidget.setLabelExpression("Exchanged Items");

        ChangeContext setPayloadOperation = ViewFactory.eINSTANCE.createChangeContext();
        setPayloadOperation.setExpression(ServiceMethod.of1(LAMutationService::setFunctionalExchangePayload).aqlSelf(ReferenceWidgetComponent.NEW_VALUE));
        payloadWidget.getBody().add(setPayloadOperation);

        // Source Widget
        ReferenceWidgetDescription sourceWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        sourceWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + FunctionalExchangeFunctionsReferenceWidgetProvider.WIDGET_NAME);
        sourceWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        sourceWidget.setReferenceNameExpression(FunctionalExchangeFunctionsReferenceWidgetProvider.SOURCE_FEATURE);
        sourceWidget.setLabelExpression("Source Function");
        sourceWidget.setIsEnabledExpression("false");

        // We need to set an operation to avoid an NPE during the widget rendering.
        ChangeContext setSourceOperation = ViewFactory.eINSTANCE.createChangeContext();
        setSourceOperation.setExpression(AQLConstants.AQL_SELF);
        sourceWidget.getBody().add(setSourceOperation);
        // no operation as this widget is currently read-only

        // Target Widget
        ReferenceWidgetDescription targetWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        targetWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + FunctionalExchangeFunctionsReferenceWidgetProvider.WIDGET_NAME);
        targetWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        targetWidget.setReferenceNameExpression(FunctionalExchangeFunctionsReferenceWidgetProvider.TARGET_FEATURE);
        targetWidget.setLabelExpression("Target Function");
        targetWidget.setIsEnabledExpression("false");
        // We need to set an operation to avoid an NPE during the widget rendering.
        ChangeContext setTargetOperation = ViewFactory.eINSTANCE.createChangeContext();
        setTargetOperation.setExpression(AQLConstants.AQL_SELF);
        targetWidget.getBody().add(setTargetOperation);
        // no operation as this widget is currently read-only

        return List.of(payloadWidget, sourceWidget, targetWidget);
    }

    private Collection<? extends FormElementDescription> createComponentExchangeWidgets() {
        // @technical-debt

        // Source Widget
        ReferenceWidgetDescription sourceWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        sourceWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + ComponentExchangePortReferenceWidgetProvider.WIDGET_NAME);
        sourceWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        sourceWidget.setReferenceNameExpression(ComponentExchangePortReferenceWidgetProvider.SOURCE_PORT_FEATURE);
        sourceWidget.setLabelExpression("Source Port");
        sourceWidget.setIsEnabledExpression("false");
        ChangeContext setSourceOperation = ViewFactory.eINSTANCE.createChangeContext();
        //        setSourceOperation
        //                .setExpression(
        //                        ServiceMethod.of2(LAMutationService::setComponentExchangeEnd).aqlSelf("true",ReferenceWidgetComponent.NEW_VALUE));
        setSourceOperation.setExpression(AQLConstants.AQL_SELF);
        // no operation as this widget is currently read-only
        sourceWidget.getBody().add(setSourceOperation);

        // Target Widget
        ReferenceWidgetDescription targetWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        targetWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + ComponentExchangePortReferenceWidgetProvider.WIDGET_NAME);
        targetWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        targetWidget.setReferenceNameExpression(ComponentExchangePortReferenceWidgetProvider.TARGET_PORT_FEATURE);
        targetWidget.setLabelExpression("Target Port");
        targetWidget.setIsEnabledExpression("false");
        ChangeContext setTargetOperation = ViewFactory.eINSTANCE.createChangeContext();
        setTargetOperation.setExpression(AQLConstants.AQL_SELF);
        // no operation as this widget is currently read-only
        //        setTargetOperation
        //                .setExpression(
        //                        ServiceMethod.of1(LAMutationService::setFunctionalExchangePayload).aqlSelf(ReferenceWidgetComponent.NEW_VALUE));
        targetWidget.getBody().add(setTargetOperation);

        // Allocated exchange items Widget
        ReferenceWidgetDescription allocatedExchangeWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        allocatedExchangeWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedExchangeItemsReferenceWidgetProvider.WIDGET_NAME);
        allocatedExchangeWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        allocatedExchangeWidget.setReferenceNameExpression(AllocatedExchangeItemsReferenceWidgetProvider.FEATURE_NAME);
        allocatedExchangeWidget.setLabelExpression("Allocated Exchange Items");
        ChangeContext setAllocatedExchangeItemsOperation = ViewFactory.eINSTANCE.createChangeContext();
        setAllocatedExchangeItemsOperation.setExpression(ServiceMethod.of4(LAMutationService::setArcadiaReferenceFeature)
                .aqlSelf(AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE), AQLUtils.aqlString(AllocatedExchangeItemsReferenceWidgetProvider.FEATURE_NAME),
                        ReferenceWidgetComponent.NEW_VALUE, AQLUtils.aqlString(SysmlPackage.eINSTANCE.getItemUsage().getName())));
        allocatedExchangeWidget.getBody().add(setAllocatedExchangeItemsOperation);

        // Allocated functional exchanges Widget
        ReferenceWidgetDescription allocatedFunctionalExchangesWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        allocatedFunctionalExchangesWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedFunctionalExchangesReferenceWidgetProvider.WIDGET_NAME);
        allocatedFunctionalExchangesWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        allocatedFunctionalExchangesWidget.setReferenceNameExpression(AllocatedFunctionalExchangesReferenceWidgetProvider.FEATURE_NAME);
        allocatedFunctionalExchangesWidget.setLabelExpression("Allocated Functional Exchange");
        ChangeContext setAllocatedFunctionalExchangesOperation = ViewFactory.eINSTANCE.createChangeContext();
        setAllocatedFunctionalExchangesOperation.setExpression(ServiceMethod.of4(LAMutationService::setArcadiaReferenceFeature)
                .aqlSelf(AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT_EXCHANGE), AQLUtils.aqlString(AllocatedFunctionalExchangesReferenceWidgetProvider.FEATURE_NAME),
                        ReferenceWidgetComponent.NEW_VALUE, AQLUtils.aqlString(SysmlPackage.eINSTANCE.getFlowUsage().getName())));
        allocatedFunctionalExchangesWidget.getBody().add(setAllocatedFunctionalExchangesOperation);

        return List.of(sourceWidget, targetWidget, allocatedExchangeWidget, allocatedFunctionalExchangesWidget);
    }

    private List<WidgetDescription> createComponentWidgets() {
        CheckboxDescription checkboxIsActor = FormFactory.eINSTANCE.createCheckboxDescription();
        checkboxIsActor.setName("CheckboxIsActorWidget");
        checkboxIsActor.setLabelExpression("Is Actor");
        checkboxIsActor.setValueExpression(ServiceMethod.of0(TransverseQueryService::isComponentActor).aqlSelf());
        ChangeContext setIsActorChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        setIsActorChangeContext.setExpression(ServiceMethod.of3(LAMutationService::setBooleanAttribute).aqlSelf(ReferenceWidgetComponent.NEW_VALUE, // newValue
                AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT), // qualified type
                AQLUtils.aqlString(ARCADIA_IS_ACTOR) // attribute name
        ));
        checkboxIsActor.getBody().add(setIsActorChangeContext);

        CheckboxDescription checkboxIsHuman = FormFactory.eINSTANCE.createCheckboxDescription();
        checkboxIsHuman.setName("CheckboxIsHumanWidget");
        checkboxIsHuman.setLabelExpression("Is Human");
        checkboxIsHuman.setValueExpression(ServiceMethod.of0(TransverseQueryService::getHumanCheckboxValue).aqlSelf());
        ChangeContext setIsHumanChangeContext = ViewFactory.eINSTANCE.createChangeContext();

        setIsHumanChangeContext.setExpression(ServiceMethod.of3(LAMutationService::setBooleanAttribute)
                .aqlSelf(ReferenceWidgetComponent.NEW_VALUE, AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT), AQLUtils.aqlString(ARCADIA_IS_HUMAN)));

        checkboxIsHuman.getBody().add(setIsHumanChangeContext);

        // @technical-debt
        // This reference widget depends on AllocatedFunctionReferenceWidgetProvider to work properly. We implemented
        // CapellaReferenceWidgetPropertiesConverter and CapellaReferenceWidgetBehaviorConverter to be able to create
        // a custom capella reference widget.
        ReferenceWidgetDescription refWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedFunctionReferenceWidgetProvider.NAME);
        refWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refWidget.setReferenceNameExpression("allocatedFunctions");
        refWidget.setLabelExpression("Allocated Functions");

        ChangeContext setNewAllocatedFunctionOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewAllocatedFunctionOperation.setExpression(
                ServiceMethod.<LAMutationService, PartUsage, ActionUsage> of1(LAMutationService::setPerformedActionUsage).aqlSelf(ReferenceWidgetComponent.NEW_VALUE));

        refWidget.getBody().add(setNewAllocatedFunctionOperation);

        return List.of(checkboxIsActor, checkboxIsHuman, refWidget);
    }

    private FormElementDescription createExchangeItemWidget() {
        FormElementIf exchangeItemWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        exchangeItemWidgetIf.setName("ExchangeItemWidgetIf");
        exchangeItemWidgetIf.setPredicateExpression(ServiceMethod.of0(LAQueryService::isExchangeItem).aqlSelf());

        exchangeItemWidgetIf.getChildren().addAll(this.createExchangeItemWidgets());
        return exchangeItemWidgetIf;
    }

    private Collection<? extends FormElementDescription> createExchangeItemWidgets() {

        RadioDescription radioDescription = FormFactory.eINSTANCE.createRadioDescription();
        radioDescription.setName("FunctionPortDirectionWidget");
        radioDescription.setLabelExpression("Direction");
        radioDescription.setValueExpression(ServiceMethod.<DetailsViewService, Element, String> of1(DetailsViewService::getEnumValue).aqlSelf("'direction'"));

        radioDescription.setCandidatesExpression(ServiceMethod.of1(TransverseQueryService::getExchangeItemEnumLiterals).aqlSelf("'direction'"));
        radioDescription.setCandidateLabelExpression(AQLConstants.AQL + SelectComponent.CANDIDATE_VARIABLE);

        ChangeContext setNewDirectionValueChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        setNewDirectionValueChangeContext.setExpression(ServiceMethod.of1(TransverseMutationService::setFeatureDirection).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        radioDescription.getBody().add(setNewDirectionValueChangeContext);

        return List.of(radioDescription);
    }
}

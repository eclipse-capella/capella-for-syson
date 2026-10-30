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

import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_COMPONENT;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_IS_ACTOR;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_IS_HUMAN;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_IS_REALIZED_BY;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_PREFIX;
import static org.eclipse.capella.model.transverse.services.CommonQueryService.ARCADIA_REALIZES;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
import org.eclipse.capella.application.configuration.details.view.referencewidget.IsRealizedByReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.RealizesReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.services.ArcadiaTraceabilityLabelService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IEditingContextProcessor;
import org.eclipse.sirius.components.emf.ResourceMetadataAdapter;
import org.eclipse.sirius.components.emf.services.JSONResourceFactory;
import org.eclipse.sirius.components.forms.components.SelectComponent;
import org.eclipse.sirius.components.view.ChangeContext;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewFactory;
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
import org.eclipse.sirius.web.application.editingcontext.EditingContext;
import org.eclipse.syson.form.services.aql.FormMutationAQLService;
import org.eclipse.syson.form.services.aql.FormQueryAQLService;
import org.eclipse.syson.sysml.Element;
import org.eclipse.syson.sysml.SysmlPackage;
import org.eclipse.syson.util.AQLConstants;
import org.eclipse.syson.util.AQLUtils;
import org.eclipse.syson.util.ServiceMethod;
import org.eclipse.syson.util.SysMLMetamodelHelper;
import org.springframework.context.annotation.Configuration;

/**
 * Provides custom Details view for Capella elements.
 * Reference action definitions expose controls; their Capella providers perform the semantic updates.
 *
 * @author frouene
 */
@Configuration
public class CapellaDetailsViewDescriptionProvider implements IEditingContextProcessor {

    public static final String CAPELLA_DETAILS_VIEW = "Capella Details View";

    public static final String SOURCE_ID = UUID.nameUUIDFromBytes(CAPELLA_DETAILS_VIEW.getBytes()).toString();

    @Override
    public void preProcess(IEditingContext editingContext) {
        if (editingContext instanceof EditingContext siriusWebEditingContext) {
            var resource = new JSONResourceFactory().createResourceFromPath(SOURCE_ID);
            resource.eAdapters().add(new ResourceMetadataAdapter(CAPELLA_DETAILS_VIEW, true));
            View view = ViewFactory.eINSTANCE.createView();
            resource.getContents().add(view);
            view.getDescriptions().add(this.createDetailsView());
            siriusWebEditingContext.getViews().add(view);
        }
    }

    private FormDescription createDetailsView() {
        String domainType = SysMLMetamodelHelper.buildQualifiedName(SysmlPackage.eINSTANCE.getElement());
        FormDescription form = FormFactory.eINSTANCE.createFormDescription();
        form.setName(CAPELLA_DETAILS_VIEW);
        form.setDomainType(domainType);
        form.setTitleExpression(CAPELLA_DETAILS_VIEW);
        // Prevent the form from being available as a standalone representation.
        form.setPreconditionExpression(AQLConstants.AQL_FALSE);

        PageDescription pageCore = FormFactory.eINSTANCE.createPageDescription();
        pageCore.setName("Capella-DetailsView-Core");
        pageCore.setDomainType(domainType);
        pageCore.setPreconditionExpression(ServiceMethod.of0(CommonQueryService::isArcadiaElement).aqlSelf());
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
        componentPortWidgetIf.setPredicateExpression(ServiceMethod.of0(CommonQueryService::isComponentPort).aqlSelf());

        componentPortWidgetIf.getChildren().addAll(this.createComponentPortWidgets());
        return componentPortWidgetIf;
    }

    private Collection<? extends FormElementDescription> createComponentPortWidgets() {

        RadioDescription radioDescription = FormFactory.eINSTANCE.createRadioDescription();
        radioDescription.setName("ComponentPortDirectionWidget");
        radioDescription.setLabelExpression("Direction");
        radioDescription.setValueExpression(ServiceMethod.<FormQueryAQLService, Element, String> of1(FormQueryAQLService::getEnumValue).aqlSelf("'direction'"));

        radioDescription.setCandidatesExpression(ServiceMethod.<FormQueryAQLService, Element, String> of1(FormQueryAQLService::getEnumCandidates).aqlSelf("'direction'"));
        radioDescription.setCandidateLabelExpression(AQLConstants.AQL + SelectComponent.CANDIDATE_VARIABLE);

        ChangeContext setNewDirectionValueChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        setNewDirectionValueChangeContext.setExpression(ServiceMethod.of1(CommonUpdateService::setFeatureDirection).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        radioDescription.getBody().add(setNewDirectionValueChangeContext);

        ReferenceWidgetDescription refAllocatedPortsWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refAllocatedPortsWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedFunctionPortsReferenceWidgetProvider.WIDGET_NAME);
        refAllocatedPortsWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refAllocatedPortsWidget.setReferenceNameExpression(AllocatedFunctionPortsReferenceWidgetProvider.FEATURE_NAME);
        refAllocatedPortsWidget.setLabelExpression("Allocated Function Ports");

        refAllocatedPortsWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        refAllocatedPortsWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        refAllocatedPortsWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());

        return List.of(radioDescription, refAllocatedPortsWidget);
    }

    private List<FormElementDescription> createArcadiaElementWidgets() {
        TextfieldDescription textfieldName = FormFactory.eINSTANCE.createTextfieldDescription();
        textfieldName.setName("ArcadiaElementNameWidget");
        textfieldName.setLabelExpression("Name");
        textfieldName.setValueExpression(ServiceMethod.of0(CommonQueryService::getArcadiaElementName).aqlSelf());
        textfieldName.setValueExpression(ServiceMethod.of0(CommonQueryService::getArcadiaElementName).aqlSelf());

        ChangeContext setNewValueOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewValueOperation.setExpression(
                ServiceMethod.<FormMutationAQLService, Element, String, Object> of2(FormMutationAQLService::setNewValue).aqlSelf("'declaredName'", ViewFormDescriptionConverter.NEW_VALUE));
        textfieldName.getBody().add(setNewValueOperation);

        RichTextDescription richTextDescription = FormFactory.eINSTANCE.createRichTextDescription();
        richTextDescription.setName("ArcadiaElementDescriptionWidget");
        richTextDescription.setLabelExpression("Description");
        richTextDescription.setIsEnabledExpression("true");
        richTextDescription.setValueExpression(ServiceMethod.of0(CommonQueryService::getArcadiaElementDescription).aqlSelf());
        ChangeContext setNewDescriptionOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewDescriptionOperation.setExpression(ServiceMethod.of1(CommonUpdateService::setElementDescription).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        richTextDescription.getBody().add(setNewDescriptionOperation);

        var statusSelectDescription = FormFactory.eINSTANCE.createSelectDescription();
        statusSelectDescription.setName("ArcadiaElementStatusWidget");
        statusSelectDescription.setLabelExpression("Status");
        statusSelectDescription.setValueExpression(ServiceMethod.of0(CommonQueryService::getStatusStringValue).aqlSelf());
        statusSelectDescription.setCandidateLabelExpression(AQLConstants.AQL + SelectComponent.CANDIDATE_VARIABLE);
        statusSelectDescription.setCandidatesExpression(ServiceMethod.of0(CommonQueryService::getStatusKindEnumLiterals).aqlSelf());
        var setNewStatusOperation = ViewFactory.eINSTANCE.createChangeContext();
        setNewStatusOperation.setExpression(ServiceMethod.of1(CommonUpdateService::setStatusKind).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        statusSelectDescription.getBody().add(setNewStatusOperation);
        return List.of(textfieldName, richTextDescription, statusSelectDescription, this.createRealizesWidget(), this.createIsRealizedByWidget());
    }

    private FormElementIf createRealizesWidget() {
        FormElementIf realizesWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        realizesWidgetIf.setName("RealizesWidgetIf");
        realizesWidgetIf.setPredicateExpression(ServiceMethod.of0(ArcadiaTraceabilityLabelService::hasRealizesWidget).aqlSelf());

        var realizesWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        realizesWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + RealizesReferenceWidgetProvider.WIDGET_NAME);
        realizesWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        realizesWidget.setReferenceNameExpression(ARCADIA_REALIZES);
        realizesWidget.setLabelExpression(ServiceMethod.of0(ArcadiaTraceabilityLabelService::getRealizesWidgetLabelValue).aqlSelf());

        realizesWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        realizesWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        realizesWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());
        realizesWidgetIf.getChildren().add(realizesWidget);

        return realizesWidgetIf;
    }

    private FormElementIf createIsRealizedByWidget() {
        FormElementIf widgetIf = FormFactory.eINSTANCE.createFormElementIf();
        widgetIf.setName("IsRealizedByWidgetIf");
        widgetIf.setPredicateExpression(ServiceMethod.of0(ArcadiaTraceabilityLabelService::hasIsRealizedByWidget).aqlSelf());

        var widget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        widget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + IsRealizedByReferenceWidgetProvider.WIDGET_NAME);
        widget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        widget.setReferenceNameExpression(ARCADIA_IS_REALIZED_BY);
        widget.setLabelExpression(ServiceMethod.of0(ArcadiaTraceabilityLabelService::getIsRealizedByWidgetLabelValue).aqlSelf());
        widget.setIsEnabledExpression(AQLConstants.AQL_FALSE);
        widgetIf.getChildren().add(widget);
        return widgetIf;
    }

    private FormElementIf createComponentWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("ComponentWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(CommonQueryService::isComponent).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createComponentWidgets());
        return componentWidgetIf;
    }

    private FormElementIf createFunctionalChainWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("FunctionalChainWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(CommonQueryService::isFunctionalChain).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createFunctionalChainWidgets());
        return componentWidgetIf;
    }

    private FormElementIf createFunctionalExchangeWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("FunctionalExchangeWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(CommonQueryService::isFunctionalExchange).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createFunctionalExchangeWidgets());
        return componentWidgetIf;
    }

    private FormElementIf createComponentExchangeWidget() {
        FormElementIf componentWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        componentWidgetIf.setName("ComponentExchangeWidgetIf");
        componentWidgetIf.setPredicateExpression(ServiceMethod.of0(CommonQueryService::isComponentExchange).aqlSelf());
        componentWidgetIf.getChildren().addAll(this.createComponentExchangeWidgets());
        return componentWidgetIf;
    }

    private Collection<? extends FormElementDescription> createFunctionalChainWidgets() {
        ReferenceWidgetDescription refInvolvedFunctionalExchangesWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refInvolvedFunctionalExchangesWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + InvolvedFunctionalExchangesReferenceWidgetProvider.WIDGET_NAME);
        refInvolvedFunctionalExchangesWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refInvolvedFunctionalExchangesWidget.setReferenceNameExpression(ARCADIA_INVOLVED_FUNCTIONAL_EXCHANGES);
        refInvolvedFunctionalExchangesWidget.setLabelExpression("Involved functional exchanges");

        refInvolvedFunctionalExchangesWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        refInvolvedFunctionalExchangesWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        refInvolvedFunctionalExchangesWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());

        ReferenceWidgetDescription refInvolvedFunctionWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refInvolvedFunctionWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + InvolvedFunctionsWidgetProvider.WIDGET_NAME);
        refInvolvedFunctionWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refInvolvedFunctionWidget.setIsEnabledExpression("false");
        refInvolvedFunctionWidget.setReferenceNameExpression("involvedFunctions");
        refInvolvedFunctionWidget.setLabelExpression("Involved functions");

        return List.of(refInvolvedFunctionalExchangesWidget, refInvolvedFunctionWidget);
    }

    private Collection<? extends FormElementDescription> createFunctionalExchangeWidgets() {
        // Exchange Item PayLoad Widget
        ReferenceWidgetDescription payloadWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        payloadWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + ExchangedItemPayloadReferenceWidgetProvider.WIDGET_NAME);
        payloadWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        payloadWidget.setReferenceNameExpression(ExchangedItemPayloadReferenceWidgetProvider.PAYLOAD_FEATURE);
        payloadWidget.setLabelExpression("Exchanged Items");

        payloadWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        payloadWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        payloadWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());

        // Source Widget
        ReferenceWidgetDescription sourceWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        sourceWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + FunctionalExchangeFunctionsReferenceWidgetProvider.WIDGET_NAME);
        sourceWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        sourceWidget.setReferenceNameExpression(FunctionalExchangeFunctionsReferenceWidgetProvider.SOURCE_FEATURE);
        sourceWidget.setLabelExpression("Source Function");
        sourceWidget.setIsEnabledExpression("false");

        // Target Widget
        ReferenceWidgetDescription targetWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        targetWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + FunctionalExchangeFunctionsReferenceWidgetProvider.WIDGET_NAME);
        targetWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        targetWidget.setReferenceNameExpression(FunctionalExchangeFunctionsReferenceWidgetProvider.TARGET_FEATURE);
        targetWidget.setLabelExpression("Target Function");
        targetWidget.setIsEnabledExpression("false");

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

        // Target Widget
        ReferenceWidgetDescription targetWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        targetWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + ComponentExchangePortReferenceWidgetProvider.WIDGET_NAME);
        targetWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        targetWidget.setReferenceNameExpression(ComponentExchangePortReferenceWidgetProvider.TARGET_PORT_FEATURE);
        targetWidget.setLabelExpression("Target Port");
        targetWidget.setIsEnabledExpression("false");

        // Allocated exchange items Widget
        ReferenceWidgetDescription allocatedExchangeWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        allocatedExchangeWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedExchangeItemsReferenceWidgetProvider.WIDGET_NAME);
        allocatedExchangeWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        allocatedExchangeWidget.setReferenceNameExpression(AllocatedExchangeItemsReferenceWidgetProvider.FEATURE_NAME);
        allocatedExchangeWidget.setLabelExpression("Allocated Exchange Items");
        allocatedExchangeWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        allocatedExchangeWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        allocatedExchangeWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());

        // Allocated functional exchanges Widget
        ReferenceWidgetDescription allocatedFunctionalExchangesWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        allocatedFunctionalExchangesWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedFunctionalExchangesReferenceWidgetProvider.WIDGET_NAME);
        allocatedFunctionalExchangesWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        allocatedFunctionalExchangesWidget.setReferenceNameExpression(AllocatedFunctionalExchangesReferenceWidgetProvider.FEATURE_NAME);
        allocatedFunctionalExchangesWidget.setLabelExpression("Allocated Functional Exchange");
        allocatedFunctionalExchangesWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        allocatedFunctionalExchangesWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        allocatedFunctionalExchangesWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());

        return List.of(sourceWidget, targetWidget, allocatedExchangeWidget, allocatedFunctionalExchangesWidget);
    }

    private List<WidgetDescription> createComponentWidgets() {
        CheckboxDescription checkboxIsActor = FormFactory.eINSTANCE.createCheckboxDescription();
        checkboxIsActor.setName("CheckboxIsActorWidget");
        checkboxIsActor.setLabelExpression("Is Actor");
        checkboxIsActor.setValueExpression(ServiceMethod.of0(CommonQueryService::isComponentActor).aqlSelf());
        ChangeContext setIsActorChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        setIsActorChangeContext.setExpression(ServiceMethod.of3(CommonUpdateService::setBooleanAttribute)
                .aqlSelf(
                        AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT), // qualified type
                        AQLUtils.aqlString(ARCADIA_IS_ACTOR), // attribute name
                        ReferenceWidgetComponent.NEW_VALUE // newValue

                ));
        checkboxIsActor.getBody().add(setIsActorChangeContext);

        CheckboxDescription checkboxIsHuman = FormFactory.eINSTANCE.createCheckboxDescription();
        checkboxIsHuman.setName("CheckboxIsHumanWidget");
        checkboxIsHuman.setLabelExpression("Is Human");
        checkboxIsHuman.setValueExpression(ServiceMethod.of0(CommonQueryService::getHumanCheckboxValue).aqlSelf());
        ChangeContext setIsHumanChangeContext = ViewFactory.eINSTANCE.createChangeContext();

        setIsHumanChangeContext.setExpression(ServiceMethod.of3(CommonUpdateService::setBooleanAttribute)
                .aqlSelf(
                        AQLUtils.aqlString(ARCADIA_PREFIX + ARCADIA_COMPONENT),
                        AQLUtils.aqlString(ARCADIA_IS_HUMAN),
                        ReferenceWidgetComponent.NEW_VALUE
                ));

        checkboxIsHuman.getBody().add(setIsHumanChangeContext);

        // Virtual Arcadia references require custom property conversion and action handlers.
        ReferenceWidgetDescription refWidget = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        refWidget.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + AllocatedFunctionReferenceWidgetProvider.NAME);
        refWidget.setReferenceOwnerExpression(AQLConstants.AQL_SELF);
        refWidget.setReferenceNameExpression("allocatedFunctions");
        refWidget.setLabelExpression("Allocated Functions");

        refWidget.setAdd(ReferenceFactory.eINSTANCE.createReferenceWidgetAdd());
        refWidget.setRemove(ReferenceFactory.eINSTANCE.createReferenceWidgetRemove());
        refWidget.setClearButton(ReferenceFactory.eINSTANCE.createReferenceWidgetClearButtonDescription());

        return List.of(checkboxIsActor, checkboxIsHuman, refWidget);
    }

    private FormElementDescription createExchangeItemWidget() {
        FormElementIf exchangeItemWidgetIf = FormFactory.eINSTANCE.createFormElementIf();
        exchangeItemWidgetIf.setName("ExchangeItemWidgetIf");
        exchangeItemWidgetIf.setPredicateExpression(ServiceMethod.of0(CommonQueryService::isExchangeItem).aqlSelf());

        exchangeItemWidgetIf.getChildren().addAll(this.createExchangeItemWidgets());
        return exchangeItemWidgetIf;
    }

    private Collection<? extends FormElementDescription> createExchangeItemWidgets() {

        RadioDescription radioDescription = FormFactory.eINSTANCE.createRadioDescription();
        radioDescription.setName("FunctionPortDirectionWidget");
        radioDescription.setLabelExpression("Direction");
        radioDescription.setValueExpression(ServiceMethod.<FormQueryAQLService, Element, String> of1(FormQueryAQLService::getEnumValue).aqlSelf("'direction'"));

        radioDescription.setCandidatesExpression(ServiceMethod.of1(CommonQueryService::getExchangeItemEnumLiterals).aqlSelf("'direction'"));
        radioDescription.setCandidateLabelExpression(AQLConstants.AQL + SelectComponent.CANDIDATE_VARIABLE);

        ChangeContext setNewDirectionValueChangeContext = ViewFactory.eINSTANCE.createChangeContext();
        setNewDirectionValueChangeContext.setExpression(ServiceMethod.of1(CommonUpdateService::setFeatureDirection).aqlSelf(ViewFormDescriptionConverter.NEW_VALUE));
        radioDescription.getBody().add(setNewDirectionValueChangeContext);

        return List.of(radioDescription);
    }
}

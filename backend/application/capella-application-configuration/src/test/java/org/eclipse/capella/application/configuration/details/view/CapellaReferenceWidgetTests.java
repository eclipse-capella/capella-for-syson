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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.capella.application.configuration.details.view.referencewidget.AllocatedFunctionPortsReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.CapellaReferenceWidgetBehaviorConverter;
import org.eclipse.capella.application.configuration.details.view.referencewidget.CapellaReferenceWidgetHandler;
import org.eclipse.capella.application.configuration.details.view.referencewidget.CapellaReferenceWidgetPropertiesConverter;
import org.eclipse.capella.application.configuration.details.view.referencewidget.ICapellaReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.IsRealizedByReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.details.view.referencewidget.RealizesReferenceWidgetProvider;
import org.eclipse.capella.application.configuration.label.services.CapellaImagePathsService;
import org.eclipse.capella.model.transverse.services.CommonCreationService;
import org.eclipse.capella.model.transverse.services.CommonQueryService;
import org.eclipse.capella.model.transverse.services.CommonUpdateService;
import org.eclipse.capella.tests.fixtures.CapellaModel;
import org.eclipse.capella.tests.fixtures.SemanticDataTestFixture;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.components.collaborative.forms.services.api.IPropertiesDescriptionRegistry;
import org.eclipse.sirius.components.collaborative.widget.reference.ReferenceWidgetDescriptor;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.sirius.components.core.api.IFeedbackMessageService;
import org.eclipse.sirius.components.core.api.IIdentityService;
import org.eclipse.sirius.components.core.api.ILabelService;
import org.eclipse.sirius.components.core.api.IObjectSearchService;
import org.eclipse.sirius.components.core.api.IReadOnlyObjectPredicate;
import org.eclipse.sirius.components.emf.services.api.IEMFKindService;
import org.eclipse.sirius.components.forms.description.AbstractControlDescription;
import org.eclipse.sirius.components.forms.description.FlexboxContainerDescription;
import org.eclipse.sirius.components.forms.description.FormDescription;
import org.eclipse.sirius.components.forms.description.IfDescription;
import org.eclipse.sirius.components.forms.description.PageDescription;
import org.eclipse.sirius.components.interpreter.AQLInterpreter;
import org.eclipse.sirius.components.representations.Failure;
import org.eclipse.sirius.components.representations.Success;
import org.eclipse.sirius.components.representations.VariableManager;
import org.eclipse.sirius.components.view.ViewFactory;
import org.eclipse.sirius.components.view.emf.form.FormIdProvider;
import org.eclipse.sirius.components.view.emf.form.ViewFormDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.GroupDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.PageDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.formelements.ComposedFormElementDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.widgets.RadioDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.widgets.RichTextDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.widgets.SelectDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.widgets.TextfieldDescriptionConverter;
import org.eclipse.sirius.components.view.emf.form.converters.widgets.api.IWidgetDescriptionConverter;
import org.eclipse.sirius.components.view.emf.operations.ChangeContextOperationHandler;
import org.eclipse.sirius.components.view.emf.operations.OperationExecutor;
import org.eclipse.sirius.components.view.emf.widget.reference.ReferenceWidgetDescriptionConverter;
import org.eclipse.sirius.components.view.widget.reference.ReferenceFactory;
import org.eclipse.sirius.components.widget.reference.ReferenceWidget;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetComponent;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetComponentProps;
import org.eclipse.sirius.components.widget.reference.ReferenceWidgetDescription;
import org.eclipse.sirius.web.application.views.details.services.DetailsViewReferenceWidgetAddElementHandler;
import org.eclipse.sirius.web.application.views.details.services.DetailsViewReferenceWidgetClearHandler;
import org.eclipse.syson.sysml.FeatureDirectionKind;
import org.eclipse.syson.sysml.SysmlPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Exercises conversion, rendering and reference events with the Sirius Web reference widget API.
 *
 * @author tbezierslafosse
 */
public class CapellaReferenceWidgetTests {

    private static final SemanticDataTestFixture SEMANTIC_DATA_TEST_FIXTURE = new SemanticDataTestFixture();

    private final IIdentityService identityService = mock(IIdentityService.class);

    private final IObjectSearchService objectSearchService = mock(IObjectSearchService.class);

    private final DetailsViewReferenceWidgetAddElementHandler defaultAddHandler = mock(DetailsViewReferenceWidgetAddElementHandler.class);

    private final DetailsViewReferenceWidgetClearHandler defaultClearHandler = mock(DetailsViewReferenceWidgetClearHandler.class);

    private final CapellaReferenceWidgetHandler handler = new CapellaReferenceWidgetHandler(this.objectSearchService, this.defaultAddHandler, this.defaultClearHandler);

    private final FormDescription formDescription = mock(FormDescription.class);

    private final IEditingContext editingContext = mock(IEditingContext.class);

    private final CommonQueryService queryService = new CommonQueryService();

    private CapellaModel capellaModel;

    private ReferenceWidgetDescriptionConverter referenceConverter;

    private ViewFormDescriptionConverter formConverter;

    private final AQLInterpreter interpreter = new AQLInterpreter(List.of(CommonQueryService.class, CommonUpdateService.class), List.of(), List.of(SysmlPackage.eINSTANCE));

    @BeforeEach
    public void setUp() {
        this.capellaModel = SEMANTIC_DATA_TEST_FIXTURE.createCapellaModel();
        when(this.identityService.getId(any())).thenAnswer(invocation -> EcoreUtil.getURI(invocation.getArgument(0, EObject.class)).toString());
        var feedback = mock(IFeedbackMessageService.class);
        var labels = mock(ILabelService.class);
        var kinds = mock(IEMFKindService.class);
        when(kinds.getKind(any(EClass.class))).thenReturn("sysml::Element");
        IReadOnlyObjectPredicate readOnly = object -> false;
        var operations = new OperationExecutor(List.of(new ChangeContextOperationHandler()));
        List<ICapellaReferenceWidgetProvider> providers = List.of(new RealizesReferenceWidgetProvider(), new IsRealizedByReferenceWidgetProvider(),
                new AllocatedFunctionPortsReferenceWidgetProvider());
        var properties = new CapellaReferenceWidgetPropertiesConverter(this.identityService, readOnly, labels, kinds, providers, new CapellaImagePathsService(labels));
        var behavior = new CapellaReferenceWidgetBehaviorConverter(operations, feedback, providers);
        var ids = new FormIdProvider(this.identityService);
        this.referenceConverter = new ReferenceWidgetDescriptionConverter(ids, properties, behavior);
        List<IWidgetDescriptionConverter> widgets = List.of(this.referenceConverter,
                new TextfieldDescriptionConverter(this.identityService, readOnly, operations, feedback, ids),
                new RichTextDescriptionConverter(this.identityService, readOnly, operations, feedback, ids),
                new SelectDescriptionConverter(this.identityService, readOnly, this.objectSearchService, labels, operations, feedback, ids),
                new RadioDescriptionConverter(this.identityService, readOnly, this.objectSearchService, operations, feedback, ids));
        var composed = new ComposedFormElementDescriptionConverter(this.identityService, readOnly, ids, widgets);
        var groups = new GroupDescriptionConverter(this.identityService, ids, widgets, composed);
        this.formConverter = new ViewFormDescriptionConverter(this.identityService, ids, new PageDescriptionConverter(groups, ids, this.identityService, widgets));
    }

    @Test
    public void convertDetailsViewShouldBuildAllReferenceWidgets() {
        var registry = mock(IPropertiesDescriptionRegistry.class);
        new CapellaPropertiesConfigurer(this.formConverter, List.of(), mock(IFeedbackMessageService.class), object -> false, List.of()).addPropertiesDescriptions(registry);
        var pages = ArgumentCaptor.forClass(PageDescription.class);
        verify(registry, atLeastOnce()).add(pages.capture());
        var references = pages.getAllValues().stream().flatMap(page -> page.getGroupDescriptions().stream())
                .flatMap(group -> group.getControlDescriptions().stream()).flatMap(this::getReferences).toList();
        assertThat(references).isNotEmpty().allSatisfy(reference -> {
            assertThat(reference.getReferenceNameProvider().apply(new VariableManager())).startsWith(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX);
            assertThat(reference.getSetHandlerProvider()).isNotNull();
            assertThat(reference.getItemRemoveHandlerProvider()).isNotNull();
            assertThat(reference.getClearButtonDescription()).isNotNull();
        });
    }

    @Test
    public void addRemoveAndClearShouldMaintainRealizesAndInverseReferences() {
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var second = new CommonCreationService().createFunction(realized);
        when(this.objectSearchService.getObject(this.editingContext, "first")).thenReturn(Optional.of(realized));
        when(this.objectSearchService.getObject(this.editingContext, "second")).thenReturn(Optional.of(second));
        var description = this.convert(RealizesReferenceWidgetProvider.WIDGET_NAME, "realizes", "aql:self.setRealizes(newValue)", "aql:true");
        var widget = this.render(description, realizer);

        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("first"))).isInstanceOf(Success.class);
        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("second"))).isInstanceOf(Success.class);
        assertThat(this.queryService.getRealizes(realizer)).containsExactly(second, realized);
        assertThat(this.queryService.getIsRealizedBy(realized)).contains(realizer);
        assertThat(this.queryService.getIsRealizedBy(second)).contains(realizer);

        var variables = new VariableManager();
        variables.put(VariableManager.SELF, realizer);
        variables.put(ReferenceWidgetComponent.ITEM_VARIABLE, realized);
        assertThat(description.getItemRemoveHandlerProvider().apply(variables)).isInstanceOf(Success.class);
        assertThat(this.queryService.getRealizes(realizer)).containsExactly(second);
        assertThat(this.queryService.getIsRealizedBy(realized)).doesNotContain(realizer);

        assertThat(this.handler.clear(this.editingContext, this.formDescription, widget)).isInstanceOf(Success.class);
        assertThat(this.queryService.getRealizes(realizer)).isEmpty();
        assertThat(this.queryService.getIsRealizedBy(second)).doesNotContain(realizer);
    }

    @Test
    public void addWhenSelectingSeveralValuesShouldPreserveExistingReferences() {
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        var creation = new CommonCreationService();
        var second = creation.createFunction(realized);
        var third = creation.createFunction(realized);
        new CommonUpdateService().setRealizes(realizer, realized);
        when(this.objectSearchService.getObject(this.editingContext, this.identityService.getId(realized))).thenReturn(Optional.of(realized));
        when(this.objectSearchService.getObject(this.editingContext, "second")).thenReturn(Optional.of(second));
        when(this.objectSearchService.getObject(this.editingContext, "third")).thenReturn(Optional.of(third));
        var widget = this.render(this.convert(RealizesReferenceWidgetProvider.WIDGET_NAME, "realizes", "aql:self.setRealizes(newValue)", "aql:true"), realizer);

        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("second", "third", "second"))).isInstanceOf(Success.class);
        assertThat(this.queryService.getRealizes(realizer)).containsExactly(realized, second, third);
        assertThat(this.queryService.getIsRealizedBy(realized)).contains(realizer);
        assertThat(this.queryService.getIsRealizedBy(second)).contains(realizer);
        assertThat(this.queryService.getIsRealizedBy(third)).contains(realizer);
    }

    @Test
    public void clearShouldRemoveAllocatedFunctionPortsWithoutDeletingTheirTargets() {
        var creation = new CommonCreationService();
        var component = creation.createComponent(this.capellaModel.getLogicalArchitecturePerspective().getStructurePackage().getElement());
        var port = creation.createComponentPort(component, FeatureDirectionKind.IN);
        var function = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var functionPort = creation.createFunctionPort(function, FeatureDirectionKind.IN);
        when(this.objectSearchService.getObject(this.editingContext, "port")).thenReturn(Optional.of(functionPort));
        var description = this.convert(AllocatedFunctionPortsReferenceWidgetProvider.WIDGET_NAME, AllocatedFunctionPortsReferenceWidgetProvider.FEATURE_NAME,
                "aql:self.setArcadiaReferenceFeature('Arcadia::ComponentPort', 'allocatedFunctionPorts', newValue, 'ItemUsage')", "aql:true");
        var widget = this.render(description, port);

        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("port"))).isInstanceOf(Success.class);
        assertThat(this.queryService.getFeatureReferenceValue(port, AllocatedFunctionPortsReferenceWidgetProvider.FEATURE_NAME)).containsExactly(functionPort);
        assertThat(this.handler.clear(this.editingContext, this.formDescription, widget)).isInstanceOf(Success.class);
        assertThat(this.queryService.getFeatureReferenceValue(port, AllocatedFunctionPortsReferenceWidgetProvider.FEATURE_NAME)).isEmpty();
        assertThat(functionPort.eContainer()).isNotNull();
    }

    @Test
    public void addWhenAnIdIsMissingShouldLeaveTheModelUnchanged() {
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        when(this.objectSearchService.getObject(this.editingContext, "valid")).thenReturn(Optional.of(realized));
        var widget = this.render(this.convert(RealizesReferenceWidgetProvider.WIDGET_NAME, "realizes", "aql:self.setRealizes(newValue)", "aql:true"), realizer);

        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("valid", "missing"))).isInstanceOf(Failure.class);
        assertThat(this.queryService.getRealizes(realizer)).isEmpty();
        assertThat(this.queryService.getIsRealizedBy(realized)).doesNotContain(realizer);
    }

    @Test
    public void addWhenSelectionIsEmptyShouldPreserveExistingValues() {
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        new CommonUpdateService().setRealizes(realizer, realized);
        var widget = this.render(this.convert(RealizesReferenceWidgetProvider.WIDGET_NAME, "realizes", "aql:self.setRealizes(newValue)", "aql:true"), realizer);

        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of())).isInstanceOf(Success.class);
        assertThat(this.queryService.getRealizes(realizer)).containsExactly(realized);
    }

    @Test
    public void addAndClearWhenReadOnlyShouldRejectChanges() {
        var realizer = this.capellaModel.getLogicalArchitecturePerspective().getFunctionsPackage().getRootFunction().getElement();
        var realized = this.capellaModel.getSystemAnalysisPerspective().getFunctionsPackage().getRootFunction().getElement();
        new CommonUpdateService().setRealizes(realizer, realized);
        var widget = this.render(this.convert(IsRealizedByReferenceWidgetProvider.WIDGET_NAME, "isRealizedBy", "aql:self", "aql:false"), realized);

        assertThat(widget.isReadOnly()).isTrue();
        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("other"))).isInstanceOf(Failure.class);
        assertThat(this.handler.clear(this.editingContext, this.formDescription, widget)).isInstanceOf(Failure.class);
        assertThat(this.queryService.getIsRealizedBy(realized)).containsExactly(realizer);
    }

    @Test
    public void addAndClearWhenWidgetIsOrdinaryShouldDelegateToSiriusWeb() {
        var widget = mock(ReferenceWidget.class);
        when(widget.getReferenceName()).thenReturn("ownedRelationship");
        var success = new Success();
        when(this.defaultAddHandler.add(this.editingContext, this.formDescription, widget, List.of("value"))).thenReturn(success);
        when(this.defaultClearHandler.clear(this.editingContext, this.formDescription, widget)).thenReturn(success);

        assertThat(this.handler.add(this.editingContext, this.formDescription, widget, List.of("value"))).isSameAs(success);
        assertThat(this.handler.clear(this.editingContext, this.formDescription, widget)).isSameAs(success);
        verify(this.defaultAddHandler).add(this.editingContext, this.formDescription, widget, List.of("value"));
        verify(this.defaultClearHandler).clear(this.editingContext, this.formDescription, widget);
        when(this.defaultAddHandler.canHandle(this.formDescription)).thenReturn(true);
        assertThat(this.handler.canHandle(this.formDescription)).isTrue();
        assertThat(this.handler.canHandle(mock(FormDescription.class))).isFalse();
    }

    private ReferenceWidgetDescription convert(String widgetName, String referenceName, String operation, String enabled) {
        var view = ReferenceFactory.eINSTANCE.createReferenceWidgetDescription();
        view.setName(ICapellaReferenceWidgetProvider.CAPELLA_REF_WIDGET_PREFIX + widgetName);
        view.setLabelExpression("Reference");
        view.setReferenceNameExpression(referenceName);
        view.setReferenceOwnerExpression("aql:self");
        view.setIsEnabledExpression(enabled);
        var change = ViewFactory.eINSTANCE.createChangeContext();
        change.setExpression(operation);
        view.getBody().add(change);
        new ResourceImpl(URI.createURI("sirius:///reference-test")).getContents().add(view);
        return (ReferenceWidgetDescription) this.referenceConverter.convert(view, this.interpreter).orElseThrow();
    }

    private ReferenceWidget render(ReferenceWidgetDescription description, EObject owner) {
        var variables = new VariableManager();
        variables.put(VariableManager.SELF, owner);
        var element = new ReferenceWidgetComponent(new ReferenceWidgetComponentProps(variables, description)).render();
        return (ReferenceWidget) new ReferenceWidgetDescriptor().instanciate((String) element.getType(), element.getProps(), List.of()).orElseThrow();
    }

    private Stream<ReferenceWidgetDescription> getReferences(AbstractControlDescription control) {
        Stream<ReferenceWidgetDescription> references = Stream.empty();
        if (control instanceof ReferenceWidgetDescription reference) {
            references = Stream.of(reference);
        } else if (control instanceof IfDescription conditional) {
            references = conditional.getControlDescriptions().stream().flatMap(this::getReferences);
        } else if (control instanceof FlexboxContainerDescription container) {
            references = container.getChildren().stream().flatMap(this::getReferences);
        }
        return references;
    }
}

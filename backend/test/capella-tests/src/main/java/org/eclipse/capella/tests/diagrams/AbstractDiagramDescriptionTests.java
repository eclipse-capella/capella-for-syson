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

package org.eclipse.capella.tests.diagrams;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.acceleo.query.ast.Call;
import org.eclipse.acceleo.query.parser.AstResult;
import org.eclipse.acceleo.query.runtime.IQueryEnvironment;
import org.eclipse.acceleo.query.runtime.IService;
import org.eclipse.acceleo.query.runtime.Query;
import org.eclipse.acceleo.query.runtime.QueryParsing;
import org.eclipse.acceleo.query.runtime.ServiceUtils;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.components.collaborative.diagrams.DiagramServices;
import org.eclipse.sirius.components.diagrams.Node;
import org.eclipse.sirius.components.emf.services.DefaultLabelFeatureProvider;
import org.eclipse.sirius.components.emf.services.api.IDefaultLabelFeatureProvider;
import org.eclipse.sirius.components.interpreter.SimpleCrossReferenceProvider;
import org.eclipse.sirius.components.trees.TreeItem;
import org.eclipse.sirius.components.view.View;
import org.eclipse.sirius.components.view.ViewPackage;
import org.eclipse.sirius.components.view.builder.generated.view.ViewBuilder;
import org.eclipse.sirius.components.view.builder.providers.IColorProvider;
import org.eclipse.sirius.components.view.builder.providers.IRepresentationDescriptionProvider;
import org.eclipse.sirius.components.view.diagram.DiagramDescription;
import org.eclipse.sirius.components.view.diagram.DiagramElementDescription;
import org.eclipse.sirius.components.view.diagram.DiagramPalette;
import org.eclipse.sirius.components.view.diagram.EdgePalette;
import org.eclipse.sirius.components.view.diagram.EdgeTool;
import org.eclipse.sirius.components.view.diagram.InsideLabelDescription;
import org.eclipse.sirius.components.view.diagram.LabelOverflowStrategy;
import org.eclipse.sirius.components.view.diagram.InsideLabelPosition;
import org.eclipse.sirius.components.view.diagram.NodeDescription;
import org.eclipse.sirius.components.view.diagram.NodeTool;
import org.eclipse.sirius.components.view.diagram.OutsideLabelDescription;
import org.eclipse.sirius.components.view.emf.CanonicalServices;
import org.eclipse.sirius.components.view.emf.IJavaServiceProvider;
import org.eclipse.syson.sysml.metamodel.helper.EMFUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Checks the structure of the Capella for SysON diagrams.
 *
 * @author gdaniel
 */
public abstract class AbstractDiagramDescriptionTests {

    protected static final String AQL_PREFIX = "aql:";

    protected IDefaultLabelFeatureProvider defaultLabelFeatureProvider = new DefaultLabelFeatureProvider();

    protected List<Class<?>> diagramServicesClasses;

    protected DiagramDescription diagramDescription;

    protected Set<String> environmentServices;

    @BeforeEach
    public void setUp() {
        ViewBuilder viewBuilder = new ViewBuilder();
        View view = viewBuilder.build();
        IColorProvider colorProvider = this.getColorProvider(view);
        IRepresentationDescriptionProvider representationDescriptionProvider = this.getRepresentationDescriptionProvider();
        this.diagramDescription = (DiagramDescription) representationDescriptionProvider.create(colorProvider);
        view.getDescriptions().add(this.diagramDescription);
        IJavaServiceProvider serviceProvider = this.getJavaServiceProvider();
        this.diagramServicesClasses = serviceProvider.getServiceClasses(view);
        IQueryEnvironment environment = Query.newEnvironmentWithDefaultServices(new SimpleCrossReferenceProvider());
        this.environmentServices = new HashSet<>();
        // Add all the AQL operations as services
        environment.getLookupEngine().getRegisteredServices().stream()
                .map(IService::getName)
                .forEach(this.environmentServices::add);
        ServiceUtils.getReceiverServices(null, Node.class).stream()
                .map(IService::getName)
                .forEach(this.environmentServices::add);
        ServiceUtils.getReceiverServices(null, TreeItem.class).stream()
                .map(IService::getName)
                .forEach(this.environmentServices::add);
        ServiceUtils.getReceiverServices(null, CanonicalServices.class).stream()
                .map(IService::getName)
                .forEach(this.environmentServices::add);
        ServiceUtils.getReceiverServices(null, DiagramServices.class).stream()
                .map(IService::getName)
                .forEach(this.environmentServices::add);

    }

    protected abstract IColorProvider getColorProvider(View view);

    protected abstract IRepresentationDescriptionProvider getRepresentationDescriptionProvider();

    protected abstract IJavaServiceProvider getJavaServiceProvider();

    @Test
    @DisplayName("Each EdgeDescription has reconnect tools")
    public void eachEdgeHasReconnectTools() {
        SoftAssertions softly = new SoftAssertions();
        this.diagramDescription.getEdgeDescriptions()
                .forEach(edgeDescription -> softly.assertThat(edgeDescription.getPalette())
                        .as("EdgeDescription %s should have a palette", edgeDescription.getName())
                        .isNotNull()
                        .extracting(EdgePalette::getEdgeReconnectionTools)
                        .as("EdgeDescription %s should have %s reconnection tools", edgeDescription.getName(), 2)
                        .asInstanceOf(InstanceOfAssertFactories.LIST)
                        .hasSize(2));
        softly.assertAll();
    }

    @Test
    @DisplayName("Each EdgeDescription with a center label expression has a direct edit tool")
    public void eachEdgeWithCenterLabelHasDirectEditTool() {
        SoftAssertions softly = new SoftAssertions();
        this.diagramDescription.getEdgeDescriptions().stream()
                .filter(edgeDescription -> edgeDescription.getCenterLabelExpression() != null && !edgeDescription.getCenterLabelExpression().isBlank())
                .forEach(edgeDescription -> softly.assertThat(edgeDescription.getPalette())
                        .as("EdgeDescription %s should have a palette", edgeDescription.getName())
                        .isNotNull()
                        .extracting(EdgePalette::getCenterLabelEditTool)
                        .as("EdgeDescription %s should have a center label edit tool", edgeDescription.getName(), edgeDescription.getCenterLabelExpression())
                        .isNotNull());
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription with an inside label has a direct edit tool")
    public void eachNodeHasDirectEditTool() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(this.isCompartment().negate())
                .filter(nodeDescription -> nodeDescription.getInsideLabel() != null && nodeDescription.getInsideLabel().getLabelExpression() != null
                        && !nodeDescription.getInsideLabel().getLabelExpression().isBlank())
                .forEach(nodeDescription -> {
                    softly.assertThat(nodeDescription.getPalette())
                            .as("NodeDescription %s should have a palette", nodeDescription.getName())
                            .isNotNull();
                    if (nodeDescription.getPalette() != null) {
                        softly.assertThat(nodeDescription.getPalette().getLabelEditTool())
                                .as("NodeDescription %s should have a label edit tool", nodeDescription.getName())
                                .isNotNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription with an inside label should wrap overflowing text")
    public void eachNodeInsideLabelShouldWrapOverflowingText() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, InsideLabelDescription.class)
                .forEach(insideLabelDescription -> softly.assertThat(insideLabelDescription.getOverflowStrategy())
                        .as("InsideLabel of %s should wrap overflowing text", this.getDescriptionName(insideLabelDescription))
                        .isEqualTo(LabelOverflowStrategy.WRAP));
        softly.assertAll();
    }

    @Test
    @DisplayName("Each NodeDescription with an outside label should not have a strategy for overflowing text")
    public void eachNodeOutsideLabelShouldNotHaveAStrategyForOverflowingText() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, OutsideLabelDescription.class)
                .forEach(outsideLabelDescription -> softly.assertThat(outsideLabelDescription.getOverflowStrategy())
                        .as("OutsideLabel of %s should not have a strategy for overflowing text", this.getDescriptionName(outsideLabelDescription))
                        .isEqualTo(LabelOverflowStrategy.NONE));
        softly.assertAll();
    }

    @Test
    @DisplayName("Each Function has its name vertically centered")
    public void functionLabelsShouldBeVerticallyCentered() {
        assertThat(EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(nodeDescription -> nodeDescription.getName() != null && nodeDescription.getName().endsWith("FunctionNodeDescription")))
                .allSatisfy(nodeDescription -> assertThat(nodeDescription.getInsideLabel().getPosition())
                        .as("Function NodeDescription %s should have its label vertically centered", nodeDescription.getName())
                        .isEqualTo(InsideLabelPosition.MIDDLE_CENTER));
    }

    @Test
    @DisplayName("Each NodeDescription has a delete tool")
    public void eachNodeHasDeleteTool() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class)
                .filter(this.isCompartment().negate())
                .forEach(nodeDescription -> {
                    softly.assertThat(nodeDescription.getPalette())
                            .as("NodeDescription %s should have a palette", nodeDescription.getName())
                            .isNotNull();
                    if (nodeDescription.getPalette() != null) {
                        softly.assertThat(nodeDescription.getPalette().getDeleteTool())
                                .as("NodeDescription %s should have a delete tool", nodeDescription.getName())
                                .isNotNull();
                    }
                });
        softly.assertAll();
    }

    @Test
    @DisplayName("Every reused Node Description has a container")
    public void allReusedNodeDescriptionsHaveAContainer() {
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeDescription.class).forEach(nodeDescription -> {
            nodeDescription.getReusedChildNodeDescriptions().forEach(reusedChildNodeDescription -> {
                Assertions.assertThat(reusedChildNodeDescription.eContainer()).as("Reused child Node Description '%s' of '%s' has no container. At the very least, FakeNodeDescriptionProvider could contain it."
                        .formatted(reusedChildNodeDescription.getName(), nodeDescription.getName())).isNotNull();
            });
            nodeDescription.getReusedBorderNodeDescriptions().forEach(reusedBorderNodeDescription -> {
                Assertions.assertThat(reusedBorderNodeDescription.eContainer()).as("Reused border Node Description '%s' of '%s' has no container. At the very least, FakeNodeDescriptionProvider could contain it."
                        .formatted(reusedBorderNodeDescription.getName(), nodeDescription.getName())).isNotNull();
            });
        });
    }

    @Test
    @DisplayName("Diagram has a semantic drag & drop tool")
    public void diagramHasSemanticDragAndDropTool() {
        assertThat(this.diagramDescription.getPalette())
                .as("DiagramDescription should have a palette")
                .isNotNull()
                .extracting(DiagramPalette::getDropTool)
                .as("DiagramDescription should have a drop tool")
                .isNotNull();
    }

    @Test
    @DisplayName("Each NodeTool has an icon URL")
    public void eachNodeToolHasAnIconURL() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, NodeTool.class)
                .forEach(nodeTool -> this.checkIconURL(nodeTool.getIconURLsExpression(), nodeTool, softly));
        softly.assertAll();
    }

    @Test
    @DisplayName("Each EdgeTool has an iconURL")
    public void eachEdgeToolHasAnIconURL() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.allContainedObjectOfType(this.diagramDescription, EdgeTool.class)
                .forEach(edgeTool -> this.checkIconURL(edgeTool.getIconURLsExpression(), edgeTool, softly));
        softly.assertAll();
    }

    @Test
    @DisplayName("Each service called in AQL is a public method from a service classe")
    public void eachAQLServicesIsPublicServiceMethod() {
        SoftAssertions softly = new SoftAssertions();
        EMFUtils.eAllContentStreamWithSelf(this.diagramDescription)
                .forEach(eObject -> {
                    List<String> interpretedExpressions = this.getInterpretedExpressions(eObject);
                    if (!interpretedExpressions.isEmpty()) {
                        interpretedExpressions.stream()
                                .filter(Objects::nonNull)
                                .filter(expression -> !expression.isBlank())
                                .collect(Collectors.toSet())
                                .forEach(expression -> {
                                    if (expression.startsWith(AQL_PREFIX)) {
                                        String expressionBody = expression.substring(AQL_PREFIX.length());
                                        AstResult astResult = QueryParsing.newBuilder().build(expressionBody);
                                        softly.assertThat(astResult.getErrors()).isEmpty();
                                        List<String> calledServices = EMFUtils.allContainedObjectOfType(astResult.getAst(), Call.class)
                                                .map(Call::getServiceName)
                                                .filter(name -> name != null && !name.isBlank())
                                                .toList();
                                        softly.assertThat(calledServices)
                                                .as("Expression %s in %s is not a default environment service or a registered service", expression, this.getDescriptionName(eObject))
                                                .allMatch(serviceName -> this.environmentServices.contains(serviceName) || this.isRegisteredServiceMethod(serviceName));
                                    }
                                });
                    }
                });
        softly.assertAll();
    }

    private List<String> getInterpretedExpressions(EObject eObject) {
        return eObject.eClass().getEAllStructuralFeatures().stream()
                .filter(EAttribute.class::isInstance)
                .map(EAttribute.class::cast)
                .filter(attribute -> attribute.getEType() == ViewPackage.Literals.INTERPRETED_EXPRESSION)
                .map(expressionAttribute -> (String) eObject.eGet(expressionAttribute))
                .toList();
    }

    /**
     * Checks if {@code serviceName} is a registered service in the representation's {@link IJavaServiceProvider}.
     * <p>
     * This method only checks for public methods available in registered service classes.
     * </p>
     *
     * @param serviceName the service to check
     * @return {@code true} if the service matches a publicly available method in a registered service, {@code false} otherwise
     */
    private boolean isRegisteredServiceMethod(String serviceName) {
        for (Class<?> diagramServiceClass : this.diagramServicesClasses) {
            for (Method method : diagramServiceClass.getMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    if (method.getName().equals(serviceName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getDescriptionName(EObject eObject) {
        return this.getContainingDiagramElementDescription(eObject)
                .map(DiagramElementDescription::getName)
                // Use the DiagramDescription name if we can't find any parent DiagramElementDescription, this is the best guess we have.
                .orElse(this.diagramDescription.getName());
    }

    private Optional<DiagramElementDescription> getContainingDiagramElementDescription(EObject eObject) {
        Optional<DiagramElementDescription> result = Optional.empty();
        if (eObject instanceof DiagramElementDescription) {
            result = Optional.of((DiagramElementDescription) eObject);
        } else if (eObject.eContainer() != null) {
            result = this.getContainingDiagramElementDescription(eObject.eContainer());
        }
        return result;
    }

    private void checkIconURL(String iconURLExpression, EObject parent, SoftAssertions softly) {
        softly.assertThat(iconURLExpression).as("Icon URL Expression in %s %s '%s' should not be null", this.getDescriptionName(parent), parent.eClass().getName(), this.getName(parent)).isNotNull();
        if (iconURLExpression != null) {
            if (iconURLExpression.startsWith(AQL_PREFIX)) {
                String aqlExpression = iconURLExpression.substring(AQL_PREFIX.length());
                if (aqlExpression.startsWith("'") && aqlExpression.endsWith("'")) {
                    // The AQL expression is a single string defining the URL of the icon
                    String aqlIconURL = aqlExpression.substring(1, aqlExpression.length() - 1);
                    URL iconURL = AbstractDiagramDescriptionTests.class.getResource(aqlIconURL);
                    softly.assertThat(iconURL)
                            .as("Icon URL Expression %s in %s %s '%s' should reference an existing icon", iconURLExpression, this.getDescriptionName(parent), parent.eClass().getName(),
                                    this.getName(parent))
                            .isNotNull();
                } else {
                    // The AQL expression is an actual expression, we can only check it is not blank
                    softly.assertThat(aqlExpression)
                            .as("Icon URL expression %s in %s %s '%s' should not be blank", iconURLExpression, this.getDescriptionName(parent), parent.eClass().getName(), this.getName(parent))
                            .isNotBlank();
                }
            } else {
                // The icon is provided as a plain string
                URL iconURL = AbstractDiagramDescriptionTests.class.getResource(iconURLExpression);
                softly.assertThat(iconURL).as("Icon URL Expression %s in %s %s '%s' should reference an existing icon", iconURLExpression, this.getDescriptionName(parent), parent.eClass().getName(),
                                this.getName(parent))
                        .isNotNull();
            }
        }
    }

    private String getName(EObject eObject) {
        return this.defaultLabelFeatureProvider.getDefaultLabelEAttribute(eObject)
                .map(eObject::eGet)
                .map(String.class::cast)
                .orElse(eObject.eClass().getName());
    }

    private Predicate<NodeDescription> isCompartment() {
        return nodeDescription -> nodeDescription.getName() != null && nodeDescription.getName().contains("Compartment");
    }
}

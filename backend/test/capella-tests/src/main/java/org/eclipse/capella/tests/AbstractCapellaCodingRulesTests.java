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
package org.eclipse.capella.tests;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.capella.tests.semantic.AbstractSemanticTests;
import org.eclipse.sirius.components.annotations.Builder;
import org.eclipse.sirius.components.annotations.Immutable;
import org.eclipse.sirius.components.tests.architecture.AbstractCodingRulesTests;
import org.eclipse.sirius.components.view.diagram.provider.DefaultToolsFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A Capella specific coding rules implementation.
 *
 * @author fbarbin
 */
public abstract class AbstractCapellaCodingRulesTests extends AbstractCodingRulesTests {

    private static final Set<String> PERSPECTIVE_SERVICE_PACKAGES = Set.of(
            "org.eclipse.capella.model.services.operational.analysis",
            "org.eclipse.capella.model.services.system.analysis",
            "org.eclipse.capella.model.services.logical.architecture",
            "org.eclipse.capella.model.services.physical.architecture");

    private static final String TRANSVERSE_SERVICE_PACKAGE = "org.eclipse.capella.model.services.transverse";

    private static final String WHEN = "When";

    private static final String SHOULD = "Should";

    protected abstract JavaClasses getTestClasses();

    @Override
    public void noMethodsShouldBeStatic() {
        ArchRule rule = ArchRuleDefinition.noMethods()
                .that()
                .areDeclaredInClassesThat()
                .resideInAPackage(this.getProjectRootPackage())
                .and()
                .areDeclaredInClassesThat()
                .areNotAnnotatedWith(Immutable.class)
                .and()
                .areDeclaredInClassesThat()
                .areNotAssignableTo(Enum.class)
                .and()
                .areDeclaredInClassesThat()
                .areNotAssignableTo(Record.class)
                .and(this.isNotLambda())
                .and(this.isNotSwitchTable())
                .and(this.isNotRecordStaticBuilder())
                .and(this.isNotBuilder())
                .should()
                .beStatic()
                .allowEmptyShould(true);

        rule.check(this.getClasses());
    }

    /**
     * Checks that edges and nodes providers do not directly call services.
     * <p>
     * Services should be referenced in providers via `ServiceMethod.ofX(ServiceClass::serviceMethod)`, but should never be called as part of the view creation process. This check ensures that service
     * classes are focused on representation/semantic data, and do not creep on view creation.
     * </p>
     */
    @Test
    public void noServiceShouldBeCalledInViewProviders() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that()
                .resideInAnyPackage("..view.edges..", "..view.nodes..")
                .and()
                .haveSimpleNameEndingWith("Provider")
                .should()
                .callCodeUnitWhere(this.isCallToService())
                .because("view providers should reference Capella model services through ServiceMethod instead of calling them directly")
                .allowEmptyShould(true);

        rule.check(this.getClasses());
    }

    /**
     * Checks that a query service ({@code XXXQueryService}) never relies on a mutation service.
     * <p>
     * Query services are read-only services that retrieve information from the semantic data, representation, etc. They shouldn't need mutation-level operations to compute such information. Such
     * dependency usually reveal an architecture issue: either the method doesn't belong to a mutation service, or the query service doesn't need it at all.
     * </p>
     */
    @Test
    public void noQueryServiceShouldUseMutationService() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that()
                .haveSimpleNameEndingWith("QueryService")
                .should()
                .dependOnClassesThat()
                .haveSimpleNameEndingWith("MutationService")
                .because("query services should not depend on mutation services")
                .allowEmptyShould(true);

        rule.check(this.getClasses());
    }

    /**
     * Checks that a perspective-level service doesn't have the same name as a transverse service.
     * <p>
     * This rule avoids ambiguous AQL service calls. If two services named `myService` have the same arguments, AQL cannot determine which one to call and will select one in an undeterministic way. It
     * is recommended to suffix perspective-level services with the perspective prefix (e.g. createComponentLA) to prevent this issue.
     */
    @Test
    public void noPerspectiveLevelServiceShouldBeNamedAsTransverseService() {
        Map<String, Set<String>> transverseServiceOwnersByMethodName = this.getClasses().stream()
                .filter(javaClass -> javaClass.getPackageName().equals(TRANSVERSE_SERVICE_PACKAGE))
                .filter(javaClass -> javaClass.getSimpleName().matches(".*Services?"))
                .flatMap(javaClass -> javaClass.getMethods().stream())
                .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                .collect(Collectors.groupingBy(JavaMethod::getName,
                        Collectors.mapping(method -> method.getOwner().getSimpleName(), Collectors.toSet())));

        ArchRule rule = ArchRuleDefinition.methods()
                .that()
                .arePublic()
                .and(this.areDeclaredInPerspectiveLevelService())
                .should(this.haveNameDistinctFromTransverseServices(transverseServiceOwnersByMethodName))
                .because(
                        "perspective-level and transverse services available in the same view should have unambiguous method names; rename the perspective-level method with its perspective suffix, for example createComponentLA")
                .allowEmptyShould(true);

        rule.check(this.getClasses());
    }

    @Test
    public void noClassShouldUseSysONDeleteService() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .that()
                .doNotHaveFullyQualifiedName("org.eclipse.capella.model.services.transverse.TransverseMutationService")
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("org.eclipse.syson.services.DeleteService")
                .orShould()
                .dependOnClassesThat()
                .haveFullyQualifiedName("org.eclipse.capella.model.services.CapellaDeleteService")
                .orShould()
                .callMethodWhere(this.isCallToEcoreDeleteMethod())
                .because("semantic deletion should always be handled by TransverseMutationService#delete")
                .allowEmptyShould(true);

        rule.check(this.getClasses());
    }

    /**
     * Checks that no class use Sirius Web {@link DefaultToolsFactory}.
     * <p>
     * {@link DefaultToolsFactory} was initially designed to provide default tools when creating a studio, but its scope has expanded beyond that purpose. In the future, Sirius Web may break this
     * factory, and we should rely on the SysON implementation instead. You can check <a href="https://github.com/eclipse-syson/syson/issues/2452">syson#2452</a> for more information.
     */
    @Test
    public void noClassShouldUseDefaultToolsFactory() {
        ArchRule rule = ArchRuleDefinition.noClasses()
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName(DefaultToolsFactory.class.getName())
                .because("default tools should be provided by DiagramDefaultToolsFactory instead of DefaultToolsFactory")
                .allowEmptyShould(true);

        rule.check(this.getClasses());
    }

    /**
     * Checks that semantic test methods follow the naming convention.
     * <p>
     * The naming convention is {@code <operation>[<operationContext][When<context>]Should<outcome>}. {@code operationContext} is usually used to add information on the operation being tested (e.g.
     * {@code deleteComponentExchange} when the {@code delete} operation is tested on a component exchange). {@code When} is usually used to provide additional contextual information, (e.g.
     * deleteComponentExchangeWhenPortHasOutDirection). A test should always end with a {@code Should} clause with an outcome.
     * <p>
     * {@code @DisplayName} annotation is not necessary since the method name has to be expressive. Duplicating the information in an annotation adds divergence risk while adding little value.
     */
    @Test
    public void semanticTestMethodsShouldFollowNamingConvention() {
        JavaClasses transverseServiceClasses = new ClassFileImporter().importPackages("org.eclipse.capella.model.transverse.services..");

        Set<String> testableMethodNames = transverseServiceClasses.get("org.eclipse.capella.model.transverse.services.TransverseMutationService")
                .getMethods()
                .stream()
                .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
                .map(JavaMethod::getName)
                .collect(Collectors.toSet());

        ArchRule rule = ArchRuleDefinition.methods()
                .that()
                .areDeclaredInClassesThat()
                .areAssignableTo(AbstractSemanticTests.class)
                .and()
                .arePublic()
                .and()
                .areAnnotatedWith(Test.class)
                .should(this.followNamingConvention(testableMethodNames))
                .andShould()
                .notBeAnnotatedWith(DisplayName.class)
                .because("test method names should follow <operation>[When][<context>]Should<outcome> and should be used as their display names")
                .allowEmptyShould(true);

        rule.check(this.getTestClasses());
    }

    /**
     * Matches calls to the {@code delete} overloads and {@code deleteAll} on {@code EcoreUtil}.
     *
     * @return A predicate used to reject direct calls to EcoreUtil deletion methods
     */
    private DescribedPredicate<JavaMethodCall> isCallToEcoreDeleteMethod() {
        return new DescribedPredicate<>("calls an EcoreUtil deletion method") {
            @Override
            public boolean test(JavaMethodCall javaMethodCall) {
                String targetOwnerName = javaMethodCall.getTargetOwner().getName();
                String targetMethodName = javaMethodCall.getName();
                return "org.eclipse.emf.ecore.util.EcoreUtil".equals(targetOwnerName) && ("delete".equals(targetMethodName) || "deleteAll".equals(targetMethodName));
            }
        };
    }

    private ArchCondition<JavaMethod> haveNameDistinctFromTransverseServices(Map<String, Set<String>> transverseServiceOwnersByMethodName) {
        return new ArchCondition<>("have a name distinct from all transverse service methods") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                String methodName = method.getName();
                if (transverseServiceOwnersByMethodName.containsKey(methodName)) {
                    String transverseServiceOwners = transverseServiceOwnersByMethodName.get(methodName).stream()
                            .sorted()
                            .collect(Collectors.joining(", "));
                    String message = method.getFullName() + " has the same name as a public method in " + transverseServiceOwners;
                    events.add(SimpleConditionEvent.violated(method, message));
                }
            }
        };
    }

    private DescribedPredicate<JavaMethod> areDeclaredInPerspectiveLevelService() {
        return new DescribedPredicate<>("are declared in a perspective-level service") {
            @Override
            public boolean test(JavaMethod method) {
                String ownerPackageName = method.getOwner().getPackageName();
                String ownerSimpleName = method.getOwner().getSimpleName();
                return PERSPECTIVE_SERVICE_PACKAGES.contains(ownerPackageName) && ownerSimpleName.matches("(OA|SA|LA|PA).*Services?");
            }
        };
    }

    /**
     * Matches method and constructor calls to the Capella model services module. ArchUnit represents method references
     * separately, so calls such as {@code ServiceMethod.of0(TransverseQueryService::getComponentExchanges)} are not
     * matched by this predicate.
     *
     * @return A predicate used to reject direct service calls
     */
    private DescribedPredicate<JavaCall<?>> isCallToService() {
        return new DescribedPredicate<>("calls a Capella model service") {
            @Override
            public boolean test(JavaCall<?> javaCall) {
                return javaCall.getTargetOwner().getPackageName().startsWith("org.eclipse.capella.model.services.");
            }
        };
    }

    /**
     * Lambda are compiled as hidden static methods named lambda$XXX. This method will help detect them and ignore them.
     *
     * @return A predicate which will help us ignore lambda methods
     */
    private DescribedPredicate<JavaMethod> isNotLambda() {
        return new DescribedPredicate<>("is not a lambda") {
            @Override
            public boolean test(JavaMethod javaMethod) {
                return !(javaMethod.getName().startsWith("lambda$") || javaMethod.getName().startsWith("Lambda$"));
            }
        };
    }

    /**
     * Some switch can be compiled as hidden static methods named $SWITCH_TABLE$. This predicate will help detect them
     * and ignore them.
     *
     * @return A predicate which help us ignore switch expressions
     */
    private DescribedPredicate<JavaMethod> isNotSwitchTable() {
        return new DescribedPredicate<>("is not a switch table (whatever that is...)") {
            @Override
            public boolean test(JavaMethod javaMethod) {
                return !javaMethod.getFullName().contains("$SWITCH_TABLE$");
            }
        };
    }

    /**
     * Used to detect that the static method is not defined in a record and does not return the same record.
     *
     * @return A predicate used to ignore some static methods
     */
    private DescribedPredicate<JavaMethod> isNotRecordStaticBuilder() {
        return new DescribedPredicate<>("is not an alternate builder in a record") {
            @Override
            public boolean test(JavaMethod javaMethod) {
                return !(javaMethod.getOwner().isRecord() && javaMethod.getRawReturnType().equals(javaMethod.getOwner()));
            }
        };
    }

    /**
     * Used to detect that the static method is not returning a type annotated with @Builder.
     *
     * @return A predicate used to ignore some static methods
     */
    private DescribedPredicate<JavaMethod> isNotBuilder() {
        return new DescribedPredicate<>("is not returning a type annotated with @Builder") {
            @Override
            public boolean test(JavaMethod javaMethod) {
                return !(javaMethod.getRawReturnType().isAnnotatedWith(Builder.class) || javaMethod.getRawReturnType().getSimpleName().endsWith("Builder"));
            }
        };
    }

    private ArchCondition<JavaMethod> followNamingConvention(Set<String> testableMethodNames) {
        return new ArchCondition<>("follow the test method naming convention") {
            @Override
            public void check(JavaMethod testMethod, ConditionEvents events) {
                AbstractCapellaCodingRulesTests.this.findNamingViolation(testMethod.getName(), testableMethodNames)
                        .ifPresent(message -> events.add(SimpleConditionEvent.violated(testMethod, testMethod.getFullName() + " " + message)));
            }
        };
    }

    private Optional<String> findNamingViolation(String testMethodName, Set<String> testableMethodNames) {
        Optional<String> result = Optional.empty();
        int shouldIndex = testMethodName.indexOf(SHOULD);
        if (shouldIndex < 0) {
            result = Optional.of("does not contain the mandatory Should clause");
        } else if (shouldIndex == 0 || shouldIndex + SHOULD.length() == testMethodName.length()) {
            result = Optional.of("must have an operation before Should and an outcome after it");
        } else if (testMethodName.indexOf(SHOULD, shouldIndex + SHOULD.length()) >= 0) {
            result = Optional.of("contains more than one Should clause");
        } else {
            boolean startsWithTestableMethodName = testableMethodNames.stream().anyMatch(testMethodName::startsWith);
            if (!startsWithTestableMethodName) {
                result = Optional.of("does not start with a public TransverseMutationService method name");
            } else {
                int whenIndex = testMethodName.indexOf(WHEN);
                if (whenIndex > shouldIndex) {
                    result = Optional.of("has a When clause after its Should clause");
                } else if (whenIndex >= 0 && testMethodName.indexOf(WHEN, whenIndex + WHEN.length()) >= 0) {
                    result = Optional.of("contains more than one When clause");
                } else if (whenIndex >= 0 && whenIndex + WHEN.length() == shouldIndex) {
                    result = Optional.of("has a When clause without a context");
                }
            }
        }
        return result;
    }

}

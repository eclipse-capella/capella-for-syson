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
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import org.eclipse.sirius.components.annotations.Builder;
import org.eclipse.sirius.components.annotations.Immutable;
import org.eclipse.sirius.components.tests.architecture.AbstractCodingRulesTests;
import org.junit.jupiter.api.Test;

/**
 * A Capella specific coding rules implementation.
 *
 * @author fbarbin
 */
public abstract class AbstractCapellaCodingRulesTests extends AbstractCodingRulesTests {

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

}

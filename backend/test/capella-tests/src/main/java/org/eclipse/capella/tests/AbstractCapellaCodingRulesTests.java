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
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import org.eclipse.sirius.components.annotations.Builder;
import org.eclipse.sirius.components.annotations.Immutable;
import org.eclipse.sirius.components.tests.architecture.AbstractCodingRulesTests;

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

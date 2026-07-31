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
package org.eclipse.capella.model.services.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.capella.model.services.transverse.AbstractSemanticTests;
import org.eclipse.capella.model.services.transverse.TransverseMutationService;
import org.eclipse.capella.tests.AbstractCapellaCodingRulesTests;
import org.junit.jupiter.api.Test;

/**
 * Coding rules tests.
 *
 * @author sbegaudeau
 */
public class CodingRulesTests extends AbstractCapellaCodingRulesTests {

    private static final String WHEN = "When";

    private static final String SHOULD = "Should";

    @Override
    protected String getProjectRootPackage() {
        return ArchitectureConstants.CAPELLA_MODEL_SERVICES;
    }

    @Override
    protected JavaClasses getClasses() {
        return ArchitectureConstants.CLASSES;
    }

    protected JavaClasses getTestClasses() {
        return ArchitectureConstants.TEST_CLASSES;
    }

    @Test
    @Override
    public void noClassesShouldUseApacheCommons() {
        super.noClassesShouldUseApacheCommons();
    }

    /**
     * Checks that semantic test methods follow the naming convention.
     * <p>
     * The naming convention is {@code <operation>[<operationContext][When<context>]Should<outcome>}. {@code operationContext} is usually used to add information on the operation being tested (e.g.
     * {@code deleteComponentExchange} when the {@code delete} operation is tested on a component exchange). {@code When} is usually used to provide additional contextual information, (e.g.
     * deleteComponentExchangeWhenPortHasOutDirection). A test should always end with a {@code Should} clause with an outcome.
     */
    @Test
    public void semanticTestMethodsShouldFollowNamingConvention() {
        Set<String> testableMethodNames = this.getClasses().get(TransverseMutationService.class)
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
                .because("test method names should follow <operation>[When][<context>]Should<outcome>");

        rule.check(this.getTestClasses());
    }

    private ArchCondition<JavaMethod> followNamingConvention(Set<String> testableMethodNames) {
        return new ArchCondition<>("follow the test method naming convention") {
            @Override
            public void check(JavaMethod testMethod, ConditionEvents events) {
                CodingRulesTests.this.findNamingViolation(testMethod.getName(), testableMethodNames)
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

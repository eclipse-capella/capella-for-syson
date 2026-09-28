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
package org.eclipse.capella.application.configuration.explorer.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.capella.model.services.operational.analysis.OAExplorerFilterServiceProvider;
import org.eclipse.capella.model.transverse.services.api.IExplorerFilterServiceProvider;
import org.eclipse.sirius.components.core.api.IEditingContext;
import org.eclipse.syson.services.api.ISysONResourceService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Tests discovery, injection and composition of explorer filter providers.
 *
 * @author tbezierslafosse
 */
public class CapellaExplorerFilterServiceProviderTests {

    @Test
    public void perspectiveProvidersShouldBeDiscoveredAndCombined() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ISysONResourceService.class, () -> mock(ISysONResourceService.class));
            context.register(CapellaExplorerFilterService.class);
            context.scan(OAExplorerFilterServiceProvider.class.getPackageName());
            context.registerBean("firstFilter", IExplorerFilterServiceProvider.class, () -> () -> object -> !"first".equals(object));
            context.registerBean("secondFilter", IExplorerFilterServiceProvider.class, () -> () -> object -> !"second".equals(object));
            context.refresh();

            assertThat(context.getBeansOfType(IExplorerFilterServiceProvider.class).values())
                    .hasSize(3)
                    .anyMatch(OAExplorerFilterServiceProvider.class::isInstance);
            List<Object> elements = List.of("first", "visible", "second");
            assertThat(context.getBean(CapellaExplorerFilterService.class).applyVisibilityFilters(new IEditingContext.NoOp(), elements, List.of()))
                    .containsExactly("visible");
            assertThat(elements).containsExactly("first", "visible", "second");
        }
    }

    @Test
    public void explorerShouldWorkWithoutPerspectiveProviders() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean(ISysONResourceService.class, () -> mock(ISysONResourceService.class));
            context.register(CapellaExplorerFilterService.class);
            context.refresh();

            assertThat(context.getBean(CapellaExplorerFilterService.class).applyVisibilityFilters(new IEditingContext.NoOp(), List.of("visible"), List.of()))
                    .containsExactly("visible");
        }
    }
}

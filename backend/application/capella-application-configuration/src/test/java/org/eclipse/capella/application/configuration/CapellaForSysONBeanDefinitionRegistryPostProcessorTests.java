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
package org.eclipse.capella.application.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.sirius.components.view.emf.diagram.tools.DiagramElementPaletteVariableManagerProvider;
import org.eclipse.syson.diagram.common.view.services.SysONDiagramElementPaletteVariableManagerProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Verifies that palette overrides retain an injectable SysON delegate without competing primary beans.
 *
 * @author tbezierslafosse
 */
public class CapellaForSysONBeanDefinitionRegistryPostProcessorTests {

    @Test
    public void postProcessBeanDefinitionRegistryShouldDemoteOnlyTheSysONPaletteProvider() {
        var registry = new DefaultListableBeanFactory();
        var syson = new RootBeanDefinition(SysONDiagramElementPaletteVariableManagerProvider.class);
        syson.setPrimary(true);
        registry.registerBeanDefinition("syson", syson);
        var other = new RootBeanDefinition(DiagramElementPaletteVariableManagerProvider.class);
        other.setPrimary(true);
        registry.registerBeanDefinition("other", other);

        new CapellaForSysONBeanDefinitionRegistryPostProcessor().postProcessBeanDefinitionRegistry(registry);

        assertThat(registry.containsBeanDefinition("syson")).isTrue();
        assertThat(registry.getBeanDefinition("syson").isPrimary()).isFalse();
        assertThat(registry.getBeanDefinition("other").isPrimary()).isTrue();
    }
}

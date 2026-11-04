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

import java.util.List;
import java.util.Objects;

import org.eclipse.sirius.web.application.library.services.ProjectLibrariesImporter;
import org.eclipse.syson.application.expressions.services.ExpressionsPaletteToolsProvider;
import org.eclipse.syson.application.sysmlv2.SysMLv2ProjectTemplatesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * Configures the bean definition registry.
 * <p>
 * This class is used to remove SysON beans from the registry, after they have been added, since the extension doesn't have a way to configure SysON's component scan.
 * This is for example required when an extension overrides a SysON implementation that is injected as a list, where {@code @Primary} isn't enough to override it.
 * </p>
 * <p>
 * Using this component ensures the beans are removed when running Capella for SysON as a SysON extension, but also when running it as a standalone application (e.g. for development purposes).
 * </p>
 *
 * @author gdaniel
 */
@Component
public class CapellaForSysONBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    private static final List<Class<?>> EXCLUDED_BEAN_CLASSES = List.of(
            SysMLv2ProjectTemplatesProvider.class, // Replaced by CapellaForSysONSysMLv2ProjectTemplatesProvider
            ProjectLibrariesImporter.class, // Replaced with CapellaForSysONProjectLibrariesImporter
            ExpressionsPaletteToolsProvider.class // Removed: we don't want to add expressions via the palette
    );

    private final Logger logger = LoggerFactory.getLogger(CapellaForSysONBeanDefinitionRegistryPostProcessor.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition =  registry.getBeanDefinition(beanName);
            if (EXCLUDED_BEAN_CLASSES.stream().anyMatch(excluded -> Objects.equals(beanDefinition.getBeanClassName(), excluded.getName()))) {
                registry.removeBeanDefinition(beanName);
                this.logger.atDebug()
                        .setMessage("Removed bean {}")
                        .addArgument(beanName)
                        .addKeyValue("beanName", beanName)
                        .log();
            }
        }
    }
}

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

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures the static resources served by Capella for SysON.
 *
 * @technical-debt this class should be replaced with the appropriate service/configuration once Sirius Web supports the customization of static locations (see sirius-web#6734).
 *
 * @author gdaniel
 */
@Configuration
public class CapellaForSysONWebMvcConfiguration implements WebMvcConfigurer {


    private final String[] allowedOriginPatterns;
    private final String[] allowedHeaders;
    private final String[] allowedMethods;
    private final boolean allowedCredentials;
    private final String[] staticLocations;

    public CapellaForSysONWebMvcConfiguration(@Value("${sirius.components.cors.allowedOriginPatterns:}") String[] allowedOriginPatterns, @Value("${sirius.components.cors.allowedHeaders:}") String[] allowedHeaders, @Value("${sirius.components.cors.allowedMethods:}") String[] allowedMethods, @Value("${sirius.components.cors.allowedCredentials:false}") boolean allowedCredentials, WebProperties webProperties) {
        this.allowedOriginPatterns = Objects.requireNonNull(allowedOriginPatterns);
        this.allowedHeaders = Objects.requireNonNull(allowedHeaders);
        this.allowedMethods = Objects.requireNonNull(allowedMethods);
        this.allowedCredentials = allowedCredentials;
        this.staticLocations = webProperties.getResources().getStaticLocations();
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                "/**/*.css",
                "/**/*.html",
                "/**/*.js",
                "/**/*.chunk.js",
                "/**/*.js.map",
                "/**/*.json",
                "/**/*.ico",
                "/**/*.ttf",
                "/**/media/**",
                "/**/*.jpg",
                "/**/*.jpeg",
                "/**/*.png",
                "/**/*.svg")
                .addResourceLocations(this.staticLocations);
    }

    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration corsRegistration = registry.addMapping("/**");
        if (this.allowedOriginPatterns.length > 0) {
            corsRegistration.allowedOriginPatterns(this.allowedOriginPatterns);
        }

        if (this.allowedHeaders.length > 0) {
            corsRegistration.allowedHeaders(this.allowedHeaders);
        }

        if (this.allowedMethods.length > 0) {
            corsRegistration.allowedMethods(this.allowedMethods);
        }

        corsRegistration.allowCredentials(this.allowedCredentials);
    }
}

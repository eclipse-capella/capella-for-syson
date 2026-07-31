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

import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RequestPredicates.pathExtension;
import static org.springframework.web.servlet.function.RouterFunctions.route;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sirius.web.infrastructure.configuration.mvc.IBackendPathPredicate;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * Redirects request to frontend paths to the proper static resources.
 *
 * @techncial-debt this class should be replaced with the appropriate service/configuration once Sirius Web supports the customization of static locations (see sirius-web#6734).
 *
 * @author gdaniel
 */
@Configuration
public class CapellaForSysONFrontendRouterConfiguration {


    private final ResourceLoader resourceLoader;

    public CapellaForSysONFrontendRouterConfiguration(ResourceLoader resourceLoader) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader);
    }

    @Bean("capellaRedirectToIndex")
    public RouterFunction<ServerResponse> redirectToIndex(List<IBackendPathPredicate> backendResourcePredicates, WebProperties webProperties) {
        var extensionsToIgnore = List.of("css", "html", "js", "js.map", "chunk.js", "json", "ico", "ttf", "jpg", "jpeg", "png", "svg");

        var singlePageApplicationPredicate = path("/api/**")
                .or(path("/v3/api-docs/**"))
                .or(path("/subscriptions"))
                .or(pathExtension(extension -> extension != null && extensionsToIgnore.contains(extension)))
                .or(request -> backendResourcePredicates.stream().anyMatch(backendResourcePredicate -> backendResourcePredicate.isBackendPath(request.path())))
                .negate();

        Optional<Resource> optionalIndex = Arrays.stream(webProperties.getResources().getStaticLocations())
                .map(location -> resourceLoader.getResource(location + "index.html"))
                .filter(Resource::exists)
                .findFirst();

        if (optionalIndex.isEmpty()) {
            return route()
                    .GET(singlePageApplicationPredicate, request -> ServerResponse.notFound().build())
                    .build();
        }
        return route()
                .resource(singlePageApplicationPredicate, optionalIndex.get())
                .build();
    }
}

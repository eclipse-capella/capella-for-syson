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

package org.eclipse.capella.application.configuration.arcadia.services;

import java.util.Objects;
import java.util.UUID;

import org.eclipse.capella.application.configuration.arcadia.services.api.IArcadiaLibraryPublisher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

/**
 * Initializes the Arcadia library.
 * <p>
 * This service checks on startup if the Arcadia library exists, and initializes it if it doesn't. Note that it is possible to de-activate this service with the
 * {@code org.eclipse.capella.arcadia-library-initialization.enabled = false} property, for example to prevent it from running in test contexts where the initialization is performed by other means.
 * </p>
 *
 * @author gdaniel
 */
@Service
@ConditionalOnBooleanProperty(value = "org.eclipse.capella.arcadia-library-initialization.enabled", matchIfMissing = true)
public class ArcadiaLibraryInitializer implements CommandLineRunner {

    private final IArcadiaLibraryPublisher arcadiaLibraryPublisher;

    public ArcadiaLibraryInitializer(IArcadiaLibraryPublisher arcadiaLibraryPublisher) {
        this.arcadiaLibraryPublisher = Objects.requireNonNull(arcadiaLibraryPublisher);
    }

    @Override
    public void run(String... args) throws Exception {
        this.arcadiaLibraryPublisher.publish(new PublishArcadiaLibraryCommand(UUID.randomUUID(), "capella", "arcadia", "0.0.1", "SysMLv2 implementation of the Arcadia library"));
    }
}

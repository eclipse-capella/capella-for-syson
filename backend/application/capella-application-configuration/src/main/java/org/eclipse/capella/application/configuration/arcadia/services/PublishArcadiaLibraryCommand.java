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

import java.util.UUID;

import org.eclipse.sirius.components.events.ICause;

import jakarta.validation.constraints.NotNull;

/**
 * Command used to request the creation of the Arcadia library.
 *
 * @author gdaniel
 */
public record PublishArcadiaLibraryCommand(
        @NotNull UUID id,
        @NotNull String namespace,
        @NotNull String name,
        @NotNull String version,
        @NotNull String description) implements ICause {
}

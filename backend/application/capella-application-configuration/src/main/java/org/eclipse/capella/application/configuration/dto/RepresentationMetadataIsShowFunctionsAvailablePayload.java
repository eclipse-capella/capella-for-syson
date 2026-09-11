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

package org.eclipse.capella.application.configuration.dto;

import java.util.UUID;

import org.eclipse.sirius.components.core.api.IPayload;

/**
 * Payload for RepresentationMetadata#isShowFunctionsAvailable query.
 *
 * @author Jerome Gout
 */
public record RepresentationMetadataIsShowFunctionsAvailablePayload(UUID id, boolean isAvailable) implements IPayload { }

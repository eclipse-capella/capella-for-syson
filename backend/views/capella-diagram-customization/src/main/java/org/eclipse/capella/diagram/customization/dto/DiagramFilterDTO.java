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

package org.eclipse.capella.diagram.customization.dto;

/**
 * A description of a filter available in a Capella diagram.
 *
 * @author Jerome Gout
 */
public record DiagramFilterDTO(String id, String label, boolean state, String activeTooltip, String inactiveTooltip, String urlParam) { }
